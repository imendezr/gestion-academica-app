package com.example.gestionacademicaapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gestionacademicaapp.data.api.model.Matricula
import com.example.gestionacademicaapp.data.api.model.dto.MatriculaAlumnoDto

@Dao
interface MatriculaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(matricula: Matricula)

    @Update
    suspend fun update(matricula: Matricula)

    @Query("DELETE FROM matriculas WHERE idMatricula = :id")
    suspend fun delete(id: Long)

    @Query(
        """
        SELECT m.idMatricula, m.nota, g.numero_grupo, g.horario, 
               c.codigo AS codigoCurso, c.nombre AS nombreCurso, 
               ca.codigo AS codigoCarrera, ca.nombre AS nombreCarrera, 
               p.nombre AS nombreProfesor, p.cedula AS cedulaProfesor
        FROM matriculas m
        JOIN grupos g ON m.pk_grupo = g.idGrupo
        JOIN carrera_cursos cc ON g.id_carrera_curso = cc.idCarreraCurso
        JOIN cursos c ON cc.pk_curso = c.idCurso
        JOIN carreras ca ON cc.pk_carrera = ca.idCarrera
        JOIN profesores p ON g.id_profesor = p.idProfesor
        JOIN alumnos a ON m.pk_alumno = a.idAlumno
        WHERE a.cedula = :cedula
    """
    )
    suspend fun getMatriculasPorCedula(cedula: String): List<MatriculaAlumnoDto>

    @Query(
        """
        SELECT m.idMatricula, m.nota, g.numero_grupo, g.horario, 
               c.codigo AS codigoCurso, c.nombre AS nombreCurso, 
               ca.codigo AS codigoCarrera, ca.nombre AS nombreCarrera, 
               p.nombre AS nombreProfesor, p.cedula AS cedulaProfesor
        FROM matriculas m
        JOIN grupos g ON m.pk_grupo = g.idGrupo
        JOIN carrera_cursos cc ON g.id_carrera_curso = cc.idCarreraCurso
        JOIN cursos c ON cc.pk_curso = c.idCurso
        JOIN carreras ca ON cc.pk_carrera = ca.idCarrera
        JOIN profesores p ON g.id_profesor = p.idProfesor
        WHERE m.pk_alumno = :idAlumno AND cc.pk_ciclo = :idCiclo
    """
    )
    suspend fun getMatriculasPorAlumnoYCiclo(
        idAlumno: Long,
        idCiclo: Long
    ): List<MatriculaAlumnoDto>

    @Query(
        """
        SELECT m.idMatricula, m.nota, g.numero_grupo, g.horario, 
               c.codigo AS codigoCurso, c.nombre AS nombreCurso, 
               ca.codigo AS codigoCarrera, ca.nombre AS nombreCarrera, 
               p.nombre AS nombreProfesor, p.cedula AS cedulaProfesor
        FROM matriculas m
        JOIN grupos g ON m.pk_grupo = g.idGrupo
        JOIN carrera_cursos cc ON g.id_carrera_curso = cc.idCarreraCurso
        JOIN cursos c ON cc.pk_curso = c.idCurso
        JOIN carreras ca ON cc.pk_carrera = ca.idCarrera
        JOIN profesores p ON g.id_profesor = p.idProfesor
        WHERE m.pk_grupo = :idGrupo
    """
    )
    suspend fun getMatriculasPorGrupo(idGrupo: Long): List<MatriculaAlumnoDto>

    @Query("SELECT * FROM matriculas WHERE idMatricula = :idMatricula")
    suspend fun getById(idMatricula: Long): Matricula?

    @Query("SELECT EXISTS(SELECT 1 FROM matriculas WHERE pk_alumno = :idAlumno AND pk_grupo = :idGrupo)")
    suspend fun checkMatriculaExists(idAlumno: Long, idGrupo: Long): Boolean

    @Query("UPDATE matriculas SET pk_grupo = :idGrupo WHERE idMatricula = :idMatricula")
    suspend fun updateMatriculaGrupo(idMatricula: Long, idGrupo: Long)

    @Query("SELECT * FROM matriculas WHERE pk_grupo = :grupoId")
    suspend fun getByGrupoId(grupoId: Long): Matricula?
}
