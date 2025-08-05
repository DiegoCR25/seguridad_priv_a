# 🛡️ S15_Briceno_SeguridadPrivacidadApp – App Móvil con Protección de Datos, Forense y Zero Trust

`S15_Briceno_SeguridadPrivacidadApp` es una aplicación Android desarrollada en Kotlin que implementa un sistema completo de **protección de datos, seguridad forense y compliance**. La app simula una gestión segura de accesos, evidencias digitales y privacidad de usuarios, aplicando principios de arquitectura **Zero Trust**, **protecciones anti-reverse engineering** y **cumplimiento con GDPR/CCPA**.

Este proyecto corresponde a la **Semana 14** del curso de Seguridad y Privacidad Móvil.

---

## 🔐 Características principales

- Gestión de accesos con **tokens temporales Zero Trust**.
- Autenticación biométrica obligatoria para acciones sensibles.
- **Encriptación AES-256-GCM** de datos y logs.
- Registro detallado de eventos con timestamps y tipos de acción.
- **Protección contra debugging, emulación e ingeniería inversa**.
- Auditoría forense con integridad de logs tipo **blockchain local**.
- Exportación de logs firmados digitalmente.
- Generación de reportes de cumplimiento **GDPR / CCPA**.
- Búsqueda de incidentes mediante palabras clave.

---

## 🔎 Módulo Forense (3.4)

| Funcionalidad                             | Descripción                                                                 |
|------------------------------------------|-----------------------------------------------------------------------------|
| 🧾 **Chain of Custody**                  | Cada evento incluye hash y previousHash con SHA-256, garantizando integridad. |
| 🔐 **Logs Tamper-Evident**              | La estructura hash–previousHash impide modificaciones sin detección.       |
| 📑 **Reporte GDPR/CCPA Automático**     | Reporte JSON con lista de acciones por usuario generada en tiempo real.    |
| 🕵️ **Investigación de Incidentes**      | Filtrado de eventos por palabra clave (por ejemplo, “export” o “borrar”).  |

---

# 📸 Capturas de pantalla

## Parte 2: Implementación y Mejoras Intermedias

### 2.1 **Fortalecimiento de la Encriptación**

| 🟢 **Fortalecimiento de la Encriptación 1** | 🟢 **Fortalecimiento de la Encriptación 2** |
|------------------|---------------------------|
| <img src="pantallas/1_fortalecimiento_encriptacion_1.png" width="600" /> | <img src="pantallas/1_fortalecimiento_encriptacion_2.png" width="600" /> |

### 2.2 **Sistema de Auditoría Avanzado**

| 🔒 **Sistema de Auditoría Avanzado 1** | 🔒 **Sistema de Auditoría Avanzado 2** |
|------------------|---------------------------|
| <img src="pantallas/2_sistema_auditoria_1.png" width="600" /> | <img src="pantallas/2_sistema_auditoria_2.png" width="600" /> |

### 2.3 **Biometría y Autenticación**

| 🧾 **Biometría y Autenticación 1** | 🧾 **Biometría y Autenticación 2** |
|-------------------------|---------------------------|
| <img src="pantallas/3_biometria_autenticacion_1.png" width="600" /> | <img src="pantallas/3_biometria_autenticacion_2.png" width="600" /> |

---

## Parte 3: Arquitectura de Seguridad Avanzada

### 3.1 **Implementación de Zero-Trust Architecture**

| 🕵️ **Zero-Trust Architecture 1** | 🕵️ **Zero-Trust Architecture 2** |
|---------------------|--------------------|
| <img src="pantallas/5_zero_trust_1.png" width="600" /> | <img src="pantallas/5_zero_trust_2.png" width="600" /> |

### 3.2 **Protección Contra Ingeniería Inversa**

| 🛡️ **Protección Contra Ingeniería Inversa 1** | 🛡️ **Protección Contra Ingeniería Inversa 2** |
|--------------------------|-------------------------|
| <img src="pantallas/6_ingenieria_inversa_1.png" width="600" /> | <img src="pantallas/6_ingenieria_inversa_2.png" width="600" /> |

### 3.3 **Framework de Anonimización Avanzado**

| 🔒 **Anonimización Avanzada 1** | 🔒 **Anonimización Avanzada 2** |
|--------------------------|-------------------------|
| <img src="pantallas/7_anonimización_avanzada_1.png" width="600" /> | <img src="pantallas/7_anonimización_avanzada_2.png" width="600" /> |

### 3.4 **Análisis Forense y Compliance**

| 📜 **Análisis Forense 1** | 📜 **Análisis Forense 2** |
|-------------------------|-------------------------|
| <img src="pantallas/8_analisis_forense_1.png" width="600" /> | <img src="pantallas/8_analisis_forense_2.png" width="600" /> |

---



## 🛠️ Tecnologías utilizadas

- **Kotlin**
- Android Studio
- `BiometricPrompt` API
- `EncryptedSharedPreferences`
- SHA-256, JSON y almacenamiento local
- Detección de firma, debugging y emulador
- Diseño modular de clases

---

## 📁 Estructura del proyecto

- `DataProtectionActivity.kt`: Pantalla principal. Muestra info de seguridad, logs y botones de acción.
- `SecurityAuditManager.kt`: Módulo de auditoría de eventos seguros.
- `ZeroTrustManager.kt`: Lógica de tokens por permiso y validación granular.
- `BiometricHelper.kt`: Wrapper para autenticación biométrica.
- `ReverseEngineeringProtections.kt`: Controles anti-debugging, emulador y firma APK.
- `ForensicLogger.kt`: Registro de eventos forenses, generación de reportes, búsqueda de incidentes.
- `activity_data_protection.xml`: Diseño de interfaz con botones de logs, borrar datos, exportar e investigar.
- `strings.xml`: Traducciones centralizadas de botones y mensajes.

---

## 🧪 Ejemplos de eventos registrados

```json
[
  {
    "timestamp": 1722954805123,
    "userId": "user123",
    "action": "EXPORT",
    "details": "Logs exportados digitalmente",
    "previousHash": "o8O0vZKmfN...",
    "hash": "u92djSk9s8h..."
  },
  {
    "timestamp": 1722954220999,
    "userId": "user123",
    "action": "SECURITY",
    "details": "Intentó borrar todos los datos",
    "previousHash": "GENESIS",
    "hash": "k2FbdfL2os4..."
  }
]
```
---

## ⚙️ Requisitos técnicos
- Android Studio **Hedgehog** o superior
- **Target SDK** 33+
- **Min SDK** 24+
- **Kotlin** y **ViewBinding** habilitados
- Permisos biométricos en el **AndroidManifest.xml**

## 🔐 Consideraciones de privacidad y compliance
- No se almacenan datos personales reales.
- Los datos están **anonimizados** o **simulados**.
- Se implementa el principio de **privacy-by-design**.
