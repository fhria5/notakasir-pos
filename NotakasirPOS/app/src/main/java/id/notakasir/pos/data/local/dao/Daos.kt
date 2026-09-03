package id.notakasir.pos.data.local.dao

import androidx.room.*
import id.notakasir.pos.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name") fun all(): Flow<List<Category>>
    @Query("SELECT * FROM categories ORDER BY name") suspend fun allOnce(): List<Category>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(c: Category): Long
    @Delete suspend fun delete(c: Category)
}

@Dao interface ProductDao {
    @Query("SELECT * FROM products WHERE (:cat==0 OR categoryId==:cat) AND (name LIKE '%'||:q||'%' OR IFNULL(barcode,'')==:q) ORDER BY name")
    fun search(q: String, cat: Long): Flow<List<Product>>
    @Query("SELECT * FROM products WHERE id=:id") suspend fun byId(id: Long): Product?
    @Query("SELECT * FROM products WHERE barcode=:code LIMIT 1") suspend fun byBarcode(code: String): Product?
    @Query("SELECT * FROM products WHERE stock<=:t ORDER BY stock") suspend fun lowStock(t: Int = 5): List<Product>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(p: Product): Long
    @Delete suspend fun delete(p: Product)
    @Query("UPDATE products SET stock=stock-:qty WHERE id=:id") suspend fun decStock(id: Long, qty: Int)
    @Query("UPDATE products SET stock=:s WHERE id=:id") suspend fun setStock(id: Long, s: Int)
}

@Dao interface VariantDao {
    @Query("SELECT * FROM variants WHERE productId=:pid") suspend fun byProduct(pid: Long): List<ProductVariant>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(v: ProductVariant): Long
    @Delete suspend fun delete(v: ProductVariant)
}

@Dao interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name") fun all(): Flow<List<Customer>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(c: Customer): Long
    @Delete suspend fun delete(c: Customer)
    @Query("UPDATE customers SET loyaltyPoints=loyaltyPoints+:p WHERE id=:id") suspend fun addPoints(id: Long, p: Int)
}

@Dao interface TableDao {
    @Query("SELECT * FROM tables ORDER BY area, code") fun all(): Flow<List<RestaurantTable>>
    @Query("SELECT * FROM tables ORDER BY area, code") suspend fun allOnce(): List<RestaurantTable>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(t: RestaurantTable): Long
    @Delete suspend fun delete(t: RestaurantTable)
    @Query("UPDATE tables SET status=:s WHERE id=:id") suspend fun setStatus(id: Long, s: String)
}

@Dao interface BillDao {
    @Query("SELECT * FROM bills WHERE status='OPEN' ORDER BY createdAt DESC") fun openBills(): Flow<List<Bill>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(b: Bill): Long
    @Query("SELECT * FROM bills WHERE id=:id") suspend fun byId(id: Long): Bill?
    @Query("UPDATE bills SET status=:s WHERE id=:id") suspend fun setStatus(id: Long, s: String)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertItem(i: BillItem): Long
    @Query("SELECT * FROM bill_items WHERE billId=:bid") fun items(bid: Long): Flow<List<BillItem>>
    @Query("SELECT * FROM bill_items WHERE billId=:bid") suspend fun itemsOnce(bid: Long): List<BillItem>
    @Query("DELETE FROM bill_items WHERE id=:id") suspend fun delItem(id: Long)
    @Query("DELETE FROM bill_items WHERE billId=:bid") suspend fun clearItems(bid: Long)
    @Query("UPDATE bill_items SET kitchenStatus=:s WHERE id=:id") suspend fun setKitchen(id: Long, s: String)
}

@Dao interface TxnDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(t: Transaction): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertItems(l: List<TransactionItem>)
    @Query("SELECT * FROM transactions WHERE createdAt BETWEEN :a AND :b ORDER BY createdAt DESC") suspend fun range(a: Long, b: Long): List<Transaction>
    @Query("SELECT * FROM transactions ORDER BY createdAt DESC LIMIT :n") suspend fun latest(n: Int): List<Transaction>
    @Query("SELECT IFNULL(SUM(total),0) FROM transactions WHERE createdAt BETWEEN :a AND :b") suspend fun sumTotal(a: Long, b: Long): Long
    @Query("SELECT COUNT(*) FROM transactions WHERE createdAt BETWEEN :a AND :b") suspend fun countTxn(a: Long, b: Long): Int
    @Query("SELECT IFNULL(SUM(total),0) FROM transactions WHERE paymentMethod=:m AND createdAt BETWEEN :a AND :b") suspend fun sumByMethod(m: String, a: Long, b: Long): Long
    @Query("SELECT IFNULL(SUM(tax),0) AS tax, IFNULL(SUM(service),0) AS service FROM transactions WHERE createdAt BETWEEN :a AND :b") suspend fun sumTaxService(a: Long, b: Long): TaxService
    @Query("SELECT productName, SUM(qty) as q, SUM(qty*unitPrice) as omzet FROM transaction_items WHERE transactionId IN (SELECT id FROM transactions WHERE createdAt BETWEEN :a AND :b) GROUP BY productName ORDER BY q DESC LIMIT 5")
    suspend fun topProducts(a: Long, b: Long): List<TopRow>
    @Query("SELECT * FROM transaction_items WHERE transactionId=:tid") suspend fun itemsOf(tid: Long): List<TransactionItem>
}

data class TaxService(val tax: Long = 0, val service: Long = 0)
data class TopRow(val productName: String = "", val q: Int = 0, val omzet: Long = 0)

@Dao interface ShiftDao {
    @Query("SELECT * FROM shifts ORDER BY openedAt DESC LIMIT 1") suspend fun last(): CashShift?
    @Query("SELECT * FROM shifts ORDER BY openedAt DESC") fun all(): kotlinx.coroutines.flow.Flow<List<CashShift>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(s: CashShift): Long
}

@Dao interface UserDao {
    @Query("SELECT * FROM users ORDER BY name") fun all(): Flow<List<User>>
    @Query("SELECT * FROM users ORDER BY name") suspend fun allOnce(): List<User>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(u: User): Long
    @Delete suspend fun delete(u: User)
    @Query("SELECT * FROM users WHERE pinHash=:h LIMIT 1") suspend fun byPin(h: String): User?
}
