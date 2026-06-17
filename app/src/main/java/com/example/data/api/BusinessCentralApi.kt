package com.example.data.api

import com.squareup.moshi.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// --- BC API Data Structures ---

data class BcItem(
    @Json(name = "id") val id: String,
    @Json(name = "number") val number: String,
    @Json(name = "displayName") val displayName: String,
    @Json(name = "itemGroup") val itemGroup: String?,
    @Json(name = "unitOfMeasure") val unitOfMeasure: String?
)

data class BcResponse<T>(
    @Json(name = "value") val value: List<T>
)

data class BcOrder(
    @Json(name = "id") val id: String,
    @Json(name = "number") val number: String,
    @Json(name = "orderDate") val orderDate: String,
    @Json(name = "customerName") val customerName: String?,
    @Json(name = "vendorName") val vendorName: String?,
    @Json(name = "status") val status: String
)

data class BcWarehouseActivity(
    @Json(name = "no") val no: String,
    @Json(name = "type") val type: String, // Receipt, PutAway, Pick, Shipment
    @Json(name = "sourceNo") val sourceNo: String,
    @Json(name = "locationCode") val locationCode: String,
    @Json(name = "lines") val lines: List<BcActivityLine>
)

data class BcActivityLine(
    @Json(name = "itemNo") val itemNo: String,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "binCode") val binCode: String?,
    @Json(name = "unitOfMeasure") val unitOfMeasure: String?
)

data class BcPostResult(
    @Json(name = "success") val success: Boolean,
    @Json(name = "documentNo") val documentNo: String,
    @Json(name = "message") val message: String?
)

// --- Retrofit API Interface ---

interface BusinessCentralApiService {

    @GET("v2.0/companies({companyId})/items")
    suspend fun getItems(
        @Path("companyId") companyId: String,
        @Header("Authorization") authHeader: String
    ): BcResponse<BcItem>

    @GET("v2.0/companies({companyId})/purchaseOrders")
    suspend fun getPurchaseOrders(
        @Path("companyId") companyId: String,
        @Header("Authorization") authHeader: String
    ): BcResponse<BcOrder>

    @GET("v2.0/companies({companyId})/salesOrders")
    suspend fun getSalesOrders(
        @Path("companyId") companyId: String,
        @Header("Authorization") authHeader: String
    ): BcResponse<BcOrder>

    @POST("v2.0/companies({companyId})/warehouseReceipts/post")
    suspend fun postWarehouseReceipt(
        @Path("companyId") companyId: String,
        @Header("Authorization") authHeader: String,
        @Body activity: BcWarehouseActivity
    ): BcPostResult

    @POST("v2.0/companies({companyId})/warehouseShipments/post")
    suspend fun postWarehouseShipment(
        @Path("companyId") companyId: String,
        @Header("Authorization") authHeader: String,
        @Body activity: BcWarehouseActivity
    ): BcPostResult
}

// --- Dynamic API Client ---

object BusinessCentralClient {
    private var BASE_URL = "https://api.businesscentral.dynamics.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    val service: BusinessCentralApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(BusinessCentralApiService::class.java)
    }

    // Allows dynamic custom API endpoint routing for different BC On-Premise / SaaS tenants
    fun updateBaseUrl(newUrl: String) {
        if (newUrl.isNotBlank() && (newUrl.startsWith("http://") || newUrl.startsWith("https://"))) {
            BASE_URL = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        }
    }
}
