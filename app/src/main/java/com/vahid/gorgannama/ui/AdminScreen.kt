package com.vahid.gorgannama.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vahid.gorgannama.data.Business
import com.vahid.gorgannama.data.Categories
import com.vahid.gorgannama.data.Lead
import com.vahid.gorgannama.data.LeadStatus
import com.vahid.gorgannama.data.PayStatus
import com.vahid.gorgannama.data.Plan
import com.vahid.gorgannama.data.Store
import com.vahid.gorgannama.util.Fa
import com.vahid.gorgannama.util.Files
import com.vahid.gorgannama.util.Time
import kotlinx.coroutines.launch

@Composable
fun AdminScreen(showBack: Boolean) {
    val cs = MaterialTheme.colorScheme
    var tab by remember { mutableIntStateOf(0) }
    val titles = listOf("داشبورد", "مغازه‌ها", "ویزیت‌ها", "تنظیمات")
    Column(Modifier.fillMaxSize()) {
        Surface(color = cs.primary) {
            Row(
                Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBack) {
                    IconButton(onClick = { Nav.pop() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "بازگشت", tint = cs.onPrimary)
                    }
                } else {
                    Spacer(Modifier.width(8.dp))
                }
                Text("پنل مدیریت", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                    color = cs.onPrimary, modifier = Modifier.weight(1f))
                if (Store.adminUnlocked) {
                    IconButton(onClick = { Store.adminUnlocked = false }) {
                        Icon(Icons.Filled.Lock, "قفل کردن", tint = cs.onPrimary)
                    }
                }
            }
        }
        if (!Store.adminUnlocked) {
            PinGate()
        } else {
            TabRow(selectedTabIndex = tab, containerColor = cs.primary, contentColor = cs.onPrimary) {
                titles.forEachIndexed { i, t ->
                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t, maxLines = 1) })
                }
            }
            Box(Modifier.fillMaxSize()) {
                when (tab) {
                    0 -> AdminDashboard()
                    1 -> AdminBusinesses()
                    2 -> AdminLeads()
                    else -> AdminSettings()
                }
            }
        }
    }
}

