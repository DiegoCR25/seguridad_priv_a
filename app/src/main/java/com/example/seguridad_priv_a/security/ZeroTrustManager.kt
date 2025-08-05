package com.example.seguridad_priv_a.security

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.util.Base64
import android.util.Log
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*
import kotlin.collections.HashMap

class ZeroTrustManager(private val context: Context) {

    private val activeTokens = HashMap<String, TokenData>()
    private val tokenTTL = 5 * 60 * 1000L // 5 minutos en milisegundos

    data class TokenData(val permission: String, val issuedAt: Long)

    fun requestToken(permission: String): String {
        val token = UUID.randomUUID().toString()
        val issuedAt = System.currentTimeMillis()
        activeTokens[token] = TokenData(permission, issuedAt)
        return token
    }

    fun validateToken(token: String, requiredPermission: String): Boolean {
        val data = activeTokens[token] ?: return false
        val isExpired = System.currentTimeMillis() - data.issuedAt > tokenTTL
        val isValid = data.permission == requiredPermission && !isExpired

        if (!isValid) activeTokens.remove(token)

        return isValid
    }

    fun clearExpiredTokens() {
        val now = System.currentTimeMillis()
        activeTokens.entries.removeIf { now - it.value.issuedAt > tokenTTL }
    }

    fun getTokenRemainingTime(token: String): Long {
        val data = activeTokens[token] ?: return 0
        val remaining = tokenTTL - (System.currentTimeMillis() - data.issuedAt)
        return if (remaining > 0) remaining else 0
    }

    fun performAppAttestation(): Boolean {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                context.packageManager.getPackageInfo(
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

            if (signatures.isNullOrEmpty()) {
                Log.e("ZeroTrust", "❌ No hay firmas disponibles.")
                return false
            }

            val cert = signatures[0]
            val certFactory = CertificateFactory.getInstance("X.509")
            val x509 = certFactory.generateCertificate(cert.toByteArray().inputStream()) as X509Certificate
            val issuer = x509.issuerX500Principal.name

            val trustedIssuer = "CN=Android Debug" // Personaliza esto si estás firmando con otro certificado
            val isTrusted = issuer.contains(trustedIssuer)

            Log.i("ZeroTrust", "✔️ Attestation completada. Trusted: $isTrusted")
            return isTrusted
        } catch (e: Exception) {
            Log.e("ZeroTrust", "❌ Error en attestation: ${e.message}")
            false
        }
    }

}
