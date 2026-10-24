# Tukra ProGuard / R8 Rules

# Kotlin & Coroutines
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable
-dontwarn kotlin.**
-dontwarn kotlinx.coroutines.**

# Kotlinx Serialization
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keepclassmembers class * {
    @kotlinx.serialization.Contextual <fields>;
}
-keepclasseswithmembers class * {
    public static *** Companion;
}
-keepclassmembers class * {
    *** Companion;
}
-keep class * implements kotlinx.serialization.KSerializer {
    <init>(...);
}
-keep class com.xsparks.tukra.core.network.model.** { *; }
-keepclassmembers class com.xsparks.tukra.core.network.model.** { *; }

# Retrofit 2 & OkHttp
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Razorpay SDK
-keep class com.razorpay.** { *; }
-dontwarn com.razorpay.**
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# ZXing QR Generation
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# ML Kit Barcode Scanning & CameraX
-keep class com.google.mlkit.vision.barcode.** { *; }
-keep class androidx.camera.** { *; }
-dontwarn com.google.mlkit.**
-dontwarn androidx.camera.**

# Jetpack Compose & Navigation
-keep class androidx.compose.ui.** { *; }
-keep class androidx.navigation.** { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
