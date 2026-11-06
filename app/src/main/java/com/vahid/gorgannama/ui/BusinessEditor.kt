package com.vahid.gorgannama.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vahid.gorgannama.data.Business
import com.vahid.gorgannama.data.Categories
import com.vahid.gorgannama.data.PayStatus
import com.vahid.gorgannama.data.Plan
import com.vahid.gorgannama.data.Store
import com.vahid.gorgannama.util.Fa
import com.vahid.gorgannama.util.Files
import com.vahid.gorgannama.util.Geo
import com.vahid.gorgannama.util.Hours
import com.vahid.gorgannama.util.Time
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun BusinessEditor(id: String?, fromLeadId: String?) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val today = Time.today()
    val bizId = remember { id ?: Store.newId() }
    val isNew = remember { id == null || Store.byId(id) == null }
    val init = remember {
        (if (id != null) Store.byId(id) else null) ?: run {
            val l = fromLeadId?.let { lid -> Store.leads.firstOrNull { it.id == lid } }
            Business(
                id = bizId,
                name = l?.shopName ?: "",
                categoryId = l?.categoryId?.takeIf { it.isNotBlank() } ?: "other",
                neighborhood = l?.neighborhood ?: "",
                address = l?.address ?: "",
                mobile = l?.phone ?: "",
                ownerName = l?.ownerName ?: "",
                ownerMobile = l?.phone ?: "",
                plan = if (l != null) Plan.BASIC else Plan.FREE
            )
        }
    }

    var name by remember { mutableStateOf(init.name) }
    var cat by remember { mutableStateOf(init.categoryId) }
    var hood by remember { mutableStateOf(init.neighborhood) }
    var address by remember { mutableStateOf(init.address) }
    var phone by remember { mutableStateOf(init.phone) }
    var mobile by remember { mutableStateOf(init.mobile) }
    var instagram by remember { mutableStateOf(init.instagram) }
    var eitaa by remember { mutableStateOf(init.eitaa) }
    var rubika by remember { mutableStateOf(init.rubika) }
    var website by remember { mutableStateOf(init.website) }
    var desc by remember { mutableStateOf(init.description) }
    var tags by remember { mutableStateOf(init.tags.joinToString("، ")) }
    var coords by remember { mutableStateOf(if (init.lat != null && init.lng != null) "${init.lat}, ${init.lng}" else "") }
    var hours by remember { mutableStateOf(init.hours) }
    var closed by remember { mutableStateOf(init.closedDays) }
    var imageUrl by remember { mutableStateOf(init.imageUrl) }
    var offerTitle by remember { mutableStateOf(init.offerTitle) }
    var offerDetail by remember { mutableStateOf(init.offerDetail) }
    var offerUntil by remember { mutableStateOf(init.offerUntil) }
    var verified by remember { mutableStateOf(init.verified) }
    var active by remember { mutableStateOf(init.active) }
    var plan by remember { mutableStateOf(init.plan) }
    var payStatus by remember { mutableStateOf(init.payStatus) }
    var ownerName by remember { mutableStateOf(init.ownerName) }
    var ownerMobile by remember { mutableStateOf(init.ownerMobile) }
    var note by remember { mutableStateOf(init.adminNote) }
    var amount by remember { mutableStateOf(Store.price(init.plan).toString()) }
    var confirmDelete by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = withContext(Dispatchers.IO) { Files.copyImage(ctx, uri) }
                if (path != null) imageUrl = path else toast(ctx, "عکس ذخیره نشد")
            }
        }
    }

    fun build(): Business {
        val g = Geo.parse(coords)
        val base = Store.byId(bizId) ?: init
        return base.copy(
            name = name.trim(),
            categoryId = cat,
            neighborhood = hood.trim(),
            address = address.trim(),
            phone = Fa.latin(phone).trim(),
            mobile = Fa.latin(mobile).trim(),
            instagram = instagram.trim().removePrefix("@"),
            eitaa = eitaa.trim().removePrefix("@"),
            rubika = rubika.trim().removePrefix("@"),
            website = website.trim(),
            description = desc.trim(),
            tags = tags.split(',', '،').map { it.trim() }.filter { it.isNotBlank() }.distinct(),
            lat = g?.first,
            lng = g?.second,
            hours = Fa.latin(hours).trim(),
            closedDays = closed,
            imageUrl = imageUrl.trim(),
            offerTitle = offerTitle.trim(),
            offerDetail = offerDetail.trim(),
            offerUntil = if (offerTitle.isBlank()) null else offerUntil,
            verified = verified,
            active = active,
            plan = plan,
            payStatus = payStatus,
            ownerName = ownerName.trim(),
            ownerMobile = Fa.latin(ownerMobile).trim(),
            adminNote = note.trim()
        )
    }

    fun save(): Business? {
        if (name.isBlank()) {
            toast(ctx, "اسم کسب‌وکار را وارد کن")
            return null
        }
        val b = build()
        Store.upsertBusiness(b)
        if (fromLeadId != null) {
            val l = Store.leads.firstOrNull { it.id == fromLeadId }
            if (l != null && l.businessId != b.id) Store.upsertLead(l.copy(businessId = b.id))
        }
        return Store.byId(b.id)
    }

    Scaffold(topBar = {
        BackTopBar(if (isNew) "ثبت کسب‌وکار" else "ویرایش کسب‌وکار") {
            if (!isNew) IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Filled.Delete, "حذف") }
        }
    }) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Group("اطلاعات اصلی") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BizAvatar(init.copy(name = name.ifBlank { "؟" }, categoryId = cat, imageUrl = imageUrl), 72.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedButton(onClick = {
                            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }) {
                            Icon(Icons.Filled.Image, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("عکس از گالری")
                        }
                        if (imageUrl.isNotBlank()) TextButton(onClick = { imageUrl = "" }) { Text("حذف عکس") }
                    }
                }
                Field(
                    if (imageUrl.startsWith("/")) "" else imageUrl, { imageUrl = it.trim() },
                    "یا لینک عکس (https)", keyboard = KeyboardType.Uri,
                    supporting = "عکس گالری فقط روی همین گوشی است؛ برای نسخه عمومی لینک عکس بگذار"
                )
                Field(name, { name = it }, "اسم کسب‌وکار *")
                ChoiceMenu("دسته", Categories.all.map { it.id }, cat, { Categories.title(it) }, { cat = it })
                Field(hood, { hood = it }, "محله")
                HoodChips { hood = it }
                Field(address, { address = it }, "آدرس دقیق", minLines = 2)
                Field(desc, { desc = it }, "معرفی کوتاه (چه می‌فروشد، چه خدمتی دارد)", minLines = 3)
                Field(tags, { tags = it }, "برچسب‌ها با ویرگول (پیتزا، پیک رایگان)")
            }

            Group("راه‌های ارتباط") {
                Field(phone, { phone = it }, "تلفن ثابت", keyboard = KeyboardType.Phone)
                Field(mobile, { mobile = it }, "موبایل", keyboard = KeyboardType.Phone)
                Field(instagram, { instagram = it }, "آیدی اینستاگرام")
                Field(eitaa, { eitaa = it }, "آیدی ایتا")
                Field(rubika, { rubika = it }, "آیدی روبیکا")
                Field(website, { website = it }, "وب‌سایت", keyboard = KeyboardType.Uri)
            }

            Group("موقعیت روی نقشه") {
                val g = Geo.parse(coords)
                Field(
                    coords, { coords = it }, "مختصات یا لینک گوگل‌مپ",
                    supporting = if (g != null) "ثبت شد: " + Fa.digits("${g.first}, ${g.second}")
                    else "در نقشه روی مغازه بزن، مختصات را کپی کن و اینجا بچسبان (مثل 36.84, 54.43)"
                )
                OutlinedButton(onClick = { Act.map(ctx, build()) }, enabled = g != null || address.isNotBlank()) {
                    Icon(Icons.Filled.Map, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("نمایش روی نقشه")
                }
            }

            Group("ساعت کاری") {
                Field(hours, { hours = it }, "مثل 09:00-13:00, 17:00-22:00", supporting = Hours.statusText(build()))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "۹ تا ۲۱" to "09:00-21:00",
                        "دو شیفت" to "09:00-13:00, 17:00-22:00",
                        "عصرها" to "16:00-23:00",
                        "۲۴ ساعته" to "00:00-24:00"
                    ).forEach { (t, v) -> AssistChip(onClick = { hours = v }, label = { Text(t) }) }
                }
                Text("روزهای تعطیل", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Fa.WEEK.forEach { (v, t) ->
                        FilterChip(
                            selected = v in closed,
                            onClick = { closed = if (v in closed) closed - v else closed + v },
                            label = { Text(t) }
                        )
                    }
                }
            }

            Group("تخفیف یا پیشنهاد ویژه") {
                Field(offerTitle, { offerTitle = it }, "عنوان (مثل ۲۰٪ تخفیف خرید اول)")
                Field(offerDetail, { offerDetail = it }, "توضیح و شرایط", minLines = 2)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf<Pair<Long?, String>>(null to "بدون تاریخ", 7L to "۷ روز", 15L to "۱۵ روز", 30L to "۳۰ روز").forEach { (d, t) ->
                        FilterChip(
                            selected = if (d == null) offerUntil == null else offerUntil == today + d,
                            onClick = { offerUntil = if (d == null) null else today + d },
                            label = { Text(t) }
                        )
                    }
                }
                val until = offerUntil
                if (until != null) Text("معتبر تا " + Fa.longDate(until), style = MaterialTheme.typography.bodySmall)
            }

            Group("نمایش") {
                SwitchRow("در برنامه نمایش داده شود", active) { active = it }
                SwitchRow("تیک تأیید (اطلاعات را چک کرده‌ام)", verified) { verified = it }
            }

            Group("اشتراک و پرداخت (فقط برای تو)") {
                Field(ownerName, { ownerName = it }, "اسم صاحب کسب‌وکار")
                Field(ownerMobile, { ownerMobile = it }, "موبایل صاحب کسب‌وکار (برای یادآوری تمدید)", keyboard = KeyboardType.Phone)
                Text("پلن", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Plan.values().forEach { p ->
                        FilterChip(
                            selected = plan == p,
                            onClick = { plan = p; amount = Store.price(p).toString() },
                            label = { Text(if (p == Plan.FREE) p.title else p.title + " · " + Fa.money(Store.price(p))) }
                        )
                    }
                }
                val current = Store.byId(bizId)
                val end = current?.planEnd
                Text(
                    if (end == null) "هنوز پرداختی ثبت نشده"
                    else if (end >= today) "اعتبار تا ${Fa.longDate(end)} (${Fa.rel(end, today)})"
                    else "منقضی شده در ${Fa.longDate(end)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                ChoiceMenu("وضعیت پرداخت", PayStatus.values().toList(), payStatus, { it.title }, { payStatus = it })
                if (plan != Plan.FREE) {
                    Field(
                        amount, { amount = Fa.latin(it).filter { c -> c.isDigit() } }, "مبلغ دریافتی (تومان)",
                        keyboard = KeyboardType.Number, supporting = amount.toLongOrNull()?.let { Fa.money(it) }
                    )
                    Button(onClick = {
                        val saved = save()
                        if (saved != null) {
                            val nb = Store.activatePlan(saved.id, saved.plan, amount.toLongOrNull() ?: Store.price(saved.plan))
                            payStatus = PayStatus.PAID
                            toast(ctx, "پرداخت ثبت شد. اعتبار تا " + Fa.longDate(nb?.planEnd ?: today))
                        }
                    }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Payments, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if ((end ?: -1L) >= today) "ثبت پرداخت و تمدید یک‌ساله" else "ثبت پرداخت و فعال‌سازی یک‌ساله")
                    }
                }
                val pays = Store.payments.filter { it.businessId == bizId }.sortedByDescending { it.day }
                if (pays.isNotEmpty()) {
                    Text("سابقه پرداخت", style = MaterialTheme.typography.labelLarge)
                    pays.forEach { p ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${Fa.date(p.day)} · ${Fa.money(p.amount)} · ${p.note}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { Store.deletePayment(p.id) }) {
                                Icon(Icons.Filled.Close, "حذف این پرداخت", Modifier.size(18.dp))
                            }
                        }
                    }
                }
                Field(note, { note = it }, "یادداشت خصوصی", minLines = 2)
            }

            Button(
                onClick = { if (save() != null) { toast(ctx, "ذخیره شد"); Nav.pop() } },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("ذخیره") }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("حذف کسب‌وکار") },
            text = { Text("«$name» حذف شود؟ سابقه پرداخت‌ها برای گزارش درآمد می‌ماند.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    Store.deleteBusiness(bizId)
                    Nav.closeBusiness(bizId)
                }) { Text("حذف") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("انصراف") } }
        )
    }
}
