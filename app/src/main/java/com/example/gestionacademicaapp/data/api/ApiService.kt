package com.example.gestionacademicaapp.data.api

import com.example.gestionacademicaapp.data.api.model.Alumno
import com.example.gestionacademicaapp.data.api.model.Carrera
import com.example.gestionacademicaapp.data.api.model.CarreraCurso
import com.example.gestionacademicaapp.data.api.model.Ciclo
import com.example.gestionacademicaapp.data.api.model.Curso
import com.example.gestionacademicaapp.data.api.model.Grupo
import com.example.gestionacademicaapp.data.api.model.Matricula
import com.example.gestionacademicaapp.data.api.model.Profesor
import com.example.gestionacademicaapp.data.api.model.Usuario
import com.example.gestionacademicaapp.data.api.model.dto.CursoDto
import com.example.gestionacademicaapp.data.api.model.dto.GrupoDto
import com.example.gestionacademicaapp.data.api.model.dto.MatriculaAlumnoDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // ALUMNOS
    @POST("alumnos/insertar")
    suspend fun insertAlumno(@Body alumno: Alumno): Response<Unit>

    @PUT("alumnos/modificar")
    suspend fun updateAlumno(@Body alumno: Alumno): Response<Unit>

    @DELETE("alumnos/eliminar/{id}")
    suspend fun deleteAlumno(@Path("id") id: Long): Response<Unit>

    @GET("alumnos/listar")
    suspend fun getAllAlumnos(): List<Alumno>

    @GET("alumnos/buscarPorCedula")
    suspend fun getAlumnoByCedula(@Query("cedula") cedula: String): Alumno

    @GET("alumnos/buscarPorNombre")
    suspend fun getAlumnoByNombre(@Query("nombre") nombre: String): Alumno

    @GET("alumnos/buscarPorCarrera")
    suspend fun getAlumnosByCarrera(@Query("carrera") idCarrera: Long): List<Alumno>


    // CARRERAS
    @POST("carreras/insertar")
    suspend fun insertCarrera(@Body carrera: Carrera): Response<Unit>

    @PUT("carreras/modificar")
    suspend fun updateCarrera(@Body carrera: Carrera): Response<Unit>

    @DELETE("carreras/eliminar/{id}")
    suspend fun deleteCarrera(@Path("id") id: Long): Response<Unit>

    @GET("carreras/listar")
    suspend fun getAllCarreras(): List<Carrera>

    @GET("carreras/buscarPorCodigo")
    suspend fun getCarreraByCodigo(@Query("codigo") codigo: String): Carrera

    @GET("carreras/buscarPorNombre")
    suspend fun getCarreraByNombre(@Query("nombre") nombre: String): Carrera

    @POST("carreras/insertarCursoACarrera/{idCarrera}/{idCurso}/{idCiclo}")
    suspend fun addCursoToCarrera(
        @Path("idCarrera") idCarrera: Long,
        @Path("idCurso") idCurso: Long,
        @Path("idCiclo") idCiclo: Long
    ): Response<Unit>

    @DELETE("carreras/eliminarCursoDeCarrera/{idCarrera}/{idCurso}")
    suspend fun removeCursoFromCarrera(
        @Path("idCarrera") idCarrera: Long,
        @Path("idCurso") idCurso: Long
    ): Response<Unit>

    @PUT("carreras/modificarOrdenCursoCarrera/{idCarrera}/{idCurso}/{nuevoIdCiclo}")
    suspend fun updateCursoOrden(
        @Path("idCarrera") idCarrera: Long,
        @Path("idCurso") idCurso: Long,
        @Path("nuevoIdCiclo") nuevoIdCiclo: Long
    ): Response<Unit>

    // CARRERA-CURSO
    @POST("carrera-curso/insertar")
    suspend fun insertCarreraCurso(@Body carreraCurso: CarreraCurso): Response<Unit>

    @PUT("carrera-curso/modificar")
    suspend fun updateCarreraCurso(@Body carreraCurso: CarreraCurso): Response<Unit>

    @DELETE("carrera-curso/eliminar")
    suspend fun deleteCarreraCurso(
        @Query("idCarrera") idCarrera: Long,
        @Query("idCurso") idCurso: Long
    ): Response<Unit>

    @GET("carrera-curso/cursos")
    suspend fun getCursosByCarreraYCiclo(
        @Query("idCarrera") idCarrera: Long,
        @Query("idCiclo") idCiclo: Long
    ): List<CursoDto>

    @GET("carrera-curso/listar")
    suspend fun getAllCarreraCurso(): List<CarreraCurso>


    // CICLOS
    @POST("ciclos/insertar")
    suspend fun insertCiclo(@Body ciclo: Ciclo): Response<Unit>

    @PUT("ciclos/modificar")
    suspend fun updateCiclo(@Body ciclo: Ciclo): Response<Unit>

    @DELETE("ciclos/eliminar/{id}")
    suspend fun deleteCiclo(@Path("id") id: Long): Response<Unit>

    @GET("ciclos/listar")
    suspend fun getAllCiclos(): List<Ciclo>

    @GET("ciclos/buscarPorAnnio")
    suspend fun getCicloByAnio(@Query("annio") anio: Long): Ciclo

    @POST("ciclos/activarCiclo/{id}")
    suspend fun activateCiclo(@Path("id") id: Long): Response<Unit>

    @GET("ciclos/buscarPorId/{id}")
    suspend fun getCicloById(@Path("id") id: Long): Ciclo


    // CURSOS
    @POST("cursos/insertar")
    suspend fun insertCurso(@Body curso: Curso): Response<Unit>

    @PUT("cursos/modificar")
    suspend fun updateCurso(@Body curso: Curso): Response<Unit>

    @DELETE("cursos/eliminar/{id}")
    suspend fun deleteCurso(@Path("id") id: Long): Response<Unit>

    @GET("cursos/listar")
    suspend fun getAllCursos(): List<Curso>

    @GET("cursos/buscarPorCodigo")
    suspend fun getCursoByCodigo(@Query("codigo") codigo: String): Curso

    @GET("cursos/buscarPorNombre")
    suspend fun getCursoByNombre(@Query("nombre") nombre: String): Curso

    @GET("cursos/buscarCursosPorCarrera")
    suspend fun getCursosByCarrera(@Query("idCarrera") idCarrera: Long): List<CursoDto>

    @GET("cursos/buscarCursosPorCarreraYCiclo/{idCarrera}/{idCiclo}")
    suspend fun getCursosByCarreraAndCiclo(
        @Path("idCarrera") idCarrera: Long,
        @Path("idCiclo") idCiclo: Long
    ): List<CursoDto>

    @GET("cursos/buscarCursosPorCiclo/{idCiclo}")
    suspend fun getCursosByCiclo(
        @Path("idCiclo") idCiclo: Long
    ): List<CursoDto>


    // GRUPOS
    @POST("grupos/insertar")
    suspend fun insertGrupo(@Body grupo: Grupo): Response<Unit>

    @PUT("grupos/modificar")
    suspend fun updateGrupo(@Body grupo: Grupo): Response<Unit>

    @DELETE("grupos/eliminar/{id}")
    suspend fun deleteGrupo(@Path("id") id: Long): Response<Unit>

    @GET("grupos/listar")
    suspend fun getAllGrupos(): List<Grupo>

    @GET("grupos/buscarGruposPorCarreraCurso/{idCarrera}/{idCurso}")
    suspend fun getGruposByCarreraCurso(
        @Path("idCarrera") idCarrera: Long,
        @Path("idCurso") idCurso: Long
    ): List<GrupoDto>

    @GET("grupos/buscarGruposPorCursoCicloCarrera/{idCurso}/{idCiclo}/{idCarrera}")
    suspend fun getGruposByCursoCicloCarrera(
        @Path("idCurso") idCurso: Long,
        @Path("idCiclo") idCiclo: Long,
        @Path("idCarrera") idCarrera: Long
    ): List<GrupoDto>


    // MATRÍCULAS
    @POST("matricular/insertar")
    suspend fun insertMatricula(@Body matricula: Matricula): Response<Unit>

    @PUT("matricular/modificar")
    suspend fun updateMatricula(@Body matricula: Matricula): Response<Unit>

    @DELETE("matricular/eliminar/{id}")
    suspend fun deleteMatricula(@Path("id") id: Long): Response<Unit>

    @GET("matricular/listarMatriculasPorAlumno/{cedula}")
    suspend fun getMatriculasPorCedula(
        @Path("cedula") cedula: String
    ): List<MatriculaAlumnoDto>

    @GET("matricular/listarMatriculasPorAlumnoYCiclo/{idAlumno}/{idCiclo}")
    suspend fun getMatriculasPorAlumnoYCiclo(
        @Path("idAlumno") idAlumno: Long,
        @Path("idCiclo") idCiclo: Long
    ): List<MatriculaAlumnoDto>


    // PROFESORES
    @POST("profesores/insertar")
    suspend fun insertProfesor(@Body profesor: Profesor): Response<Unit>

    @PUT("profesores/modificar")
    suspend fun updateProfesor(@Body profesor: Profesor): Response<Unit>

    @DELETE("profesores/eliminar/{id}")
    suspend fun deleteProfesor(@Path("id") id: Long): Response<Unit>

    @GET("profesores/listar")
    suspend fun getAllProfesores(): List<Profesor>

    @GET("profesores/buscarPorCedula")
    suspend fun getProfesorByCedula(@Query("cedula") cedula: String): Profesor

    @GET("profesores/buscarPorNombre")
    suspend fun getProfesorByNombre(@Query("nombre") nombre: String): Profesor


    // USUARIOS
    @POST("usuarios/insertar")
    suspend fun insertUsuario(@Body usuario: Usuario): Response<Unit>

    @PUT("usuarios/modificar")
    suspend fun updateUsuario(@Body usuario: Usuario): Response<Unit>

    @DELETE("usuarios/eliminar/{id}")
    suspend fun deleteUsuario(@Path("id") id: Long): Response<Unit>

    @GET("usuarios/listar")
    suspend fun getAllUsuarios(): List<Usuario>

    @GET("usuarios/buscarPorCedula")
    suspend fun getUsuarioByCedula(@Query("cedula") cedula: String): Usuario

    @POST("usuarios/login")
    suspend fun login(
        @Query("cedula") cedula: String,
        @Query("clave") clave: String
    ): Usuario
}
