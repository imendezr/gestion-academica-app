package com.example.gestionacademicaapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gestionacademicaapp.data.api.model.Grupo
import com.example.gestionacademicaapp.data.api.model.dto.CursoDto
import com.example.gestionacademicaapp.data.api.model.dto.GrupoDto
import com.example.gestionacademicaapp.data.api.model.dto.GrupoProfesorDto

@Dao
interface GrupoDao {
    @Query("SELECT * FROM grupos")
    suspend fun getAll(): List<Grupo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(grupo: Grupo)

    @Update
    suspend fun update(grupo: Grupo)

    @Query("DELETE FROM grupos WHERE idGrupo = :id")
    suspend fun delete(id: Long)

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
    suspend fun getCursosByCarreraAndCiclo(idCarrera: Long, idCiclo: Long): List<CursoDto>

    @Query(
        """
        SELECT g.idGrupo, g.id_carrera_curso AS idCarreraCurso, g.numero_grupo, g.horario, 
               g.id_profesor AS idProfesor, p.nombre AS nombreProfesor
        FROM grupos g
        JOIN profesores p ON g.id_profesor = p.idProfesor
        JOIN carrera_cursos cc ON g.id_carrera_curso = cc.idCarreraCurso
        WHERE cc.pk_carrera = :idCarrera AND cc.pk_curso = :idCurso
    """
    )
    suspend fun getGruposByCarreraCurso(idCarrera: Long, idCurso: Long): List<GrupoDto>

    @Query(
        """
        SELECT g.idGrupo, g.id_carrera_curso AS idCarreraCurso, g.numero_grupo, g.horario, 
               g.id_profesor AS idProfesor, p.nombre AS nombreProfesor
        FROM grupos g
        JOIN profesores p ON g.id_profesor = p.idProfesor
        JOIN carrera_cursos cc ON g.id_carrera_curso = cc.idCarreraCurso
        WHERE cc.pk_curso = :idCurso AND cc.pk_ciclo = :idCiclo AND cc.pk_carrera = :idCarrera
    """
    )
    suspend fun getGruposByCursoCicloCarrera(
        idCurso: Long,
        idCiclo: Long,
        idCarrera: Long
    ): List<GrupoDto>

    @Query(
        """
        SELECT g.idGrupo, g.id_carrera_curso AS idCarreraCurso, g.numero_grupo, g.horario, 
               g.id_profesor AS idProfesor, p.nombre AS nombreProfesor
        FROM grupos g
        JOIN profesores p ON g.id_profesor = p.idProfesor
        JOIN carrera_cursos cc ON g.id_carrera_curso = cc.idCarreraCurso
        JOIN alumnos a ON p.cedula = :cedula
        WHERE g.id_profesor = a.idAlumno
    """
    )
    suspend fun getGruposByProfesor(cedula: String): List<GrupoDto>

    @Query(
        """
        SELECT g.idGrupo, g.numero_grupo, g.horario, c.codigo AS codigoCurso, c.nombre AS nombreCurso,
               ca.codigo AS codigoCarrera, ca.nombre AS nombreCarrera, ci.anio, ci.numero AS numeroCiclo
        FROM grupos g
        JOIN carrera_cursos cc ON g.id_carrera_curso = cc.idCarreraCurso
        JOIN cursos c ON cc.pk_curso = c.idCurso
        JOIN carreras ca ON cc.pk_carrera = ca.idCarrera
        JOIN ciclos ci ON cc.pk_ciclo = ci.idCiclo
        JOIN profesores p ON g.id_profesor = p.idProfesor
        WHERE p.cedula = :cedula AND ci.estado = 'ACTIVO'
    """
    )
    suspend fun getGruposByProfesorCicloActivo(cedula: String): List<GrupoProfesorDto>

    @Query(
        """
        SELECT g.idGrupo, g.id_carrera_curso AS idCarreraCurso, g.numero_grupo, g.horario, 
               g.id_profesor AS idProfesor, p.nombre AS nombreProfesor
        FROM grupos g
        JOIN profesores p ON g.id_profesor = p.idProfesor
        JOIN matriculas m ON g.idGrupo = m.pk_grupo
        WHERE m.idMatricula = :idMatricula
    """
    )
    suspend fun getGrupoByMatriculaId(idMatricula: Long): GrupoDto?

    @Query(
        """
        SELECT c.idCurso, c.codigo, c.nombre, c.creditos, c.horas_semanales, 
               cc.idCarreraCurso, ci.anio, ci.numero AS numeroCiclo, ci.idCiclo
        FROM grupos g
        JOIN carrera_cursos cc ON g.id_carrera_curso = cc.idCarreraCurso
        JOIN cursos c ON cc.pk_curso = c.idCurso
        JOIN ciclos ci ON cc.pk_ciclo = ci.idCiclo
        WHERE g.idGrupo = :idGrupo
    """
    )
    suspend fun getCursoByGrupoId(idGrupo: Long): CursoDto?
}
