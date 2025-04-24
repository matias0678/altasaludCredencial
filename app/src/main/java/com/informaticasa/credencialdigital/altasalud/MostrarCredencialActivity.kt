package com.informaticasa.credencialdigital.altasalud

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.informaticasa.credencialdigital.R
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar


class MostrarCredencialActivity : AppCompatActivity() {

    private lateinit var textViewFechaActual: TextView
    //private lateinit var scrollView: ScrollView
    private lateinit var btnShare: Button

    // Función para obtener la fecha actual en formato dd-MM-yyyy
    private fun obtenerFechaActual(): String {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1 // Sumar 1 porque los meses empiezan desde 0
        val year = calendar.get(Calendar.YEAR)

        // Formatear la fecha con dos dígitos en el mes
        return String.format("%02d/%02d/%d", day, month, year)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> {
                // volver atrás
                borrarDatosSesion()

                // Iniciar la actividad de inicio de sesión
                val abrirLoginPantalla = Intent(this, LoginActivity::class.java)
                startActivity(abrirLoginPantalla)

                finish()
                return true
            }

            R.id.action_exit -> {
                // Código para salir de la aplicación
                finishAffinity() // Cierra todas las actividades de la aplicación
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun borrarDatosSesion() {
        // Limpiar Shared Preferences
        val preferences = getSharedPreferences("mis_prefs", Context.MODE_PRIVATE)
        preferences.edit().clear().apply()
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppThemeMostrar)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mostrar_credencial)

        supportActionBar?.setTitle("")

        val numdoc = intent.getStringExtra("numdoc")
        val nombre = intent.getStringExtra("nombre")
        val tipdoc = intent.getStringExtra("tipdoc")
        val baja = intent.getStringExtra("baja")
        val numero = intent.getStringExtra("numero")
        val orden = intent.getStringExtra("orden")
        val descrip = intent.getStringExtra("descrip")

        // Obtiene shared preferences
        val prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE)
        // Lee fecha ingreso
        val ingre = prefs.getString("ingre", null)

        // Obtener referencias a las vistas mediante findViewById
        val textViewTipoDoc: TextView = findViewById(R.id.textViewTipoDoc)
        val textViewNombre: TextView = findViewById(R.id.textViewNombre)
        val textViewEstado: TextView = findViewById(R.id.textViewEstado)
        val textViewNumero: TextView = findViewById(R.id.textViewNumero)
        val textViewIngre: TextView = findViewById(R.id.textViewIngre)
        val textViewPlan: TextView = findViewById(R.id.textViewPlan)

        // Inicializar textViewFechaActual
        textViewFechaActual = findViewById(R.id.textViewFechaActual)

        // Verificar la referencia del textViewFechaActual antes de asignar el valor
        if (::textViewFechaActual.isInitialized) {
            textViewFechaActual.text = "Solicitada:\n${obtenerFechaActual()}"
        }

        // Mostrar el valor de "baja" en un Toast
        if (baja == null || baja.equals("null", ignoreCase = true)) {
            // Activa
            textViewEstado.text = "Condición:\nActivo"
        } else if (baja.isEmpty()) {
            // Activa
            textViewEstado.text = "Condición:\nActivo"
        } else {
            // Inactiva
            textViewEstado.text = "Condición:\nInactivo"
        }

        // Formatear el número de orden para que tenga dos dígitos
        val numeroOrden = "$numero/${orden?.padStart(2, '0')}"

        //Primero separar la fecha de la hora. Podemos usar split:
        val partesIngre = ingre?.split(" ")

        val fechaIngre = partesIngre?.get(0) // "2010-05-01"

        //Luego dividir la fecha en día, mes y año con otro split:
        val datosFechaIngre = fechaIngre?.split("-")

        val diaIngre = datosFechaIngre?.get(2)
        val mesIngre = datosFechaIngre?.get(1)
        val anioIngre = datosFechaIngre?.get(0)

        //Por último, juntar en el formato deseado:
        val fechaFormateadaIngre = "$diaIngre/$mesIngre/$anioIngre"

        // Mostrar los datos en las vistas
        textViewTipoDoc.text = "$tipdoc: \n$numdoc"
        textViewNombre.text = "$nombre"
        textViewNumero.text = "Afiliado:\n$numeroOrden"
        textViewPlan.text = "Plan:\n$descrip"
        textViewIngre.text = "Ingreso:\n$fechaFormateadaIngre"

        // Obtener referencia al ScrollView
        //scrollView = findViewById(R.id.scrollMostrarCredencialVertical)

        // Después de inflar el diseño en onCreate
        btnShare = findViewById(R.id.btnShare)

        // Establecer el OnClickListener para el botón
        btnShare.setOnClickListener {
            // Obtener la vista que se compartirá (en este caso, contentLayout)
            val viewToShare = findViewById<View>(android.R.id.content)

            Log.d("Dimensions", "Width: ${viewToShare.width}, Height: ${viewToShare.height}")

            // Crear la imagen desde la vista
            val imagen = crearImagenDesdeView(viewToShare, btnShare)

            // Mostrar botón nuevamente antes de compartir
            btnShare.visibility = View.VISIBLE

            // Compartir la imagen
            compartirImagen(imagen)
        }

    }

    fun crearImagenDesdeView(view: View, btnShare: Button): Bitmap {
        // Ocultar el botón antes de capturar la vista como una imagen
        btnShare.visibility = View.GONE

        // Crear una representación de la vista sin incluir el botón de compartir
        val withoutShareButtonBitmap = Bitmap.createBitmap(
            view.width,
            view.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(withoutShareButtonBitmap)
        view.draw(canvas)

        // Restaurar la visibilidad del botón después de capturar la vista
        btnShare.visibility = View.VISIBLE

        return withoutShareButtonBitmap
    }

    private fun compartirImagen(imagen: Bitmap) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/jpeg"

        // Guardar el bitmap en la memoria externa para poder compartirlo
        val cachePath = externalCacheDir?.absolutePath + "/imagen_credencial.jpg"
        val stream = FileOutputStream(cachePath)
        imagen.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.close()

        val uri = FileProvider.getUriForFile(this, packageName + ".fileprovider", File(cachePath))

        intent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(intent, "Compartir credencial"))
    }
}
