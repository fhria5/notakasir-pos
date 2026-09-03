package id.notakasir.pos.ui.shift

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import id.notakasir.pos.NotaKasirApp
import id.notakasir.pos.data.local.entity.CashShift
import id.notakasir.pos.databinding.FragmentFormBinding
import id.notakasir.pos.util.Currency
import kotlinx.coroutines.launch

class ShiftFragment : Fragment() {
    private var _b: FragmentFormBinding? = null
    private val b get() = _b!!
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentFormBinding.inflate(i, c, false); return b.root
    }
    override fun onViewCreated(v: View, s: Bundle?) {
        val repo = (requireActivity().application as NotaKasirApp).repo
        val prefs = requireContext().getSharedPreferences("nk_setup", 0)
        lifecycleScope.launch {
            val last = repo.shifts.last()
            val open = last != null && last.closedAt == null
            b.tvTitle.text = if (open) "Shift berjalan — tutup shift" else "Buka shift"
            b.etA.hint = if (open) "Kas fisik akhir (Rp)" else "Modal awal (Rp)"
            b.etB.visibility = View.GONE; b.etC.visibility = View.GONE
            b.tvInfo.text = if (open) "Dibuka ${java.util.Date(last!!.openedAt)} • modal ${Currency.rp(last.openingCash)}" else "Catat modal awal laci kas."
            b.btnSave.text = if (open) "Tutup Shift" else "Buka Shift"
            b.btnSave.setOnClickListener {
                lifecycleScope.launch {
                    val v = b.etA.text.toString().toLongOrNull() ?: 0L
                    if (!open) {
                        repo.shifts.upsert(CashShift(openingCash = v, openedBy = prefs.getString("user", "Kasir") ?: "Kasir"))
                    } else {
                        val (a, z) = id.notakasir.pos.util.DayRange.today()
                        val cash = repo.txn.sumByMethod("CASH", a, z)
                        val expected = last!!.openingCash + cash
                        val diff = v - expected
                        repo.shifts.upsert(last.copy(closingCashActual = v, closedAt = System.currentTimeMillis()))
                        b.tvInfo.text = "Ekspektasi ${Currency.rp(expected)} • Selisih ${Currency.rp(diff)}" + if (diff != 0L) " (SELISIH!)" else " (COCOK)"
                        return@launch
                    }
                    activity?.recreate()
                }
            }
        }
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
