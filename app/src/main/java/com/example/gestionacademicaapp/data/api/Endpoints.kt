package com.example.gestionacademicaapp.data.api

object Endpoints {

    // Alumnos
    const val ALUMNOS_ALL = "alumnos/listar"
    fun alumnoByCedula(cedula: String) = "alumnos/buscarPorCedula?cedula=$cedula"
    fun alumnoByNombre(nombre: String) = "alumnos/buscarPorNombre?nombre=$nombre"
    fun alumnosByCarrera(idCarrera: Int) = "alumnos/buscarPorCarrera?carrera=$idCarrera"
    fun alumnoHistorial(idAlumno: Int) = "alumnos/historialAlumno/$idAlumno"
    const val ALUMNO_INSERT = "alumnos/insertar"
    const val ALUMNO_UPDATE = "alumnos/modificar"
    fun alumnoDelete(id: Int) = "alumnos/eliminar/$id"

    // Carreras
    const val CARRERAS_ALL = "carreras/listar"
    fun carreraByCodigo(codigo: String) = "carreras/buscarPorCodigo?codigo=$codigo"
    fun carreraByNombre(nombre: String) = "carreras/buscarPorNombre?nombre=$nombre"
    const val CARRERA_INSERT = "carreras/insertar"
    const val CARRERA_UPDATE = "carreras/modificar"
    fun carreraDelete(id: Int) = "carreras/eliminar/$id"
    fun addCursoToCarrera(idCarrera: Int, idCurso: Int, idCiclo: Int) =
        "carreras/insertarCursoACarrera/$idCarrera/$idCurso/$idCiclo"
    fun removeCursoFromCarrera(idCarrera: Int, idCurso: Int) =
        "carreras/eliminarCursoDeCarrera/$idCarrera/$idCurso"
    fun updateCursoOrden(idCarrera: Int, idCurso: Int, nuevoIdCiclo: Int) =
        "carreras/modificarOrdenCursoCarrera/$idCarrera/$idCurso/$nuevoIdCiclo"

    // Ciclos
    const val CICLOS_ALL = "ciclos/listar"
    const val CICLO_INSERT = "ciclos/insertar"
    const val CICLO_UPDATE = "ciclos/modificar"
    fun cicloDelete(id: Int) = "ciclos/eliminar/$id"
    fun cicloByAnio(anio: Int) = "ciclos/buscarPorAnnio?annio=$anio"
    fun cicloActivate(id: Int) = "ciclos/activarCiclo/$id"

    // Cursos
    const val CURSOS_ALL = "cursos/listar"
    fun cursoByCodigo(codigo: String) = "cursos/buscarPorCodigo?codigo=$codigo"
    fun cursoByNombre(nombre: String) = "cursos/buscarPorNombre?nombre=$nombre"
    fun cursosByCarrera(idCarrera: Int) = "cursos/buscarCursosPorCarrera?idCarrera=$idCarrera"
    const val CURSO_INSERT = "cursos/insertar"
    const val CURSO_UPDATE = "cursos/modificar"
    fun cursoDelete(id: Int) = "cursos/eliminar/$id"

    // Grupos
    const val GRUPOS_ALL = "grupos/listar"
    const val GRUPO_INSERT = "grupos/insertar"
    const val GRUPO_UPDATE = "grupos/modificar"
    fun grupoDelete(id: Int) = "grupos/eliminar/$id"
    fun cursosByCarreraAndCurso(idCarrera: Int, idCurso: Int) =
        "grupos/buscarCursosPorCarreraYCiclo/$idCarrera/$idCurso"
    fun gruposByCarreraCurso(idCarreraCurso: Long) =
        "grupos/buscarGruposPorCarreraCurso/$idCarreraCurso"

    // Matrículas
    const val MATRICULAS_ALL = "matricular/listar"
    const val MATRICULA_INSERT = "matricular/insertar"
    const val MATRICULA_UPDATE = "matricular/modificar"
    fun matriculaDelete(id: Int) = "matricular/eliminar/$id"

    // Profesores
    const val PROFESORES_ALL = "profesores/listar"
    fun profesorByCedula(cedula: String) = "profesores/buscarPorCedula?cedula=$cedula"
    fun profesorByNombre(nombre: String) = "profesores/buscarPorNombre?nombre=$nombre"
    const val PROFESOR_INSERT = "profesores/insertar"
    const val PROFESOR_UPDATE = "profesores/modificar"
    fun profesorDelete(id: Int) = "profesores/eliminar/$id"

    // Usuarios
    const val USUARIOS_ALL = "usuarios/listar"
    fun usuarioByCedula(cedula: String) = "usuarios/buscarPorCedula?cedula=$cedula"
    const val USUARIO_INSERT = "usuarios/insertar"
    const val USUARIO_UPDATE = "usuarios/modificar"
    fun usuarioDelete(id: Int) = "usuarios/eliminar/$id"
    fun login(cedula: String, contrasena: String) = "usuarios/login?cedula=$cedula&clave=$contrasena"
}
