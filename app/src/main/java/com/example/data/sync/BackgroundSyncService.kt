package com.example.data.sync

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.IBinder
import android.util.Log
import com.example.data.db.WmsDatabase
import com.example.data.repository.WmsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BackgroundSyncService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var repository: WmsRepository
    private var connectivityManager: ConnectivityManager? = null

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.d("BackgroundSyncService", "Internet connection restored! Automatically initiating offline transaction sink...")
            syncStagedTransactions()
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("BackgroundSyncService", "Offline Transaction Background Sync Service created!")
        
        val db = WmsDatabase.getDatabase(applicationContext)
        repository = WmsRepository(db.wmsDao())

        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        try {
            connectivityManager?.registerNetworkCallback(networkRequest, networkCallback)
        } catch (e: Exception) {
            Log.e("BackgroundSyncService", "Configuration exception on network callback registration: ${e.message}")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("BackgroundSyncService", "Auto-sync triggered from command invocation.")
        syncStagedTransactions()
        return START_STICKY
    }

    private fun syncStagedTransactions() {
        serviceScope.launch {
            try {
                // 1. Synchronize transaction logs
                val pendingTxnCount = repository.syncOfflineTransactions()
                Log.d("BackgroundSyncService", "Cleared and synchronized $pendingTxnCount logged transactions.")

                // 2. Clear completed local documents awaiting synchronization
                val db = WmsDatabase.getDatabase(applicationContext)
                val docs = db.wmsDao().getAllDocuments().first()
                var docSyncCount = 0
                for (doc in docs) {
                    if (doc.status == "COMPLETED") {
                        val success = repository.postDocumentToBC(doc.docNo)
                        if (success) {
                            docSyncCount++
                        }
                    }
                }
                Log.d("BackgroundSyncService", "Backlog verification: posted $docSyncCount completed documents directly to D365 ERP.")
            } catch (e: Exception) {
                Log.e("BackgroundSyncService", "Auto-sync execution fault: ${e.message}")
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        try {
            connectivityManager?.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            // ignore
        }
        serviceScope.cancel()
        Log.d("BackgroundSyncService", "BackgroundSyncService closed.")
    }
}
