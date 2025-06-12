package com.example.gestionacademicaapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gestionacademicaapp.data.api.model.CarreraCurso
import com.example.gestionacademicaapp.data.api.model.dto.CursoDto

@Dao
interface CarreraCursoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(carreraCurso: CarreraCurso)

    @Update
    suspend fun update(carreraCurso: CarreraCurso)

    @Query("DELETE FROM carrera_cursos WHERE pk_carrera = :idCarrera AND pk_curso = :idCurso")
    suspend fun delete(idCarrera: Long, idCurso: Long)

    @Query("SELECT * FROM carrera_cursos")
    suspend fun getAll(): List<CarreraCurso>

    @Query(
        """
        SELECT c.idCurso, c.codigo, c.nombre, c.creditos, c.horas_semanales, 
               cc.idCarreraCurso, ci.anio, ci.numero AS numeroCiclo, ci.idCiclo
        FROM carrera_cursos cc
        JOIN cursos c ON cc.pk_curso = c.idCurso
        JOIN ciclos ci ON cc.pk_ciclo = ci.idCiclo
        WHERE cc.pk_carrera = :idCarrera AND cc.pk_ciclo = :idCiclo
    """
    )
    suspend fun getCursosByCarreraYCiclo(idCarrera: Long, idCiclo: Long): List<CursoDto>

    @Query("SELECT EXISTS(SELECT 1 FROM grupos WHERE id_carrera_curso IN (SELECT idCarreraCurso FROM carrera_cursos WHERE pk_carrera = :idCarrera AND pk_curso = :idCurso))")
    suspend fun tieneGruposAsociados(idCarrera: Long, idCurso: Long): Boolean
}
