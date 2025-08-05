package com.example.seguridad_priv_a.security

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

class BiometricHelper(
    private val context: Context,
    private val onAuthenticated: () -> Unit,
    private val onFailed: () -> Unit
) {

    private val executor: Executor = ContextCompat.getMainExecutor(context)

    fun authenticate() {
        val biometricManager = BiometricManager.from(context)
        val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)

        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            showBiometricPrompt()
        } else {
            showPinFallbackDialog()
        }
    }

    private fun showBiometricPrompt() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(decodeBase64("QXV0ZW50aWNhY2nDs24gcmVxdWVyaWRh")) // "Autenticación requerida"
            .setSubtitle(decodeBase64("Q29uZmlybWEgdHUgaWRlbnRpZGFkIHBhcmEgY29udGludWFy")) // "Confirma tu identidad para continuar"
            .setNegativeButtonText(decodeBase64("VXNhciBQSU4=")) // "Usar PIN"
            .build()

        val biometricPrompt = BiometricPrompt(
            context as FragmentActivity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onAuthenticated()
                }

                override fun onAuthenticationFailed() {
                    onFailed()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    showPinFallbackDialog()
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }

    private fun showPinFallbackDialog() {
        val input = android.widget.EditText(context).apply {
            hint = decodeBase64("UElOIGRlIHJlc3BhbGRv") // "PIN de respaldo"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }

        AlertDialog.Builder(context)
            .setTitle(decodeBase64("UElOIGRlIHNlZ3VyaWRhZA==")) // "PIN de seguridad"
            .setMessage(decodeBase64("SW50cm9kdWNlIGVsIFBJTjogZGVmZWN0bzogMTIzNA==")) // "Introduce el PIN: defecto: 1234"
            .setView(input)
            .setPositiveButton(decodeBase64("QWNlcHRhcg==")) { _, _ -> // "Aceptar"
                val enteredPin = input.text.toString()
                if (enteredPin == decodeBase64("MTIzNA==")) { // "1234"
                    onAuthenticated()
                } else {
                    onFailed()
                }
            }
            .setNegativeButton(decodeBase64("Q2FuY2VsYXI=")) { _, _ -> onFailed() } // "Cancelar"
            .setCancelable(false)
            .show()
    }

    private fun decodeBase64(encoded: String): String {
        return String(android.util.Base64.decode(encoded, android.util.Base64.DEFAULT))
    }
}
