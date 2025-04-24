package com.informaticasa.credencialdigital.altasalud

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.informaticasa.credencialdigital.R
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Properties

class LoginActivity : AppCompatActivity() {

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        setTheme(R.style.AppThemeLogin)
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> {
                // Cerrar sesión
                borrarDatosSesion()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppThemeLogin)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //supportActionBar?.setTitle("")

        supportActionBar?.let { actionBar ->
            actionBar.setTitle("CREDENCIAL DIGITAL")

        }

        val spinnerTipDoc = findViewById<Spinner>(R.id.spinnerTipoDocumento)
        val tipDocLista = arrayOf("DNI", "CI",  "LC", "LE", "PAS")
        val adaptadorListaTipDoc = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            tipDocLista
        )
        spinnerTipDoc.adapter = adaptadorListaTipDoc

        spinnerTipDoc.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                (view as TextView).setTextColor(Color.BLACK)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val editTextNumDoc = findViewById<EditText>(R.id.textViewNumDocumento)
        val editTextClave = findViewById<EditText>(R.id.textViewClave)

        val btnLogin = findViewById<AppCompatButton>(R.id.btnLogin)
        btnLogin.setOnClickListener {
            val tipDocumento = spinnerTipDoc.selectedItem.toString()

            val numDocumento = editTextNumDoc.text.toString()
            //verificar que se hayan ingresado números solamente
            if (!numDocumento.matches("\\d+".toRegex())) {
                mostrarAlerta("Sólo se permiten números en el documento")
                return@setOnClickListener
            }

            val clave = editTextClave.text.toString()
            //verificar que se hayan ingresado números solamente
            if (!clave.matches("\\d+".toRegex())) {
                mostrarAlerta("Sólo se permiten números en la clave")
                return@setOnClickListener
            }

            // Llamamos a la tarea asíncrona para autenticar al usuario
            AutenticarUsuarioTask().execute(tipDocumento, numDocumento, clave)
        }
    }

    private fun mostrarMensaje(mensaje: String, duracion: Int) {
        Toast.makeText(this@LoginActivity, mensaje, duracion).show()
    }

    private fun mostrarAlerta(mensaje: String) {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Datos inválidos")
        builder.setMessage(mensaje)

        builder.setPositiveButton("Aceptar") { dialog, which ->

        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private inner class AutenticarUsuarioTask : AsyncTask<String, Void, JSONObject>() {

        override fun doInBackground(vararg params: String): JSONObject? {
            Log.d("AutenticarUsuarioTask", "Inicio doInBackground")
            //val url = URL("http://192.168.1.115/apicredencialgral/login.php") <-infoprueba
            //val url = URL("http://santasalud.dyndns.org:8888/apicredencialgral/login.php")<-server cliente

            //acceder a la URL de archivo externo app_config.prop
            val inputStream = resources.openRawResource(R.raw.app_config)
            val properties = Properties()
            properties.load(inputStream)
            val serverURL = properties.getProperty("server_url")
            val url = URL(serverURL)


            try {
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.doOutput = true

                val postData = "tipdoc=${params[0]}&numdoc=${params[1]}&clave=${params[2]}"
                val postDataBytes = postData.toByteArray(Charsets.UTF_8)

                val outputStream: OutputStream = connection.outputStream
                outputStream.write(postDataBytes)
                outputStream.flush()
                outputStream.close()

                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?

                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    return JSONObject(response.toString())
                } else {
                    mostrarMensaje(
                        "Error en la conexión. Código de respuesta: $responseCode",
                        Toast.LENGTH_SHORT
                    )
                }
            } catch (e: Exception) {
                Log.e("AutenticarUsuarioTask", "Error en doInBackground: ${e.message}")
                e.printStackTrace()
                mostrarDialogo("Error de conexión", "Inténtalo de nuevo más tarde.")
            }

            Log.d("AutenticarUsuarioTask", "Fin doInBackground con error")
            return null
        }

        override fun onPreExecute() {
            // Antes de ejecutar la tarea, muestra la ProgressBar
            findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
        }

        override fun onPostExecute(result: JSONObject?) {
            Log.d("AutenticarUsuarioTask", "Inicio onPostExecute")
            // Después de ejecutar la tarea, oculta la ProgressBar
            findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
            if (result != null) {
                try {
                    val success = result.getBoolean("success")

                    if (success) {
                        //mostrarMensaje("Conexión exitosa", Toast.LENGTH_SHORT)

                        val sharedPreferences =
                            getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()

                        val data = result.getJSONObject("data")

                        val numdoc = data.getString("numdoc")
                        editor.putString("numdoc", numdoc)
                        val nombre = data.getString("nombre")
                        editor.putString("nombre", nombre)
                        val tipdoc = data.getString("tipdoc")
                        editor.putString("tipdoc", tipdoc)
                        val baja = data.optString("baja", null)
                        val descrip = data.optString("descrip", null)

                        val numero = data.getString("numero")
                        val orden = data.getString("orden")
                        val ingre = data.getString("ingre")
                        val nacim = data.getString("nacim")

                        editor.putString("numero", numero)
                        editor.putString("orden", orden)
                        editor.putString("ingre", ingre)
                        editor.putString("nacim", nacim)

                        //MENSAJE BAJA
                        //mostrarMensaje("VALOR BAJA LOGINACTIVITY = $baja", Toast.LENGTH_SHORT)
                        editor.putString("baja", baja)

                        editor.apply()

                        val intent =
                            Intent(this@LoginActivity, MostrarCredencialActivity::class.java)
                        intent.putExtra("numdoc", numdoc)
                        intent.putExtra("nombre", nombre)
                        intent.putExtra("tipdoc", tipdoc)
                        intent.putExtra("baja", baja)
                        intent.putExtra("numero", numero)
                        intent.putExtra("orden", orden)
                        intent.putExtra("descrip", descrip)
                        startActivity(intent)
                    } else {
                        val errorMessage = result.getString("error")
                        mostrarDialogo("Error", errorMessage)
                    }
                } catch (e: Exception) {
                    Log.e("AutenticarUsuarioTask", "Error en onPostExecute: ${e.message}")
                    e.printStackTrace()
                    mostrarDialogo("Error", "Error al procesar la respuesta del servidor")
                }
            } else {
                Log.e("AutenticarUsuarioTask", "Resultado nulo en onPostExecute")
                mostrarDialogo("Error de conexión", "Inténtalo de nuevo más tarde.")
            }

            Log.d("AutenticarUsuarioTask", "Fin onPostExecute")
        }

        private fun mostrarDialogo(titulo: String, mensaje: String) {
            try {
                val builder = AlertDialog.Builder(this@LoginActivity)
                builder.setTitle(titulo)
                builder.setMessage(mensaje)
                builder.setPositiveButton("OK") { dialog, which -> dialog.dismiss() }

                val dialog: AlertDialog = builder.create()
                dialog.show()
            } catch (e: Exception) {
                Log.e("AutenticarUsuarioTask", "Error en mostrarDialogo: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