@Composable
private fun PinGate() {
    val cs = MaterialTheme.colorScheme
    var pin by remember { mutableStateOf("") }
    var err by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Icon(Icons.Filled.Lock, null, tint = cs.primary, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(16.dp))
        Text("ورود به پنل مدیریت", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            "مخصوص صاحب برنامه: مغازه‌ها، پرداخت‌ها، ویزیت‌ها و انتشار کاتالوگ",
            style = MaterialTheme.typography.bodyMedium,
            color = cs.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = pin,
            onValueChange = { pin = Fa.latin(it).filter { c -> c.isDigit() }.take(8); err = false },
            label = { Text("رمز") },
            singleLine = true,
            isError = err,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth()
        )
        if (err) Text("رمز اشتباه است", color = cs.error)
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { if (pin == Store.settings.pin) Store.unlockAdmin() else err = true },
            modifier = Modifier.fillMaxWidth()
        ) { Text("ورود") }
        if (Store.settings.pin == "1234") {
            Spacer(Modifier.height(12.dp))
            Text(
                "رمز پیش‌فرض ۱۲۳۴ است. بعد از ورود، از تنظیمات عوضش کن.",
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ---------------------------------------------------------------- dashboard
@Composable
private fun AdminDashboard() {
    val today = Time.today()
    val target = Store.settings.dailyTarget
    val (jy, jm) = Fa.jalali(today)
    val monthRevenue = Store.payments.filter { p -> val j = Fa.jalali(p.day); j[0] == jy && j[1] == jm }.sumOf { it.amount }
    val totalRevenue = Store.payments.sumOf { it.amount }
    val paidActive = Store.businesses.count { Store.isPaidActive(it, today) }
    val expiring = Store.businesses
        .filter { it.plan != Plan.FREE && (it.planEnd ?: -1L) in today..(today + 30) }
        .sortedBy { it.planEnd ?: 0L }
    val expired = Store.businesses
        .filter { it.plan != Plan.FREE && it.planEnd != null && (it.planEnd ?: 0L) < today }
        .sortedByDescending { it.planEnd ?: 0L }
    val unpaid = Store.businesses.filter { it.plan != Plan.FREE && it.payStatus != PayStatus.PAID }
    val visitsToday = Store.leads.count { it.visitDay == today }
    val last30 = Store.leads.filter { it.visitDay >= today - 30 }
    val won30 = last30.count { it.status == LeadStatus.PAID }
    val conv = if (last30.isEmpty()) 0 else won30 * 100 / last30.size
    val open = setOf(LeadStatus.INTERESTED, LeadStatus.FOLLOW_UP, LeadStatus.NOT_PRESENT)
    val followUps = Store.leads
        .filter { it.status in open && (it.followUpDay ?: Long.MAX_VALUE) <= today }
        .sortedBy { it.followUpDay ?: 0L }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("درآمد این ماه", Fa.money(monthRevenue), Modifier.weight(1f), highlight = true)
                StatCard("کل درآمد", Fa.money(totalRevenue), Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("اشتراک فعال", Fa.num(paidActive), Modifier.weight(1f))
                StatCard("تمدید نزدیک", Fa.num(expiring.size), Modifier.weight(1f))
                StatCard("بدهکار", Fa.num(unpaid.size), Modifier.weight(1f))
            }
        }
        item { TodayCard(visitsToday, target, conv, last30.size, won30) }
        if (followUps.isNotEmpty()) {
            item { SmallHeader("پیگیری‌های امروز (${Fa.num(followUps.size)})") }
            items(followUps, key = { "f" + it.id }) { LeadRow(it) }
        }
        if (expiring.isNotEmpty()) {
            item { SmallHeader("تمدیدهای ۳۰ روز آینده") }
            items(expiring, key = { "e" + it.id }) { RenewRow(it, today) }
        }
        if (expired.isNotEmpty()) {
            item { SmallHeader("منقضی‌شده‌ها: وقت تماس") }
            items(expired, key = { "x" + it.id }) { RenewRow(it, today) }
        }
        if (unpaid.isNotEmpty()) {
            item { SmallHeader("منتظر پرداخت") }
            items(unpaid, key = { "u" + it.id }) { AdminBizRow(it, today) }
        }
    }
}

