package com.example.gestionacademicaapp.data.api.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "usuarios")
@Parcelize
data class Usuario(
    @PrimaryKey val idUsuario: Long,
    @ColumnInfo(name = "cedula") var cedula: String,
    @ColumnInfo(name = "clave") val clave: String?,
    @ColumnInfo(name = "tipo") var tipo: String
) : Parcelable
