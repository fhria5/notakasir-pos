package id.notakasir.pos.ui.cashier

import androidx.lifecycle.*
import id.notakasir.pos.data.local.entity.Transaction
import id.notakasir.pos.data.repo.AppRepository
import id.notakasir.pos.data.repo.CartLine
import kotlinx.coroutines.launch

/** ViewModel keranjang + checkout bersama antar Cashier/Payment/Receipt. */
class PosViewModel(private val repo: AppRepository) : ViewModel() {
    val cart = mutableListOf<CartLine>()
    val cartTick = MutableLiveData(0)
    var taxPct = 10; var svcPct = 5
    var lastTxn: Transaction? = null
    var lastItems: List<Pair<String, String>> = emptyList()

    val subtotal: Long get() = cart.sumOf { it.lineTotal }
    val tax: Long get() = subtotal * taxPct / 100
    val svc: Long get() = subtotal * svcPct / 100
    val total: Long get() = subtotal + tax + svc

    fun add(line: CartLine) {
        val ex = cart.find { it.productId == line.productId && it.variant == line.variant && it.unitPrice == line.unitPrice }
        if (ex != null) ex.qty += line.qty else cart.add(line)
        cartTick.value = cartTick.value!! + 1
    }
    fun clear() { cart.clear(); cartTick.value = cartTick.value!! + 1 }

    fun checkout(method: String, paid: Long, customerId: Long?, done: (Long) -> Unit) {
        viewModelScope.launch {
            val (t, items) = repo.checkout(cart.toList(), method, paid, taxPct, svcPct, customerId)
            lastTxn = t
            lastItems = items.map { "${it.qty}x ${it.productName}${it.variantName?.let { v -> " ($v)" } ?: ""}" to "${it.qty} x ${it.unitPrice} = ${it.qty * it.unitPrice}" }
            clear(); done(t.id)
        }
    }

    class F(val repo: AppRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(c: Class<T>): T = PosViewModel(repo) as T
    }
}
