package com.example.gestionacademicaapp.data.api

import com.example.gestionacademicaapp.data.api.model.*
import com.example.gestionacademicaapp.data.api.model.dto.CarreraCicloCursoDto
import com.example.gestionacademicaapp.data.api.model.dto.GrupoDto
import com.example.gestionacademicaapp.data.api.model.dto.MatriculaAlumnoDto

interface ApiService {

    // Alumnos
    suspend fun insertAlumno(alumno: Alumno): Alumno
    suspend fun updateAlumno(alumno: Alumno): Alumno
    suspend fun deleteAlumno(id: Int): Boolean
    suspend fun getAllAlumnos(): List<Alumno>
    suspend fun getAlumnoByCedula(cedula: String): Alumno
    suspend fun getAlumnosByNombre(nombre: String): List<Alumno>
    suspend fun getAlumnosByCarrera(idCarrera: Int): List<Alumno>
    suspend fun getAlumnoHistorial(idAlumno: Int): List<MatriculaAlumnoDto>

    // Carreras
    suspend fun insertCarrera(carrera: Carrera): Carrera
    suspend fun updateCarrera(carrera: Carrera): Carrera
    suspend fun deleteCarrera(id: Int): Boolean
    suspend fun getAllCarreras(): List<Carrera>
    suspend fun getCarreraByCodigo(codigo: String): Carrera
    suspend fun getCarrerasByNombre(nombre: String): List<Carrera>
    suspend fun addCursoToCarrera(idCarrera: Int, idCurso: Int, idCiclo: Int): CarreraCurso
    suspend fun removeCursoFromCarrera(idCarrera: Int, idCurso: Int): Boolean
    suspend fun updateCursoOrden(idCarrera: Int, idCurso: Int, nuevoIdCiclo: Int): CarreraCurso

    // Ciclos
    suspend fun insertCiclo(ciclo: Ciclo): Ciclo
    suspend fun updateCiclo(ciclo: Ciclo): Ciclo
    suspend fun deleteCiclo(id: Int): Boolean
    suspend fun getAllCiclos(): List<Ciclo>
    suspend fun getCicloByAnio(anio: Int): Ciclo
    suspend fun activateCiclo(id: Int): Ciclo

    // Cursos
    suspend fun insertCurso(curso: Curso): Curso
    suspend fun updateCurso(curso: Curso): Curso
    suspend fun deleteCurso(id: Int): Boolean
    suspend fun getAllCursos(): List<Curso>
    suspend fun getCursoByCodigo(codigo: String): Curso
    suspend fun getCursosByNombre(nombre: String): List<Curso>
    suspend fun getCursosByCarrera(idCarrera: Int): List<Curso>

    // Grupos
    suspend fun insertGrupo(grupo: Grupo): Grupo
    suspend fun updateGrupo(grupo: Grupo): Grupo
    suspend fun deleteGrupo(id: Int): Boolean
    suspend fun getAllGrupos(): List<Grupo>
    suspend fun getCursosByCarreraAndCiclo(idCarrera: Int, idCiclo: Int): List<CarreraCicloCursoDto>
    suspend fun getGruposByCarreraCurso(idCarreraCurso: Int): List<GrupoDto>

    // Matrículas
    suspend fun insertMatricula(matricula: Matricula): Matricula
    suspend fun updateMatricula(matricula: Matricula): Matricula
    suspend fun deleteMatricula(id: Int): Boolean

    // Profesores
    suspend fun insertProfesor(profesor: Profesor): Profesor
    suspend fun updateProfesor(profesor: Profesor): Profesor
    suspend fun deleteProfesor(id: Int): Boolean
    suspend fun getAllProfesores(): List<Profesor>
    suspend fun getProfesorByCedula(cedula: String): Profesor
    suspend fun getProfesoresByNombre(nombre: String): List<Profesor>

    // Usuarios
    suspend fun insertUsuario(usuario: Usuario): Usuario
    suspend fun updateUsuario(usuario: Usuario): Usuario
    suspend fun deleteUsuario(id: Int): Boolean
    suspend fun getAllUsuarios(): List<Usuario>
    suspend fun getUsuarioByCedula(cedula: String): Usuario
    suspend fun login(cedula: String, contrasena: String): Usuario
}
