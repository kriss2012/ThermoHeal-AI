# ThermoHeal-AI Production ProGuard / R8 Rules

# Domain & Data models (keep for Room and serialization)
-keep class com.thermoheal.ai.domain.model.** { *; }
-keep class com.thermoheal.ai.data.local.entity.** { *; }
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

# Room database
-keep class androidx.room.RoomDatabase { *; }
-dontwarn androidx.room.paging.**

# Coroutines
-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler { *; }

# Hilt / Dagger
-dontwarn dagger.hilt.internal.aggregatedroot.**
-keep class * extends dagger.hilt.internal.UnsafeCasts { *; }

# iText7 PDF generation
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**
-dontwarn org.bouncycastle.**
-dontwarn org.apache.fontbox.**
