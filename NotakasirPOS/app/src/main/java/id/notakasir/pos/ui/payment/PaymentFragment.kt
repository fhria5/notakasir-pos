package id.notakasir.pos.ui.payment

import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import id.notakasir.pos.NotaKasirApp
import id.notakasir.pos.R
import id.notakasir.pos.databinding.FragmentPaymentBinding
import id.notakasir.pos.ui.cashier.PosViewModel
import id.notakasir.pos.util.Currency

class PaymentFragment : Fragment() {
    private var _b: FragmentPaymentBinding? = null
    private val b get() = _b!!
    private val vm: PosViewModel by activityViewModels { PosViewModel.F((requireActivity().application as NotaKasirApp).repo) }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentPaymentBinding.inflate(i, c, false); return b.root
    }
    override fun onViewCreated(v: View, s: Bundle?) {
        b.tvTotal.text = "Total ${Currency.rp(vm.total)} (tunai/QRIS)"
        val nums = listOf(0L, 10000L, 20000L, 50000L, 100000L, vm.total)
        b.gridNominal.removeAllViews()
        nums.distinct().forEach { n ->
            val btn = Button(context).apply { text = if (n == 0L) "Uang pas" else Currency.rp(n) }
            btn.setOnClickListener { b.etPaid.setText(if (n == 0L) vm.total.toString() else n.toString()); calc() }
            b.gridNominal.addView(btn)
        }
        b.etPaid.setText(vm.total.toString())
        b.etPaid.setOnEditorActionListener { _, _, _ -> calc(); true }
        b.btnConfirm.setOnClickListener {
            val method = if (b.rbQris.isChecked) "QRIS" else "CASH"
            val paid = b.etPaid.text.toString().toLongOrNull() ?: 0L
            if (method == "CASH" && paid < vm.total) { b.tvChange.text = "Nominal kurang!"; return@setOnClickListener }
            val finalPaid = if (method == "QRIS") vm.total else paid
            vm.checkout(method, finalPaid, null) { findNavController().navigate(R.id.to_receipt) }
        }
        calc()
    }
    private fun calc() {
        val paid = b.etPaid.text.toString().toLongOrNull() ?: 0L
        b.tvChange.text = "Kembalian ${Currency.rp((paid - vm.total).coerceAtLeast(0))}"
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
