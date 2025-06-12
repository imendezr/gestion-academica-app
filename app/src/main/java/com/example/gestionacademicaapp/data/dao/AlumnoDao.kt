package com.example.gestionacademicaapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gestionacademicaapp.data.api.model.Alumno

@Dao
interface AlumnoDao {
    @Query("SELECT * FROM alumnos")
    suspend fun getAll(): List<Alumno>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alumno: Alumno)

    @Update
    suspend fun update(alumno: Alumno)

    @Query("DELETE FROM alumnos WHERE idAlumno = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM alumnos WHERE cedula = :cedula")
    suspend fun deleteByCedula(cedula: String)

    @Query("SELECT * FROM alumnos WHERE idAlumno = :idAlumno")
    suspend fun getById(idAlumno: Long): Alumno?

    @Query("SELECT * FROM alumnos WHERE cedula = :cedula")
    suspend fun getByCedula(cedula: String): Alumno?

    @Query("SELECT * FROM alumnos WHERE nombre = :nombre")
    suspend fun getByNombre(nombre: String): Alumno?

    @Query("SELECT * FROM alumnos WHERE pk_carrera = :idCarrera")
    suspend fun getByCarrera(idCarrera: Long): List<Alumno>

    @Query(
        """
        SELECT a.* FROM alumnos a
        JOIN carrera_cursos cc ON a.pk_carrera = cc.pk_carrera
        WHERE cc.pk_ciclo = :idCiclo
        GROUP BY a.idAlumno
    """
    )
    suspend fun getAlumnosConOfertaEnCiclo(idCiclo: Long): List<Alumno>
}
