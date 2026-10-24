package com.xsparks.tukra.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- Auth DTOs ---

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val campusId: String? = null,
    val role: String = "STUDENT",
    val hostel: String? = null,
    val dietPreference: String? = null
)

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val name: String,
    val role: String,
    val campusId: String? = null,
    val hostel: String? = null,
    val dietPreference: String? = null,
    val outletId: String? = null
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserDto
)

@Serializable
data class UpdateUserRequest(
    val name: String? = null,
    val hostel: String? = null,
    val dietPreference: String? = null
)

@Serializable
data class DeviceTokenRequest(
    val token: String
)

// --- Campus & Outlet DTOs ---

@Serializable
data class CampusDto(
    val id: String,
    val name: String,
    val location: String? = null,
    val breakTimes: List<String> = emptyList()
)

@Serializable
data class OutletDto(
    val id: String,
    val name: String,
    val category: String,
    val opensAt: String? = null,
    val closesAt: String? = null,
    val acceptsOrdersUntil: String? = null,
    val slotCapacity: Int? = null,
    val isOpen: Boolean = true,
    val waitMinutes: Int? = null,
    val sourceMenuUrl: String? = null
)

// --- Menu DTOs ---

@Serializable
data class MenuItemDto(
    val id: String,
    val outletId: String,
    val name: String,
    val description: String? = null,
    val pricePaise: Int,
    val isVeg: Boolean = true,
    val category: String = "All",
    val categoryName: String? = null,
    val isAvailable: Boolean = true,
    val prepTimeSeconds: Int = 180,
    val prepUnits: Int = 1,
    val imageUrl: String? = null
)

@Serializable
data class MenuResponse(
    val items: List<MenuItemDto>
)

// --- Slot DTOs ---

@Serializable
data class SlotDto(
    val id: String,
    val startsAt: String,
    val endsAt: String,
    val remaining: Int
)

// --- Order DTOs ---

@Serializable
data class OrderItemInput(
    val menuItemId: String,
    val qty: Int,
    val notes: String? = null
)

@Serializable
data class CreateOrderRequest(
    val outletId: String,
    val slotId: String,
    val items: List<OrderItemInput>,
    val note: String? = null,
    val paymentMethod: String = "WALLET" // "WALLET" or "DEMO"
)

@Serializable
data class OrderItemDto(
    val name: String,
    val qty: Int,
    val unitPricePaise: Int
)

@Serializable
data class OrderDto(
    val id: String,
    val code: String,
    val status: String,
    val totalPaise: Int,
    val qrToken: String? = null,
    val slotStartsAt: String? = null,
    val outletName: String? = null,
    val studentFirstName: String? = null,
    val note: String? = null,
    val items: List<OrderItemDto> = emptyList(),
    val readyAt: String? = null,
    val createdAt: String? = null,
    val rejectReason: String? = null
)

// --- Wallet DTOs ---

@Serializable
data class WalletTxnDto(
    val id: String,
    val amountPaise: Int,
    val type: String, // "TOPUP", "ORDER", "REFUND", "PLATE"
    val referenceId: String? = null,
    val createdAt: String
)

@Serializable
data class WalletResponse(
    val balancePaise: Int,
    val transactions: List<WalletTxnDto> = emptyList()
)

@Serializable
data class RazorpayOrderResponse(
    val orderId: String,
    val amount: Int,
    val currency: String
)

@Serializable
data class TopupVerifyRequest(
    val razorpayPaymentId: String,
    val razorpayOrderId: String,
    val razorpaySignature: String
)

@Serializable
data class DemoTopupRequest(
    val amountPaise: Int
)

// --- Vendor DTOs ---

@Serializable
data class UpdateOrderStatusRequest(
    val status: String,
    val rejectReason: String? = null
)

@Serializable
data class VerifyQrRequest(
    val token: String
)

@Serializable
data class VerifyQrResponse(
    val message: String,
    val order: OrderDto? = null
)

@Serializable
data class UpdateItemRequest(
    val isAvailable: Boolean? = null,
    val pricePaise: Int? = null
)

@Serializable
data class SyncResponse(
    val jobId: String
)

@Serializable
data class SyncStatusResponse(
    val id: String,
    val status: String,
    val progress: String? = null,
    val streamingUrl: String? = null,
    val proposalCount: Int = 0,
    val numSteps: Int = 0,
    val errorMessage: String? = null
)

@Serializable
data class ProposalDto(
    val id: String,
    val jobId: String? = null,
    val changeType: String, // "NEW", "PRICE_CHANGE", "REMOVED", "AVAILABILITY"
    val menuItemId: String? = null,
    val itemName: String,
    val oldPricePaise: Int? = null,
    val newPricePaise: Int? = null,
    val flagged: Boolean = false,
    val isVeg: Boolean = true,
    val category: String? = null,
    val imageUrl: String? = null,
    val status: String = "PENDING"
)

@Serializable
data class ProposalDecisionRequest(
    val approve: Boolean
)

@Serializable
data class ApproveAllResponse(
    val success: Boolean = true,
    val approvedCount: Int = 0
)

