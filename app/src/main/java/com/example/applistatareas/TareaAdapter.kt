package com.example.applistatareas

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import java.util.*

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
        val txtCategoria = view.findViewById<TextView>(R.id.txtCategoria)
        val txtFechaVencimiento = view.findViewById<TextView>(R.id.txtFechaVencimiento)
        val viewPrioridad = view.findViewById<View>(R.id.viewPrioridad)
        val btnEditar = view.findViewById<ImageButton>(R.id.btnEditar)
        val btnEliminar = view.findViewById<ImageButton>(R.id.btnEliminar)

        // Configurar número de tarea
        txtNumeroTarea.text = (position + 1).toString()

        // Configurar nombre de tarea
        txtNombreTarea.text = tarea.nombre

        // Configurar estado visual de completada
        if (tarea.completada) {
            txtNombreTarea.paintFlags = txtNombreTarea.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            txtNombreTarea.alpha = 0.6f
            imgEstado.setImageResource(R.drawable.ic_check_box_24)
            imgEstado.setColorFilter(ContextCompat.getColor(view.context, android.R.color.holo_green_dark))
        } else {
            txtNombreTarea.paintFlags = txtNombreTarea.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            txtNombreTarea.alpha = 1.0f
            imgEstado.setImageResource(R.drawable.ic_check_box_outline_blank_24)
            imgEstado.setColorFilter(ContextCompat.getColor(view.context, android.R.color.darker_gray))
        }

        // Configurar categoría
        txtCategoria.text = tarea.categoria
        txtCategoria.visibility = if (tarea.categoria != "General") View.VISIBLE else View.GONE

        // Configurar fecha de vencimiento
        tarea.getFechaVencimientoFormateada()?.let { fecha ->
            txtFechaVencimiento.text = fecha
            txtFechaVencimiento.visibility = View.VISIBLE

            // Cambiar color si está vencida
            if (tarea.estaVencida() && !tarea.completada) {
                txtFechaVencimiento.setTextColor(ContextCompat.getColor(view.context, android.R.color.holo_red_dark))
            } else {
                txtFechaVencimiento.setTextColor(ContextCompat.getColor(view.context, android.R.color.darker_gray))
            }
        } ?: run {
            txtFechaVencimiento.visibility = View.GONE
        }

        // Configurar indicador de prioridad
        viewPrioridad.setBackgroundColor(tarea.prioridad.color)

        // Configurar click en estado
        imgEstado.setOnClickListener {
            tarea.completada = !tarea.completada
            mainActivity.guardarTareas()
            notifyDataSetChanged()
        }

        // Configurar botón editar
        btnEditar.setOnClickListener {
            mostrarDialogoEditar(view, tarea, position)
        }

        // Configurar botón eliminar
        btnEliminar.setOnClickListener {
            mostrarDialogoEliminar(view, position)
        }

        return view
    }

    private fun mostrarDialogoEditar(view: View, tarea: Tarea, position: Int) {
        val dialogView = LayoutInflater.from(view.context).inflate(R.layout.dialog_editar_tarea, null)

        val editNombre = dialogView.findViewById<EditText>(R.id.editNombreTarea)
        val spinnerCategoria = dialogView.findViewById<Spinner>(R.id.spinnerCategoria)
        val spinnerPrioridad = dialogView.findViewById<Spinner>(R.id.spinnerPrioridad)
        val btnFecha = dialogView.findViewById<Button>(R.id.btnSeleccionarFecha)
        val btnEliminarFecha = dialogView.findViewById<Button>(R.id.btnEliminarFecha)

        // Configurar valores actuales
        editNombre.setText(tarea.nombre)

        // Configurar spinner de categorías
        val categorias = arrayOf("General", "Trabajo", "Personal", "Estudios", "Hogar", "Salud")
        val adapterCategoria = ArrayAdapter(view.context, android.R.layout.simple_spinner_item, categorias)
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = adapterCategoria
        spinnerCategoria.setSelection(categorias.indexOf(tarea.categoria))

        // Configurar spinner de prioridades
        val prioridades = Tarea.Prioridad.values().map { it.valor }.toTypedArray()
        val adapterPrioridad = ArrayAdapter(view.context, android.R.layout.simple_spinner_item, prioridades)
        adapterPrioridad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPrioridad.adapter = adapterPrioridad
        spinnerPrioridad.setSelection(tarea.prioridad.ordinal)

        // Configurar fecha
        var fechaSeleccionada = tarea.fechaVencimiento
        actualizarBotonFecha(btnFecha, fechaSeleccionada)

        btnFecha.setOnClickListener {
            val calendar = Calendar.getInstance()
            fechaSeleccionada?.let { calendar.timeInMillis = it }

            DatePickerDialog(
                view.context,
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

        AlertDialog.Builder(view.context)
            .setTitle("Editar tarea")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoNombre = editNombre.text.toString().trim()
                if (nuevoNombre.isNotEmpty()) {
                    tarea.nombre = nuevoNombre
                    tarea.categoria = categorias[spinnerCategoria.selectedItemPosition]
                    tarea.prioridad = Tarea.Prioridad.values()[spinnerPrioridad.selectedItemPosition]
                    tarea.fechaVencimiento = fechaSeleccionada
                    mainActivity.guardarTareas()
                    notifyDataSetChanged()
                } else {
                    Toast.makeText(view.context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
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
            boton.text = "Seleccionar fecha"
        }
    }

    private fun mostrarDialogoEliminar(view: View, position: Int) {
        AlertDialog.Builder(view.context)
            .setTitle("Eliminar tarea")
            .setMessage("¿Estás seguro de que quieres eliminar esta tarea?")
            .setPositiveButton("Eliminar") { _, _ ->
                tareas.removeAt(position)
                mainActivity.guardarTareas()
                notifyDataSetChanged()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
