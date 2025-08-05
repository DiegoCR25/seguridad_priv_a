package com.example.seguridad_priv_a.security

import android.content.Context
import android.util.Base64
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.security.KeyPairGenerator
import java.security.Signature
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class SecurityAuditManager(private val context: Context) {

    private val accessTimestamps = ConcurrentHashMap<String, MutableList<Long>>()
    private val auditLog = mutableListOf<JSONObject>()
    private val maxRequestsPerMinute = 5

    // Registra un evento de auditoría
    fun registerEvent(userId: String, eventType: String, metadata: String = "") {
        val now = System.currentTimeMillis()

        // Actualiza los timestamps
        val userAccesses = accessTimestamps.getOrPut(userId) { mutableListOf() }
        userAccesses.add(now)

        // Elimina accesos viejos (> 60s)
        userAccesses.removeIf { now - it > 60_000 }

        // Revisión de patrones anómalos
        if (userAccesses.size > maxRequestsPerMinute) {
            Log.w("SecurityAudit", "🚨 Acceso sospechoso por $userId")
            addAuditLog(userId, "ANOMALOUS_BEHAVIOR", "Demasiadas solicitudes")
        }

        // Guarda el evento
        addAuditLog(userId, eventType, metadata)
    }

    private fun addAuditLog(userId: String, type: String, metadata: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val entry = JSONObject().apply {
            put("timestamp", timestamp)
            put("user", userId)
            put("event", type)
            put("details", metadata)
        }
        auditLog.add(entry)
    }

    fun exportLogsSigned(): String {
        val jsonArray = JSONArray(auditLog)
        val logsString = jsonArray.toString()

        val signature = signData(logsString)
        val signedObject = JSONObject().apply {
            put("logs", jsonArray)
            put("signature", signature)
        }

        return signedObject.toString(4)
    }

    private fun signData(data: String): String {
        try {
            val keyGen = KeyPairGenerator.getInstance("RSA")
            keyGen.initialize(2048)
            val keyPair = keyGen.genKeyPair()

            val privateKey = keyPair.private
            val signature = Signature.getInstance("SHA256withRSA")
            signature.initSign(privateKey)
            signature.update(data.toByteArray())
            val signedBytes = signature.sign()

            return Base64.encodeToString(signedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e("SecurityAudit", "Error al firmar logs: ${e.message}")
            return ""
        }
    }
}
