// Generator kode lisensi Notakasir — dijalankan di PC penjual.
// Cara pakai: kotlinc LicenseGenerator.kt -include-runtime -d gen.jar && java -jar gen.jar AB12 3650
//   arg1 = 4 char id device/pembeli (mis. inisial + angka), arg2 = hari kedaluwarsa epoch-day (opsional, default 3650 ~ 10 thn).
// Rumus HARUS sama dengan LicenseManager.checksum di aplikasi.

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

const val SECRET = "N0t4k4s1r-s3cr3t-2026-umkm"
const val ALPH = "ABCDEFGHJKMNPQRSTVWXYZ23456789"

fun checksum(payload: String): String {
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(SecretKeySpec(SECRET.toByteArray(), "HmacSHA256"))
    val h = mac.doFinal(payload.toByteArray())
    var acc = ((h[0].toInt() and 0xFF) shl 24) or ((h[1].toInt() and 0xFF) shl 16) or ((h[2].toInt() and 0xFF) shl 8) or (h[3].toInt() and 0xFF)
    if (acc < 0) acc = -acc
    return (0 until 4).map { ALPH[(acc shr (it * 5)) and 31] }.joinToString("")
}

fun main(args: Array<String>) {
    val dev = (args.getOrNull(0) ?: "AB12").uppercase().take(4).padEnd(4, 'A')
    val exp = args.getOrNull(1)?.toLongOrNull() ?: 3650L
    val payload = (dev + exp.toString(16).uppercase().padStart(8, '0')).take(12)
        .map { if (it in ALPH) it else 'A' }.joinToString("")
    val code = (payload + checksum(payload)).chunked(4).joinToString("-")
    println(code)
}
