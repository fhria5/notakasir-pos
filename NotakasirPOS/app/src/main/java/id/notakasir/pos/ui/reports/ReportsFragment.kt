package id.notakasir.pos.ui.reports

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import id.notakasir.pos.NotaKasirApp
import id.notakasir.pos.databinding.FragmentReportBinding
import id.notakasir.pos.util.BackupHelper
import id.notakasir.pos.util.Currency
import kotlinx.coroutines.launch

class ReportsFragment : Fragment() {
    private var _b: FragmentReportBinding? = null
    private val b get() = _b!!
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentReportBinding.inflate(i, c, false); return b.root
    }
    override fun onViewCreated(v: View, s: Bundle?) {
        val repo = (requireActivity().application as NotaKasirApp).repo
        lifecycleScope.launch {
            val s = repo.todaySummary()
            b.tvTitle.text = "Laporan Harian"
            b.tvSummary.text = "Omzet ${Currency.rp(s.omzet)} • ${s.count} trx\nTunai ${Currency.rp(s.cash)} • QRIS ${Currency.rp(s.qris)}\nPajak ${Currency.rp(s.tax)} • Servis ${Currency.rp(s.service)}"
            b.tvTop.text = "Terlaris:\n" + s.top.joinToString("\n") { "• ${it.productName} x${it.q} (${Currency.rp(it.omzet)})" }.ifBlank { "-" }
            val entries = listOf(BarEntry(0f, s.cash.toFloat()), BarEntry(1f, s.qris.toFloat()))
            b.chart.data = BarData(BarDataSet(entries, "Tunai vs QRIS"))
            b.chart.invalidate()
        }
        b.btnBackup.setOnClickListener {
            startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).setType("application/octet-stream").putExtra(Intent.EXTRA_TITLE, "notakasir-backup.db"), 11)
        }
        b.btnRestore.setOnClickListener {
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).setType("application/octet-stream"), 12)
        }
    }
    @Deprecated("saf")
    override fun onActivityResult(rc: Int, res: Int, d: Intent?) {
        super.onActivityResult(rc, res, d)
        if (res != Activity.RESULT_OK || d?.data == null) return
        lifecycleScope.launch {
            if (rc == 11) BackupHelper.export(requireContext(), d.data!!)
            else BackupHelper.importDb(requireContext(), d.data!!)
        }
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
