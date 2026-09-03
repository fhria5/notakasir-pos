package id.notakasir.pos.util

import java.text.NumberFormat
import java.util.Locale

object Currency {
    private val nf = NumberFormat.getInstance(Locale("in", "ID"))
    fun rp(v: Long): String = "Rp" + nf.format(v)
}

object PinHash {
    fun hash(pin: String): String {
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val salted = "notakasir#$pin#umkm"
        return md.digest(salted.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}

object DayRange {
    fun today(): Pair<Long, Long> {
        val c = java.util.Calendar.getInstance()
        c.set(java.util.Calendar.HOUR_OF_DAY, 0); c.set(java.util.Calendar.MINUTE, 0)
        c.set(java.util.Calendar.SECOND, 0); c.set(java.util.Calendar.MILLISECOND, 0)
        val a = c.timeInMillis; c.add(java.util.Calendar.DAY_OF_MONTH, 1)
        return a to c.timeInMillis - 1
    }
}
