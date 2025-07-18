package com.example.applistatareas

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var adapter: TareaAdapter
    private val listaTareas = mutableListOf<Tarea>()
    private var filtroCategoria = "Todas"
    private var ordenamiento = "Fecha creación"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cargarTareas()
        configurarListView()
        configurarBotonAgregar()
        actualizarContadores()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> {
                mostrarDialogoFiltros()
                true
            }
            R.id.action_sort -> {
                mostrarDialogoOrdenamiento()
                true
            }
            R.id.action_stats -> {
                mostrarEstadisticas()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun configurarListView() {
        listView = findViewById(R.id.listViewTareas)
        adapter = TareaAdapter(obtenerTareasFiltradas(), this)
        listView.adapter = adapter
    }

    private fun configurarBotonAgregar() {
        val fabAgregar: FloatingActionButton = findViewById(R.id.fabAgregarTarea)
        fabAgregar.setOnClickListener {
            mostrarDialogoNuevaTarea()
        }
    }

    private fun mostrarDialogoNuevaTarea() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_nueva_tarea, null)

        val editNombre = dialogView.findViewById<EditText>(R.id.editNombreTarea)
        val spinnerCategoria = dialogView.findViewById<Spinner>(R.id.spinnerCategoria)
        val spinnerPrioridad = dialogView.findViewById<Spinner>(R.id.spinnerPrioridad)
        val btnFecha = dialogView.findViewById<Button>(R.id.btnSeleccionarFecha)
        val btnEliminarFecha = dialogView.findViewById<Button>(R.id.btnEliminarFecha)

        // Configurar spinner de categorías
        val categorias = arrayOf("General", "Trabajo", "Personal", "Estudios", "Hogar", "Salud")
        val adapterCategoria = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = adapterCategoria

        // Configurar spinner de prioridades
        val prioridades = Tarea.Prioridad.values().map { it.valor }.toTypedArray()
        val adapterPrioridad = ArrayAdapter(this, android.R.layout.simple_spinner_item, prioridades)
        adapterPrioridad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPrioridad.adapter = adapterPrioridad
        spinnerPrioridad.setSelection(1) // Media por defecto

        // Configurar fecha
        var fechaSeleccionada: Long? = null

        btnFecha.setOnClickListener {
            val calendar = Calendar.getInstance()
            fechaSeleccionada?.let { calendar.timeInMillis = it }

            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val nuevaFecha = Calendar.getInstance()
                    nuevaFecha.set(year, month, dayOfMonth)
                    fechaSeleccionada = nuevaFecha.timeInMillis
                    actualizarBotonFecha(btnFecha, fechaSeleccionada)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnEliminarFecha.setOnClickListener {
            fechaSeleccionada = null
            actualizarBotonFecha(btnFecha, null)
        }

        AlertDialog.Builder(this)
            .setTitle("Nueva tarea")
            .setView(dialogView)
            .setPositiveButton("Agregar") { _, _ ->
                val nombre = editNombre.text.toString().trim()
                if (nombre.isNotEmpty()) {
                    val nuevaTarea = Tarea(
                        nombre = nombre,
                        categoria = categorias[spinnerCategoria.selectedItemPosition],
                        prioridad = Tarea.Prioridad.values()[spinnerPrioridad.selectedItemPosition],
                        fechaVencimiento = fechaSeleccionada
                    )
                    agregarTarea(nuevaTarea)
                } else {
                    mostrarMensaje("El nombre no puede estar vacío")
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun actualizarBotonFecha(boton: Button, fecha: Long?) {
        if (fecha != null) {
            val fechaFormateada = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(fecha))
            boton.text = fechaFormateada
        } else {
            boton.text = "Seleccionar fecha (opcional)"
        }
    }

    private fun agregarTarea(tarea: Tarea) {
        listaTareas.add(tarea)
        guardarTareas()
        actualizarLista()
        actualizarContadores()
    }

    private fun mostrarDialogoFiltros() {
        val categorias = arrayOf("Todas", "General", "Trabajo", "Personal", "Estudios", "Hogar", "Salud")
        val posicionActual = categorias.indexOf(filtroCategoria)

        AlertDialog.Builder(this)
            .setTitle("Filtrar por categoría")
            .setSingleChoiceItems(categorias, posicionActual) { dialog, which ->
                filtroCategoria = categorias[which]
                actualizarLista()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoOrdenamiento() {
        val opciones = arrayOf("Fecha creación", "Nombre", "Prioridad", "Fecha vencimiento")
        val posicionActual = opciones.indexOf(ordenamiento)

        AlertDialog.Builder(this)
            .setTitle("Ordenar por")
            .setSingleChoiceItems(opciones, posicionActual) { dialog, which ->
                ordenamiento = opciones[which]
                actualizarLista()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarEstadisticas() {
        val total = listaTareas.size
        val completadas = listaTareas.count { it.completada }
        val pendientes = total - completadas
        val vencidas = listaTareas.count { it.estaVencida() && !it.completada }

        val mensaje = """
            📊 Estadísticas de tareas:
            
            Total: $total
            Completadas: $completadas
            Pendientes: $pendientes
            Vencidas: $vencidas
            
            Progreso: ${if (total > 0) (completadas * 100 / total) else 0}%
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Estadísticas")
            .setMessage(mensaje)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun obtenerTareasFiltradas(): MutableList<Tarea> {
        var tareasFiltradas = if (filtroCategoria == "Todas") {
            listaTareas.toMutableList()
        } else {
            listaTareas.filter { it.categoria == filtroCategoria }.toMutableList()
        }

        // Aplicar ordenamiento
        tareasFiltradas = when (ordenamiento) {
            "Nombre" -> tareasFiltradas.sortedBy { it.nombre }.toMutableList()
            "Prioridad" -> tareasFiltradas.sortedBy { it.prioridad.ordinal }.toMutableList()
            "Fecha vencimiento" -> tareasFiltradas.sortedBy { it.fechaVencimiento ?: Long.MAX_VALUE }.toMutableList()
            else -> tareasFiltradas.sortedByDescending { it.fechaCreacion }.toMutableList()
        }

        return tareasFiltradas
    }

    private fun actualizarLista() {
        adapter = TareaAdapter(obtenerTareasFiltradas(), this)
        listView.adapter = adapter
    }

    private fun actualizarContadores() {
        val headerTareas = findViewById<TextView>(R.id.headerTareas)
        val total = listaTareas.size
        val completadas = listaTareas.count { it.completada }
        headerTareas.text = "Mis Tareas ($completadas/$total)"
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    fun guardarTareas() {
        val preferencias = getSharedPreferences("tareas", Context.MODE_PRIVATE)
        val editor = preferencias.edit()

        // Guardar cada tarea como JSON simple
        val tareasJson = listaTareas.mapIndexed { index, tarea ->
            "${tarea.nombre}|${tarea.completada}|${tarea.categoria}|${tarea.fechaVencimiento ?: ""}|${tarea.prioridad.ordinal}|${tarea.fechaCreacion}"
        }.joinToString("\n")

        editor.putString("lista", tareasJson)
        editor.apply()

        actualizarContadores()
    }

    private fun cargarTareas() {
        val preferencias = getSharedPreferences("tareas", Context.MODE_PRIVATE)
        val tareasJson = preferencias.getString("lista", null)

        listaTareas.clear()

        if (!tareasJson.isNullOrEmpty()) {
            tareasJson.split("\n").forEach { linea ->
                if (linea.isNotEmpty()) {
                    try {
                        val partes = linea.split("|")
                        if (partes.size >= 6) {
                            val tarea = Tarea(
                                nombre = partes[0],
                                completada = partes[1].toBoolean(),
                                categoria = partes[2],
                                fechaVencimiento = if (partes[3].isEmpty()) null else partes[3].toLong(),
                                prioridad = Tarea.Prioridad.values()[partes[4].toInt()],
                                fechaCreacion = partes[5].toLong()
                            )
                            listaTareas.add(tarea)
                        }
                    } catch (e: Exception) {
                        // Ignorar tareas con formato incorrecto
                    }
                }
            }
        }
    }
}
