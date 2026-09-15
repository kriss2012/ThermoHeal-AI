# ThermoHeal-AI proguard rules
-keep class com.thermoheal.ai.domain.model.** { *; }
-keep class com.thermoheal.ai.data.local.entity.** { *; }
-keepattributes *Annotation*
-dontwarn kotlinx.coroutines.**
