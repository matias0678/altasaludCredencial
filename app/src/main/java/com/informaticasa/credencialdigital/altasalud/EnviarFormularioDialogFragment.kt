import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.informaticasa.credencialdigital.R
import org.json.JSONObject

class EnviarFormularioDialogFragment : DialogFragment() {

    private lateinit var nombreAfiliado: String
    private lateinit var tipoDocumento: String
    private lateinit var numeroDocumento: String

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = activity?.layoutInflater
        val dialogView = inflater?.inflate(R.layout.enviar_formulario, null)

        val builder = AlertDialog.Builder(activity)
        builder.setView(dialogView)
            .setTitle("Complete el formulario")
            .setNegativeButton("Cancelar") { dialog, which ->
                // Aquí se maneja la lógica para cancelar el formulario
                dialog.dismiss()
            }

        val dialog = builder.create()

        // Obtener referencia al botón de compartir credencial en la vista del formulario
        val btnCompartirCredencial = dialogView?.findViewById<Button>(R.id.buttonShareCredential)
        // Asignar un OnClickListener al botón de compartir credencial
        btnCompartirCredencial?.setOnClickListener {
            // Obtener los datos ingresados en el formulario
            val editTextNombre = dialogView.findViewById<EditText>(R.id.editTextName)
            val editTextNumeroDocumento = dialogView.findViewById<EditText>(R.id.editTextDocumentNumber)

            nombreAfiliado = getNombreAfiliado() // Obtener el nombre del afiliado
            tipoDocumento = "DNI" // Reemplazar esto con la obtención del tipo de documento desde el formulario
            numeroDocumento = editTextNumeroDocumento.text.toString()

            // Validar el nombre
            if (!validarNombre(nombreAfiliado)) {
                editTextNombre.error = "El nombre debe contener solo letras y tener al menos 7 caracteres"
                return@setOnClickListener
            }

            // Validar el número de documento
            if (!validarNumeroDocumento(numeroDocumento)) {
                editTextNumeroDocumento.error = "El número de documento debe contener solo números"
                return@setOnClickListener
            }

            // Generar el mensaje con el enlace y los datos del formulario
            val mensaje = generarMensaje()
            // Crear un Intent para compartir el mensaje
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, mensaje)
            // Mostrar las aplicaciones disponibles para compartir el mensaje
            startActivity(Intent.createChooser(intent, "Compartir con"))
        }

        return dialog
    }

    private fun generarMensaje(): String {
        // Obtener los datos de la credencial
        val sharedPreferences = activity?.getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE)
        val nombreAfiliado = sharedPreferences?.getString("nombre", "") ?: ""
        val tipoDocumento = sharedPreferences?.getString("tipdoc", "") ?: ""
        val numeroDocumento = sharedPreferences?.getString("numdoc", "") ?: ""
        val baja = sharedPreferences?.getString("baja", "") ?: ""
        val descrip = sharedPreferences?.getString("descrip", "") ?: ""
        val numero = sharedPreferences?.getString("numero", "") ?: ""
        val orden = sharedPreferences?.getString("orden", "") ?: ""
        val ingre = sharedPreferences?.getString("ingre", "") ?: ""
        val nacim = sharedPreferences?.getString("nacim", "") ?: ""

        // Crear un objeto JSON con los datos de la credencial
        val credencialJson = JSONObject().apply {
            put("nombre", nombreAfiliado)
            put("tipoDocumento", tipoDocumento)
            put("numeroDocumento", numeroDocumento)
            put("baja", baja)
            put("descrip", descrip)
            put("numero", numero)
            put("orden", orden)
            put("ingre", ingre)
            put("nacim", nacim)
        }

        // Encriptar los datos utilizando Base64
        val datosEncriptados = Base64.encodeToString(credencialJson.toString().toByteArray(Charsets.UTF_8), Base64.DEFAULT)
        // Generar la URL con los datos encriptados como parámetro de consulta
        val enlace = "http://santasalud.dyndns.org:8888/formulario?data=$datosEncriptados"

        // Mensaje a compartir
        return "¡Hola!, $nombreAfiliado quiere compartir su credencial digital de ALTA SALUD.\n" +
                "Ingresá al siguiente link $enlace\n" +
                "La misma estará disponible por 12 horas, vencido el tiempo, deberás solicitarle al titular que vuelva a compartirla con vos nuevamente."
    }


    // Función para obtener el nombre del afiliado
    private fun getNombreAfiliado(): String {
        val sharedPreferences = activity?.getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE)
        return sharedPreferences?.getString("nombre", "") ?: ""
    }

    // Función para encriptar los datos del afiliado
    private fun encriptarDatosAfiliado(nombre: String, tipoDocumento: String, numeroDocumento: String): String {
        // Convertir los datos a un formato JSON
        val datosJson = "{\"nombre\":\"$nombre\",\"tipoDocumento\":\"$tipoDocumento\",\"numeroDocumento\":\"$numeroDocumento\"}"
        // Encriptar los datos utilizando Base64
        return Base64.encodeToString(datosJson.toByteArray(Charsets.UTF_8), Base64.DEFAULT)
    }

    private fun validarNombre(nombre: String): Boolean {
        val regex = "^[A-Za-z ]{7,30}$".toRegex()
        return regex.matches(nombre)
    }

    // Función para validar el número de documento
    private fun validarNumeroDocumento(numeroDocumento: String): Boolean {
        return numeroDocumento.matches("\\d+".toRegex())
    }
}
