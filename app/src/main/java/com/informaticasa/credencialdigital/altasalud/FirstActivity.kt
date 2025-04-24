package com.informaticasa.credencialdigital.altasalud

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.informaticasa.credencialdigital.R

class FirstActivity : AppCompatActivity() {

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)

        val btnIngresar = findViewById<AppCompatButton>(R.id.btnIngresar)
        btnIngresar.setOnClickListener {
            val abrirLoginPantalla = Intent(this, LoginActivity::class.java)
            startActivity(abrirLoginPantalla)
        }

        val btnTurnosweb = findViewById<AppCompatButton>(R.id.btnTurnosweb)
        btnTurnosweb.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://turnos.elijasuturno.com.ar:444/altasalud"))
            startActivity(browserIntent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        setTheme(R.style.AppTheme)
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> {
                // volver atrás
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
}
