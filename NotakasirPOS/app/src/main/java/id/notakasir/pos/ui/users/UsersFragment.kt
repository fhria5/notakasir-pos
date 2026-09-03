package id.notakasir.pos.ui.users

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import id.notakasir.pos.NotaKasirApp
import id.notakasir.pos.data.local.entity.User
import id.notakasir.pos.databinding.FragmentListBinding
import id.notakasir.pos.databinding.ItemProductBinding
import id.notakasir.pos.util.PinHash
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UsersFragment : Fragment() {
    private var _b: FragmentListBinding? = null
    private val b get() = _b!!
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentListBinding.inflate(i, c, false); return b.root
    }
    override fun onViewCreated(v: View, s: Bundle?) {
        val repo = (requireActivity().application as NotaKasirApp).repo
        val role = requireContext().getSharedPreferences("nk_setup", 0).getString("role", "STAF")
        b.tvTitle.text = "Pengguna & PIN"
        b.tvSub.text = if (role != "PEMILIK") "Hanya Pemilik yang bisa ubah (mode baca)." else "Tambah staf: format Nama/PIN, tahan untuk hapus."
        b.btnAdd.text = "Tambah Staf (Nama/PIN)"
        b.btnAdd.isEnabled = role == "PEMILIK"
        b.btnAdd.setOnClickListener {
            lifecycleScope.launch { repo.users.upsert(User(name = "Staf-${System.currentTimeMillis() % 1000}", role = "STAF", pinHash = PinHash.hash("0000"))) }
        }
        lifecycleScope.launch {
            repo.users.all().collectLatest { list ->
                b.rvList.adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<H>() {
                    override fun onCreateViewHolder(p: ViewGroup, v: Int) = H(ItemProductBinding.inflate(layoutInflater, p, false))
                    override fun getItemCount() = list.size
                    override fun onBindViewHolder(h: H, i: Int) {
                        h.x.tvName.text = list[i].name; h.x.tvPrice.text = list[i].role; h.x.tvStock.text = ""
                        h.x.root.setOnLongClickListener {
                            if (role == "PEMILIK") lifecycleScope.launch { repo.users.delete(list[i]) }
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
