package com.vahid.gorgannama.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vahid.gorgannama.data.Business
import com.vahid.gorgannama.data.Categories
import com.vahid.gorgannama.data.Store
import com.vahid.gorgannama.util.Fa
import com.vahid.gorgannama.util.Hours
import com.vahid.gorgannama.util.Time

private fun shareText(b: Business): String {
    val phone = b.mobile.ifBlank { b.phone }
    return buildString {
        append(b.name).append('\n')
        append(subtitleOf(b)).append('\n')
        if (b.address.isNotBlank()) append("آدرس: ").append(b.address).append('\n')
        if (phone.isNotBlank()) append("تماس: ").append(phone).append('\n')
        if (b.offerTitle.isNotBlank()) append("پیشنهاد: ").append(b.offerTitle).append('\n')
        append("از گرگان‌نما")
    }
}

@Composable
fun DetailScreen(id: String) {
    val b = Store.byId(id)
    if (b == null) {
        Scaffold(topBar = { BackTopBar("پیدا نشد") }) { p ->
            Box(Modifier.padding(p)) {
                EmptyState(Icons.Filled.Storefront, "این کسب‌وکار دیگر نیست", "ممکن است حذف شده باشد")
            }
        }
        return
    }
    val ctx = LocalContext.current
    val cs = MaterialTheme.colorScheme
    val today = Time.today()
    val fav = b.id in Store.favorites
    val phone = b.mobile.ifBlank { b.phone }

    Scaffold(topBar = {
        BackTopBar(b.name) {
            IconButton(onClick = { Store.toggleFavorite(b.id) }) {
                Icon(if (fav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, "نشان کردن")
            }
            if (Store.adminUnlocked) {
                IconButton(onClick = { Nav.push(Route.EditBusiness(b.id)) }) { Icon(Icons.Filled.Edit, "ویرایش") }
            }
        }
    }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState())) {
            BizBanner(b, 200.dp)
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        b.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (b.verified) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Filled.Verified, "تأییدشده", tint = cs.primary)
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Pill(Categories.title(b.categoryId), cs.surfaceVariant, cs.onSurfaceVariant)
                    if (b.neighborhood.isNotBlank()) Pill(b.neighborhood, cs.surfaceVariant, cs.onSurfaceVariant)
                    OpenBadge(b)
                    if (Store.isFeatured(b, today)) Pill("ویترین ویژه", cs.secondaryContainer, cs.onSecondaryContainer)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { Act.dial(ctx, phone) },
                        enabled = phone.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) { ActionLabel(Icons.Filled.Call, "تماس") }
                    FilledTonalButton(
                        onClick = { Act.map(ctx, b) },
                        enabled = b.lat != null || b.address.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) { ActionLabel(Icons.Filled.Directions, "مسیریابی") }
                    FilledTonalButton(
                        onClick = { Act.share(ctx, shareText(b)) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) { ActionLabel(Icons.Filled.Share, "معرفی") }
                }

                if (Store.hasOffer(b, today)) {
                    Surface(color = cs.tertiaryContainer, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.LocalOffer, null, tint = cs.onTertiaryContainer)
                                Spacer(Modifier.width(8.dp))
                                Text(b.offerTitle, style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold, color = cs.onTertiaryContainer)
                            }
                            if (b.offerDetail.isNotBlank()) Text(b.offerDetail, color = cs.onTertiaryContainer)
                            val until = b.offerUntil
                            if (until != null) Text("معتبر تا " + Fa.longDate(until),
                                style = MaterialTheme.typography.labelLarge, color = cs.onTertiaryContainer)
                        }
                    }
                }

                Surface(color = cs.surfaceVariant, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Schedule, null, tint = cs.onSurfaceVariant)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(Hours.statusText(b), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            if (b.hours.isNotBlank()) Text("ساعت کاری: " + Fa.digits(b.hours), style = MaterialTheme.typography.bodyMedium)
                            val closed = Hours.closedText(b)
                            if (closed.isNotBlank()) Text(closed, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                if (b.description.isNotBlank()) Text(b.description, style = MaterialTheme.typography.bodyLarge)

                Card(colors = CardDefaults.cardColors(containerColor = cs.surface)) {
                    if (b.address.isNotBlank()) InfoRow(Icons.Filled.Place, "آدرس", b.address) { Act.map(ctx, b) }
                    if (b.phone.isNotBlank()) InfoRow(Icons.Filled.Phone, "تلفن", Fa.digits(b.phone)) { Act.dial(ctx, b.phone) }
                    if (b.mobile.isNotBlank()) InfoRow(Icons.Filled.Smartphone, "موبایل", Fa.digits(b.mobile)) { Act.dial(ctx, b.mobile) }
                    if (b.instagram.isNotBlank()) InfoRow(Icons.Filled.PhotoCamera, "اینستاگرام", "@" + b.instagram.removePrefix("@")) {
                        Act.view(ctx, Act.social("https://instagram.com/", b.instagram))
                    }
                    if (b.eitaa.isNotBlank()) InfoRow(Icons.Filled.Forum, "ایتا", "@" + b.eitaa.removePrefix("@")) {
                        Act.view(ctx, Act.social("https://eitaa.com/", b.eitaa))
                    }
                    if (b.rubika.isNotBlank()) InfoRow(Icons.Filled.Forum, "روبیکا", "@" + b.rubika.removePrefix("@")) {
                        Act.view(ctx, Act.social("https://rubika.ir/", b.rubika))
                    }
                    if (b.website.isNotBlank()) InfoRow(Icons.Filled.Language, "وب‌سایت", b.website) { Act.view(ctx, Act.web(b.website)) }
                }

                if (b.tags.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        b.tags.forEach { t -> Pill("#$t", cs.primaryContainer, cs.onPrimaryContainer) }
                    }
                }

                TextButton(onClick = {
                    val admin = Store.settings.adminPhone
                    if (admin.isBlank()) toast(ctx, "شماره پشتیبانی هنوز تنظیم نشده")
                    else Act.sms(ctx, admin, "گزارش اشتباه در اطلاعات «${b.name}»:\n")
                }) {
                    Icon(Icons.Filled.Flag, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("اطلاعات اشتباه است؟ گزارش بده")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ActionLabel(icon: ImageVector, text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null)
        Spacer(Modifier.height(2.dp))
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(value) },
        overlineContent = { Text(label) },
        leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable(onClick = onClick)
    )
}
