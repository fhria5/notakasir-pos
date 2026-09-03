package id.notakasir.pos.printer

import android.bluetooth.BluetoothSocket
import java.io.OutputStream
import java.util.UUID

/**
 * Printer thermal ESC/POS minimal via Bluetooth SPP — tanpa library eksternal.
 * Cara pakai: EscPosPrinter.print(macAddress, bytes).
 * Alamat MAC disimpan di Pengaturan ("printer_mac").
 */
object EscPosPrinter {
    private val SPP: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    fun buildReceipt(lines: List<String>): ByteArray {
        val out = mutableListOf<Byte>()
        fun raw(vararg b: Int) = b.forEach { out.add(it.toByte()) }
        fun text(s: String) = out.addAll(s.toByteArray(charset("GBK")).toList())
        raw(0x1B, 0x40)          // init
        raw(0x1B, 0x61, 0x01)    // center
        lines.forEach { text(it + "\n") }
        raw(0x0A, 0x0A, 0x1D, 0x56, 0x41, 0x03) // feed + cut
        return out.toByteArray()
    }

    fun buildKitchenTicket(table: String, items: List<String>): ByteArray =
        buildReceipt(listOf("** TIKET DAPUR **", "Meja: $table", "------------------------------") + items)

    @Throws(Exception::class)
    fun print(adapter: android.bluetooth.BluetoothAdapter, mac: String, data: ByteArray) {
        val dev = adapter.getRemoteDevice(mac)
        val sock: BluetoothSocket = dev.createRfcommSocketToServiceRecord(SPP)
        adapter.cancelDiscovery()
        sock.connect()
        val os: OutputStream = sock.outputStream
        os.write(data); os.flush()
        Thread.sleep(500)
        sock.close()
    }
}
