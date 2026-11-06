package com.vahid.gorgannama.data

import com.vahid.gorgannama.Config

enum class Plan(val title: String) {
    FREE("رایگان"),
    BASIC("پایه یک‌ساله"),
    FEATURED("ویژه یک‌ساله")
}

enum class PayStatus(val title: String) {
    UNPAID("پرداخت‌نشده"),
    PARTIAL("پرداخت ناقص"),
    PAID("پرداخت‌شده")
}

enum class LeadStatus(val title: String) {
    INTERESTED("علاقه‌مند"),
    FOLLOW_UP("باید پیگیری کنم"),
    NOT_PRESENT("صاحبش نبود"),
    PAID("مشتری شد"),
    REJECTED("نخواست")
}

data class Business(
    val id: String,
    val name: String,
    val categoryId: String = "other",
    val neighborhood: String = "",
    val address: String = "",
    val phone: String = "",
    val mobile: String = "",
    val instagram: String = "",
    val eitaa: String = "",
    val rubika: String = "",
    val website: String = "",
    val description: String = "",
    val tags: List<String> = emptyList(),
    val lat: Double? = null,
    val lng: Double? = null,
    val hours: String = "",
    /** java.time.DayOfWeek values: 1=Mon ... 6=Sat, 7=Sun */
    val closedDays: Set<Int> = emptySet(),
    val imageUrl: String = "",
    val offerTitle: String = "",
    val offerDetail: String = "",
    /** epochDay */
    val offerUntil: Long? = null,
    val verified: Boolean = false,
    val active: Boolean = true,
    val plan: Plan = Plan.FREE,
    val planStart: Long? = null,
    val planEnd: Long? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val sample: Boolean = false,
    // ---- private, never exported to the public catalog ----
    val ownerName: String = "",
    val ownerMobile: String = "",
    val payStatus: PayStatus = PayStatus.UNPAID,
    val adminNote: String = ""
)

data class Payment(
    val id: String,
    val businessId: String,
    val businessName: String,
    val amount: Long,
    val day: Long,
    val plan: Plan,
    val note: String = ""
)

data class Lead(
    val id: String,
    val shopName: String,
    val ownerName: String = "",
    val phone: String = "",
    val neighborhood: String = "",
    val categoryId: String = "",
    val address: String = "",
    val status: LeadStatus = LeadStatus.INTERESTED,
    val note: String = "",
    val visitDay: Long,
    val followUpDay: Long? = null,
    val businessId: String? = null
)

data class Settings(
    val pin: String = "1234",
    val catalogUrl: String = Config.DEFAULT_CATALOG_URL,
    val adminPhone: String = Config.ADMIN_PHONE,
    val priceBasic: Long = 100_000,
    val priceFeatured: Long = 300_000,
    val dailyTarget: Int = 20,
    val autoSync: Boolean = true,
    val lastSync: Long = 0,
    val showAdminTab: Boolean = !Config.PUBLIC_BUILD,
    val adminDevice: Boolean = false
)
