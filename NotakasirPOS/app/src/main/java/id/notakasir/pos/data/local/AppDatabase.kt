package id.notakasir.pos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import id.notakasir.pos.data.local.dao.*
import id.notakasir.pos.data.local.entity.*

@Database(
    entities = [Category::class, Product::class, ProductVariant::class, Customer::class,
        RestaurantTable::class, Bill::class, BillItem::class, Transaction::class,
        TransactionItem::class, CashShift::class, User::class],
    version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun variantDao(): VariantDao
    abstract fun customerDao(): CustomerDao
    abstract fun tableDao(): TableDao
    abstract fun billDao(): BillDao
    abstract fun txnDao(): TxnDao
    abstract fun shiftDao(): ShiftDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile private var I: AppDatabase? = null
        fun get(ctx: Context): AppDatabase = I ?: synchronized(this) {
            I ?: Room.databaseBuilder(ctx.applicationContext, AppDatabase::class.java, "notakasir.db")
                .fallbackToDestructiveMigration()
                .build().also { I = it }
        }
        /** Path file DB untuk backup/restore via SAF. */
        fun dbFile(ctx: Context) = ctx.getDatabasePath("notakasir.db")
    }
}
