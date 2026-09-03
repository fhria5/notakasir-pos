package id.notakasir.pos.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import id.notakasir.pos.R
import id.notakasir.pos.databinding.FragmentFormBinding
import id.notakasir.pos.licensing.LicenseManager

class SettingsFragment : Fragment() {
    private var _b: FragmentFormBinding? = null
    private val b get() = _b!!
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentFormBinding.inflate(i, c, false); return b.root
    }
    override fun onViewCreated(v: View, s: Bundle?) {
        val prefs = requireContext().getSharedPreferences("nk_setup", 0)
        b.tvTitle.text = "Pengaturan & Lisensi"
        b.etA.hint = "Kode aktivasi XXXX-XXXX-XXXX-XXXX"
        b.etB.hint = "MAC printer (cth 00:11:22:33:44:55)"
        b.etB.setText(prefs.getString("printer_mac", ""))
        b.etC.hint = "Nama toko"
        b.etC.setText(prefs.getString("store", ""))
        b.tvInfo.text = LicenseManager.statusText(requireContext())
        b.btnSave.text = "Aktivasi + Simpan"
        b.btnSave.setOnClickListener {
            val code = b.etA.text.toString()
            val msg = if (code.isBlank()) "—" else if (LicenseManager.activate(requireContext(), code)) "Aktivasi BERHASIL" else "Kode SALAH (checksum gagal)"
            prefs.edit().putString("printer_mac", b.etB.text.toString().trim())
                .putString("store", b.etC.text.toString().ifBlank { "Toko Saya" }).apply()
            b.tvInfo.text = LicenseManager.statusText(requireContext()) + "\n$msg"
        }
        // tombol navigasi modul lain via long-press info? Sediakan menu sederhana:
        b.tvInfo.setOnLongClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(LicenseManager.sellerWaLink())))
            true
        }
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
