# 1. Keep generated serializer classes
-keep class **$$serializer { *; }

# 2. Keep serializer() methods on companion/INSTANCE
-keepclassmembers class * {
    public static kotlinx.serialization.KSerializer serializer(...);
}

# 3. Preserve core descriptor implementation
-keep class kotlinx.serialization.internal.PluginGeneratedSerialDescriptor { *; }
-keep class kotlinx.serialization.descriptors.SerialDescriptor { *; }

# 4. Retain runtime annotations
-keepattributes RuntimeVisibleAnnotations, AnnotationDefault, *Annotation*

# 5. (Optional) Keep all serializable classes if needed
#-keep @kotlinx.serialization.Serializable class * { *; }