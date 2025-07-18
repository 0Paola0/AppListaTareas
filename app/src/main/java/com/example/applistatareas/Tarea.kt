package com.example.applistatareas

import java.text.SimpleDateFormat
import java.util.*

data class Tarea(
    var nombre: String,
    var completada: Boolean = false,
    var categoria: String = "General",
    var fechaVencimiento: Long? = null,
    var prioridad: Prioridad = Prioridad.MEDIA,
    var fechaCreacion: Long = System.currentTimeMillis()
) {
    enum class Prioridad(val valor: String, val color: Int) {
        ALTA("Alta", 0xFFE57373.toInt()),
        MEDIA("Media", 0xFFFFB74D.toInt()),
        BAJA("Baja", 0xFF81C784.toInt())
    }

    fun getFechaVencimientoFormateada(): String? {
        return fechaVencimiento?.let {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
        }
    }

    fun estaVencida(): Boolean {
        return fechaVencimiento?.let { it < System.currentTimeMillis() } ?: false
    }

    fun diasParaVencer(): Int? {
        return fechaVencimiento?.let {
            val diff = it - System.currentTimeMillis()
            (diff / (1000 * 60 * 60 * 24)).toInt()
        }
    }
}
