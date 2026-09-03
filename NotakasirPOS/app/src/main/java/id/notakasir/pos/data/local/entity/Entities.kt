package id.notakasir.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long = 0,
    val name: String,
    val price: Long,
    val stock: Int = 0,
    val barcode: String? = null,
    val hasVariant: Boolean = false
)

@Entity(tableName = "variants", primaryKeys = ["id"])
data class ProductVariant(
    val id: Long = 0,
    val productId: Long = 0,
    val groupName: String = "Ukuran",
    val name: String,
    val extraPrice: Long = 0
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val loyaltyPoints: Int = 0
)

@Entity(tableName = "tables")
data class RestaurantTable(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val area: String = "Indoor",
    val status: String = "EMPTY"
)

@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tableId: Long? = null,
    val status: String = "OPEN",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bill_items")
data class BillItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val billId: Long = 0,
    val productId: Long = 0,
    val productName: String = "",
    val variantName: String? = null,
    val qty: Int = 1,
    val unitPrice: Long = 0,
    val kitchenStatus: String = "DITERIMA"
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val total: Long = 0,
    val subtotal: Long = 0,
    val tax: Long = 0,
    val service: Long = 0,
    val discount: Long = 0,
    val paymentMethod: String = "CASH",
    val paidAmount: Long = 0,
    val changeAmount: Long = 0,
    val customerId: Long? = null,
    val billId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transaction_items")
data class TransactionItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionId: Long = 0,
    val productName: String = "",
    val variantName: String? = null,
    val qty: Int = 1,
    val unitPrice: Long = 0
)

@Entity(tableName = "shifts")
data class CashShift(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val openingCash: Long = 0,
    val cashIn: Long = 0,
    val cashOut: Long = 0,
    val closingCashActual: Long? = null,
    val openedBy: String = "",
    val openedAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null
)

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val role: String = "STAF",
    val pinHash: String = ""
)
