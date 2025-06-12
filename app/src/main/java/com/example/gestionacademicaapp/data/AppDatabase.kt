package com.example.gestionacademicaapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.gestionacademicaapp.data.api.model.Alumno
import com.example.gestionacademicaapp.data.api.model.Carrera
import com.example.gestionacademicaapp.data.api.model.CarreraCurso
import com.example.gestionacademicaapp.data.api.model.Ciclo
import com.example.gestionacademicaapp.data.api.model.Curso
import com.example.gestionacademicaapp.data.api.model.Grupo
import com.example.gestionacademicaapp.data.api.model.Matricula
import com.example.gestionacademicaapp.data.api.model.Profesor
import com.example.gestionacademicaapp.data.api.model.Usuario
import com.example.gestionacademicaapp.data.dao.AlumnoDao
import com.example.gestionacademicaapp.data.dao.CarreraCursoDao
import com.example.gestionacademicaapp.data.dao.CarreraDao
import com.example.gestionacademicaapp.data.dao.CicloDao
import com.example.gestionacademicaapp.data.dao.CursoDao
import com.example.gestionacademicaapp.data.dao.GrupoDao
import com.example.gestionacademicaapp.data.dao.MatriculaDao
import com.example.gestionacademicaapp.data.dao.ProfesorDao
import com.example.gestionacademicaapp.data.dao.UsuarioDao

@Database(
    entities = [
        Alumno::class,
        Carrera::class,
        CarreraCurso::class,
        Ciclo::class,
        Curso::class,
        Grupo::class,
        Matricula::class,
        Profesor::class,
        Usuario::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alumnoDao(): AlumnoDao
    abstract fun carreraDao(): CarreraDao
    abstract fun carreraCursoDao(): CarreraCursoDao
    abstract fun cicloDao(): CicloDao
    abstract fun cursoDao(): CursoDao
    abstract fun grupoDao(): GrupoDao
    abstract fun matriculaDao(): MatriculaDao
    abstract fun profesorDao(): ProfesorDao
    abstract fun usuarioDao(): UsuarioDao
}
