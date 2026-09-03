package id.notakasir.pos.ui.lock

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import id.notakasir.pos.NotaKasirApp
import id.notakasir.pos.R
import id.notakasir.pos.databinding.FragmentFormBinding
import id.notakasir.pos.util.PinHash
import kotlinx.coroutines.launch

class PinLockFragment : Fragment() {
    private var _b: FragmentFormBinding? = null
    private val b get() = _b!!
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentFormBinding.inflate(i, c, false); return b.root
    }
    override fun onViewCreated(v: View, s: Bundle?) {
        b.tvTitle.text = "Masuk dengan PIN"
        b.etA.visibility = View.GONE; b.etB.visibility = View.GONE
        b.etC.hint = "PIN (Pemilik 1234 / Kasir 0000)"
        b.btnSave.text = "Buka"
        b.btnSave.setOnClickListener {
            lifecycleScope.launch {
                val repo = (requireActivity().application as NotaKasirApp).repo
                val u = repo.users.byPin(PinHash.hash(b.etC.text.toString()))
                if (u != null) {
                    requireContext().getSharedPreferences("nk_setup", 0).edit()
                        .putBoolean("unlocked", true).putString("role", u.role).putString("user", u.name).apply()
                    findNavController().navigate(R.id.to_cashier)
                } else b.tvInfo.text = "PIN salah"
            }
        }
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
