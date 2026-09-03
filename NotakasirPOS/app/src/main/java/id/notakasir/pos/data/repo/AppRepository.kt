package id.notakasir.pos.data.repo

import android.content.Context
import id.notakasir.pos.data.local.AppDatabase
import id.notakasir.pos.data.local.entity.*
import id.notakasir.pos.util.DayRange
import kotlinx.coroutines.flow.Flow

/** Repository tunggal di atas semua DAO (pola Repository, tanpa DI berat agar ringan di JStudio). */
class AppRepository(ctx: Context) {
    val db = AppDatabase.get(ctx)
    val cats = db.categoryDao()
    val products = db.productDao()
    val variants = db.variantDao()
    val customers = db.customerDao()
    val tables = db.tableDao()
    val bills = db.billDao()
    val txn = db.txnDao()
    val shifts = db.shiftDao()
    val users = db.userDao()

    fun searchProducts(q: String, cat: Long): Flow<List<Product>> = products.search(q, cat)
    suspend fun todaySummary(): TodaySummary {
        val (a, b) = DayRange.today()
        val ts = txn.sumTaxService(a, b)
        return TodaySummary(
            omzet = txn.sumTotal(a, b), count = txn.countTxn(a, b),
            cash = txn.sumByMethod("CASH", a, b), qris = txn.sumByMethod("QRIS", a, b),
            tax = ts.tax, service = ts.service, top = txn.topProducts(a, b)
        )
    }
    suspend fun checkout(
        lines: List<CartLine>, method: String, paid: Long,
        taxPct: Int, svcPct: Int, customerId: Long?
    ): Pair<Transaction, List<TransactionItem>> {
        val sub = lines.sumOf { it.unitPrice * it.qty }
        val tax = sub * taxPct / 100
        val svc = sub * svcPct / 100
        val total = sub + tax + svc
        val t = Transaction(total = total, subtotal = sub, tax = tax, service = svc,
            paymentMethod = method, paidAmount = paid, changeAmount = (paid - total).coerceAtLeast(0),
            customerId = customerId)
        val id = txn.insert(t)
        val items = lines.map {
            TransactionItem(transactionId = id, productName = it.name, variantName = it.variant, qty = it.qty, unitPrice = it.unitPrice)
        }
        txn.insertItems(items)
        lines.forEach { products.decStock(it.productId, it.qty) }
        if (customerId != null) customers.addPoints(customerId, (total / 10000).toInt())
        return t.copy(id = id) to items
    }
}

data class CartLine(
    val productId: Long, val name: String, val variant: String? = null,
    val unitPrice: Long, var qty: Int = 1
) { val lineTotal: Long get() = unitPrice * qty }

data class TodaySummary(
    val omzet: Long, val count: Int, val cash: Long, val qris: Long,
    val tax: Long, val service: Long,
    val top: List<id.notakasir.pos.data.local.dao.TopRow>
)
