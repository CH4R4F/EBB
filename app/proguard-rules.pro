# Ebb release (R8)

# --- Hilt / Dagger ---
-dontwarn dagger.**
-keepclasseswithmembers class * {
    @dagger.* <methods>;
}
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel
-keep class dagger.hilt.internal.aggregatedroot.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keepclasseswithmembers class * {
    @dagger.assisted.AssistedInject <init>(...);
}
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keep @androidx.hilt.work.HiltWorker class * extends androidx.work.Worker

# --- Room ---
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# --- Compose (runtime stability) ---
-keep class androidx.compose.runtime.Composer { *; }
-keep class androidx.compose.ui.platform.** { *; }
-dontwarn androidx.compose.**

# --- WorkManager ---
-keep class * extends androidx.work.ListenableWorker { *; }
-keep class androidx.work.impl.** { *; }

# --- Application ---
-keep class app.ebbplanner.EbbApplication { *; }
-keep class app.ebbplanner.** { *; }