package com.skillet.multistoreapp.presentation.customer.cart

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

fun openWhatsApp(
    context: Context,
    phoneNumber: String,
    message: String
) {
    val clientPhoneNumber = phoneNumber.filter { character ->
        character.isDigit()
    }

    if (clientPhoneNumber.isBlank()) {
        Toast.makeText(
            context,
            "El número de teléfono no es válido",
            Toast.LENGTH_SHORT
        ).show()
        return
    }
    val phoneWithCountryCode =
        if(clientPhoneNumber.startsWith("52")){
            clientPhoneNumber
        }else{
            "52$clientPhoneNumber"
        }

    val encodeMessage =
        Uri.encode(message)
    // El esquema directo whatsapp:// suele ser más confiable para pasar el parámetro 'text'
    // en algunas versiones de la app, especialmente en cuentas vinculadas.
    val whatsappUrl = "whatsapp://send?phone=$phoneWithCountryCode&text=$encodeMessage"

    val whatsAppIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse(whatsappUrl)
    )

    try {
        // Primero intentamos con el esquema directo que es el que mejor suele inyectar el texto
        context.startActivity(whatsAppIntent)
    } catch (error: ActivityNotFoundException) {
        // Si falla el esquema directo, probamos con el enlace universal wa.me
        val universalUrl = "https://wa.me/$phoneWithCountryCode?text=$encodeMessage"
        val universalIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(universalUrl)
        )
        try {
            context.startActivity(universalIntent)
        } catch (error: ActivityNotFoundException) {
            // Si falla, intentar forzar WhatsApp Business por si acaso
            val whatsAppBussinnesIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(whatsappUrl)
            ).apply {
                setPackage("com.whatsapp.w4b")
            }
            try {
                context.startActivity(whatsAppBussinnesIntent)
            } catch (error: ActivityNotFoundException) {
                Toast.makeText(
                    context,
                    "WhatsApp no está instalado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}