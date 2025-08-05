package com.example.seguridad_priv_a

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.seguridad_priv_a.databinding.ActivityDataProtectionBinding
import com.example.seguridad_priv_a.security.*

class DataProtectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDataProtectionBinding
    private lateinit var auditManager: SecurityAuditManager
    private lateinit var zeroTrustManager: ZeroTrustManager
    private lateinit var forensicLogger: ForensicLogger

    private val userId = "user123"
    private var activeToken: String? = null

    private val dataProtectionManager by lazy {
        (application as PermissionsApplication).dataProtectionManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataProtectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auditManager = SecurityAuditManager(this)
        zeroTrustManager = ZeroTrustManager(this)
        forensicLogger = ForensicLogger(this)

        val isAppGenuine = zeroTrustManager.performAppAttestation()
        if (!isAppGenuine) {
            Toast.makeText(this, "⚠️ Integridad comprometida", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        if (!ReverseEngineeringProtections.isSignatureValid(this)) {
            Toast.makeText(this, "⚠️ Firma digital no válida", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        if (ReverseEngineeringProtections.performChecks(this)) {
            Toast.makeText(this, "⚠️ Entorno inseguro detectado (debugging/emulador)", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupUI()
        requestTokenAndLoad()

        dataProtectionManager.logAccess("NAVIGATION", "DataProtectionActivity abierta")
        auditManager.registerEvent(userId, "NAVIGATION", "Actividad abierta")
        forensicLogger.logEvent(userId, "NAVIGATION", "Entró a DataProtectionActivity")
    }

    private fun requestTokenAndLoad() {
        activeToken = zeroTrustManager.requestToken("ACCESS_LOGS")
        loadDataProtectionInfo()
        loadAccessLogs()
    }

    private fun setupUI() {
        binding.btnViewLogs.setOnClickListener {
            if (validateAccess("ACCESS_LOGS")) {
                loadAccessLogs()
                auditManager.registerEvent(userId, "ACTION", "Consultó logs de acceso")
                forensicLogger.logEvent(userId, "ACCESS_LOGS", "Consultó logs desde UI")
                Toast.makeText(this, "Logs actualizados", Toast.LENGTH_SHORT).show()
            } else {
                promptReauthentication()
            }
        }

        binding.btnClearData.setOnClickListener {
            if (validateAccess("CLEAR_DATA")) {
                auditManager.registerEvent(userId, "SECURITY", "Intentó borrar todos los datos")
                forensicLogger.logEvent(userId, "DATA_MANAGEMENT", "Solicitó eliminación de datos")
                showClearDataDialog()
            } else {
                promptReauthentication()
            }
        }

        binding.btnExportLogs?.setOnClickListener {
            if (validateAccess("EXPORT_LOGS")) {
                val signedLog = auditManager.exportLogsSigned()
                showExportedLogs(signedLog)
                auditManager.registerEvent(userId, "EXPORT", "Logs exportados digitalmente")
                forensicLogger.logEvent(userId, "EXPORT", "Exportó logs firmados digitalmente")
            } else {
                promptReauthentication()
            }
        }

        // Nuevo botón visual para mostrar reporte GDPR
        binding.btnComplianceReport?.setOnClickListener {
            if (validateAccess("EXPORT_LOGS")) {
                val report = forensicLogger.generateComplianceReport().toString(2)
                AlertDialog.Builder(this)
                    .setTitle("Reporte Forense (GDPR)")
                    .setMessage(report)
                    .setPositiveButton("OK", null)
                    .show()
                forensicLogger.logEvent(userId, "COMPLIANCE", "Visualizó el reporte GDPR/CCPA")
            } else {
                promptReauthentication()
            }
        }
    }

    private fun validateAccess(requiredPermission: String): Boolean {
        val token = activeToken ?: return false
        val isValid = zeroTrustManager.validateToken(token, requiredPermission)
        if (!isValid) {
            Toast.makeText(this, "🔒 Token expirado o sin privilegios", Toast.LENGTH_SHORT).show()
        }
        return isValid
    }

    private fun promptReauthentication() {
        val biometricHelper = BiometricHelper(
            context = this,
            onAuthenticated = {
                activeToken = zeroTrustManager.requestToken("ACCESS_LOGS")
                Toast.makeText(this, "🔓 Acceso renovado", Toast.LENGTH_SHORT).show()
            },
            onFailed = {
                Toast.makeText(this, "Autenticación fallida", Toast.LENGTH_SHORT).show()
            }
        )

        biometricHelper.authenticate()
    }

    private fun showExportedLogs(signedLog: String) {
        AlertDialog.Builder(this)
            .setTitle("Logs Firmados")
            .setMessage(signedLog)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun loadDataProtectionInfo() {
        val info = dataProtectionManager.getDataProtectionInfo()
        val infoText = StringBuilder()

        infoText.append("🔐 INFORMACIÓN DE SEGURIDAD\n\n")
        info.forEach { (key, value) ->
            infoText.append("• $key: $value\n")
        }

        infoText.append("\n📊 EVIDENCIAS DE PROTECCIÓN:\n")
        infoText.append("• Encriptación AES-256-GCM activa\n")
        infoText.append("• Todos los accesos registrados\n")
        infoText.append("• Datos anonimizados automáticamente\n")
        infoText.append("• Almacenamiento local seguro\n")
        infoText.append("• No hay compartición de datos\n")

        binding.tvDataProtectionInfo.text = infoText.toString()

        dataProtectionManager.logAccess("DATA_PROTECTION", "Información de protección mostrada")
        auditManager.registerEvent(userId, "INFO", "Visualizó información de seguridad")
        forensicLogger.logEvent(userId, "DATA_PROTECTION", "Visualizó información de seguridad")
    }

    private fun loadAccessLogs() {
        val logs = dataProtectionManager.getAccessLogs()
        binding.tvAccessLogs.text = if (logs.isNotEmpty()) {
            logs.joinToString("\n")
        } else {
            "No hay logs disponibles"
        }
        dataProtectionManager.logAccess("DATA_ACCESS", "Logs de acceso consultados")
        forensicLogger.logEvent(userId, "DATA_ACCESS", "Accedió a los logs de acceso")
    }

    private fun showClearDataDialog() {
        AlertDialog.Builder(this)
            .setTitle("Borrar Todos los Datos")
            .setMessage("¿Estás seguro de que deseas borrar todos los datos almacenados y logs de acceso? Esta acción no se puede deshacer.")
            .setPositiveButton("Borrar") { _, _ ->
                clearAllData()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun clearAllData() {
        dataProtectionManager.clearAllData()
        binding.tvAccessLogs.text = "Todos los datos han sido borrados"
        binding.tvDataProtectionInfo.text =
            "🔐 DATOS BORRADOS DE FORMA SEGURA\n\nTodos los datos personales y logs han sido eliminados del dispositivo."

        Toast.makeText(this, "Datos borrados de forma segura", Toast.LENGTH_LONG).show()
        dataProtectionManager.logAccess("DATA_MANAGEMENT", "Todos los datos borrados por el usuario")
        auditManager.registerEvent(userId, "DATA_MANAGEMENT", "Eliminación total de datos")
        forensicLogger.logEvent(userId, "DATA_MANAGEMENT", "Eliminación total de datos realizada")
    }

    override fun onResume() {
        super.onResume()
        zeroTrustManager.clearExpiredTokens()
        loadAccessLogs()
    }
}
