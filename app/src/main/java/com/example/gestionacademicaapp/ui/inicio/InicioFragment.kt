package com.example.gestionacademicaapp.ui.inicio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.gestionacademicaapp.R
import com.example.gestionacademicaapp.databinding.FragmentInicioBinding

class InicioFragment : Fragment() {

    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inicioViewModel =
            ViewModelProvider(this)[InicioViewModel::class.java]

        _binding = FragmentInicioBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textHome: TextView = binding.textHome
        val textSub: TextView = binding.textSub

        inicioViewModel.text.observe(viewLifecycleOwner) {
            textHome.text = it
            textSub.text = getString(R.string.dashboard_según_rol)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
