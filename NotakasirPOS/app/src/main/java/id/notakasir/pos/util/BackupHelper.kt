package id.notakasir.pos.util

import android.content.Context
import android.net.Uri
import id.notakasir.pos.data.local.AppDatabase
import java.io.FileInputStream
import java.io.FileOutputStream

/** Backup/restore file .db via Storage Access Framework (SAF). */
object BackupHelper {
    fun export(ctx: Context, uri: Uri): Boolean = try {
        AppDatabase.get(ctx).close()
        ctx.contentResolver.openOutputStream(uri)?.use { out ->
            FileInputStream(AppDatabase.dbFile(ctx)).copyTo(out)
        }
        true
    } catch (e: Exception) { false }

    fun importDb(ctx: Context, uri: Uri): Boolean = try {
        AppDatabase.get(ctx).close()
        ctx.contentResolver.openInputStream(uri)?.use { ins ->
            FileOutputStream(AppDatabase.dbFile(ctx)).use { ins.copyTo(it) }
        }
        true
    } catch (e: Exception) { false }
}
