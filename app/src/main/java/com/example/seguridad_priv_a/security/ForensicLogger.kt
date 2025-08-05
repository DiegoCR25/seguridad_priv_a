package com.example.seguridad_priv_a.security

import android.content.Context
import android.util.Base64
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

class ForensicLogger(private val context: Context) {

    private val evidenceFile = File(context.filesDir, "forensic_log_chain.json")

    data class ForensicEntry(
        val timestamp: Long,
        val userId: String,
        val action: String,
        val details: String,
        val previousHash: String,
        val hash: String
    )

    // Agrega una entrada a la "cadena forense"
    fun logEvent(userId: String, action: String, details: String) {
        val previousHash = getLastHash()
        val timestamp = System.currentTimeMillis()

        val data = "$timestamp|$userId|$action|$details|$previousHash"
        val currentHash = sha256(data)

        val entry = JSONObject().apply {
            put("timestamp", timestamp)
            put("userId", userId)
            put("action", action)
            put("details", details)
            put("previousHash", previousHash)
            put("hash", currentHash)
        }

        appendToChain(entry)
    }

    private fun appendToChain(entry: JSONObject) {
        val array = if (evidenceFile.exists()) {
            JSONArray(evidenceFile.readText())
        } else {
            JSONArray()
        }
        array.put(entry)
        evidenceFile.writeText(array.toString())
    }

    private fun getLastHash(): String {
        if (!evidenceFile.exists()) return "GENESIS"
        val array = JSONArray(evidenceFile.readText())
        if (array.length() == 0) return "GENESIS"
        return array.getJSONObject(array.length() - 1).getString("hash")
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    // 📄 Exporta reporte GDPR/CCPA en JSON
    fun generateComplianceReport(): JSONObject {
        val array = if (evidenceFile.exists()) JSONArray(evidenceFile.readText()) else JSONArray()
        val groupedByUser = mutableMapOf<String, MutableList<String>>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val user = obj.getString("userId")
            val action = obj.getString("action")

            groupedByUser.putIfAbsent(user, mutableListOf())
            groupedByUser[user]?.add(action)
        }

        return JSONObject().apply {
            put("generatedAt", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))
            put("users", JSONArray().apply {
                groupedByUser.forEach { (user, actions) ->
                    put(JSONObject().apply {
                        put("userId", user)
                        put("actions", JSONArray(actions))
                    })
                }
            })
        }
    }

    // 🕵️ Búsqueda de eventos sospechosos (investigación)
    fun filterEventsByKeyword(keyword: String): List<ForensicEntry> {
        val array = if (evidenceFile.exists()) JSONArray(evidenceFile.readText()) else JSONArray()
        val result = mutableListOf<ForensicEntry>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            if (obj.getString("details").contains(keyword, ignoreCase = true)) {
                result.add(
                    ForensicEntry(
                        obj.getLong("timestamp"),
                        obj.getString("userId"),
                        obj.getString("action"),
                        obj.getString("details"),
                        obj.getString("previousHash"),
                        obj.getString("hash")
                    )
                )
            }
        }

        return result
    }
}