@Composable
private fun SmallHeader(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 10.dp))
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier, highlight: Boolean = false) {
    val cs = MaterialTheme.colorScheme
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) cs.primary else cs.surface,
            contentColor = if (highlight) cs.onPrimary else cs.onSurface
        )
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun TodayCard(visits: Int, target: Int, conv: Int, n30: Int, won30: Int) {
    val cs = MaterialTheme.colorScheme
    Card(colors = CardDefaults.cardColors(containerColor = cs.secondaryContainer, contentColor = cs.onSecondaryContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("ویزیت امروز", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("${Fa.num(visits)} از ${Fa.num(target)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            }
            LinearProgressIndicator(
                progress = { (visits.toFloat() / target.coerceAtLeast(1)).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp)
            )
            Text(
                if (n30 == 0) "هنوز ویزیتی ثبت نشده؛ هر مغازه را بعد از سر زدن ثبت کن."
                else "۳۰ روز اخیر: ${Fa.num(n30)} ویزیت، ${Fa.num(won30)} مشتری، نرخ تبدیل ٪${Fa.num(conv)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = { Nav.push(Route.EditLead(null)) }) {
                Icon(Icons.Filled.Add, null)
                Spacer(Modifier.width(6.dp))
                Text("ثبت ویزیت جدید")
            }
        }
    }
}

@Composable
private fun RenewRow(b: Business, today: Long) {
    val ctx = LocalContext.current
    val end = b.planEnd ?: today
    val phone = b.ownerMobile.ifBlank { b.mobile.ifBlank { b.phone } }
    val owner = b.ownerName.ifBlank { "" }
    Card(onClick = { Nav.push(Route.EditBusiness(b.id)) }, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(b.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${b.plan.title} · پایان: ${Fa.date(end)} (${Fa.rel(end, today)})", style = MaterialTheme.typography.bodySmall)
                Text("تمدید: " + Fa.money(Store.price(b.plan)), style = MaterialTheme.typography.bodySmall)
            }
            IconButton(enabled = phone.isNotBlank(), onClick = {
                val msg = buildString {
                    append("سلام")
                    if (owner.isNotBlank()) append(" ").append(owner)
                    append("، اشتراک «").append(b.name).append("» در گرگان‌نما ")
                    append(if (end >= today) "در تاریخ ${Fa.date(end)} تمام می‌شود." else "تمام شده است.")
                    append(" تمدید یک‌ساله: ").append(Fa.money(Store.price(b.plan))).append(".")
                }
                Act.sms(ctx, phone, msg)
            }) { Icon(Icons.Filled.Sms, "پیامک یادآوری") }
            IconButton(enabled = phone.isNotBlank(), onClick = { Act.dial(ctx, phone) }) { Icon(Icons.Filled.Call, "تماس") }
        }
    }
}

// ---------------------------------------------------------------- businesses
private enum class BizFilter(val title: String) {
    ALL("همه"), PAID("پولی فعال"), EXPIRED("منقضی"), UNPAID("بدهکار"), FREE("رایگان"), HIDDEN("مخفی")
}

@Composable
private fun AdminBusinesses() {
    var q by remember { mutableStateOf("") }
    var f by remember { mutableStateOf(BizFilter.ALL) }
    val today = Time.today()
    val tokens = Fa.normalize(q).split(' ').filter { it.isNotBlank() }
    val list = Store.businesses.filter { b ->
        when (f) {
            BizFilter.ALL -> true
            BizFilter.PAID -> Store.isPaidActive(b, today)
            BizFilter.EXPIRED -> b.plan != Plan.FREE && b.planEnd != null && (b.planEnd ?: 0L) < today
            BizFilter.UNPAID -> b.plan != Plan.FREE && b.payStatus != PayStatus.PAID
            BizFilter.FREE -> b.plan == Plan.FREE
            BizFilter.HIDDEN -> !b.active
        } && (tokens.isEmpty() || Store.haystack(b).let { h -> tokens.all { h.contains(it) } } ||
            Fa.latin(b.ownerMobile + b.mobile + b.phone).contains(Fa.latin(q.trim())))
    }.sortedByDescending { it.updatedAt }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = q, onValueChange = { q = it },
                placeholder = { Text("جستجو در مغازه‌ها") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(10.dp))
            FilledIconButton(onClick = { Nav.push(Route.EditBusiness(null)) }, modifier = Modifier.size(52.dp)) {
                Icon(Icons.Filled.Add, "افزودن")
            }
        }
        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BizFilter.values().forEach { x -> FilterChip(selected = f == x, onClick = { f = x }, label = { Text(x.title) }) }
        }
        Text("${Fa.num(list.size)} مورد", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 16.dp))
        if (list.isEmpty()) {
            EmptyState(Icons.Filled.Storefront, "موردی نیست", "با دکمه + اولین مغازه را اضافه کن")
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)) {
                items(list, key = { it.id }) { b ->
                    Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) { AdminBizRow(b, today) }
                }
            }
        }
    }
}

fun planLine(b: Business, today: Long): String {
    val end = b.planEnd
    return if (b.plan == Plan.FREE) "رایگان"
    else if (end == null) "${b.plan.title} · فعال نشده"
    else if (end >= today) "${b.plan.title} · تا ${Fa.date(end)}"
    else "${b.plan.title} · منقضی ${Fa.date(end)}"
}

