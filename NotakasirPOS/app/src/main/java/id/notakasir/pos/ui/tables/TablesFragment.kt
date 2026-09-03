package id.notakasir.pos.ui.tables

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import id.notakasir.pos.NotaKasirApp
import id.notakasir.pos.data.local.entity.RestaurantTable
import id.notakasir.pos.databinding.FragmentListBinding
import id.notakasir.pos.databinding.ItemProductBinding
import id.notakasir.pos.util.Currency
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TablesFragment : Fragment() {
    private var _b: FragmentListBinding? = null
    private val b get() = _b!!
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentListBinding.inflate(i, c, false); return b.root
    }
    override fun onViewCreated(v: View, s: Bundle?) {
        val repo = (requireActivity().application as NotaKasirApp).repo
        b.tvTitle.text = "Meja (ketuk = toggle isi/kosong)"
        b.tvSub.text = "Indoor & Outdoor • pisah/gabung bill via Bill"
        b.btnAdd.text = "Tambah Meja"
        b.btnAdd.setOnClickListener {
            lifecycleScope.launch {
                val n = (repo.tables.allOnce().size + 1)
                repo.tables.upsert(RestaurantTable(code = "M$n", area = if (n % 2 == 0) "Outdoor" else "Indoor"))
            }
        }
        lifecycleScope.launch {
            repo.tables.all().collectLatest { list ->
                b.rvList.adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<H>() {
                    override fun onCreateViewHolder(p: ViewGroup, v: Int) = H(ItemProductBinding.inflate(layoutInflater, p, false))
                    override fun getItemCount() = list.size
                    override fun onBindViewHolder(h: H, i: Int) {
                        val t = list[i]
                        h.x.tvName.text = "${t.code} • ${t.area}"
                        h.x.tvPrice.text = t.status
                        h.x.tvStock.text = ""
                        h.x.root.setOnClickListener {
                            lifecycleScope.launch { repo.tables.setStatus(t.id, if (t.status == "EMPTY") "OCCUPIED" else "EMPTY") }
                        }
                    }
                }
            }
        }
    }
    inner class H(val x: ItemProductBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(x.root)
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
