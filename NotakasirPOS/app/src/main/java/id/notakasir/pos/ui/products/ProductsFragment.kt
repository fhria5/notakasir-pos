package id.notakasir.pos.ui.products

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import id.notakasir.pos.NotaKasirApp
import id.notakasir.pos.R
import id.notakasir.pos.databinding.FragmentListBinding
import id.notakasir.pos.databinding.ItemProductBinding
import id.notakasir.pos.util.Currency
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProductsFragment : Fragment() {
    private var _b: FragmentListBinding? = null
    private val b get() = _b!!
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentListBinding.inflate(i, c, false); return b.root
    }
    override fun onViewCreated(v: View, s: Bundle?) {
        val repo = (requireActivity().application as NotaKasirApp).repo
        b.tvTitle.text = "Produk & Stok"
        lifecycleScope.launch {
            repo.lowStockToast()
            repo.searchProducts("", 0).collectLatest { list ->
                b.tvSub.text = "${list.size} produk • stok menipis ≤5 ditandai"
                b.rvList.adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<H>() {
                    override fun onCreateViewHolder(p: ViewGroup, v: Int) = H(ItemProductBinding.inflate(layoutInflater, p, false))
                    override fun getItemCount() = list.size
                    override fun onBindViewHolder(h: H, i: Int) {
                        val p = list[i]
                        h.x.tvName.text = p.name; h.x.tvPrice.text = Currency.rp(p.price)
                        h.x.tvStock.text = "Stok ${p.stock} • tap: +1 stok, tahan: hapus"
                        h.x.root.setOnClickListener { lifecycleScope.launch { repo.products.setStock(p.id, p.stock + 1) } }
                        h.x.root.setOnLongClickListener { lifecycleScope.launch { repo.products.delete(p) }; true }
                    }
                }
            }
        }
        b.btnAdd.text = "Tambah Produk"
        b.btnAdd.setOnClickListener { findNavController().navigate(R.id.productFormFragment) }
    }
    private suspend fun id.notakasir.pos.data.repo.AppRepository.lowStockToast() {
        val low = products.lowStock(5)
        if (low.isNotEmpty()) Toast.makeText(requireContext(), "Stok menipis: ${low.take(3).joinToString { it.name }}", Toast.LENGTH_LONG).show()
    }
    inner class H(val x: ItemProductBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(x.root)
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
