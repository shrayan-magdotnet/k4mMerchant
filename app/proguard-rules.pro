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

# always keep model classes
-keep class com.kash4me.data.** { *; }
-keepclassmembers class com.kash4me.data.** { *; }
-keep class androidx.core.app.CoreComponentFactory { *; }
-dontwarn javax.annotation.**

# ProGuard rules for Chuck library

-keep class com.readystatesoftware.chuck.** { *; }

# If you're using ChuckInterceptor with OkHttp
-keep class okhttp3.internal.platform.Platform
-keep class okhttp3.OkHttpClient {
    void setInterceptor(okhttp3.Interceptor);
}

# ProGuard rules for CircleImageView library

-keep class de.hdodenhof.circleimageview.** { *; }
-keepclassmembers class de.hdodenhof.circleimageview.** {
    *;
}

# ProGuard rules for SmoothProgressBar Circular library

-keep class com.github.castorflex.smoothprogressbar.** { *; }
-keepclassmembers class com.github.castorflex.smoothprogressbar.** {
    *;
}

# ProGuard rules for Kotlin Coroutines Android library

-keepclassmembers class kotlinx.coroutines.android.HandlerDispatcher {
    <init>(...);
}

-keepclassmembers class kotlinx.coroutines.android.AndroidExceptionPreHandler {
    <init>(...);
}

# ProGuard rules for Kotlin Coroutines Core library

-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

# ProGuard rule for CountryCodePicker library

-keep class com.hbb20.** { *; }

# ProGuard rules for CurrencyEditText library

-keep class com.blackcat.currencyedittext.CurrencyEditText { *; }

# ProGuard rules for DataStore

-keep class androidx.datastore.*.** {*;}

# ProGuard rules for Dagger Hilt

-keep class dagger.hilt.android.** { *; }
-keep class javax.inject.** { *; }
-keepclassmembers class * {
    @dagger.hilt.* *;
}
-keepclasseswithmembers class * {
    @dagger.hilt.* <fields>;
}
-keepclasseswithmembers class * {
    @dagger.hilt.* <methods>;
}

# ProGuard rules for Dagger Hilt Compiler

-dontwarn dagger.hilt.android.**
-keep class dagger.hilt.android.** { *; }
-keep class javax.inject.** { *; }
-keepclassmembers class * {
    @dagger.hilt.* *;
}
-keepclasseswithmembers class * {
    @dagger.hilt.* <fields>;
}
-keepclasseswithmembers class * {
    @dagger.hilt.* <methods>;
}

# ProGuard rules for EasyPermissions library

-keep class pub.devrel.easypermissions.** { *; }
-dontwarn pub.devrel.easypermissions.**

# ProGuard rules for Security Crypto library

-keep class androidx.security.crypto.** { *; }
-keepnames class androidx.security.crypto.EncryptedData
-keepnames class androidx.security.crypto.MasterKeys

# ProGuard rules for Fragment KTX library

-keep class androidx.fragment.app.Fragment { *; }
-keep class androidx.fragment.app.FragmentStateAdapter { *; }
-keep class androidx.fragment.app.FragmentStatePagerAdapter { *; }
-keep class androidx.fragment.app.FragmentPagerAdapter { *; }
-keep class androidx.fragment.app.DialogFragment { *; }

# ProGuard rules for Glide library
-keep public class com.bumptech.glide.** {
    public *;
}

# Glide specific ProGuard rules
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

-keep class com.bumptech.glide.Registry {
    public <methods>;
}

-dontwarn com.bumptech.glide.**

# ProGuard rules for LiveData KTX library

-keep class androidx.lifecycle.LiveData { *; }
-keep class androidx.lifecycle.MutableLiveData { *; }

# ProGuard rules for ViewModel KTX library

-keep class androidx.lifecycle.ViewModel { *; }
-keep class androidx.lifecycle.ViewModelProvider { *; }

# ProGuard rules for Material Components library

-keep class com.google.android.material.** { *; }

# ProGuard rules for Navigation Runtime KTX library

-keep class androidx.navigation.** { *; }
-keep class * implements androidx.navigation.NavController { *; }

# ProGuard rule for Navigation Fragment KTX library
-keep class androidx.navigation.fragment.NavHostFragment { *; }

# ProGuard rule for Navigation UI KTX library
-keep class androidx.navigation.ui.** { *; }

# ProGuard rules for Places library
-keep class com.google.android.libraries.places.** { *; }
-keepclassmembers class com.google.android.libraries.places.** { *; }

# ProGuard rules for Play Services Location library
-keep class com.google.android.gms.location.** { *; }

# ProGuard rules for ZXing Android Embedded library
-keep class com.google.zxing.** { *; }
-keep class com.journeyapps.** { *; }
-keep class jp.sourceforge.** { *; }
-keepclassmembers class com.google.zxing.** { *; }
-keepclassmembers class com.journeyapps.** { *; }
-keepclassmembers class jp.sourceforge.** { *; }

# ProGuard rule for RecyclerView library
-keep class androidx.recyclerview.widget.** { *; }

