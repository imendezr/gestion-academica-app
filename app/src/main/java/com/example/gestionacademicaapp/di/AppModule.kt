package com.example.gestionacademicaapp.di

import com.example.gestionacademicaapp.data.api.ApiClient
import com.example.gestionacademicaapp.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApiClient(): ApiClient = ApiClient

    @Provides
    @Singleton
    fun provideCarreraRepository(): CarreraRepository = CarreraRepository()

    @Provides
    @Singleton
    fun provideCursoRepository(): CursoRepository = CursoRepository()

    @Provides
    @Singleton
    fun provideProfesorRepository(): ProfesorRepository = ProfesorRepository()

    @Provides
    @Singleton
    fun provideAlumnoRepository(): AlumnoRepository = AlumnoRepository()

    @Provides
    @Singleton
    fun provideCicloRepository(): CicloRepository = CicloRepository()

    @Provides
    @Singleton
    fun provideGrupoRepository(): GrupoRepository = GrupoRepository()

    @Provides
    @Singleton
    fun provideUsuarioRepository(): UsuarioRepository = UsuarioRepository()

    @Provides
    @Singleton
    fun provideCarreraCursoRepository(): CarreraCursoRepository = CarreraCursoRepository()
}
