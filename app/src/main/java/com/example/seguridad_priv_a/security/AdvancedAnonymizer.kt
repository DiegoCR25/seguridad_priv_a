package com.example.seguridad_priv_a.security

import kotlin.math.*
import java.util.*
import kotlin.collections.HashMap

// Datos básicos para anonimización
data class PersonalData(val id: String, val name: String, val email: String, val age: Int, val city: String)
data class AnonymizedData(val groupId: String, val generalizedAge: String, val generalizedCity: String)
data class NumericData(val label: String, val value: Double)
data class MaskingPolicy(val type: DataType)
data class RetentionPolicy(val retentionMillis: Long, val timestamp: Long = System.currentTimeMillis())

enum class DataType {
    EMAIL, PHONE, NAME
}

class AdvancedAnonymizer {

    // 1. K-Anonymity simple con generalización
    fun anonymizeWithKAnonymity(data: List<PersonalData>, k: Int): List<AnonymizedData> {
        val grouped = data.groupBy { it.age / 10 to it.city }
            .filter { it.value.size >= k }

        val result = mutableListOf<AnonymizedData>()
        for ((key, group) in grouped) {
            val (ageGroup, city) = key
            val generalizedAge = "${ageGroup * 10}-${ageGroup * 10 + 9}"
            val groupId = UUID.randomUUID().toString().substring(0, 8)
            group.forEach {
                result.add(
                    AnonymizedData(
                        groupId = groupId,
                        generalizedAge = generalizedAge,
                        generalizedCity = city.replaceFirstChar { '*' }
                    )
                )
            }
        }
        return result
    }

    // 2. Differential privacy con mecanismo de Laplace
    fun applyDifferentialPrivacy(data: NumericData, epsilon: Double): NumericData {
        val sensitivity = 1.0
        val scale = sensitivity / epsilon
        val noise = laplaceNoise(scale)
        return data.copy(value = data.value + noise)
    }

    private fun laplaceNoise(scale: Double): Double {
        val u = Random().nextDouble() - 0.5
        return -scale * sign(u) * ln(1 - 2 * abs(u))
    }

    // 3. Data masking personalizado
    fun maskByDataType(data: Any, maskingPolicy: MaskingPolicy): Any {
        return when (maskingPolicy.type) {
            DataType.EMAIL -> {
                val email = data as String
                val parts = email.split("@")
                if (parts.size == 2) "${parts[0].take(2)}****@${parts[1]}" else "****"
            }
            DataType.PHONE -> {
                val phone = data as String
                phone.replaceRange(3, phone.length - 2, "****")
            }
            DataType.NAME -> {
                val name = data as String
                "${name.first()}****"
            }
        }
    }

    // 4. Verificar si datos deben eliminarse por retención
    fun shouldDelete(policy: RetentionPolicy): Boolean {
        val now = System.currentTimeMillis()
        return now - policy.timestamp > policy.retentionMillis
    }
}