@Serializable
data class AddItemRequest(
    val name: String,
    val pricePaise: Int,
    val isVeg: Boolean = true,
    val categoryId: String? = null,
    val description: String? = null,
    val prepSeconds: Int = 180,
    val prepUnits: Int = 1,
    val spiceLevel: Int? = null
)

@Serializable
data class UpdateOutletRequest(
    val isOpen: Boolean? = null,
    val slotCapacity: Int? = null,
    val acceptsOrdersUntil: String? = null
)

// --- Plate DTOs ---

@Serializable
data class PlateOpenRequest(
    val plateCode: String,
    val outletId: String
)

@Serializable
data class PlateLinkRequest(
    val plateCode: String
)

@Serializable
data class PlateAddItemRequest(
    val menuItemId: String,
    val qty: Int = 1
)

@Serializable
data class PlateSessionItemDto(
    val id: String,
    val name: String,
    val qty: Int,
    val unitPricePaise: Int
)

@Serializable
data class PlateSessionDto(
    val id: String,
    val plateCode: String,
    val outletId: String,
    val userId: String? = null,
    val status: String, // "OPEN", "BILLED", "PAID"
    val totalPaise: Int = 0,
    val items: List<PlateSessionItemDto> = emptyList(),
    val studentName: String? = null
)

// --- Health DTO ---

@Serializable
data class HealthFeatures(
    val tinyfish: Boolean = false,
    val razorpay: Boolean = false,
    val push: Boolean = false,
    val demoPay: Boolean = false
)

@Serializable
data class HealthResponse(
    val ok: Boolean,
    val time: String? = null,
    val features: HealthFeatures = HealthFeatures()
)

// --- Mess DTOs ---

@Serializable
data class MessMenuItemDto(
    val messMenuId: String,
    val outletId: String,
    val meal: String, // "BREAKFAST", "LUNCH", "SNACKS", "DINNER"
    val menuItemId: String,
    val dishName: String,
    val pricePaise: Int = 0,
    val isVeg: Boolean = true,
    val description: String? = null,
    val imageUrl: String? = null,
    val plannedQty: Int? = null,
    val reservationCount: Int = 0
)

@Serializable
data class MessMenuResponse(
    val serviceDate: String,
    val items: List<MessMenuItemDto> = emptyList()
)

@Serializable
data class PostMessMenuRequest(
    val outletId: String,
    val serviceDate: String,
    val meal: String,
    val menuItemId: String,
    val plannedQty: Int? = null
)

@Serializable
data class DemandForecastItemDto(
    val menuItemId: String,
    val name: String,
    val category: String = "General",
    val isVeg: Boolean = true,
    val fourWeekWeekdayAvg: Int = 0,
    val currentReservations: Int = 0,
    val plannedQty: Int? = null,
    val suggestedQty: Int = 15,
    val confidenceLabel: String = "Estimate"
)

@Serializable
data class DemandForecastResponse(
    val outletId: String,
    val targetDate: String,
    val dayOfWeek: String,
    val items: List<DemandForecastItemDto> = emptyList()
)

// --- Admin DTOs ---

@Serializable
data class AdminOverviewKpis(
    val ordersToday: Int = 0,
    val revenueTodayPaise: Int = 0,
    val revenueTodayRupees: Int = 0,
    val activeOutlets: Int = 0,
    val totalOutlets: Int = 0,
    val avgWaitMinutes: Int = 0
)

@Serializable
data class HourlyOrderStat(
    val hour: Int,
    val label: String,
    val count: Int,
    val revenuePaise: Int
)

@Serializable
data class AdminOverviewResponse(
    val kpis: AdminOverviewKpis,
    val ordersByHour: List<HourlyOrderStat> = emptyList()
)

@Serializable
data class TopDishStat(
    val name: String,
    val outletName: String,
    val totalQty: Int,
    val totalRevenuePaise: Int
)

@Serializable
data class WasteAuditItem(
    val meal: String,
    val dishName: String,
    val plannedQty: Int,
    val consumedQty: Int,
    val estimatedSurplus: Int,
    val wastePercent: Int,
    val status: String
)

@Serializable
data class AdminDemandResponse(
    val date: String,
    val topDishes: List<TopDishStat> = emptyList(),
    val wasteAudit: List<WasteAuditItem> = emptyList()
)

@Serializable
data class AdminOutletItem(
    val id: String,
    val name: String,
    val type: String,
    val isOpen: Boolean = true,
    val slotCapacity: Int? = null,
    val opensAt: String? = null,
    val acceptsOrdersUntil: String? = null,
    val ownerName: String? = null,
    val totalOrders: Int = 0,
    val lastSyncStatus: String? = null,
    val lastSyncAt: String? = null
)

@Serializable
data class AdminSyncJobItem(
    val id: String,
    val outletId: String,
    val outletName: String? = null,
    val tinyfishRunId: String? = null,
    val status: String,
    val streamingUrl: String? = null,
    val progress: String? = null,
    val startedAt: String? = null,
    val finishedAt: String? = null,
    val numSteps: Int? = null,
    val proposalCount: Int = 0,
    val errorMessage: String? = null
)
