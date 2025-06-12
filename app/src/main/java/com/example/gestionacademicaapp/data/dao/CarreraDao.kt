package com.example.gestionacademicaapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gestionacademicaapp.data.api.model.Carrera

@Dao
interface CarreraDao {
    @Query("SELECT * FROM carreras")
    suspend fun getAll(): List<Carrera>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(carrera: Carrera)

    @Update
    suspend fun update(carrera: Carrera)

    @Query("DELETE FROM carreras WHERE idCarrera = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM carreras WHERE codigo = :codigo")
    suspend fun getByCodigo(codigo: String): Carrera?

    @Query("SELECT * FROM carreras WHERE nombre = :nombre")
    suspend fun getByNombre(nombre: String): Carrera?
}
