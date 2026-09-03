# Notakasir POS — Kasir UMKM (F&B & Retail)

Brand sendiri: **Notakasir** — Ungu Tinta `#5B21B6` + Amber Kunyit `#F59E0B`.
Stack: Kotlin native, XML + ViewBinding, MVVM, Room, Navigation, ZXing (scan), MPAndroidChart, ESC/POS sendiri (tanpa lib printer berat).

## Buka di JStudio
1. Copy folder `NotakasirPOS` ke HP, buka via JStudio → Open Project.
2. Tunggu **Sync Gradle** (butuh internet pertama kali untuk download Room/Navigation/MPAndroidChart).
3. Jika error KSP/kapt: proyek ini sudah pakai `kapt` (bukan KSP) supaya ringan di HP.
4. Run ▶ ke HP/emulator, atau **Build → Build APK (debug)** untuk `app-debug.apk`.
5. Rilis: `Build → Generate Signed Bundle/APK`, isi keystore; `versionCode/versionName` di `app/build.gradle`. ProGuard aktif di release (obfuscate secret lisensi).

Alasan permission:
- `CAMERA` → scan barcode di Kasir (`perm_camera_rationale`).
- `BLUETOOTH_CONNECT/SCAN` (Android 12+) → cetak struk & tiket dapur thermal.
- Storage via SAF (`ACTION_CREATE/OPEN_DOCUMENT`) → backup/restore `.db`, tanpa permission storage permanen.

## Modul → file
| Modul | File |
|---|---|
| Setup + brand | `settings.gradle`, `app/build.gradle`, `res/values/*`, `MainActivity.kt`, `nav_graph.xml` |
| Room + seed | `data/local/entity/Entities.kt`, `data/local/dao/Daos.kt`, `AppDatabase.kt`, `SeedData.kt`, `data/repo/AppRepository.kt` |
| Kasir→Bayar→Struk | `ui/cashier/{PosViewModel,CashierFragment}`, `ui/payment/PaymentFragment`, `ui/receipt/ReceiptFragment`, `printer/EscPosPrinter.kt`, `util/Receipt.kt` |
| Meja | `ui/tables/TablesFragment` (+ `Bill`/`BillItem` siap untuk pisah/gabung) |
| Produk/Stok | `ui/products/{ProductsFragment,ProductFormFragment}` (tap=+1 stok, tahan=hapus, toast stok menipis) |
| Laporan | `ui/reports/ReportsFragment` (query Room: omzet, tunai vs QRIS, pajak, servis, terlaris + chart) |
| Analitik | `ui/analytics/AnalyticsFragment` (tren 7 hari, MPAndroidChart) |
| Shift/Kas | `ui/shift/ShiftFragment` (modal awal, tutup + deteksi selisih) |
| User/PIN | `ui/lock/PinLockFragment`, `ui/users/UsersFragment`, `util/PinHash` (SHA-256+salt) |
| Pelanggan/WA | `ui/customers/CustomersFragment`, `util/WaShare` (wa.me + share intent) |
| Trial/Lisensi | `licensing/LicenseManager.kt` (HMAC offline, `XXXX-XXXX-XXXX-XXXX`), `tools/LicenseGenerator.kt` |
| Onboarding | `ui/onboarding/OnboardingFragment` (5 langkah + seed kopi & makanan) |
| Backup | `util/BackupHelper.kt` via SAF |
| Pengaturan | `ui/settings/SettingsFragment` (status trial/aktif, input kode, MAC printer, link WA penjual) |

## Akun & uji cepat
- Onboarding isi nama toko → PIN pemilik default `1234`, kasir `0000`.
- Kasir: tambah produk di menu Produk, cari/scan, pilih varian, Bayar (tunai nominal cepat / QRIS), Struk → cetak (isi MAC printer di Pengaturan) / kirim WA.
- Laporan: otomatis dari transaksi hari ini. Shift: buka dengan modal → tutup dengan kas fisik (selisih terdeteksi).
- Lisensi: `first_launch` di `nk_license`; trial 14 hari; aktivasi offline di Pengaturan; generate kode via `tools/LicenseGenerator.kt`.
