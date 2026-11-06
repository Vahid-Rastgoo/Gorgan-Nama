package com.vahid.gorgannama.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.vahid.gorgannama.Config
import com.vahid.gorgannama.util.Fa
import com.vahid.gorgannama.util.Time
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/** Single source of truth. Everything is saved to one JSON file in app storage. */
object Store {
    private const val DB = "db.json"
    private var dir: File? = null

    val businesses = mutableStateListOf<Business>()
    val payments = mutableStateListOf<Payment>()
    val leads = mutableStateListOf<Lead>()
    val favorites = mutableStateListOf<String>()

    var settings by mutableStateOf(Settings())
        private set
    var adminUnlocked by mutableStateOf(false)
    var syncing by mutableStateOf(false)
        private set

    fun newId(): String = UUID.randomUUID().toString()

    fun init(ctx: Context) {
        if (dir != null) return
        dir = ctx.filesDir
        val db = File(ctx.filesDir, DB)
        if (db.exists()) {
            runCatching { applyBackup(JSONObject(db.readText())) }
            return
        }
        // first run: load the catalog bundled in assets/catalog.json
        runCatching {
            val text = ctx.assets.open("catalog.json").bufferedReader().use { it.readText() }
            val list = Json.catalogFrom(JSONObject(text))
            businesses.addAll(if (Config.PUBLIC_BUILD) list.filter { !it.sample } else list)
        }
        save()
    }

    private fun save() {
        val d = dir ?: return
        runCatching {
            val text = Json.backup(businesses, payments, leads, favorites, settings).toString()
            val tmp = File(d, "$DB.tmp")
            tmp.writeText(text)
            val target = File(d, DB)
            if (!tmp.renameTo(target)) {
                target.writeText(text)
                tmp.delete()
            }
        }
    }

    // ---------------- queries ----------------
    fun visible(): List<Business> = businesses.filter { it.active }
    fun byId(id: String): Business? = businesses.firstOrNull { it.id == id }
    fun isPaidActive(b: Business, today: Long = Time.today()): Boolean =
        b.plan != Plan.FREE && (b.planEnd ?: -1L) >= today
    fun isFeatured(b: Business, today: Long = Time.today()): Boolean =
        b.plan == Plan.FEATURED && (b.planEnd ?: -1L) >= today
    fun hasOffer(b: Business, today: Long = Time.today()): Boolean =
        b.offerTitle.isNotBlank() && (b.offerUntil ?: Long.MAX_VALUE) >= today
    fun price(p: Plan): Long = when (p) {
        Plan.FREE -> 0L
        Plan.BASIC -> settings.priceBasic
        Plan.FEATURED -> settings.priceFeatured
    }
    fun neighborhoods(): List<String> =
        (Neighborhoods.DEFAULT + businesses.map { it.neighborhood.trim() }).filter { it.isNotBlank() }.distinct()

    fun haystack(b: Business): String = Fa.normalize(
        listOf(
            b.name, Categories.title(b.categoryId), b.neighborhood, b.address,
            b.description, b.offerTitle, b.tags.joinToString(" ")
        ).joinToString(" ")
    )

    // ---------------- mutations ----------------
    fun upsertBusiness(b: Business) {
        val now = System.currentTimeMillis()
        val i = businesses.indexOfFirst { it.id == b.id }
        if (i >= 0) businesses[i] = b.copy(updatedAt = now)
        else businesses.add(b.copy(createdAt = if (b.createdAt == 0L) now else b.createdAt, updatedAt = now))
        save()
    }

    fun deleteBusiness(id: String) {
        businesses.removeAll { it.id == id }
        favorites.remove(id)
        save()
    }

    fun toggleFavorite(id: String) {
        if (!favorites.remove(id)) favorites.add(id)
        save()
    }

    /** Registers a payment and gives the business 365 days (renewals stack on the current end date). */
    fun activatePlan(id: String, plan: Plan, amount: Long): Business? {
        val b = byId(id) ?: return null
        val today = Time.today()
        val renewing = (b.planEnd ?: -1L) >= today
        val start = if (renewing) (b.planStart ?: today) else today
        val end = if (renewing) (b.planEnd ?: today) + 365 else today + 364
        val nb = b.copy(plan = plan, planStart = start, planEnd = end, payStatus = PayStatus.PAID)
        upsertBusiness(nb)
        payments.add(Payment(newId(), id, b.name, amount, today, plan, if (renewing) "تمدید" else "فعال‌سازی"))
        val li = leads.indexOfFirst { it.businessId == id }
        if (li >= 0) leads[li] = leads[li].copy(status = LeadStatus.PAID)
        save()
        return byId(id)
    }

    fun deletePayment(id: String) {
        payments.removeAll { it.id == id }
        save()
    }

