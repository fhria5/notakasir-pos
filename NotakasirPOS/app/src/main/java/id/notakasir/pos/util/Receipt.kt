package id.notakasir.pos.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import id.notakasir.pos.data.local.entity.Transaction
import id.notakasir.pos.data.local.dao.TopRow

object ReceiptText {
    fun build(store: String, t: Transaction, items: List<Pair<String, String>>): String {
        val sb = StringBuilder()
        sb.appendLine("      $store")
        sb.appendLine("--------------------------------")
        items.forEach { (n, p) -> sb.appendLine(n); sb.appendLine("  $p") }
        sb.appendLine("--------------------------------")
        sb.appendLine("Subtotal : ${Currency.rp(t.subtotal)}")
        sb.appendLine("Pajak    : ${Currency.rp(t.tax)}")
        sb.appendLine("Servis   : ${Currency.rp(t.service)}")
        sb.appendLine("TOTAL    : ${Currency.rp(t.total)}")
        sb.appendLine("${t.paymentMethod} Bayar ${Currency.rp(t.paidAmount)} Kembali ${Currency.rp(t.changeAmount)}")
        sb.appendLine("Terima kasih! - Notakasir POS")
        return sb.toString()
    }
}

object WaShare {
    fun sendReceipt(ctx: Context, phone: String, text: String) {
        val clean = phone.replace(Regex("[^0-9]"), "").replaceFirst(Regex("^0"), "62")
        val uri = Uri.parse("https://wa.me/$clean?text=" + Uri.encode(text))
        ctx.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
    fun shareText(ctx: Context, text: String) {
        val i = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
        ctx.startActivity(Intent.createChooser(i, "Kirim struk"))
    }
}
