package com.example.gestionacademicaapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gestionacademicaapp.data.api.model.Usuario

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios")
    suspend fun getAll(): List<Usuario>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(usuario: Usuario)

    @Update
    suspend fun update(usuario: Usuario)

    @Query("DELETE FROM usuarios WHERE idUsuario = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM usuarios WHERE cedula = :cedula")
    suspend fun getByCedula(cedula: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE cedula = :cedula AND clave = :clave")
    suspend fun login(cedula: String, clave: String): Usuario?
}
