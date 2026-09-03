package id.notakasir.pos.ui.receipt

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import id.notakasir.pos.NotaKasirApp
import id.notakasir.pos.R
import id.notakasir.pos.databinding.FragmentReceiptBinding
import id.notakasir.pos.printer.EscPosPrinter
import id.notakasir.pos.ui.cashier.PosViewModel
import id.notakasir.pos.util.Currency
import id.notakasir.pos.util.ReceiptText
import id.notakasir.pos.util.WaShare
import kotlinx.coroutines.*

class ReceiptFragment : Fragment() {
    private var _b: FragmentReceiptBinding? = null
    private val b get() = _b!!
    private val vm: PosViewModel by activityViewModels { PosViewModel.F((requireActivity().application as NotaKasirApp).repo) }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentReceiptBinding.inflate(i, c, false); return b.root
    }
    override fun onViewCreated(v: View, s: Bundle?) {
        val t = vm.lastTxn
        if (t == null) { findNavController().popBackStack(); return }
        val prefs = requireContext().getSharedPreferences("nk_setup", 0)
        val store = prefs.getString("store", "Toko Saya") ?: "Toko Saya"
        val text = ReceiptText.build(store, t, vm.lastItems)
        b.tvReceipt.text = text
        b.btnPrint.setOnClickListener { doPrint(text, kitchen = false) }
        b.btnKitchen.setOnClickListener { doPrint(text, kitchen = true) }
        b.btnWa.setOnClickListener { WaShare.shareText(requireContext(), text) }
        b.btnDone.setOnClickListener { findNavController().navigate(R.id.to_cashier_home) }
    }
    private fun doPrint(text: String, kitchen: Boolean) {
        val prefs = requireContext().getSharedPreferences("nk_setup", 0)
        val mac = prefs.getString("printer_mac", "") ?: ""
        if (mac.isBlank()) { Toast.makeText(context, "Isi MAC printer di Pengaturan dulu", Toast.LENGTH_LONG).show(); return }
        val lines = (if (kitchen) listOf("** TIKET DAPUR **") else listOf("*** $text ***")).let {
            if (kitchen) listOf("TIKET DAPUR", text) else text.split("\n")
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val ad = BluetoothAdapter.getDefaultAdapter()
                EscPosPrinter.print(ad, mac, EscPosPrinter.buildReceipt(lines))
                withContext(Dispatchers.Main) { Toast.makeText(context, "Terkirim ke printer", Toast.LENGTH_SHORT).show() }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(context, "Gagal cetak: ${e.message}", Toast.LENGTH_LONG).show() }
            }
        }
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
