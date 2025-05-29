package com.example.gestionacademicaapp.data.api

import com.example.gestionacademicaapp.data.api.model.*
import com.example.gestionacademicaapp.data.api.model.dto.CursoDto
import com.example.gestionacademicaapp.data.api.model.dto.GrupoDto
import com.example.gestionacademicaapp.data.api.model.dto.MatriculaAlumnoDto

interface ApiService {

    // ALUMNOS
    suspend fun insertAlumno(alumno: Alumno): Alumno // POST /api/alumnos/insertar
    suspend fun updateAlumno(alumno: Alumno): Alumno // PUT /api/alumnos/modificar
    suspend fun deleteAlumno(id: Long): Boolean       // DELETE /api/alumnos/eliminar/{id}
    suspend fun getAllAlumnos(): List<Alumno>         // GET /api/alumnos/listar
    suspend fun getAlumnoByCedula(cedula: String): Alumno // GET /api/alumnos/buscarPorCedula?cedula={}
    suspend fun getAlumnoByNombre(nombre: String): Alumno // GET /api/alumnos/buscarPorNombre?nombre={}
    suspend fun getAlumnosByCarrera(idCarrera: Long): List<Alumno> // GET /api/alumnos/buscarPorCarrera?carrera={}
    suspend fun getAlumnoHistorial(idAlumno: Long): List<MatriculaAlumnoDto> // GET /api/alumnos/historialAlumno/{id}

    // CARRERAS
    suspend fun insertCarrera(carrera: Carrera): Carrera // POST /api/carreras/insertar
    suspend fun updateCarrera(carrera: Carrera): Carrera // PUT /api/carreras/modificar
    suspend fun deleteCarrera(id: Long): Boolean          // DELETE /api/carreras/eliminar/{id}
    suspend fun getAllCarreras(): List<Carrera>           // GET /api/carreras/listar
    suspend fun getCarreraByCodigo(codigo: String): Carrera // GET /api/carreras/buscarPorCodigo?codigo={}
    suspend fun getCarreraByNombre(nombre: String): Carrera // GET /api/carreras/buscarPorNombre?nombre={}
    suspend fun addCursoToCarrera(idCarrera: Long, idCurso: Long, idCiclo: Long): CarreraCurso // POST /api/carreras/insertarCursoACarrera/{pkCarrera}/{pkCurso}/{pkCiclo}
    suspend fun removeCursoFromCarrera(idCarrera: Long, idCurso: Long): Boolean // DELETE /api/carreras/eliminarCursoDeCarrera/{pkCarrera}/{pkCurso}
    suspend fun updateCursoOrden(idCarrera: Long, idCurso: Long, nuevoIdCiclo: Long): CarreraCurso // PUT /api/carreras/modificarOrdenCursoCarrera/{pkCarrera}/{pkCurso}/{nuevoPkCiclo}

    // CICLOS
    suspend fun insertCiclo(ciclo: Ciclo): Ciclo // POST /api/ciclos/insertar
    suspend fun updateCiclo(ciclo: Ciclo): Ciclo // PUT /api/ciclos/modificar
    suspend fun deleteCiclo(id: Long): Boolean   // DELETE /api/ciclos/eliminar/{id}
    suspend fun getAllCiclos(): List<Ciclo>      // GET /api/ciclos/listar
    suspend fun getCicloByAnio(anio: Int): Ciclo // GET /api/ciclos/buscarPorAnnio?annio={}
    suspend fun activateCiclo(id: Long): Boolean   // POST /api/ciclos/activarCiclo/{id}

    // CURSOS
    suspend fun insertCurso(curso: Curso): Curso // POST /api/cursos/insertar
    suspend fun updateCurso(curso: Curso): Curso // PUT /api/cursos/modificar
    suspend fun deleteCurso(id: Long): Boolean   // DELETE /api/cursos/eliminar/{id}
    suspend fun getAllCursos(): List<Curso>      // GET /api/cursos/listar
    suspend fun getCursoByCodigo(codigo: String): Curso // GET /api/cursos/buscarPorCodigo?codigo={}
    suspend fun getCursoByNombre(nombre: String): Curso // GET /api/cursos/buscarPorNombre?nombre={}
    suspend fun getCursosByCarrera(idCarrera: Long): List<CursoDto> // GET /api/cursos/buscarCursosPorCarrera?idCarrera={}
    suspend fun getCursosByCarreraAndCiclo(idCarrera: Long, idCiclo: Long): List<CursoDto> // GET /api/cursos/buscarCursosPorCarreraYCiclo/{pkCarrera}/{pkCiclo}

    // GRUPOS
    suspend fun insertGrupo(grupo: Grupo): Grupo // POST /api/grupos/insertar
    suspend fun updateGrupo(grupo: Grupo): Grupo // PUT /api/grupos/modificar
    suspend fun deleteGrupo(id: Long): Boolean   // DELETE /api/grupos/eliminar/{id}
    suspend fun getAllGrupos(): List<Grupo>      // GET /api/grupos/listar
    suspend fun getGruposByCarreraCurso(idCarrera: Long, idCurso: Long): List<GrupoDto> // GET /api/grupos//buscarGruposPorCarreraCurso/{pkCarrera}/{pkCurso}}

    // MATRÍCULAS
    suspend fun insertMatricula(matricula: Matricula): Matricula // POST /api/matricular/insertar
    suspend fun updateMatricula(matricula: Matricula): Matricula // PUT /api/matricular/modificar
    suspend fun deleteMatricula(id: Long): Boolean               // DELETE /api/matricular/eliminar/{id}
    suspend fun getAllMatriculas(): List<Matricula>              // GET /api/matricular/listar

    // PROFESORES
    suspend fun insertProfesor(profesor: Profesor): Profesor // POST /api/profesores/insertar
    suspend fun updateProfesor(profesor: Profesor): Profesor // PUT /api/profesores/modificar
    suspend fun deleteProfesor(id: Long): Boolean             // DELETE /api/profesores/eliminar/{id}
    suspend fun getAllProfesores(): List<Profesor>            // GET /api/profesores/listar
    suspend fun getProfesorByCedula(cedula: String): Profesor // GET /api/profesores/buscarPorCedula?cedula={}
    suspend fun getProfesorByNombre(nombre: String): Profesor // GET /api/profesores/buscarPorNombre?nombre={}

    // USUARIOS
    suspend fun insertUsuario(usuario: Usuario): Usuario // POST /api/usuarios/insertar
    suspend fun updateUsuario(usuario: Usuario): Usuario // PUT /api/usuarios/modificar
    suspend fun deleteUsuario(id: Long): Boolean          // DELETE /api/usuarios/eliminar/{id}
    suspend fun getAllUsuarios(): List<Usuario>           // GET /api/usuarios/listar
    suspend fun getUsuarioByCedula(cedula: String): Usuario // GET /api/usuarios/buscarPorCedula?cedula={}
    suspend fun login(cedula: String, clave: String): Usuario // POST /api/usuarios/login?cedula={}&clave={}
}
