package com.vahid.gorgannama.data

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class BackupData(
    val businesses: List<Business>,
    val payments: List<Payment>,
    val leads: List<Lead>,
    val favorites: List<String>,
    val settings: Settings
)

object Json {
    const val TYPE_CATALOG = "gorgan-nama-catalog"
    const val TYPE_BACKUP = "gorgan-nama-backup"

    private fun JSONObject.s(k: String): String = if (isNull(k)) "" else optString(k, "")
    private fun JSONObject.l(k: String): Long? = if (isNull(k)) null else optLong(k)
    private fun JSONObject.d(k: String): Double? =
        if (isNull(k)) null else optDouble(k).takeIf { !it.isNaN() }
    private fun JSONObject.b(k: String, def: Boolean): Boolean = if (isNull(k)) def else optBoolean(k, def)
    private fun JSONObject.arr(k: String): JSONArray = optJSONArray(k) ?: JSONArray()

    private fun plan(v: String): Plan = Plan.values().firstOrNull { it.name == v } ?: Plan.FREE
    private fun pay(v: String): PayStatus = PayStatus.values().firstOrNull { it.name == v } ?: PayStatus.UNPAID
    private fun leadStatus(v: String): LeadStatus =
        LeadStatus.values().firstOrNull { it.name == v } ?: LeadStatus.INTERESTED

    // ---------- business ----------
    fun biz(b: Business, full: Boolean): JSONObject {
        val o = JSONObject()
        o.put("id", b.id)
        o.put("name", b.name)
        o.put("categoryId", b.categoryId)
        o.put("neighborhood", b.neighborhood)
        o.put("address", b.address)
        o.put("phone", b.phone)
        o.put("mobile", b.mobile)
        o.put("instagram", b.instagram)
        o.put("eitaa", b.eitaa)
        o.put("rubika", b.rubika)
        o.put("website", b.website)
        o.put("description", b.description)
        o.put("tags", JSONArray(b.tags))
        if (b.lat != null) o.put("lat", b.lat)
        if (b.lng != null) o.put("lng", b.lng)
        o.put("hours", b.hours)
        o.put("closedDays", JSONArray(b.closedDays.toList()))
        // local gallery photos only make sense on this phone
        o.put("imageUrl", if (full || b.imageUrl.startsWith("http")) b.imageUrl else "")
        o.put("offerTitle", b.offerTitle)
        o.put("offerDetail", b.offerDetail)
        if (b.offerUntil != null) o.put("offerUntil", b.offerUntil)
        o.put("verified", b.verified)
        o.put("active", b.active)
        o.put("plan", b.plan.name)
        if (b.planStart != null) o.put("planStart", b.planStart)
        if (b.planEnd != null) o.put("planEnd", b.planEnd)
        o.put("createdAt", b.createdAt)
        o.put("updatedAt", b.updatedAt)
        o.put("sample", b.sample)
        if (full) {
            o.put("ownerName", b.ownerName)
            o.put("ownerMobile", b.ownerMobile)
            o.put("payStatus", b.payStatus.name)
            o.put("adminNote", b.adminNote)
        }
        return o
    }

    fun bizFrom(o: JSONObject): Business {
        val tagsArr = o.arr("tags")
        val daysArr = o.arr("closedDays")
        return Business(
            id = o.s("id").ifBlank { UUID.randomUUID().toString() },
            name = o.s("name"),
            categoryId = o.s("categoryId").ifBlank { "other" },
            neighborhood = o.s("neighborhood"),
            address = o.s("address"),
            phone = o.s("phone"),
            mobile = o.s("mobile"),
            instagram = o.s("instagram"),
            eitaa = o.s("eitaa"),
            rubika = o.s("rubika"),
            website = o.s("website"),
            description = o.s("description"),
            tags = (0 until tagsArr.length()).map { tagsArr.optString(it, "") }.filter { it.isNotBlank() },
            lat = o.d("lat"),
            lng = o.d("lng"),
            hours = o.s("hours"),
            closedDays = (0 until daysArr.length()).map { daysArr.optInt(it) }.filter { it in 1..7 }.toSet(),
            imageUrl = o.s("imageUrl"),
            offerTitle = o.s("offerTitle"),
            offerDetail = o.s("offerDetail"),
            offerUntil = o.l("offerUntil"),
            verified = o.b("verified", false),
            active = o.b("active", true),
            plan = plan(o.s("plan")),
            planStart = o.l("planStart"),
            planEnd = o.l("planEnd"),
            createdAt = o.l("createdAt") ?: 0L,
            updatedAt = o.l("updatedAt") ?: 0L,
            sample = o.b("sample", false),
            ownerName = o.s("ownerName"),
            ownerMobile = o.s("ownerMobile"),
            payStatus = pay(o.s("payStatus")),
            adminNote = o.s("adminNote")
        )
    }

    // ---------- payment / lead / settings ----------
    private fun payment(p: Payment) = JSONObject()
        .put("id", p.id).put("businessId", p.businessId).put("businessName", p.businessName)
        .put("amount", p.amount).put("day", p.day).put("plan", p.plan.name).put("note", p.note)