    fun upsertLead(l: Lead) {
        val i = leads.indexOfFirst { it.id == l.id }
        if (i >= 0) leads[i] = l else leads.add(l)
        save()
    }

    fun deleteLead(id: String) {
        leads.removeAll { it.id == id }
        save()
    }

    fun updateSettings(s: Settings) {
        settings = s
        save()
    }

    fun unlockAdmin() {
        adminUnlocked = true
        if (!settings.adminDevice) updateSettings(settings.copy(adminDevice = true))
    }

    fun removeSamples(): Int {
        val ids = businesses.filter { it.sample }.map { it.id }.toSet()
        businesses.removeAll { it.sample }
        favorites.removeAll { it in ids }
        save()
        return ids.size
    }

    // ---------------- import / export ----------------
    fun exportCatalog(): String = Json.catalog(businesses.filter { it.active }).toString(2)

    fun exportBackup(): String = Json.backup(businesses, payments, leads, favorites, settings).toString(2)

    fun isBackup(text: String): Boolean =
        runCatching { JSONObject(text).optString("type") == Json.TYPE_BACKUP }.getOrDefault(false)

    fun import(text: String): String {
        val o = runCatching { JSONObject(text) }.getOrNull() ?: return "فایل معتبر نیست"
        return when (o.optString("type")) {
            Json.TYPE_BACKUP -> {
                applyBackup(o)
                save()
                "پشتیبان بازیابی شد: ${Fa.num(businesses.size)} کسب‌وکار"
            }
            Json.TYPE_CATALOG -> {
                val n = runCatching { mergeCatalog(Json.catalogFrom(o), replace = false) }.getOrNull()
                    ?: return "فایل کاتالوگ خراب است"
                save()
                "${Fa.num(n)} کسب‌وکار وارد یا به‌روز شد"
            }
            else -> "نوع فایل شناخته نشد"
        }
    }

    private fun applyBackup(o: JSONObject) {
        val d = Json.backupFrom(o)
        businesses.clear(); businesses.addAll(d.businesses)
        payments.clear(); payments.addAll(d.payments)
        leads.clear(); leads.addAll(d.leads)
        favorites.clear(); favorites.addAll(d.favorites)
        settings = d.settings
    }

    /** Public fields come from the catalog, private admin fields are kept from this phone. */
    private fun mergeCatalog(list: List<Business>, replace: Boolean): Int {
        val local = businesses.associateBy { it.id }
        val merged = list.map { r ->
            val l = local[r.id]
            if (l == null) r else r.copy(
                ownerName = l.ownerName,
                ownerMobile = l.ownerMobile,
                payStatus = l.payStatus,
                adminNote = l.adminNote,
                imageUrl = r.imageUrl.ifBlank { l.imageUrl }
            )
        }
        if (replace) {
            businesses.clear()
            businesses.addAll(merged)
            val ids = merged.map { it.id }.toSet()
            favorites.removeAll { it !in ids }
        } else {
            merged.forEach { m ->
                val i = businesses.indexOfFirst { it.id == m.id }
                if (i >= 0) businesses[i] = m else businesses.add(m)
            }
        }
        return merged.size
    }

    suspend fun sync(): String {
        val url = settings.catalogUrl.trim()
        if (url.isBlank()) return "لینک کاتالوگ در تنظیمات خالی است"
        if (!url.startsWith("https://")) return "لینک کاتالوگ باید با https شروع شود"
        if (syncing) return "در حال به‌روزرسانی…"
        syncing = true
        return try {
            val text = withContext(Dispatchers.IO) {
                val c = URL(url).openConnection() as HttpURLConnection
                c.connectTimeout = 15_000
                c.readTimeout = 20_000
                c.setRequestProperty("Cache-Control", "no-cache")
                try {
                    if (c.responseCode !in 200..299) throw IllegalStateException("کد پاسخ ${c.responseCode}")
                    c.inputStream.bufferedReader().use { it.readText() }
                } finally {
                    c.disconnect()
                }
            }
            val list = Json.catalogFrom(JSONObject(text))
            // on the admin phone never delete local work, just upsert
            val n = mergeCatalog(list, replace = !settings.adminDevice)
            settings = settings.copy(lastSync = System.currentTimeMillis())
            save()
            "به‌روز شد: ${Fa.num(n)} کسب‌وکار"
        } catch (e: Exception) {
            "به‌روزرسانی نشد: ${e.message ?: "خطای شبکه"}"
        } finally {
            syncing = false
        }
    }

    suspend fun autoSync() {
        val s = settings
        if (s.autoSync && s.catalogUrl.isNotBlank() && System.currentTimeMillis() - s.lastSync > 6 * 3_600_000L) {
            sync()
        }
    }
}
