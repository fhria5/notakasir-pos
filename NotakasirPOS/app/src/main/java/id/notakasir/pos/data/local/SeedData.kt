package id.notakasir.pos.data.local

import id.notakasir.pos.data.local.entity.*
import id.notakasir.pos.util.PinHash

/** Seed contoh: kopi & makanan. Dijalankan sekali saat onboarding selesai / DB kosong. */
object SeedData {
    suspend fun run(db: AppDatabase) {
        if (db.categoryDao().allOnce().isNotEmpty()) return
        val minuman = db.categoryDao().upsert(Category(name = "Minuman"))
        val makanan = db.categoryDao().upsert(Category(name = "Makanan"))
        val snack = db.categoryDao().upsert(Category(name = "Snack"))
        val p1 = db.productDao().upsert(Product(categoryId = minuman, name = "Kopi Susu Gula Aren", price = 15000, stock = 50, barcode = "10001", hasVariant = true))
        val p2 = db.productDao().upsert(Product(categoryId = minuman, name = "Americano", price = 12000, stock = 50, barcode = "10002", hasVariant = true))
        db.productDao().upsert(Product(categoryId = makanan, name = "Nasi Goreng Spesial", price = 18000, stock = 30, barcode = "20001"))
        db.productDao().upsert(Product(categoryId = makanan, name = "Mie Ayam", price = 15000, stock = 30, barcode = "20002"))
        db.productDao().upsert(Product(categoryId = snack, name = "Pisang Goreng (4)", price = 12000, stock = 20, barcode = "30001"))
        listOf("Reguler" to 0L, "Large" to 4000L).forEachIndexed { i, (n, e) ->
            db.variantDao().upsert(ProductVariant(id = System.currentTimeMillis() + i, productId = p1, groupName = "Ukuran", name = n, extraPrice = e))
        }
        listOf("Hot" to 0L, "Ice" to 2000L).forEachIndexed { i, (n, e) ->
            db.variantDao().upsert(ProductVariant(id = System.currentTimeMillis() + 100 + i, productId = p2, groupName = "Saji", name = n, extraPrice = e))
        }
        listOf("A1" to "Indoor", "A2" to "Indoor", "B1" to "Outdoor", "B2" to "Outdoor").forEach {
            db.tableDao().upsert(RestaurantTable(code = it.first, area = it.second))
        }
        db.userDao().upsert(User(name = "Pemilik", role = "PEMILIK", pinHash = PinHash.hash("1234")))
        db.userDao().upsert(User(name = "Kasir", role = "STAF", pinHash = PinHash.hash("0000")))
        db.customerDao().upsert(Customer(name = "Pelanggan Umum", phone = ""))
    }
}
