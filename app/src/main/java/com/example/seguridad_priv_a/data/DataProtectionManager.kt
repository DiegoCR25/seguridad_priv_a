package com.example.seguridad_priv_a.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class DataProtectionManager(private val context: Context) {

    private lateinit var encryptedPrefs: SharedPreferences
    private lateinit var accessLogPrefs: SharedPreferences

    private val ROTATION_INTERVAL_DAYS = 30L
    private val PREF_KEY_ROTATION_TIMESTAMP = "last_key_rotation"

    fun initialize() {
        try {
            // Crear o recuperar clave maestra
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            // Crear SharedPreferences encriptado
            encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                "secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            // SharedPreferences para logs de acceso
            accessLogPrefs = context.getSharedPreferences("access_logs", Context.MODE_PRIVATE)

        } catch (e: Exception) {
            // Fallback si la encriptación falla
            encryptedPrefs = context.getSharedPreferences("fallback_prefs", Context.MODE_PRIVATE)
            accessLogPrefs = context.getSharedPreferences("access_logs", Context.MODE_PRIVATE)
        }

        // Intentar rotación de clave
        rotateEncryptionKey()
    }

    fun rotateEncryptionKey(): Boolean {
        val sharedPrefs = context.getSharedPreferences("rotation_meta", Context.MODE_PRIVATE)
        val lastRotation = sharedPrefs.getLong(PREF_KEY_ROTATION_TIMESTAMP, 0L)
        val now = System.currentTimeMillis()

        val daysSinceRotation = TimeUnit.MILLISECONDS.toDays(now - lastRotation)
        if (daysSinceRotation >= ROTATION_INTERVAL_DAYS) {
            try {
                // Borrar prefs encriptadas para forzar nueva clave
                context.deleteSharedPreferences("secure_prefs")

                // Recrear configuración
                initialize()

                // Guardar nuevo timestamp
                sharedPrefs.edit().putLong(PREF_KEY_ROTATION_TIMESTAMP, now).apply()

                logAccess("KEY_ROTATION", "Clave maestra rotada automáticamente")
                return true
            } catch (e: Exception) {
                logAccess("KEY_ROTATION", "Error al rotar clave: ${e.message}")
            }
        }
        return false
    }

    fun storeSecureData(key: String, value: String) {
        val hmac = generateHMAC(value, key)

        encryptedPrefs.edit().apply {
            putString(key, value)
            putString("${key}_hmac", hmac)
        }.apply()

        logAccess("DATA_STORAGE", "Dato almacenado de forma segura: $key")
    }

    fun getSecureData(key: String): String? {
        val data = encryptedPrefs.getString(key, null)
        if (data != null) {
            val isIntact = verifyDataIntegrity(key)
            if (isIntact) {
                logAccess("DATA_ACCESS", "Dato accedido: $key (verificado OK)")
            } else {
                logAccess("DATA_ACCESS", "¡Integridad comprometida! $key")
            }
        }
        return data
    }

    fun verifyDataIntegrity(key: String): Boolean {
        val data = encryptedPrefs.getString(key, null) ?: return false
        val storedHmac = encryptedPrefs.getString("${key}_hmac", null) ?: return false

        val expectedHmac = generateHMAC(data, key)
        return storedHmac == expectedHmac
    }

    private fun generateHMAC(data: String, secret: String): String {
        val hmacKey = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(hmacKey)
        val hmacBytes = mac.doFinal(data.toByteArray())
        return Base64.encodeToString(hmacBytes, Base64.NO_WRAP)
    }

    fun deriveUserKey(userId: String, password: String): ByteArray {
        val salt = generateUserSalt(userId)
        return deriveKeyFromPassword(password, salt)
    }

    private fun generateUserSalt(userId: String): String {
        val key = "salt_$userId"
        val existingSalt = encryptedPrefs.getString(key, null)
        if (existingSalt != null) return existingSalt

        val saltBytes = ByteArray(16)
        SecureRandom().nextBytes(saltBytes)
        val salt = Base64.encodeToString(saltBytes, Base64.NO_WRAP)

        encryptedPrefs.edit().putString(key, salt).apply()
        return salt
    }

    private fun deriveKeyFromPassword(password: String, saltBase64: String): ByteArray {
        val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
        val spec = PBEKeySpec(password.toCharArray(), salt, 10000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }

    fun logAccess(category: String, action: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val logEntry = "$timestamp - $category: $action"

        val existingLogs = accessLogPrefs.getString("logs", "") ?: ""
        val newLogs = if (existingLogs.isEmpty()) {
            logEntry
        } else {
            "$existingLogs\n$logEntry"
        }

        accessLogPrefs.edit().putString("logs", newLogs).apply()

        val logLines = newLogs.split("\n")
        if (logLines.size > 100) {
            val trimmedLogs = logLines.takeLast(100).joinToString("\n")
            accessLogPrefs.edit().putString("logs", trimmedLogs).apply()
        }
    }

    fun getAccessLogs(): List<String> {
        val logsString = accessLogPrefs.getString("logs", "") ?: ""
        return if (logsString.isEmpty()) {
            emptyList()
        } else {
            logsString.split("\n").reversed()
        }
    }

    fun clearAllData() {
        encryptedPrefs.edit().clear().apply()
        accessLogPrefs.edit().clear().apply()
        logAccess("DATA_MANAGEMENT", "Todos los datos han sido borrados de forma segura")
    }

    fun getDataProtectionInfo(): Map<String, String> {
        return mapOf(
            "Encriptación" to "AES-256-GCM",
            "Almacenamiento" to "Local encriptado",
            "Logs de acceso" to "${getAccessLogs().size} entradas",
            "Última limpieza" to (getSecureData("last_cleanup") ?: "Nunca"),
            "Estado de seguridad" to "Activo"
        )
    }

    fun anonymizeData(data: String): String {
        return data.replace(Regex("[0-9]"), "*")
            .replace(Regex("[A-Za-z]{3,}"), "***")
    }
}
