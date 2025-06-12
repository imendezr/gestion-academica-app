package com.example.gestionacademicaapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gestionacademicaapp.data.api.model.Profesor

@Dao
interface ProfesorDao {
    @Query("SELECT * FROM profesores")
    suspend fun getAll(): List<Profesor>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profesor: Profesor)

    @Update
    suspend fun update(profesor: Profesor)

    @Query("DELETE FROM profesores WHERE idProfesor = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM profesores WHERE cedula = :cedula")
    suspend fun deleteByCedula(cedula: String)

    @Query("SELECT * FROM profesores WHERE cedula = :cedula")
    suspend fun getByCedula(cedula: String): Profesor?

    @Query("SELECT * FROM profesores WHERE nombre = :nombre")
    suspend fun getByNombre(nombre: String): Profesor?
}
