package id.notakasir.pos.ui.customers

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import id.notakasir.pos.NotaKasirApp
import id.notakasir.pos.data.local.entity.Customer
import id.notakasir.pos.databinding.FragmentListBinding
import id.notakasir.pos.databinding.ItemProductBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CustomersFragment : Fragment() {
    private var _b: FragmentListBinding? = null
    private val b get() = _b!!
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentListBinding.inflate(i, c, false); return b.root
    }
    override fun onViewCreated(v: View, s: Bundle?) {
        val repo = (requireActivity().application as NotaKasirApp).repo
        b.tvTitle.text = "Pelanggan & Poin"
        b.tvSub.text = "Poin +1 tiap Rp10rb. Tahan item untuk kirim kupon via WA."
        b.btnAdd.text = "Tambah Pelanggan"
        b.btnAdd.setOnClickListener {
            lifecycleScope.launch { repo.customers.upsert(Customer(name = "Member ${System.currentTimeMillis() % 10000}")) }
        }
        lifecycleScope.launch {
            repo.customers.all().collectLatest { list ->
                b.rvList.adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<H>() {
                    override fun onCreateViewHolder(p: ViewGroup, v: Int) = H(ItemProductBinding.inflate(layoutInflater, p, false))
                    override fun getItemCount() = list.size
                    override fun onBindViewHolder(h: H, i: Int) {
                        val c = list[i]
                        h.x.tvName.text = c.name; h.x.tvPrice.text = "${c.loyaltyPoints} poin"; h.x.tvStock.text = c.phone
                        h.x.root.setOnLongClickListener {
                            id.notakasir.pos.util.WaShare.shareText(requireContext(), "Halo ${c.name}, kupon member Notakasir: diskon Rp5rb untuk ${c.loyaltyPoints} poin kamu!")
                            true
                        }
                    }
                }
            }
        }
    }
    inner class H(val x: ItemProductBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(x.root)
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
