package com.xsparks.tukra.core.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TukraApi {

    // --- Health ---
    @GET("health")
    suspend fun getHealth(): HealthResponse

    // --- Auth ---
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @GET("me")
    suspend fun getCurrentUser(): UserDto

    @PATCH("me")
    suspend fun updateCurrentUser(@Body body: UpdateUserRequest): UserDto

    @DELETE("me")
    suspend fun deleteAccount(): Map<String, Boolean>

    @POST("me/device-token")
    suspend fun saveDeviceToken(@Body body: DeviceTokenRequest): Map<String, Boolean>

    // --- Campus & Outlets ---
    @GET("campus")
    suspend fun getCampus(): CampusDto

    @GET("outlets")
    suspend fun getOutlets(): List<OutletDto>

    @GET("outlets/{id}/menu")
    suspend fun getOutletMenu(
        @Path("id") outletId: String,
        @Query("diet") diet: String? = null
    ): MenuResponse

    @GET("outlets/{id}/slots")
    suspend fun getOutletSlots(
        @Path("id") outletId: String,
        @Query("units") units: Int = 1,
        @Query("longestPrepSeconds") longestPrepSeconds: Int = 180
    ): List<SlotDto>

    // --- Orders ---
    @POST("orders")
    suspend fun createOrder(@Body body: CreateOrderRequest): OrderDto

    @GET("orders")
    suspend fun getOrders(@Query("status") status: String? = null): List<OrderDto>

    @GET("orders/{id}")
    suspend fun getOrderById(@Path("id") orderId: String): OrderDto

    @POST("orders/{id}/cancel")
    suspend fun cancelOrder(@Path("id") orderId: String): OrderDto

    // --- Wallet ---
    @GET("wallet")
    suspend fun getWallet(): WalletResponse

    @POST("wallet/topup/order")
    suspend fun createTopupOrder(@Body body: Map<String, Int>): RazorpayOrderResponse

    @POST("wallet/topup/verify")
    suspend fun verifyTopup(@Body body: TopupVerifyRequest): WalletResponse

    @POST("wallet/topup/demo")
    suspend fun demoTopup(@Body body: DemoTopupRequest): WalletResponse

    // --- Vendor ---
    @GET("vendor/orders")
    suspend fun getVendorOrders(@Query("status") status: String? = null): List<OrderDto>

    @PATCH("vendor/orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") orderId: String,
        @Body body: UpdateOrderStatusRequest
    ): OrderDto

    @POST("vendor/orders/verify-qr")
    suspend fun verifyOrderQr(@Body body: VerifyQrRequest): VerifyQrResponse

    @PATCH("vendor/items/{id}")
    suspend fun updateVendorItem(
        @Path("id") itemId: String,
        @Body body: UpdateItemRequest
    ): MenuItemDto

    @POST("vendor/sync")
    suspend fun startMenuSync(@Body body: Map<String, String>): SyncResponse

    @GET("vendor/sync/{jobId}")
    suspend fun getSyncStatus(@Path("jobId") jobId: String): SyncStatusResponse

    @GET("vendor/proposals")
    suspend fun getVendorProposals(): List<ProposalDto>

    @POST("vendor/proposals/{id}/decision")
    suspend fun decideProposal(
        @Path("id") proposalId: String,
        @Body body: ProposalDecisionRequest
    ): ProposalDto

    @POST("vendor/proposals/approve-all")
    suspend fun approveAllProposals(): Map<String, Int>

    // --- Plate & Mess ---
    @POST("plate/open")
    suspend fun openPlateSession(@Body body: PlateOpenRequest): PlateSessionDto

    @POST("plate/link")
    suspend fun linkPlateSession(@Body body: PlateLinkRequest): PlateSessionDto

    @POST("plate/{id}/items")
    suspend fun addPlateItem(
        @Path("id") plateId: String,
        @Body body: PlateAddItemRequest
    ): PlateSessionDto

    @POST("plate/{id}/finish")
    suspend fun finishPlateSession(@Path("id") plateId: String): PlateSessionDto

    @GET("plate/{id}")
    suspend fun getPlateSession(@Path("id") plateId: String): PlateSessionDto

    @POST("plate/{id}/pay")
    suspend fun payPlateSession(@Path("id") plateId: String): PlateSessionDto
}
