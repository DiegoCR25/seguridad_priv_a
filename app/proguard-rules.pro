# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ========== PROTECCIÓN CONTRA INGENIERÍA INVERSA ==========

# Evita que se eliminen o renombren nuestras clases de seguridad
-keep class com.example.seguridad_priv_a.security.** { *; }

# Quitar logs en versión release (para ocultar rastros de depuración)
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# Mantener anotaciones importantes
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Keep classes related to Google Tink and javax.annotation
-keep class javax.annotation.** { *; }
-keep class javax.annotation.concurrent.** { *; }

# Keep classes used by Tink
-keep class com.google.crypto.tink.** { *; }
-keep class com.google.crypto.tink.proto.** { *; }

# Google API Client (com.google.api)
-keep class com.google.api.client.http.** { *; }
-keep class com.google.api.client.http.javanet.NetHttpTransport { *; }

# Joda-Time (org.joda.time)
-keep class org.joda.time.** { *; }

# Clases faltantes de javax.annotation (requeridas por Tink)
-keep class javax.annotation.** { *; }
-keep class javax.annotation.concurrent.** { *; }
-keep class javax.annotation.concurrent.ThreadSafe { *; }

# Mantener las clases de javax.naming
-keep class javax.naming.** { *; }
-keep class javax.naming.directory.** { *; }
-keep class javax.naming.ldap.** { *; }

# Mantener las clases de org.ietf.jgss
-keep class org.ietf.jgss.** { *; }

# Mantener las clases de Joda-Time
-keep class org.joda.** { *; }