    private fun paymentFrom(o: JSONObject) = Payment(
        id = o.s("id").ifBlank { UUID.randomUUID().toString() },
        businessId = o.s("businessId"),
        businessName = o.s("businessName"),
        amount = o.l("amount") ?: 0L,
        day = o.l("day") ?: 0L,
        plan = plan(o.s("plan")),
        note = o.s("note")
    )

    private fun lead(l: Lead): JSONObject {
        val o = JSONObject()
            .put("id", l.id).put("shopName", l.shopName).put("ownerName", l.ownerName)
            .put("phone", l.phone).put("neighborhood", l.neighborhood).put("categoryId", l.categoryId)
            .put("address", l.address).put("status", l.status.name).put("note", l.note)
            .put("visitDay", l.visitDay)
        if (l.followUpDay != null) o.put("followUpDay", l.followUpDay)
        if (l.businessId != null) o.put("businessId", l.businessId)
        return o
    }

    private fun leadFrom(o: JSONObject) = Lead(
        id = o.s("id").ifBlank { UUID.randomUUID().toString() },
        shopName = o.s("shopName"),
        ownerName = o.s("ownerName"),
        phone = o.s("phone"),
        neighborhood = o.s("neighborhood"),
        categoryId = o.s("categoryId"),
        address = o.s("address"),
        status = leadStatus(o.s("status")),
        note = o.s("note"),
        visitDay = o.l("visitDay") ?: 0L,
        followUpDay = o.l("followUpDay"),
        businessId = o.s("businessId").ifBlank { null }
    )

    private fun settings(s: Settings) = JSONObject()
        .put("pin", s.pin).put("catalogUrl", s.catalogUrl).put("adminPhone", s.adminPhone)
        .put("priceBasic", s.priceBasic).put("priceFeatured", s.priceFeatured)
        .put("dailyTarget", s.dailyTarget).put("autoSync", s.autoSync).put("lastSync", s.lastSync)
        .put("showAdminTab", s.showAdminTab).put("adminDevice", s.adminDevice)

    private fun settingsFrom(o: JSONObject?): Settings {
        val d = Settings()
        if (o == null) return d
        return Settings(
            pin = o.s("pin").ifBlank { d.pin },
            catalogUrl = if (o.has("catalogUrl")) o.s("catalogUrl") else d.catalogUrl,
            adminPhone = if (o.has("adminPhone")) o.s("adminPhone") else d.adminPhone,
            priceBasic = o.l("priceBasic") ?: d.priceBasic,
            priceFeatured = o.l("priceFeatured") ?: d.priceFeatured,
            dailyTarget = (o.l("dailyTarget") ?: d.dailyTarget.toLong()).toInt(),
            autoSync = o.b("autoSync", d.autoSync),
            lastSync = o.l("lastSync") ?: 0L,
            showAdminTab = o.b("showAdminTab", d.showAdminTab),
            adminDevice = o.b("adminDevice", false)
        )
    }

    // ---------- documents ----------
    fun catalog(list: List<Business>): JSONObject {
        val a = JSONArray()
        list.forEach { a.put(biz(it, false)) }
        return JSONObject()
            .put("type", TYPE_CATALOG)
            .put("version", 1)
            .put("updatedAt", System.currentTimeMillis())
            .put("businesses", a)
    }

    fun catalogFrom(o: JSONObject): List<Business> {
        val a = o.optJSONArray("businesses") ?: throw IllegalArgumentException("فایل کاتالوگ معتبر نیست")
        return (0 until a.length())
            .mapNotNull { i -> a.optJSONObject(i)?.let { bizFrom(it) } }
            .filter { it.name.isNotBlank() }
    }

    fun backup(
        businesses: List<Business>, payments: List<Payment>, leads: List<Lead>,
        favorites: List<String>, settings: Settings
    ): JSONObject {
        val bz = JSONArray(); businesses.forEach { bz.put(biz(it, true)) }
        val pz = JSONArray(); payments.forEach { pz.put(payment(it)) }
        val lz = JSONArray(); leads.forEach { lz.put(lead(it)) }
        return JSONObject()
            .put("type", TYPE_BACKUP)
            .put("version", 1)
            .put("exportedAt", System.currentTimeMillis())
            .put("businesses", bz)
            .put("payments", pz)
            .put("leads", lz)
            .put("favorites", JSONArray(favorites))
            .put("settings", settings(settings))
    }

    fun backupFrom(o: JSONObject): BackupData {
        val bz = o.arr("businesses")
        val pz = o.arr("payments")
        val lz = o.arr("leads")
        val fz = o.arr("favorites")
        return BackupData(
            businesses = (0 until bz.length()).mapNotNull { i -> bz.optJSONObject(i)?.let { bizFrom(it) } },
            payments = (0 until pz.length()).mapNotNull { i -> pz.optJSONObject(i)?.let { paymentFrom(it) } },
            leads = (0 until lz.length()).mapNotNull { i -> lz.optJSONObject(i)?.let { leadFrom(it) } },
            favorites = (0 until fz.length()).map { fz.optString(it, "") }.filter { it.isNotBlank() },
            settings = settingsFrom(o.optJSONObject("settings"))
        )
    }
}
