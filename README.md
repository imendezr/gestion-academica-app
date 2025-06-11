
# Sistema de Gestión Académica

Este proyecto es una **aplicación móvil Android** desarrollada con Kotlin y arquitectura MVVM para gestionar la información académica de una universidad. El sistema interactúa con un backend en Java y una base de datos Oracle mediante procedimientos almacenados.

---

## Estructura del Proyecto

```
app/src/main/java/com/example/gestionacademicaapp/
│
├── data/                          # Lógica de acceso a datos
│   ├── api/
│   │   └── ApiService.kt         # Define endpoints del backend
│   ├── api/model/
│	│		├── dto/                  # Data Transfer Objects
│ 	│ 		│	├── CursoDto.kt
│ 	│ 		│	├── GrupoDto.kt
│ 	│ 		│	├── GrupoProfesorDto.kt
│ 	│ 		│	└── MatriculaAlumnoDto.kt
│	│		├── Alumno.kt
│	│		├── Carrera.kt
│	│		├── CarreraCurso.kt
│	│		├── Ciclo.kt
│	│		├── Curso.kt
│	│		├── Grupo.kt
│	│		├── Matricula.kt
│	│		├── Profesor.kt
│	│		└── Usuario.kt
│   │   
│   └── repository/               # Repositorios por entidad
│		├── AlumnoRepository.kt
│		├── CarreraCursoRepository.kt
│		├── CarreraRepository.kt
│		├── CicloRepository.kt
│		├── CursoRepository.kt
│		├── GrupoRepository.kt
│		├── MatriculaRepository.kt
│		├── ProfesorRepository.kt
│		└── UsuarioRepository.kt
│
├── di/
│   └── AppModule.kt              # Inyección de dependencias con Hilt
│
├── ui/                            # Vista y lógica de presentación
│   ├── MainActivity.kt
│   ├── alumnos/
│   │   ├── AlumnosAdapter.kt
│   │   ├── AlumnosFragment.kt
│   │   └── AlumnosViewModel.kt
│   ├── carreras/
│   │   ├── CarrerasAdapter.kt
│   │   ├── CarrerasFragment.kt
│   │   ├── CarrerasViewModel.kt
│   │   ├── CarreraCursosAdapter.kt
│   │   ├── CarreraCursosFragment.kt
│   │   └── CarreraCursosViewModel.kt
│   ├── ciclos/
│   │   ├── CiclosAdapter.kt
│   │   ├── CiclosFragment.kt
│   │   └── CiclosViewModel.kt
│   ├── cursos/
│   │   ├── CursosAdapter.kt
│   │   ├── CursosFragment.kt
│   │   └── CursosViewModel.kt
│   ├── historial_academico/
│   │   ├── HistorialAcademicoFragment.kt
│   │   └── HistorialAcademicoViewModel.kt
│   ├── inicio/
│   │   ├── InicioFragment.kt
│   │   └── InicioViewModel.kt
│   ├── login/
│   │   ├── LoginActivity.kt
│   │   └── LoginViewModel.kt
│   ├── matricula/
│   │   ├── MatriculaAdapter.kt
│   │   ├── MatriculaCursoGrupoAdapter.kt
│   │   ├── MatriculaCursoGrupoFragment.kt
│   │   ├── MatriculaDetailsAdapter.kt
│   │   ├── MatriculaDetailsFragment.kt
│   │   ├── MatriculaDetailsViewModel.kt
│   │   ├── MatriculaFragment.kt
│   │   └── MatriculaViewModel.kt
│   ├── notas/
│   │   ├── NotasAdapter.kt
│   │   ├── NotasFragment.kt
│   │   └── NotasViewModel.kt
│   ├── oferta_academica/
│   │   ├── CursoAdapter.kt
│   │   ├── GrupoAdapter.kt
│   │   ├── GruposOfertaFragment.kt
│   │   ├── OfertaAcademicaFragment.kt
│   │   └── OfertaAcademicaViewModel.kt
│   ├── perfil/
│   │   ├── PerfilAdapter.kt
│   │   ├── PerfilFragment.kt
│   │   └── PerfilViewModel.kt
│   ├── profesores/
│   │   ├── ProfesoresAdapter.kt
│   │   ├── ProfesoresFragment.kt
│   │   └── ProfesoresViewModel.kt
│   ├── usuarios/
│   │   ├── UsuariosAdapter.kt
│   │   ├── UsuariosFragment.kt
│   │   └── UsuariosViewModel.kt
│   └── common/
│       ├── DialogFormularioFragment.kt
│       ├── CampoFormulario.kt
│       ├── adapter/
│ 		│ 	└── BaseAdapter.kt
│       ├── state/
│ 		│ 	└── UiState.kt
│       └── validators/           # Validaciones de campos de texto, fechas, etc.
│ 			├── AlumnoValidator.kt
│ 			├── CarreraCursoValidator.kt
│ 			├── CarreraValidator.kt
│ 			├── CicloValidator.kt
│ 			├── CursoValidator.kt
│ 			├── GrupoValidator.kt
│ 			├── ProfesorValidator.kt
│ 			└── UsuarioValidator.kt
│
└── utils/
    ├── Constants.kt              # Constantes globales
    ├── ErrorHandler.kt           # Manejo de errores centralizado
    ├── Extensions.kt             # Funciones de extensión útiles
    ├── Notificador.kt            # Utilidad para mostrar Snackbars
    ├── ResourceProvider.kt       # Acceso a strings desde ViewModels
    ├── RolePermissions.kt        # Permisos por tipo de usuario
    ├── SearchViewUtils.kt        # Configuración estandarizada de SearchView
    └── SessionManager.kt         # Manejo de sesión de usuario actual
 
```

---

## Arquitectura

Se implementa un enfoque basado en **MVVM**, utilizando:

- **ViewModel + LiveData**: para manejar estados de UI reactivos.
- **Repository Pattern**: capa intermedia que abstrae llamadas HTTP.
- **Data Binding**: para vincular XML y ViewModel eficientemente.
- **Jetpack Navigation**: navegación de una sola actividad con múltiples fragments.
- **Hilt**: para inyección de dependencias.

---

## Perfiles de Usuario

- **Administrador**: Accede a todo excepto a registrar notas.
- **Matriculador**: Solo puede gestionar matrículas.
- **Profesor**: Solo puede ver sus cursos y registrar notas.
- **Alumno**: Consulta su historial académico.

---

## Funcionalidades

1. **Login / Registro de usuarios**
2. **Gestión de Carreras** (asociación de cursos por ciclo)
3. **Gestión de Cursos**
4. **Gestión de Ciclos Académicos**
5. **Gestión de Profesores**
6. **Gestión de Alumnos**
7. **Oferta Académica** (programar grupos por ciclo y curso)
8. **Matrícula** (por alumno y por grupo)
9. **Registro de Notas**
10. **Consulta de Historial Académico**
11. **Seguridad de Usuarios**

---

## Requisitos y Ejecución

### Android

- Conexión al backend REST (ver configuración de `ApiClient`).

## Buenas Prácticas

- Reutilización de componentes: `DialogFormularioFragment`, `ListAdapter`, etc.
- Estilo visual coherente y accesibilidad.
- Separación clara de capas y responsabilidades.
- Módulos adaptables a múltiples dispositivos.
