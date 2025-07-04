package com.example.applistatareas

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var adapter: TareaAdapter
    private val listaTareas = mutableListOf<Tarea>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cargarTareas()
        configurarListView()
        configurarBotonAgregar()
    }

    private fun configurarListView() {
        listView = findViewById(R.id.listViewTareas)
        adapter = TareaAdapter(listaTareas, this)
        listView.adapter = adapter
    }

    private fun configurarBotonAgregar() {
        val fabAgregar: FloatingActionButton = findViewById(R.id.fabAgregarTarea)
        fabAgregar.setOnClickListener {
            mostrarDialogoNuevaTarea()
        }
    }

    private fun mostrarDialogoNuevaTarea() {
        val editText = EditText(this)
        
        AlertDialog.Builder(this)
            .setTitle("Nueva tarea")
            .setView(editText)
            .setPositiveButton("Agregar") { _, _ ->
                val texto = editText.text.toString().trim()
                if (texto.isNotEmpty()) {
                    agregarTarea(texto)
                } else {
                    mostrarMensaje("El nombre no puede estar vacío")
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun agregarTarea(nombre: String) {
        listaTareas.add(Tarea(nombre))
        guardarTareas()
        adapter.notifyDataSetChanged()
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    fun guardarTareas() {
        val preferencias = getSharedPreferences("tareas", Context.MODE_PRIVATE)
        val listaComoTexto = listaTareas.joinToString("\n") { tarea ->
            val estado = if (tarea.completada) "[X]" else "[ ]"
            "$estado ${tarea.nombre}"
        }
        preferencias.edit().putString("lista", listaComoTexto).apply()
    }

    fun cargarTareas() {
        val preferencias = getSharedPreferences("tareas", Context.MODE_PRIVATE)
        val listaComoTexto = preferencias.getString("lista", null)
        
        listaTareas.clear()
        
        if (listaComoTexto?.isNotEmpty() == true) {
            listaComoTexto.split("\n").forEach { linea ->
                val completada = linea.startsWith("[X]")
                val nombre = linea.removePrefix("[X]").removePrefix("[ ]").trim()
                if (nombre.isNotEmpty()) {
                    listaTareas.add(Tarea(nombre, completada))
                }
            }
        }
    }
}