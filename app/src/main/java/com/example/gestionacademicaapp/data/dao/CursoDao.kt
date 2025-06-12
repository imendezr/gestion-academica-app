package com.example.gestionacademicaapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gestionacademicaapp.data.api.model.Curso
import com.example.gestionacademicaapp.data.api.model.dto.CursoDto

@Dao
interface CursoDao {
    @Query("SELECT * FROM cursos")
    suspend fun getAll(): List<Curso>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(curso: Curso)

    @Update
    suspend fun update(curso: Curso)

    @Query("DELETE FROM cursos WHERE idCurso = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM cursos WHERE codigo = :codigo")
    suspend fun getByCodigo(codigo: String): Curso?

    @Query("SELECT * FROM cursos WHERE nombre = :nombre")
    suspend fun getByNombre(nombre: String): Curso?

    @Query(
        """
        SELECT c.idCurso, c.codigo, c.nombre, c.creditos, c.horas_semanales, 
               cc.idCarreraCurso, ci.anio, ci.numero AS numeroCiclo, ci.idCiclo
        FROM cursos c
        JOIN carrera_cursos cc ON c.idCurso = cc.pk_curso
        JOIN ciclos ci ON cc.pk_ciclo = ci.idCiclo
        WHERE cc.pk_carrera = :idCarrera
    """
    )
    suspend fun getByCarrera(idCarrera: Long): List<CursoDto>

    @Query(
        """
        SELECT c.idCurso, c.codigo, c.nombre, c.creditos, c.horas_semanales, 
               cc.idCarreraCurso, ci.anio, ci.numero AS numeroCiclo, ci.idCiclo
        FROM cursos c
        JOIN carrera_cursos cc ON c.idCurso = cc.pk_curso
        JOIN ciclos ci ON cc.pk_ciclo = ci.idCiclo
        WHERE cc.pk_carrera = :idCarrera AND cc.pk_ciclo = :idCiclo
    """
    )
    suspend fun getByCarreraYCiclo(idCarrera: Long, idCiclo: Long): List<CursoDto>

    @Query(
        """
        SELECT c.idCurso, c.codigo, c.nombre, c.creditos, c.horas_semanales, 
               cc.idCarreraCurso, ci.anio, ci.numero AS numeroCiclo, ci.idCiclo
        FROM cursos c
        JOIN carrera_cursos cc ON c.idCurso = cc.pk_curso
        JOIN ciclos ci ON cc.pk_ciclo = ci.idCiclo
        WHERE cc.pk_ciclo = :idCiclo
    """
    )
    suspend fun getByCiclo(idCiclo: Long): List<CursoDto>
}
