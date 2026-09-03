package id.notakasir.pos.ui.analytics

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.data.*
import id.notakasir.pos.NotaKasirApp
import id.notakasir.pos.databinding.FragmentReportBinding
import kotlinx.coroutines.launch
import java.util.Calendar

class AnalyticsFragment : Fragment() {
    private var _b: FragmentReportBinding? = null
    private val b get() = _b!!
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentReportBinding.inflate(i, c, false); return b.root
    }
    override fun onViewCreated(v: View, s: Bundle?) {
        val repo = (requireActivity().application as NotaKasirApp).repo
        b.tvTitle.text = "Analitik 7 Hari"
        b.btnBackup.visibility = View.GONE; b.btnRestore.visibility = View.GONE
        lifecycleScope.launch {
            val cal = Calendar.getInstance()
            val entries = mutableListOf<BarEntry>()
            repeat(7) { i ->
                cal.timeInMillis = System.currentTimeMillis() - (6 - i) * 86_400_000L
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
                val a = cal.timeInMillis; val bb = a + 86_400_000L - 1
                entries.add(BarEntry(i.toFloat(), repo.txn.sumTotal(a, bb).toFloat()))
            }
            b.tvSummary.text = "Tren omzet 7 hari terakhir (jam ramai lihat pola harian)."
            b.tvTop.text = ""
            b.chart.data = BarData(BarDataSet(entries, "Omzet/hari"))
            b.chart.invalidate()
        }
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
