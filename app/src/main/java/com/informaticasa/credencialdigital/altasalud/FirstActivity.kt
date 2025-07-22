package com.informaticasa.credencialdigital.altasalud

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView // Needed for your custom image header
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar // Import Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.informaticasa.credencialdigital.R
import androidx.activity.enableEdgeToEdge

class FirstActivity : AppCompatActivity() {

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Enable edge-to-edge BEFORE super.onCreate and setContentView
        // This allows content to draw behind system bars.
        enableEdgeToEdge() // Aquí es donde la usas

        setTheme(R.style.AppTheme) // Your theme here. Ensure AppTheme has NoActionBar.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)

        // 2. Configure the Toolbar to act as the Activity's ActionBar
        val toolbar = findViewById<Toolbar>(R.id.main_toolbar)
        setSupportActionBar(toolbar)

        // 3. Apply top padding to the Toolbar to push it below the system status bar
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                systemBarsInsets.top + view.paddingTop, // Add status bar height to top padding
                view.paddingRight,
                view.paddingBottom
            )
            insets // Return insets to indicate they have been consumed
        }

        // IMPORTANT: Since you explicitly asked to remove the ScrollView,
        // we no longer apply bottom insets to it. If your content might
        // exceed screen height, you *will* need a ScrollView.
        // If content is cut off at the bottom due to navigation bar,
        // you might need to apply bottom padding to mainCardView or root ConstraintLayout.
        // For now, assuming content fits.

        // Your existing button listeners
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

    // These methods will now correctly inflate and handle your menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> {
                borrarDatosSesion()
                finish()
                return true
            }
            R.id.action_exit -> {
                finishAffinity()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun borrarDatosSesion() {
        val preferences = getSharedPreferences("mis_prefs", Context.MODE_PRIVATE)
        preferences.edit().clear().apply()
    }
}