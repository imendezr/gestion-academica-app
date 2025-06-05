package com.example.gestionacademicaapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.databinding.ActivityMainBinding
import com.example.gestionacademicaapp.ui.login.LoginActivity
import com.example.gestionacademicaapp.utils.Notificador
import com.example.gestionacademicaapp.utils.RolePermissions
import com.example.gestionacademicaapp.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        navController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
            ?.findNavController()
            ?: throw IllegalStateException("NavController not found")

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_inicio,
                R.id.nav_cursos,
                R.id.nav_carreras,
                R.id.nav_profesores,
                R.id.nav_alumnos,
                R.id.nav_ciclos,
                R.id.nav_ofertaAcademica,
                R.id.nav_usuarios,
                R.id.nav_matricula,
                R.id.nav_notas,
                R.id.nav_historial,
                R.id.nav_perfil
            ),
            binding.drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        val userRole = SessionManager.getUserRole(this)
        Log.d("MainActivity", "User Role: $userRole")

        userRole?.let { setupMenuVisibility(it) }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            restrictAccessToDestination(destination)
        }

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            val allowed = checkDestinationAccess(menuItem.itemId)
            if (allowed) {
                val handled = NavigationUI.onNavDestinationSelected(menuItem, navController)
                if (handled) binding.drawerLayout.closeDrawers()
                handled
            } else {
                false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                showLogoutConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(
            navController,
            appBarConfiguration
        ) || super.onSupportNavigateUp()
    }

    override fun onStart() {
        super.onStart()
        if (!SessionManager.isLoggedIn(this)) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                performLogout()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    private fun performLogout() {
        SessionManager.clear(this)
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupMenuVisibility(userRole: String) {
        val menu = binding.navView.menu
        RolePermissions.DESTINATION_ROLES.forEach { (destinationId, roles) ->
            val isVisible = if (destinationId == R.id.nav_historial) {
                SessionManager.hasRole(this, "Alumno") // Solo visible para "Alumno"
            } else {
                roles.isEmpty() || SessionManager.hasAnyRole(this, roles)
            }
            menu.findItem(destinationId)?.isVisible = isVisible
        }
        Log.d("MainActivity", "Menu visibility updated for role: $userRole")
    }

    private fun restrictAccessToDestination(destination: NavDestination) {
        val requiredRoles = RolePermissions.DESTINATION_ROLES[destination.id] ?: emptyList()
        if (requiredRoles.isNotEmpty() && !SessionManager.hasAnyRole(this, requiredRoles)) {
            navController.navigate(RolePermissions.DEFAULT_DESTINATION)
            Notificador.show(
                view = findViewById(android.R.id.content),
                mensaje = "Acceso denegado",
                colorResId = R.color.colorError,
                anchorView = null
            )
        }
    }

    private fun checkDestinationAccess(destinationId: Int): Boolean {
        val requiredRoles = RolePermissions.DESTINATION_ROLES[destinationId] ?: emptyList()
        return requiredRoles.isEmpty() || SessionManager.hasAnyRole(this, requiredRoles)
    }
}
