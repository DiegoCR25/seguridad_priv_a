package com.example.seguridad_priv_a.security

import android.content.Context
import android.os.Build
import android.os.Debug
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.content.pm.PackageManager
import android.content.pm.Signature
import java.security.MessageDigest

object ReverseEngineeringProtections {

    fun isDeviceEmulator(): Boolean {
        return (Build.FINGERPRINT.contains("generic")
                || Build.MODEL.contains("Emulator")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || "google_sdk" == Build.PRODUCT)
    }

    fun isDebuggerAttached(): Boolean {
        return Debug.isDebuggerConnected() || Debug.waitingForDebugger()
    }

    fun isUsbDebuggingEnabled(context: Context): Boolean {
        return Settings.Secure.getInt(
            context.contentResolver,
            Settings.Secure.ADB_ENABLED, 0
        ) == 1
    }

    fun performChecks(context: Context): Boolean {
        val emulator = isDeviceEmulator()
        val debugger = isDebuggerAttached()
        val usbDebugging = isUsbDebuggingEnabled(context)

        Log.d("SecurityCheck", "Emulator=$emulator, Debugger=$debugger, USB=$usbDebugging")
        return emulator || debugger || usbDebugging
    }

    fun isSignatureValid(context: Context): Boolean {
        return try {
            val packageManager = context.packageManager
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }

            val signatures: Array<Signature>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            val expectedHash = "REEMPLAZA_ESTO_CON_TU_HASH_BASE64" // ← Personaliza esta linea
            val signature = signatures?.firstOrNull() ?: return false

            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(signature.toByteArray())
            val currentHash = Base64.encodeToString(hash, Base64.NO_WRAP)

            Log.d("SignatureCheck", "Expected=$expectedHash\nCurrent=$currentHash")
            currentHash == expectedHash
        } catch (e: Exception) {
            Log.e("SignatureCheck", "Error: ${e.message}")
            false
        }
    }
}
