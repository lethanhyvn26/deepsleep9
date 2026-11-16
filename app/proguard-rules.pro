# R8 / ProGuard Configuration for Deep Sleep v3.1 (com.sleepsounds.storytelling)
# Clean configuration for Google AdMob, Google Play Billing, Hilt/Dagger, Room, DataStore, Media3, and Firebase.

-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-renamesourcefileattribute SourceFile

-keep,allowoptimization class * implements androidx.startup.Initializer {
    public <init>();
    <init>();
    *;
}

-keep,allowoptimization class * implements com.google.android.datatransport.runtime.backends.BackendFactory {
    public <init>();
    <init>();
}
-keep,allowoptimization class * extends org.chromium.net.CronetProvider {
    public <init>(android.content.Context);
    <init>(android.content.Context);
}

-keep,allowoptimization class * extends androidx.room.RoomDatabase {
    public <init>();
    <init>();
    *;
}
-keep @androidx.room.Entity class *
-keepnames class com.sleepsounds.storytelling.data.local.entity.** { *; }
-keepclassmembers class com.sleepsounds.storytelling.data.remote.** { *; }

-keep,allowshrinking,allowoptimization,allowobfuscation class * extends androidx.lifecycle.ViewModel {
    public <init>();
    public <init>(android.app.Application);
    public <init>(androidx.lifecycle.SavedStateHandle);
    public <init>(android.app.Application, androidx.lifecycle.SavedStateHandle);
    <init>(...);
}
-keepclassmembers,allowoptimization class * implements androidx.lifecycle.LifecycleObserver {
    <init>(...);
}
-keepclassmembers,allowoptimization class * implements androidx.lifecycle.GeneratedAdapter {
    <init>(...);
}

-keep,allowshrinking,allowoptimization class * implements androidx.savedstate.SavedStateRegistry$AutoRecreated {
    public <init>();
    <init>();
}
-keep,allowshrinking,allowoptimization class * extends androidx.fragment.app.Fragment {
    public <init>();
    <init>();
}
-keep,allowoptimization class * extends android.app.AppComponentFactory {
    public <init>();
    <init>();
}
-keep,allowoptimization class * implements androidx.versionedparcelable.VersionedParcelable {
    public <init>();
    <init>();
}

-keepclassmembers,allowshrinking,allowoptimization,allowobfuscation class * extends com.google.protobuf.GeneratedMessageLite {
    <init>();
    <fields>;
}
-keepclassmembers,allowshrinking,allowoptimization,allowobfuscation class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <init>();
    <fields>;
}

-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepclasseswithmembernames,includedescriptorclasses,allowoptimization class * {
    native <methods>;
}

-dontwarn com.google.android.gms.ads.**
-dontwarn com.google.android.ump.**
-dontwarn com.google.protobuf.**
-dontwarn com.google.common.**
-dontwarn org.chromium.**
-dontwarn kotlinx.coroutines.**
