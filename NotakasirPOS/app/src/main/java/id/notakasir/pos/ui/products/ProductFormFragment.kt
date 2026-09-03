package id.notakasir.pos.ui.products

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import id.notakasir.pos.NotaKasirApp
import id.notakasir.pos.data.local.entity.Product
import id.notakasir.pos.databinding.FragmentFormBinding
import kotlinx.coroutines.launch

class ProductFormFragment : Fragment() {
    private var _b: FragmentFormBinding? = null
    private val b get() = _b!!
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentFormBinding.inflate(i, c, false); return b.root
    }
    override fun onViewCreated(v: View, s: Bundle?) {
        b.tvTitle.text = "Produk baru (kategori default 1)"
        b.etA.hint = "Nama produk"; b.etB.hint = "Harga"; b.etC.hint = "Stok"
        b.btnSave.setOnClickListener {
            val repo = (requireActivity().application as NotaKasirApp).repo
            lifecycleScope.launch {
                repo.products.upsert(Product(name = b.etA.text.toString(),
                    price = b.etB.text.toString().toLongOrNull() ?: 0L,
                    stock = b.etC.text.toString().toIntOrNull() ?: 0))
                findNavController().popBackStack()
            }
        }
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
