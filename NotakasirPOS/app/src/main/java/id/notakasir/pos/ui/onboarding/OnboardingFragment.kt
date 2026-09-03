package id.notakasir.pos.ui.onboarding

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import id.notakasir.pos.NotaKasirApp
import id.notakasir.pos.R
import id.notakasir.pos.data.local.SeedData
import id.notakasir.pos.databinding.FragmentFormBinding
import kotlinx.coroutines.launch

/** Wizard 5 langkah: nama toko → pajak → servis → PIN pemilik → selesai+seed. */
class OnboardingFragment : Fragment() {
    private var _b: FragmentFormBinding? = null
    private val b get() = _b!!
    private var step = 0
    private var store = ""; private var tax = 10; private var svc = 5; private var pin = "1234"

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentFormBinding.inflate(i, c, false); return b.root
    }
    override fun onViewCreated(v: View, s: Bundle?) {
        render()
        b.btnSave.setOnClickListener { next() }
    }
    private fun render() {
        when (step) {
            0 -> { b.tvTitle.text = "Langkah 1/5 — Nama toko"; b.etA.hint = "cth: Kopi Senja"; b.etA.visibility = View.VISIBLE; b.etB.visibility = View.GONE; b.etC.visibility = View.GONE }
            1 -> { b.tvTitle.text = "Langkah 2/5 — Pajak %"; b.etB.hint = "cth: 10"; b.etB.visibility = View.VISIBLE; b.etA.visibility = View.GONE; b.etC.visibility = View.GONE }
            2 -> { b.tvTitle.text = "Langkah 3/5 — Servis %"; b.etB.hint = "cth: 5"; b.tvInfo.text = "Kosongkan = 0" }
            3 -> { b.tvTitle.text = "Langkah 4/5 — PIN Pemilik"; b.etC.hint = "4-6 digit"; b.etB.visibility = View.GONE; b.etC.visibility = View.VISIBLE; b.etA.visibility = View.GONE }
            else -> { b.tvTitle.text = "Langkah 5/5 — Siap!"; b.tvInfo.text = "Klik Simpan untuk isi contoh produk & meja."; b.etA.visibility = View.GONE; b.etB.visibility = View.GONE; b.etC.visibility = View.GONE }
        }
    }
    private fun next() {
        when (step) {
            0 -> store = b.etA.text.toString().ifBlank { "Toko Saya" }
            1 -> tax = b.etB.text.toString().toIntOrNull() ?: 10
            2 -> svc = b.etB.text.toString().toIntOrNull() ?: 5
            3 -> pin = b.etC.text.toString().ifBlank { "1234" }
            else -> {
                val prefs = requireContext().getSharedPreferences("nk_setup", 0)
                prefs.edit().putString("store", store).putInt("tax", tax).putInt("svc", svc)
                    .putString("owner_pin", pin).putBoolean("onboard_done", true).apply()
                lifecycleScope.launch {
                    SeedData.run((requireActivity().application as NotaKasirApp).repo.db)
                    findNavController().navigate(R.id.to_pin)
                }
                return
            }
        }
        step++; render()
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