@Composable
private fun AdminBizRow(b: Business, today: Long) {
    val cs = MaterialTheme.colorScheme
    Card(onClick = { Nav.push(Route.EditBusiness(b.id)) }, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            BizAvatar(b, 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    b.name + if (b.active) "" else " (مخفی)",
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(planLine(b, today), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
            }
            if (b.plan != Plan.FREE) {
                when (b.payStatus) {
                    PayStatus.PAID -> Pill(b.payStatus.title, cs.primaryContainer, cs.onPrimaryContainer)
                    PayStatus.PARTIAL -> Pill(b.payStatus.title, cs.secondaryContainer, cs.onSecondaryContainer)
                    PayStatus.UNPAID -> Pill(b.payStatus.title, cs.tertiaryContainer, cs.onTertiaryContainer)
                }
            }
        }
    }
}

// ---------------------------------------------------------------- leads (field sales)
@Composable
private fun AdminLeads() {
    var filter by remember { mutableStateOf<LeadStatus?>(null) }
    val today = Time.today()
    val list = Store.leads.filter { filter == null || it.status == filter }
        .sortedWith(compareByDescending<Lead> { it.visitDay }.thenBy { it.shopName })
    val visitsToday = Store.leads.count { it.visitDay == today }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("امروز ${Fa.num(visitsToday)} از ${Fa.num(Store.settings.dailyTarget)} ویزیت",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("کل: ${Fa.num(Store.leads.size)} · مشتری شده: ${Fa.num(Store.leads.count { it.status == LeadStatus.PAID })}",
                    style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = { Nav.push(Route.EditLead(null)) }) {
                Icon(Icons.Filled.Add, null)
                Spacer(Modifier.width(4.dp))
                Text("ویزیت")
            }
        }
        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(selected = filter == null, onClick = { filter = null }, label = { Text("همه") })
            LeadStatus.values().forEach { st ->
                FilterChip(
                    selected = filter == st,
                    onClick = { filter = st },
                    label = { Text(st.title + " " + Fa.num(Store.leads.count { it.status == st })) }
                )
            }
        }
        if (list.isEmpty()) {
            EmptyState(Icons.Filled.Storefront, "ویزیتی ثبت نشده", "هر مغازه‌ای که سر زدی را ثبت کن تا پیگیری‌ها یادت نرود")
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(list, key = { it.id }) { LeadRow(it) }
            }
        }
    }
}

@Composable
private fun LeadRow(l: Lead) {
    val ctx = LocalContext.current
    val cs = MaterialTheme.colorScheme
    val today = Time.today()
    Card(onClick = { Nav.push(Route.EditLead(l.id)) }, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(l.shopName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                when (l.status) {
                    LeadStatus.PAID -> Pill(l.status.title, cs.primaryContainer, cs.onPrimaryContainer)
                    LeadStatus.REJECTED -> Pill(l.status.title, cs.surfaceVariant, cs.onSurfaceVariant)
                    else -> Pill(l.status.title, cs.secondaryContainer, cs.onSecondaryContainer)
                }
            }
            val info = listOf(l.ownerName, l.neighborhood, Categories.title(l.categoryId)).filter { it.isNotBlank() }
            if (info.isNotEmpty()) Text(info.joinToString(" · "), style = MaterialTheme.typography.bodySmall)
            val follow = l.followUpDay
            Text(
                "ویزیت: ${Fa.date(l.visitDay)}" + if (follow != null) "  ·  پیگیری: ${Fa.rel(follow, today)}" else "",
                style = MaterialTheme.typography.bodySmall,
                color = if (follow != null && follow <= today && l.status != LeadStatus.PAID) cs.tertiary else cs.onSurfaceVariant
            )
            if (l.note.isNotBlank()) Text(l.note, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (l.phone.isNotBlank()) {
                    AssistChip(onClick = { Act.dial(ctx, l.phone) }, label = { Text("تماس") },
                        leadingIcon = { Icon(Icons.Filled.Call, null, Modifier.size(18.dp)) })
                }
                val bid = l.businessId
                if (bid != null) {
                    AssistChip(onClick = { Nav.push(Route.EditBusiness(bid)) }, label = { Text("پرونده مغازه") },
                        leadingIcon = { Icon(Icons.Filled.Storefront, null, Modifier.size(18.dp)) })
                } else if (l.status != LeadStatus.REJECTED) {
                    AssistChip(onClick = { Nav.push(Route.EditBusiness(null, l.id)) }, label = { Text("تبدیل به مشتری") },
                        leadingIcon = { Icon(Icons.Filled.PersonAdd, null, Modifier.size(18.dp)) })
                }
            }
        }
    }
}

