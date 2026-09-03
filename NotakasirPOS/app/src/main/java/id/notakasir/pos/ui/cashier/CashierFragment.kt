package id.notakasir.pos.ui.cashier

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import id.notakasir.pos.NotaKasirApp
import id.notakasir.pos.R
import id.notakasir.pos.data.local.entity.Category
import id.notakasir.pos.data.local.entity.Product
import id.notakasir.pos.data.repo.CartLine
import id.notakasir.pos.databinding.FragmentCashierBinding
import id.notakasir.pos.databinding.ItemCategoryBinding
import id.notakasir.pos.databinding.ItemProductBinding
import id.notakasir.pos.util.Currency
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CashierFragment : Fragment() {
    private var _b: FragmentCashierBinding? = null
    private val b get() = _b!!
    private val vm: PosViewModel by activityViewModels { PosViewModel.F((requireActivity().application as NotaKasirApp).repo) }
    private var catId = 0L; private var query = ""
    private var job: Job? = null
    private val scan = registerForActivityResult(ScanContract()) { r ->
        if (r.contents != null) { b.etSearch.setText(r.contents); query = r.contents; observe() }
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentCashierBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        val repo = (requireActivity().application as NotaKasirApp).repo
        b.rvCategory.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        b.rvProduct.layoutManager = GridLayoutManager(context, 2)
        lifecycleScope.launch {
            repo.cats.all().collectLatest { list ->
                val all = listOf(Category(0, "Semua")) + list
                b.rvCategory.adapter = CatAdapter(all) { catId = it.id; observe() }
            }
        }
        b.btnScan.setOnClickListener {
            try { scan.launch(ScanOptions().setPrompt("Arahkan ke barcode").setBeepEnabled(true)) }
            catch (e: Exception) { Toast.makeText(context, "Scanner belum siap", Toast.LENGTH_SHORT).show() }
        }
        b.etSearch.setOnEditorActionListener { tv, _, _ -> query = tv.text.toString(); observe(); true }
        b.btnPay.setOnClickListener {
            if (vm.cart.isEmpty()) { Toast.makeText(context, "Keranjang kosong", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            findNavController().navigate(R.id.to_payment)
        }
        b.btnTables.setOnClickListener { findNavController().navigate(R.id.to_tables) }
        vm.cartTick.observe(viewLifecycleOwner) {
            b.tvTotals.text = "Subtotal ${Currency.rp(vm.subtotal)} • Total ${Currency.rp(vm.total)} (${vm.cart.sumOf { it.qty }} item)"
        }
        // pajak/servis dari setup toko
        val prefs = requireContext().getSharedPreferences("nk_setup", 0)
        vm.taxPct = prefs.getInt("tax", 10); vm.svcPct = prefs.getInt("svc", 5)
        b.tvStore.text = prefs.getString("store", "Toko Saya")
        observe()
    }

    private fun observe() {
        job?.cancel()
        val repo = (requireActivity().application as NotaKasirApp).repo
        job = lifecycleScope.launch {
            repo.searchProducts(query, catId).collectLatest { list ->
                b.emptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                b.rvProduct.adapter = ProdAdapter(list) { p -> onPick(p) }
            }
        }
    }

    private fun onPick(p: Product) {
        val repo = (requireActivity().application as NotaKasirApp).repo
        lifecycleScope.launch {
            val vars = repo.variants.byProduct(p.id)
            if (vars.isEmpty()) vm.add(CartLine(p.id, p.name, null, p.price))
            else {
                val names = vars.map { "${it.name} (+${Currency.rp(it.extraPrice)})" }.toTypedArray()
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Pilih varian: ${p.name}")
                    .setItems(names) { _, w ->
                        val vv = vars[w]
                        vm.add(CartLine(p.id, p.name, vv.name, p.price + vv.extraPrice))
                    }.show()
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }

    inner class ProdAdapter(val l: List<Product>, val cb: (Product) -> Unit) :
        androidx.recyclerview.widget.RecyclerView.Adapter<ProdAdapter.H>() {
        inner class H(val x: ItemProductBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(x.root)
        override fun onCreateViewHolder(p: ViewGroup, v: Int) = H(ItemProductBinding.inflate(layoutInflater, p, false))
        override fun getItemCount() = l.size
        override fun onBindViewHolder(h: H, i: Int) {
            val p = l[i]
            h.x.tvName.text = p.name; h.x.tvPrice.text = Currency.rp(p.price)
            h.x.tvStock.text = if (p.stock <= 5) "Stok menipis: ${p.stock}" else "Stok ${p.stock}"
            h.x.root.setOnClickListener { cb(p) }
        }
    }
    inner class CatAdapter(val l: List<Category>, val cb: (Category) -> Unit) :
        androidx.recyclerview.widget.RecyclerView.Adapter<CatAdapter.H>() {
        inner class H(val x: ItemCategoryBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(x.root)
        override fun onCreateViewHolder(p: ViewGroup, v: Int) = H(ItemCategoryBinding.inflate(layoutInflater, p, false))
        override fun getItemCount() = l.size
        override fun onBindViewHolder(h: H, i: Int) {
            h.x.tvCat.text = l[i].name
            h.x.root.setOnClickListener { cb(l[i]) }
        }
    }
}
