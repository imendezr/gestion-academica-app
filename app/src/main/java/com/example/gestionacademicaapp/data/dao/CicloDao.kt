package com.example.gestionacademicaapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gestionacademicaapp.data.api.model.Ciclo

@Dao
interface CicloDao {
    @Query("SELECT * FROM ciclos")
    suspend fun getAll(): List<Ciclo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ciclo: Ciclo)

    @Update
    suspend fun update(ciclo: Ciclo)

    @Query("DELETE FROM ciclos WHERE idCiclo = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM ciclos WHERE anio = :anio")
    suspend fun getByAnio(anio: Long): Ciclo?

    @Query("UPDATE ciclos SET estado = 'ACTIVO' WHERE idCiclo = :id")
    suspend fun activate(id: Long)

    @Query("SELECT * FROM ciclos WHERE idCiclo = :id")
    suspend fun getById(id: Long): Ciclo?
}
