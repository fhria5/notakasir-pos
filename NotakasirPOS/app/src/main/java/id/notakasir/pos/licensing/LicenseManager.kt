package id.notakasir.pos.licensing

import android.content.Context
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Lisensi offline HMAC-SHA256.
 * Format kode: XXXX-XXXX-XXXX-XXXX (16 char Base32-Crockford tanpa I,L,O,U).
 * 12 char pertama = payload (deviceId 4 + expiry 8 epoch-day hex), 4 char = checksum HMAC.
 * Secret ditanam di kode & diobfuscate R8 saat release.
 */
object LicenseManager {
    private const val SECRET = "N0t4k4s1r-s3cr3t-2026-umkm"
    private const val ALPH = "ABCDEFGHJKMNPQRSTVWXYZ23456789"
    private const val SELLER_WA = "6281234567890"
    const val TRIAL_DAYS = 14L

    private fun prefs(ctx: Context) = ctx.getSharedPreferences("nk_license", Context.MODE_PRIVATE)

    fun firstLaunch(ctx: Context): Long {
        val p = prefs(ctx)
        var t = p.getLong("first_launch", 0L)
        if (t == 0L) { t = System.currentTimeMillis(); p.edit().putLong("first_launch", t).apply() }
        return t
    }

    fun trialDaysLeft(ctx: Context): Long {
        val elapsed = (System.currentTimeMillis() - firstLaunch(ctx)) / 86_400_000L
        return (TRIAL_DAYS - elapsed).coerceAtLeast(0)
    }

    fun isLicensed(ctx: Context): Boolean {
        val p = prefs(ctx)
        if (p.getBoolean("licensed", false)) return true
        return trialDaysLeft(ctx) > 0
    }

    fun statusText(ctx: Context): String {
        val p = prefs(ctx)
        if (p.getBoolean("licensed", false)) return "LISENSI AKTIF • " + p.getString("code", "-")
        val left = trialDaysLeft(ctx)
        return if (left > 0) "TRIAL • sisa $left hari" else "TRIAL HABIS • butuh kode aktivasi"
    }

    fun activate(ctx: Context, rawCode: String): Boolean {
        val code = rawCode.trim().uppercase().replace("-", "")
        if (code.length != 16 || !code.all { it in ALPH }) return false
        val payload = code.substring(0, 12)
        val check = code.substring(12)
        if (checksum(payload) != check) return false
        prefs(ctx).edit().putBoolean("licensed", true).putString("code", formatted(code)).apply()
        return true
    }

    fun sellerWaLink(): String = "https://wa.me/$SELLER_WA?text=" +
        java.net.URLEncoder.encode("Halo, saya mau beli kode aktivasi Notakasir POS.", "UTF-8")

    internal fun checksum(payload: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(SECRET.toByteArray(), "HmacSHA256"))
        val h = mac.doFinal(payload.toByteArray())
        // ambil 20 bit -> 4 char base32 custom
        var acc = ((h[0].toInt() and 0xFF) shl 24) or ((h[1].toInt() and 0xFF) shl 16) or ((h[2].toInt() and 0xFF) shl 8) or (h[3].toInt() and 0xFF)
        if (acc < 0) acc = -acc
        return (0 until 4).map { ALPH[(acc shr (it * 5)) and 31] }.joinToString("")
    }

    private fun formatted(code: String) = code.chunked(4).joinToString("-")

    /** Dipakai penjual: generate kode untuk device tertentu (CLI di tools/). */
    fun generate(device4: String, expiryEpochDay: Long): String {
        val payload = (device4.uppercase().take(4).padEnd(4, 'A') + expiryEpochDay.toString(16).uppercase().padStart(8, '0')).take(12)
        val clean = payload.map { if (it in ALPH) it else 'A' }.joinToString("")
        return (clean + checksum(clean)).chunked(4).joinToString("-")
    }
}
