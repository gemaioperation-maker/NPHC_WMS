package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        LogisticsUnit::class,
        WarehouseDocument::class,
        WarehouseDocLine::class,
        WarehouseBin::class,
        OfflineTransaction::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WmsDatabase : RoomDatabase() {

    abstract fun wmsDao(): WmsDao

    companion object {
        @Volatile
        private var INSTANCE: WmsDatabase? = null

        fun getDatabase(context: Context): WmsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WmsDatabase::class.java,
                    "wms_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
