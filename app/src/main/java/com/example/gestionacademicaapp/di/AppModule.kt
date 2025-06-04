package com.example.gestionacademicaapp.di


import android.content.Context
import com.example.gestionacademicaapp.data.api.ApiService
import com.example.gestionacademicaapp.data.repository.AlumnoRepository
import com.example.gestionacademicaapp.data.repository.CarreraCursoRepository
import com.example.gestionacademicaapp.data.repository.CarreraRepository
import com.example.gestionacademicaapp.data.repository.CicloRepository
import com.example.gestionacademicaapp.data.repository.CursoRepository
import com.example.gestionacademicaapp.data.repository.GrupoRepository
import com.example.gestionacademicaapp.data.repository.MatriculaRepository
import com.example.gestionacademicaapp.data.repository.ProfesorRepository
import com.example.gestionacademicaapp.data.repository.UsuarioRepository
import com.example.gestionacademicaapp.utils.Constants
import com.example.gestionacademicaapp.utils.ResourceProvider
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
    fun provideCarreraRepository(apiService: ApiService): CarreraRepository =
        CarreraRepository(apiService)

    @Provides
    @Singleton
    fun provideCursoRepository(apiService: ApiService): CursoRepository =
        CursoRepository(apiService)

    @Provides
    @Singleton
    fun provideProfesorRepository(apiService: ApiService): ProfesorRepository =
        ProfesorRepository(apiService)

    @Provides
    @Singleton
    fun provideAlumnoRepository(apiService: ApiService): AlumnoRepository =
        AlumnoRepository(apiService)

    @Provides
    @Singleton
    fun provideCicloRepository(apiService: ApiService): CicloRepository =
        CicloRepository(apiService)

    @Provides
    @Singleton
    fun provideGrupoRepository(apiService: ApiService): GrupoRepository =
        GrupoRepository(apiService)

    @Provides
    @Singleton
    fun provideUsuarioRepository(apiService: ApiService): UsuarioRepository =
        UsuarioRepository(apiService)

    @Provides
    @Singleton
    fun provideCarreraCursoRepository(apiService: ApiService): CarreraCursoRepository =
        CarreraCursoRepository(apiService)

    @Provides
    @Singleton
    fun provideMatriculaRepository(apiService: ApiService): MatriculaRepository =
        MatriculaRepository(apiService)
}
