package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "logistics_units")
data class LogisticsUnit(
    @PrimaryKey val unitNo: String,
    val barcode: String,
    val type: String, // PALLET, CONTAINER, BOX
    val status: String, // ACTIVE, COMPLETED, EMPTY, SHIPPED
    val weight: Double = 0.0,
    val dimensions: String = "",
    val warehouseCode: String = "",
    val binCode: String = "",
    val quantity: Int = 0,
    val parentUnitNo: String? = null // For nesting Boxes -> Pallets -> Containers
)

@Entity(tableName = "warehouse_documents")
data class WarehouseDocument(
    @PrimaryKey val docNo: String,
    val type: String, // RECEIPT, PUT_AWAY, PICK, SHIPMENT, COUNT, MOVEMENT, TRANSFER
    val sourceType: String = "", // PURCHASE_ORDER, SALES_ORDER, PRODUCTION, PHYSICAL_INV
    val sourceNo: String = "",
    val status: String, // OPEN, IN_PROGRESS, COMPLETED, SYNCED
    val assignedUser: String = "",
    val createdDate: Long = System.currentTimeMillis(),
    val lastModifiedDate: Long = System.currentTimeMillis(),
    val photoPath: String? = null,
    val signatureData: String? = null // For capture validation
)

@Entity(tableName = "warehouse_doc_lines")
data class WarehouseDocLine(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val docNo: String,
    val itemNo: String,
    val itemName: String,
    val expectedQty: Int,
    val scannedQty: Int = 0,
    val uom: String = "PCS",
    val suggestedBin: String? = null,
    val confirmedBin: String? = null,
    val scannedLogisticsUnit: String? = null,
    val barcode: String = ""
)

@Entity(tableName = "warehouse_bins")
data class WarehouseBin(
    @PrimaryKey val binCode: String,
    val warehouseCode: String = "MAIN",
    val zoneCode: String = "BULK",
    val section: String = "",
    val shelf: String = "",
    val currentQty: Int = 0,
    val isSuggested: Boolean = false
)

@Entity(tableName = "offline_transactions")
data class OfflineTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String, // SCAN_RECEIPT, PUT_AWAY, PICK, SHIPMENT, MOVEMENT, COUNT
    val docNo: String? = null,
    val itemNo: String? = null,
    val fromBin: String? = null,
    val toBin: String? = null,
    val unitNo: String? = null,
    val quantity: Int = 0,
    val synced: Boolean = false
)