# Proguard rules for Retrofit

# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Keep annotation default values (e.g., retrofit2.http.Field.encoded).
-keepattributes AnnotationDefault

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# Keep inherited services.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# R8 full mode strips generic signatures from return types if not kept.
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>

# ProGuard rules for Room library
-keepnames class androidx.room.** { *; }

# If you're using Kotlin, also include the following rule
-keepclassmembers class * implements androidx.room.RoomDatabase {
    public static <fields>;
}

# ProGuard rule for Room KTX library
-keep class androidx.room.** { *; }

# ProGuard rule for Room compiler library
-dontwarn androidx.room.**

# ProGuard rule for OkHttp Logging Interceptor library
-dontwarn okhttp3.logging.**

# ProGuard rule for Paging library
-keep class androidx.paging.** { *; }

# ProGuard rules for Sentry Android SDK
-keep class io.sentry.android.** { *; }
-keep class io.sentry.core.** { *; }

# ProGuard rule for AndroidX Browser library
-keep class androidx.browser.** { *; }

# MPAndroidChart ProGuard rules
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**
-keep class com.github.mikephil.charting.charts.** { *; }
-keep class com.github.mikephil.charting.components.** { *; }
-keep class com.github.mikephil.charting.data.** { *; }
-keep class com.github.mikephil.charting.formatter.** { *; }
-keep class com.github.mikephil.charting.highlight.** { *; }
-keep class com.github.mikephil.charting.interfaces.** { *; }
-keep class com.github.mikephil.charting.listener.** { *; }
-keep class com.github.mikephil.charting.renderer.** { *; }
-keep class com.github.mikephil.charting.utils.** { *; }
-keep class com.github.mikephil.charting.animation.** { *; }
-keep class com.github.mikephil.charting.jobs.** { *; }
-keep class com.github.mikephil.charting.listener.** { *; }

# If you use MPAndroidChart combined with AndroidX, add the following rules:
-keep class androidx.appcompat.widget.** { *; }
-keep class androidx.appcompat.graphics.** { *; }
-keep class androidx.core.widget.** { *; }

# If you use MPAndroidChart combined with Kotlin, add the following rule:
-keep class kotlin.Metadata { *; }

# Keep all classes and methods in the ImagePicker library
-keep class com.github.dhaval2404.imagepicker.** { *; }

# Keep all interfaces and enums in the library
-keepclassmembers class com.github.dhaval2404.imagepicker.** {
    public protected *;
}

# Keep the libraries that ImagePicker depends on
-keep class androidx.appcompat.widget.** { *; }
-keep class androidx.core.content.** { *; }
-keep class androidx.core.view.** { *; }
-keep class androidx.core.widget.** { *; }
-keep class androidx.fragment.app.** { *; }
-keep class androidx.lifecycle.** { *; }
-keep class androidx.recyclerview.widget.** { *; }
-keep class com.yalantis.ucrop.** { *; }

# Keep Glide classes if used for image loading
-keep class com.bumptech.glide.** { *; }
-keepclassmembers class com.bumptech.glide.** {
    *;
}

# Keep OkHttp classes if used for network operations
-keep class okhttp3.** { *; }
-keepclassmembers class okhttp3.** {
    *;
}

# Keep all classes and methods in the PermissionX library
-keep class com.guolindev.permissionx.** { *; }

# Keep the PermissionXActivity class if you're using its default activity
-keep class com.guolindev.permissionx.**$PermissionActivity { *; }

# Keep the permissions callback classes
-keep class com.guolindev.permissionx.**$PermissionCallback { *; }
-keep class com.guolindev.permissionx.**$ExplainReasonCallback { *; }
-keep class com.guolindev.permissionx.**$ForwardToSettingsCallback { *; }
-keep class com.guolindev.permissionx.**$RequestCallback { *; }
-keep class com.guolindev.permissionx.**$RationalCallback { *; }

# Keep the PermissionBuilder and PermissionFragment classes
-keep class com.guolindev.permissionx.**$PermissionBuilder { *; }
-keep class com.guolindev.permissionx.**$PermissionFragment { *; }

# Keep the library's Kotlin extensions
-keep class kotlin.ExtensionFunctionType { *; }

# Keep the Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep the Kotlin standard library classes
-keep class kotlin.** { *; }

# Keep OkHttp classes if used for network operations
-keep class okhttp3.** { *; }
-keepclassmembers class okhttp3.** {
    *;
}

# Keep all classes and methods in the androidx.browser package
-keep class androidx.browser.** { *; }

# Keep the browser classes if you're using CustomTabsIntent or TrustedWebUtils
-keep class androidx.browser.customtabs.** { *; }
-keep class androidx.browser.trusted.** { *; }

# Keep the library's Kotlin extensions
-keep class kotlin.ExtensionFunctionType { *; }

# Keep the Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep the Kotlin standard library classes
-keep class kotlin.** { *; }

# Keep OkHttp classes if used for network operations
-keep class okhttp3.** { *; }
-keepclassmembers class okhttp3.** {
    *;
}
