package com.example.data.db

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WmsDao {

    // --- Warehouse Documents ---
    @Query("SELECT * FROM warehouse_documents ORDER BY createdDate DESC")
    fun getAllDocuments(): Flow<List<WarehouseDocument>>

    @Query("SELECT * FROM warehouse_documents WHERE type = :type ORDER BY createdDate DESC")
    fun getDocumentsByType(type: String): Flow<List<WarehouseDocument>>

    @Query("SELECT * FROM warehouse_documents WHERE docNo = :docNo")
    suspend fun getDocumentByNo(docNo: String): WarehouseDocument?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: WarehouseDocument)

    @Update
    suspend fun updateDocument(doc: WarehouseDocument)

    @Query("DELETE FROM warehouse_documents WHERE docNo = :docNo")
    suspend fun deleteDocument(docNo: String)

    // --- Document Lines ---
    @Query("SELECT * FROM warehouse_doc_lines WHERE docNo = :docNo")
    fun getLinesForDocumentFlow(docNo: String): Flow<List<WarehouseDocLine>>

    @Query("SELECT * FROM warehouse_doc_lines WHERE docNo = :docNo")
    suspend fun getLinesForDocument(docNo: String): List<WarehouseDocLine>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(lines: List<WarehouseDocLine>)

    @Update
    suspend fun updateLine(line: WarehouseDocLine)

    @Query("UPDATE warehouse_doc_lines SET scannedQty = :scannedQty, confirmedBin = :bin, scannedLogisticsUnit = :lu WHERE id = :lineId")
    suspend fun updateLineScanning(lineId: Int, scannedQty: Int, bin: String?, lu: String?)

    // --- Logistics Units ---
    @Query("SELECT * FROM logistics_units")
    fun getAllLogisticsUnits(): Flow<List<LogisticsUnit>>

    @Query("SELECT * FROM logistics_units WHERE unitNo = :unitNo")
    suspend fun getLogisticsUnitByNo(unitNo: String): LogisticsUnit?

    @Query("SELECT * FROM logistics_units WHERE parentUnitNo = :parentUnitNo")
    fun getNestedLogisticsUnits(parentUnitNo: String): Flow<List<LogisticsUnit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogisticsUnit(lu: LogisticsUnit)

    @Update
    suspend fun updateLogisticsUnit(lu: LogisticsUnit)

    @Query("DELETE FROM logistics_units WHERE unitNo = :unitNo")
    suspend fun deleteLogisticsUnit(unitNo: String)

    // --- Warehouse Bins ---
    @Query("SELECT * FROM warehouse_bins")
    fun getAllBins(): Flow<List<WarehouseBin>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBins(bins: List<WarehouseBin>)

    // --- Transaction Logs ---
    @Query("SELECT * FROM offline_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<OfflineTransaction>>

    @Query("SELECT * FROM offline_transactions WHERE synced = 0")
    suspend fun getUnsyncedTransactions(): List<OfflineTransaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(txn: OfflineTransaction)

    @Query("UPDATE offline_transactions SET synced = 1 WHERE id = :id")
    suspend fun markTransactionSynced(id: Int)
}
