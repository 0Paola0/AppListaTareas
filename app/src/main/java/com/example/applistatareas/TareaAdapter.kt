package com.example.applistatareas

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView

class TareaAdapter(
    private val tareas: MutableList<Tarea>,
    private val mainActivity: MainActivity
) : BaseAdapter() {

    override fun getCount(): Int = tareas.size
    override fun getItem(position: Int): Any = tareas[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context).inflate(R.layout.item_tarea, parent, false)
        val tarea = tareas[position]

        val txtNumeroTarea = view.findViewById<TextView>(R.id.txtNumeroTarea)
        val imgEstado = view.findViewById<ImageView>(R.id.imgEstado)
        val txtNombreTarea = view.findViewById<TextView>(R.id.txtNombreTarea)
        val btnEditar = view.findViewById<ImageButton>(R.id.btnEditar)
        val btnEliminar = view.findViewById<ImageButton>(R.id.btnEliminar)

        txtNumeroTarea.text = (position + 1).toString()
        txtNombreTarea.text = tarea.nombre

        if (tarea.completada) {
            imgEstado.setImageResource(R.drawable.ic_check_box_24)
            imgEstado.setColorFilter(0xFF4CAF50.toInt())
        } else {
            imgEstado.setImageResource(R.drawable.ic_check_box_outline_blank_24)
            imgEstado.setColorFilter(0xFFBDBDBD.toInt())
        }

        imgEstado.setOnClickListener {
            tarea.completada = !tarea.completada
            mainActivity.guardarTareas()
            notifyDataSetChanged()
        }

        btnEditar.setOnClickListener {
            mostrarDialogoEditar(view, tarea)
        }

        btnEliminar.setOnClickListener {
            tareas.removeAt(position)
            mainActivity.guardarTareas()
            notifyDataSetChanged()
        }

        return view
    }

    private fun mostrarDialogoEditar(view: View, tarea: Tarea) {
        val editText = EditText(view.context).apply {
            setText(tarea.nombre)
        }

        AlertDialog.Builder(view.context)
            .setTitle("Editar tarea")
            .setView(editText)
            .setPositiveButton("Guardar") { _, _ ->
                val texto = editText.text.toString().trim()
                if (texto.isNotEmpty()) {
                    tarea.nombre = texto
                    mainActivity.guardarTareas()
                    notifyDataSetChanged()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
} 