# Notakasir POS — ProGuard/R8 (sekaligus obfuscate secret lisensi)
-keep class androidx.room.** { *; }
-keep class id.notakasir.pos.data.local.entity.** { *; }
-keep class id.notakasir.pos.data.local.dao.** { *; }
-keep class com.github.mikephil.charting.** { *; }
# Obfuscate licensing: jangan keep kelas LicenseManager agar nama/method disamarkan
-repackageclasses 'a.b'
-allowaccessmodification
