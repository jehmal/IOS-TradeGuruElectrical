# TradeGuru Electrical ProGuard Rules

# Keep Gson model classes
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.tradeguru.electrical.models.** { *; }
-keep class com.tradeguru.electrical.data.db.entities.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