// ---------------------------------------------------------------- settings
@Composable
private fun AdminSettings() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val s = Store.settings
    val today = Time.today()
    var basic by remember { mutableStateOf(s.priceBasic.toString()) }
    var featured by remember { mutableStateOf(s.priceFeatured.toString()) }
    var target by remember { mutableStateOf(s.dailyTarget.toString()) }
    var phone by remember { mutableStateOf(s.adminPhone) }
    var url by remember { mutableStateOf(s.catalogUrl) }
    var auto by remember { mutableStateOf(s.autoSync) }
    var showTab by remember { mutableStateOf(s.showAdminTab) }
    var newPin by remember { mutableStateOf("") }
    var pendingRestore by remember { mutableStateOf<String?>(null) }
    var confirmSamples by remember { mutableStateOf(false) }

    val exportCatalog = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) toast(ctx, if (Files.write(ctx, uri, Store.exportCatalog())) "کاتالوگ ذخیره شد" else "ذخیره نشد")
    }
    val exportBackup = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) toast(ctx, if (Files.write(ctx, uri, Store.exportBackup())) "پشتیبان ذخیره شد" else "ذخیره نشد")
    }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val text = Files.read(ctx, uri)
            if (text == null) toast(ctx, "فایل خوانده نشد")
            else if (Store.isBackup(text)) pendingRestore = text
            else toast(ctx, Store.import(text))
        }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Group("قیمت‌ها و هدف روزانه") {
            Field(basic, { basic = Fa.latin(it).filter { c -> c.isDigit() } }, "اشتراک پایه یک‌ساله (تومان)", keyboard = KeyboardType.Number,
                supporting = basic.toLongOrNull()?.let { Fa.money(it) })
            Field(featured, { featured = Fa.latin(it).filter { c -> c.isDigit() } }, "اشتراک ویژه یک‌ساله (تومان)", keyboard = KeyboardType.Number,
                supporting = featured.toLongOrNull()?.let { Fa.money(it) })
            Field(target, { target = Fa.latin(it).filter { c -> c.isDigit() }.take(3) }, "هدف ویزیت در روز", keyboard = KeyboardType.Number)
        }
        Group("ارتباط و انتشار") {
            Field(phone, { phone = it }, "شماره پشتیبانی تو (ثبت مغازه و گزارش)", keyboard = KeyboardType.Phone)
            Field(url, { url = it.trim() }, "لینک کاتالوگ آنلاین (https)", keyboard = KeyboardType.Uri,
                supporting = "فایل خروجی کاتالوگ را جایی آپلود کن و لینک مستقیمش را اینجا بگذار")
            SwitchRow("به‌روزرسانی خودکار از این لینک", auto) { auto = it }
            SwitchRow("نمایش تب مدیریت پایین صفحه", showTab) { showTab = it }
            Text("اگر خاموش باشد، با ۵ ضربه روی عنوان «گرگان‌نما» در صفحه خانه وارد پنل می‌شوی.",
                style = MaterialTheme.typography.bodySmall)
        }
        Button(onClick = {
            Store.updateSettings(
                Store.settings.copy(
                    priceBasic = basic.toLongOrNull() ?: s.priceBasic,
                    priceFeatured = featured.toLongOrNull() ?: s.priceFeatured,
                    dailyTarget = (target.toIntOrNull() ?: s.dailyTarget).coerceAtLeast(1),
                    adminPhone = Fa.latin(phone).trim(),
                    catalogUrl = url,
                    autoSync = auto,
                    showAdminTab = showTab
                )
            )
            toast(ctx, "تنظیمات ذخیره شد")
        }, modifier = Modifier.fillMaxWidth()) { Text("ذخیره تنظیمات") }

        Group("رمز پنل") {
            Field(newPin, { newPin = Fa.latin(it).filter { c -> c.isDigit() }.take(8) }, "رمز جدید (۴ تا ۸ رقم)", keyboard = KeyboardType.NumberPassword)
            OutlinedButton(onClick = {
                if (newPin.length < 4) toast(ctx, "رمز حداقل ۴ رقم باشد")
                else {
                    Store.updateSettings(Store.settings.copy(pin = newPin))
                    newPin = ""
                    toast(ctx, "رمز عوض شد")
                }
            }) { Text("تغییر رمز") }
        }

        Group("داده‌ها و انتشار") {
            val last = Store.settings.lastSync
            Text(
                if (last == 0L) "هنوز از لینک آنلاین به‌روز نشده"
                else "آخرین به‌روزرسانی: " + Fa.date(java.time.Instant.ofEpochMilli(last).atZone(java.time.ZoneId.of("Asia/Tehran")).toLocalDate().toEpochDay()),
                style = MaterialTheme.typography.bodySmall
            )
            IconTextButton(Icons.Filled.Sync, if (Store.syncing) "در حال به‌روزرسانی…" else "به‌روزرسانی از کاتالوگ آنلاین", enabled = !Store.syncing) {
                scope.launch { toast(ctx, Store.sync()) }
            }
            IconTextButton(Icons.Filled.Upload, "خروجی کاتالوگ عمومی (برای انتشار)") {
                exportCatalog.launch("gorgan-catalog.json")
            }
            IconTextButton(Icons.Filled.Backup, "پشتیبان کامل (با پرداخت‌ها و ویزیت‌ها)") {
                exportBackup.launch("gorgan-backup-${Fa.fileDate(today)}.json")
            }
            IconTextButton(Icons.Filled.FolderOpen, "ورود کاتالوگ یا بازیابی پشتیبان") {
                importer.launch(arrayOf("application/json", "text/plain", "application/octet-stream", "*/*"))
            }
            if (Store.businesses.any { it.sample }) {
                OutlinedButton(
                    onClick = { confirmSamples = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("حذف مغازه‌های نمونه") }
            }
            Text("کاتالوگ عمومی فقط اطلاعات نمایشی را دارد؛ پرداخت‌ها، یادداشت‌ها و ویزیت‌ها فقط در پشتیبان کامل هستند.",
                style = MaterialTheme.typography.bodySmall)
        }
        Text("گرگان‌نما · نسخه ۱.۰", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(24.dp))
    }

    val restoreText = pendingRestore
    if (restoreText != null) {
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = { Text("بازیابی پشتیبان") },
            text = { Text("همه داده‌های فعلی این گوشی با فایل پشتیبان جایگزین می‌شود. ادامه می‌دهی؟") },
            confirmButton = { TextButton(onClick = { toast(ctx, Store.import(restoreText)); pendingRestore = null }) { Text("بازیابی") } },
            dismissButton = { TextButton(onClick = { pendingRestore = null }) { Text("انصراف") } }
        )
    }
    if (confirmSamples) {
        AlertDialog(
            onDismissRequest = { confirmSamples = false },
            title = { Text("حذف نمونه‌ها") },
            text = { Text("مغازه‌های نمونه که برای دیدن ظاهر برنامه گذاشته شده‌اند حذف شوند؟") },
            confirmButton = { TextButton(onClick = { val n = Store.removeSamples(); confirmSamples = false; toast(ctx, "${Fa.num(n)} نمونه حذف شد") }) { Text("حذف") } },
            dismissButton = { TextButton(onClick = { confirmSamples = false }) { Text("انصراف") } }
        )
    }
}
