package com.example.gestionacademicaapp.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.gestionacademicaapp.data.AppDatabase
import com.example.gestionacademicaapp.data.api.ApiService
import com.example.gestionacademicaapp.data.dao.AlumnoDao
import com.example.gestionacademicaapp.data.dao.CarreraCursoDao
import com.example.gestionacademicaapp.data.dao.CarreraDao
import com.example.gestionacademicaapp.data.dao.CicloDao
import com.example.gestionacademicaapp.data.dao.CursoDao
import com.example.gestionacademicaapp.data.dao.GrupoDao
import com.example.gestionacademicaapp.data.dao.MatriculaDao
import com.example.gestionacademicaapp.data.dao.ProfesorDao
import com.example.gestionacademicaapp.data.dao.UsuarioDao
import com.example.gestionacademicaapp.data.repository.AlumnoRepository
import com.example.gestionacademicaapp.data.repository.AlumnoRepositoryImpl
import com.example.gestionacademicaapp.data.repository.AlumnoRepositoryLocal
import com.example.gestionacademicaapp.data.repository.AlumnoRepositoryRemote
import com.example.gestionacademicaapp.data.repository.CarreraCursoRepository
import com.example.gestionacademicaapp.data.repository.CarreraCursoRepositoryImpl
import com.example.gestionacademicaapp.data.repository.CarreraCursoRepositoryLocal
import com.example.gestionacademicaapp.data.repository.CarreraCursoRepositoryRemote
import com.example.gestionacademicaapp.data.repository.CarreraRepository
import com.example.gestionacademicaapp.data.repository.CarreraRepositoryImpl
import com.example.gestionacademicaapp.data.repository.CarreraRepositoryLocal
import com.example.gestionacademicaapp.data.repository.CarreraRepositoryRemote
import com.example.gestionacademicaapp.data.repository.CicloRepository
import com.example.gestionacademicaapp.data.repository.CicloRepositoryImpl
import com.example.gestionacademicaapp.data.repository.CicloRepositoryLocal
import com.example.gestionacademicaapp.data.repository.CicloRepositoryRemote
import com.example.gestionacademicaapp.data.repository.CursoRepository
import com.example.gestionacademicaapp.data.repository.CursoRepositoryImpl
import com.example.gestionacademicaapp.data.repository.CursoRepositoryLocal
import com.example.gestionacademicaapp.data.repository.CursoRepositoryRemote
import com.example.gestionacademicaapp.data.repository.GrupoRepository
import com.example.gestionacademicaapp.data.repository.GrupoRepositoryImpl
import com.example.gestionacademicaapp.data.repository.GrupoRepositoryLocal
import com.example.gestionacademicaapp.data.repository.GrupoRepositoryRemote
import com.example.gestionacademicaapp.data.repository.MatriculaRepository
import com.example.gestionacademicaapp.data.repository.MatriculaRepositoryImpl
import com.example.gestionacademicaapp.data.repository.MatriculaRepositoryLocal
import com.example.gestionacademicaapp.data.repository.MatriculaRepositoryRemote
import com.example.gestionacademicaapp.data.repository.ProfesorRepository
import com.example.gestionacademicaapp.data.repository.ProfesorRepositoryImpl
import com.example.gestionacademicaapp.data.repository.ProfesorRepositoryLocal
import com.example.gestionacademicaapp.data.repository.ProfesorRepositoryRemote
import com.example.gestionacademicaapp.data.repository.UsuarioRepository
import com.example.gestionacademicaapp.data.repository.UsuarioRepositoryImpl
import com.example.gestionacademicaapp.data.repository.UsuarioRepositoryLocal
import com.example.gestionacademicaapp.data.repository.UsuarioRepositoryRemote
import com.example.gestionacademicaapp.utils.ConfigManager
import com.example.gestionacademicaapp.utils.Constants
import com.example.gestionacademicaapp.utils.ResourceProvider
import com.example.gestionacademicaapp.utils.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideResourceProvider(@ApplicationContext context: Context): ResourceProvider =
        ResourceProvider(context)

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "gestion_academica_db"
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Carreras
                db.execSQL("INSERT OR IGNORE INTO carreras (idCarrera, codigo, nombre, titulo) VALUES (1, 'INF01', 'Ingeniería en Informática', 'Bachiller en Ingeniería en Informática')")
                db.execSQL("INSERT OR IGNORE INTO carreras (idCarrera, codigo, nombre, titulo) VALUES (2, 'ADM01', 'Administración de Empresas', 'Bachiller en Administración')")
                db.execSQL("INSERT OR IGNORE INTO carreras (idCarrera, codigo, nombre, titulo) VALUES (3, 'MAT01', 'Matemáticas Aplicadas', 'Bachiller en Matemáticas Aplicadas')")

                // Cursos
                db.execSQL("INSERT OR IGNORE INTO cursos (idCurso, codigo, nombre, creditos, horas_semanales) VALUES (1, 'INF101', 'Programación I', 4, 6)")
                db.execSQL("INSERT OR IGNORE INTO cursos (idCurso, codigo, nombre, creditos, horas_semanales) VALUES (2, 'INF201', 'Estructuras de Datos', 4, 5)")
                db.execSQL("INSERT OR IGNORE INTO cursos (idCurso, codigo, nombre, creditos, horas_semanales) VALUES (3, 'ADM101', 'Contabilidad Básica', 3, 4)")
                db.execSQL("INSERT OR IGNORE INTO cursos (idCurso, codigo, nombre, creditos, horas_semanales) VALUES (4, 'ADM201', 'Gestión Financiera', 4, 5)")
                db.execSQL("INSERT OR IGNORE INTO cursos (idCurso, codigo, nombre, creditos, horas_semanales) VALUES (5, 'MAT101', 'Cálculo I', 4, 6)")
                db.execSQL("INSERT OR IGNORE INTO cursos (idCurso, codigo, nombre, creditos, horas_semanales) VALUES (6, 'MAT201', 'Álgebra Lineal', 4, 5)")

                // Ciclos
                db.execSQL("INSERT OR IGNORE INTO ciclos (idCiclo, anio, numero, fecha_inicio, fecha_fin, estado) VALUES (1, 2025, 1, '2025-02-01', '2025-06-01', 'Activo')")
                db.execSQL("INSERT OR IGNORE INTO ciclos (idCiclo, anio, numero, fecha_inicio, fecha_fin, estado) VALUES (2, 2025, 2, '2025-07-01', '2025-11-01', 'Inactivo')")

                // Carrera_Cursos
                db.execSQL("INSERT OR IGNORE INTO carrera_cursos (idCarreraCurso, pk_carrera, pk_curso, pk_ciclo) VALUES (1, 1, 1, 1)")
                db.execSQL("INSERT OR IGNORE INTO carrera_cursos (idCarreraCurso, pk_carrera, pk_curso, pk_ciclo) VALUES (2, 1, 2, 1)")
                db.execSQL("INSERT OR IGNORE INTO carrera_cursos (idCarreraCurso, pk_carrera, pk_curso, pk_ciclo) VALUES (3, 2, 3, 1)")
                db.execSQL("INSERT OR IGNORE INTO carrera_cursos (idCarreraCurso, pk_carrera, pk_curso, pk_ciclo) VALUES (4, 2, 4, 1)")
                db.execSQL("INSERT OR IGNORE INTO carrera_cursos (idCarreraCurso, pk_carrera, pk_curso, pk_ciclo) VALUES (5, 3, 5, 1)")
                db.execSQL("INSERT OR IGNORE INTO carrera_cursos (idCarreraCurso, pk_carrera, pk_curso, pk_ciclo) VALUES (6, 3, 6, 1)")

                // Profesores
                db.execSQL("INSERT OR IGNORE INTO profesores (idProfesor, cedula, nombre, telefono, email) VALUES (1, '100100100', 'Carlos Rojas', '88888888', 'crojas@una.ac.cr')")
                db.execSQL("INSERT OR IGNORE INTO profesores (idProfesor, cedula, nombre, telefono, email) VALUES (2, '200200200', 'María López', '87777777', 'mlopez@una.ac.cr')")
                db.execSQL("INSERT OR IGNORE INTO profesores (idProfesor, cedula, nombre, telefono, email) VALUES (3, '300300300', 'Luis Jiménez', '86666666', 'ljimenez@una.ac.cr')")
                db.execSQL("INSERT OR IGNORE INTO profesores (idProfesor, cedula, nombre, telefono, email) VALUES (4, '400400400', 'Ana Salas', '85555555', 'asalas@una.ac.cr')")

                // Alumnos
                db.execSQL("INSERT OR IGNORE INTO alumnos (idAlumno, cedula, nombre, telefono, email, fecha_nacimiento, pk_carrera) VALUES (1, '111111111', 'Laura Fernández', '85001234', 'laura@estudiante.una.ac.cr', '2000-03-15', 1)")
                db.execSQL("INSERT OR IGNORE INTO alumnos (idAlumno, cedula, nombre, telefono, email, fecha_nacimiento, pk_carrera) VALUES (2, '222222222', 'José Ramírez', '84005890', 'jose@estudiante.una.ac.cr', '1999-11-20', 1)")
                db.execSQL("INSERT OR IGNORE INTO alumnos (idAlumno, cedula, nombre, telefono, email, fecha_nacimiento, pk_carrera) VALUES (3, '333333333', 'Ana Rodríguez', '87007890', 'ana@estudiante.una.ac.cr', '2001-06-10', 2)")
                db.execSQL("INSERT OR IGNORE INTO alumnos (idAlumno, cedula, nombre, telefono, email, fecha_nacimiento, pk_carrera) VALUES (4, '444444444', 'David Castro', '83009876', 'david@estudiante.una.ac.cr', '2000-12-25', 2)")
                db.execSQL("INSERT OR IGNORE INTO alumnos (idAlumno, cedula, nombre, telefono, email, fecha_nacimiento, pk_carrera) VALUES (5, '555555555', 'Carla Sánchez', '81001234', 'carla@estudiante.una.ac.cr', '2001-05-12', 3)")
                db.execSQL("INSERT OR IGNORE INTO alumnos (idAlumno, cedula, nombre, telefono, email, fecha_nacimiento, pk_carrera) VALUES (6, '666666666', 'Miguel Torres', '82005678', 'miguel@estudiante.una.ac.cr', '2000-09-25', 3)")

                // Grupos
                db.execSQL("INSERT OR IGNORE INTO grupos (idGrupo, id_carrera_curso, numero_grupo, horario, id_profesor) VALUES (1, 1, 1, 'Lunes 8:00-10:00', 1)")
                db.execSQL("INSERT OR IGNORE INTO grupos (idGrupo, id_carrera_curso, numero_grupo, horario, id_profesor) VALUES (2, 1, 2, 'Martes 10:00-12:00', 2)")
                db.execSQL("INSERT OR IGNORE INTO grupos (idGrupo, id_carrera_curso, numero_grupo, horario, id_profesor) VALUES (3, 3, 1, 'Miércoles 9:00-11:00', 3)")
                db.execSQL("INSERT OR IGNORE INTO grupos (idGrupo, id_carrera_curso, numero_grupo, horario, id_profesor) VALUES (4, 4, 1, 'Jueves 14:00-16:00', 3)")
                db.execSQL("INSERT OR IGNORE INTO grupos (idGrupo, id_carrera_curso, numero_grupo, horario, id_profesor) VALUES (5, 5, 1, 'Viernes 8:00-10:00', 4)")
                db.execSQL("INSERT OR IGNORE INTO grupos (idGrupo, id_carrera_curso, numero_grupo, horario, id_profesor) VALUES (6, 6, 1, 'Lunes 10:00-12:00', 4)")

                // Matriculas
                db.execSQL("INSERT OR IGNORE INTO matriculas (idMatricula, pk_alumno, pk_grupo, nota) VALUES (1, 1, 1, 85)")
                db.execSQL("INSERT OR IGNORE INTO matriculas (idMatricula, pk_alumno, pk_grupo, nota) VALUES (2, 2, 1, 78)")
                db.execSQL("INSERT OR IGNORE INTO matriculas (idMatricula, pk_alumno, pk_grupo, nota) VALUES (3, 1, 2, 90)")
                db.execSQL("INSERT OR IGNORE INTO matriculas (idMatricula, pk_alumno, pk_grupo, nota) VALUES (4, 3, 3, 82)")
                db.execSQL("INSERT OR IGNORE INTO matriculas (idMatricula, pk_alumno, pk_grupo, nota) VALUES (5, 4, 3, 88)")
                db.execSQL("INSERT OR IGNORE INTO matriculas (idMatricula, pk_alumno, pk_grupo, nota) VALUES (6, 3, 4, 75)")
                db.execSQL("INSERT OR IGNORE INTO matriculas (idMatricula, pk_alumno, pk_grupo, nota) VALUES (7, 5, 5, 92)")
                db.execSQL("INSERT OR IGNORE INTO matriculas (idMatricula, pk_alumno, pk_grupo, nota) VALUES (8, 6, 5, 80)")

                // Usuarios
                db.execSQL("INSERT OR IGNORE INTO usuarios (idUsuario, cedula, clave, tipo) VALUES (1, '100100100', 'prof123', 'Profesor')")
                db.execSQL("INSERT OR IGNORE INTO usuarios (idUsuario, cedula, clave, tipo) VALUES (2, '200200200', 'prof456', 'Profesor')")
                db.execSQL("INSERT OR IGNORE INTO usuarios (idUsuario, cedula, clave, tipo) VALUES (3, '300300300', 'prof789', 'Profesor')")
                db.execSQL("INSERT OR IGNORE INTO usuarios (idUsuario, cedula, clave, tipo) VALUES (4, '111111111', 'alumno1', 'Alumno')")
                db.execSQL("INSERT OR IGNORE INTO usuarios (idUsuario, cedula, clave, tipo) VALUES (5, '333333333', 'alumno2', 'Alumno')")
                db.execSQL("INSERT OR IGNORE INTO usuarios (idUsuario, cedula, clave, tipo) VALUES (6, '555555555', 'alumno3', 'Alumno')")
                db.execSQL("INSERT OR IGNORE INTO usuarios (idUsuario, cedula, clave, tipo) VALUES (7, '500500500', 'admin123', 'Administrador')")
                db.execSQL("INSERT OR IGNORE INTO usuarios (idUsuario, cedula, clave, tipo) VALUES (8, '600600600', 'matri123', 'Matriculador')")
            }
        }).build()
    }

    @Provides
    @Singleton
    fun provideUsuarioDao(database: AppDatabase): UsuarioDao = database.usuarioDao()

    @Provides
    @Singleton
    fun provideProfesorDao(database: AppDatabase): ProfesorDao = database.profesorDao()

    @Provides
    @Singleton
    fun provideMatriculaDao(database: AppDatabase): MatriculaDao = database.matriculaDao()

    @Provides
    @Singleton
    fun provideGrupoDao(database: AppDatabase): GrupoDao = database.grupoDao()

    @Provides
    @Singleton
    fun provideCursoDao(database: AppDatabase): CursoDao = database.cursoDao()

    @Provides
    @Singleton
    fun provideCicloDao(database: AppDatabase): CicloDao = database.cicloDao()

    @Provides
    @Singleton
    fun provideCarreraDao(database: AppDatabase): CarreraDao = database.carreraDao()

    @Provides
    @Singleton
    fun provideCarreraCursoDao(database: AppDatabase): CarreraCursoDao = database.carreraCursoDao()

    @Provides
    @Singleton
    fun provideAlumnoDao(database: AppDatabase): AlumnoDao = database.alumnoDao()

    @Provides
    @Singleton
    fun provideConfigManager(@ApplicationContext context: Context): ConfigManager =
        ConfigManager(context)

    @Provides
    @Singleton
    fun provideSessionManager(
        @ApplicationContext context: Context,
        configManager: ConfigManager,
        usuarioDao: UsuarioDao
    ): SessionManager = SessionManager(context, configManager, usuarioDao)

    @Provides
    @Singleton
    fun provideUsuarioRepositoryRemote(apiService: ApiService): UsuarioRepositoryRemote =
        UsuarioRepositoryRemote(apiService)

    @Provides
    @Singleton
    fun provideUsuarioRepositoryLocal(usuarioDao: UsuarioDao): UsuarioRepositoryLocal =
        UsuarioRepositoryLocal(usuarioDao)

    @Provides
    @Singleton
    fun provideUsuarioRepository(
        remote: UsuarioRepositoryRemote,
        local: UsuarioRepositoryLocal,
        configManager: ConfigManager
    ): UsuarioRepository = UsuarioRepositoryImpl(remote, local, configManager)

    @Provides
    @Singleton
    fun provideProfesorRepositoryRemote(apiService: ApiService): ProfesorRepositoryRemote =
        ProfesorRepositoryRemote(apiService)

    @Provides
    @Singleton
    fun provideProfesorRepositoryLocal(profesorDao: ProfesorDao): ProfesorRepositoryLocal =
        ProfesorRepositoryLocal(profesorDao)

    @Provides
    @Singleton
    fun provideProfesorRepository(
        remote: ProfesorRepositoryRemote,
        local: ProfesorRepositoryLocal,
        configManager: ConfigManager
    ): ProfesorRepository = ProfesorRepositoryImpl(remote, local, configManager)

    @Provides
    @Singleton
    fun provideMatriculaRepositoryRemote(apiService: ApiService): MatriculaRepositoryRemote =
        MatriculaRepositoryRemote(apiService)

    @Provides
    @Singleton
    fun provideMatriculaRepositoryLocal(matriculaDao: MatriculaDao): MatriculaRepositoryLocal =
        MatriculaRepositoryLocal(matriculaDao)

    @Provides
    @Singleton
    fun provideMatriculaRepository(
        remote: MatriculaRepositoryRemote,
        local: MatriculaRepositoryLocal,
        configManager: ConfigManager
    ): MatriculaRepository = MatriculaRepositoryImpl(remote, local, configManager)

    @Provides
    @Singleton
    fun provideGrupoRepositoryRemote(apiService: ApiService): GrupoRepositoryRemote =
        GrupoRepositoryRemote(apiService)

    @Provides
    @Singleton
    fun provideGrupoRepositoryLocal(grupoDao: GrupoDao): GrupoRepositoryLocal =
        GrupoRepositoryLocal(grupoDao)

    @Provides
    @Singleton
    fun provideGrupoRepository(
        remote: GrupoRepositoryRemote,
        local: GrupoRepositoryLocal,
        configManager: ConfigManager
    ): GrupoRepository = GrupoRepositoryImpl(remote, local, configManager)

    @Provides
    @Singleton
    fun provideCursoRepositoryRemote(apiService: ApiService): CursoRepositoryRemote =
        CursoRepositoryRemote(apiService)

    @Provides
    @Singleton
    fun provideCursoRepositoryLocal(cursoDao: CursoDao): CursoRepositoryLocal =
        CursoRepositoryLocal(cursoDao)

    @Provides
    @Singleton
    fun provideCursoRepository(
        remote: CursoRepositoryRemote,
        local: CursoRepositoryLocal,
        configManager: ConfigManager
    ): CursoRepository = CursoRepositoryImpl(remote, local, configManager)

    @Provides
    @Singleton
    fun provideCicloRepositoryRemote(apiService: ApiService): CicloRepositoryRemote =
        CicloRepositoryRemote(apiService)

    @Provides
    @Singleton
    fun provideCicloRepositoryLocal(cicloDao: CicloDao): CicloRepositoryLocal =
        CicloRepositoryLocal(cicloDao)

    @Provides
    @Singleton
    fun provideCicloRepository(
        remote: CicloRepositoryRemote,
        local: CicloRepositoryLocal,
        configManager: ConfigManager
    ): CicloRepository = CicloRepositoryImpl(remote, local, configManager)

    @Provides
    @Singleton
    fun provideCarreraRepositoryRemote(apiService: ApiService): CarreraRepositoryRemote =
        CarreraRepositoryRemote(apiService)

    @Provides
    @Singleton
    fun provideCarreraRepositoryLocal(
        carreraDao: CarreraDao,
        carreraCursoDao: CarreraCursoDao
    ): CarreraRepositoryLocal = CarreraRepositoryLocal(carreraDao, carreraCursoDao)

    @Provides
    @Singleton
    fun provideCarreraRepository(
        remote: CarreraRepositoryRemote,
        local: CarreraRepositoryLocal,
        configManager: ConfigManager
    ): CarreraRepository = CarreraRepositoryImpl(remote, local, configManager)

    @Provides
    @Singleton
    fun provideCarreraCursoRepositoryRemote(apiService: ApiService): CarreraCursoRepositoryRemote =
        CarreraCursoRepositoryRemote(apiService)

    @Provides
    @Singleton
    fun provideCarreraCursoRepositoryLocal(carreraCursoDao: CarreraCursoDao): CarreraCursoRepositoryLocal =
        CarreraCursoRepositoryLocal(carreraCursoDao)

    @Provides
    @Singleton
    fun provideCarreraCursoRepository(
        remote: CarreraCursoRepositoryRemote,
        local: CarreraCursoRepositoryLocal,
        configManager: ConfigManager
    ): CarreraCursoRepository = CarreraCursoRepositoryImpl(remote, local, configManager)

    @Provides
    @Singleton
    fun provideAlumnoRepositoryRemote(apiService: ApiService): AlumnoRepositoryRemote =
        AlumnoRepositoryRemote(apiService)

    @Provides
    @Singleton
    fun provideAlumnoRepositoryLocal(alumnoDao: AlumnoDao): AlumnoRepositoryLocal =
        AlumnoRepositoryLocal(alumnoDao)

    @Provides
    @Singleton
    fun provideAlumnoRepository(
        remote: AlumnoRepositoryRemote,
        local: AlumnoRepositoryLocal,
        configManager: ConfigManager
    ): AlumnoRepository = AlumnoRepositoryImpl(remote, local, configManager)
}
