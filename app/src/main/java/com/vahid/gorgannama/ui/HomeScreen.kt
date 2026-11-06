package com.vahid.gorgannama.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vahid.gorgannama.data.Business
import com.vahid.gorgannama.data.Categories
import com.vahid.gorgannama.data.Category
import com.vahid.gorgannama.data.Store
import com.vahid.gorgannama.util.Fa
import com.vahid.gorgannama.util.Hours
import com.vahid.gorgannama.util.Time
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun HomeScreen() {
    val today = Time.today()
    val list = Store.visible()
    val featured = list.filter { Store.isFeatured(it, today) }
    val offers = list.filter { Store.hasOffer(it, today) }
    val newest = list.sortedByDescending { it.createdAt }.take(6)

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { HomeHeader(total = list.size, openNow = list.count { Hours.isOpen(it) == true }) }
        item { SectionTitle("دسته‌بندی‌ها") }
        item {
            CategoryGrid { id ->
                Nav.searchCategory = id
                Nav.tab = 1
            }
        }
        if (featured.isNotEmpty()) {
            item { SectionTitle("ویترین ویژه") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(featured, key = { "f" + it.id }) { FeaturedCard(it) }
                }
            }
        }
        if (offers.isNotEmpty()) {
            item { SectionTitle("تخفیف‌های فعال", "همه") { Nav.tab = 2 } }
            items(offers.take(3), key = { "o" + it.id }) { BusinessCard(it) }
        }
        if (newest.isNotEmpty()) {
            item { SectionTitle("تازه اضافه‌شده‌ها", "جستجو") { Nav.searchCategory = null; Nav.tab = 1 } }
            items(newest, key = { "n" + it.id }) { BusinessCard(it) }
        } else {
            item {
                EmptyState(Icons.Filled.Storefront, "هنوز کسب‌وکاری ثبت نشده", "از تب مدیریت اولین مغازه را اضافه کن")
            }
        }
        item { JoinCta() }
    }
}

@Composable
private fun HomeHeader(total: Int, openNow: Int) {
    val cs = MaterialTheme.colorScheme
    var taps by remember { mutableIntStateOf(0) }
    Box(Modifier.fillMaxWidth().background(cs.primary)) {
        Canvas(Modifier.matchParentSize()) {
            drawForest(Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.11f))
        }
        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 28.dp)) {
            Text(
                "گرگان‌نما",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    taps += 1
                    if (taps >= 5) {
                        taps = 0
                        Nav.push(Route.Admin)
                    }
                }
            )
            Text(
                "هرچی تو گرگان لازم داری، یه‌جا",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(20.dp))
            Surface(
                onClick = { Nav.tab = 1 },
                shape = RoundedCornerShape(16.dp),
                color = cs.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Search, null, tint = cs.onSurfaceVariant)
                    Spacer(Modifier.width(10.dp))
                    Text("اسم مغازه، خدمت یا محله…", color = cs.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeaderStat("${Fa.num(total)} کسب‌وکار")
                HeaderStat("${Fa.num(openNow)} تا الان بازند")
            }
        }
    }
}

/** The signature: a quiet Hyrcanian treeline behind the header. */
private fun DrawScope.drawForest(back: Color, front: Color) {
    val w = size.width
    val h = size.height
    fun trees(color: Color, base: Float, height: Float, step: Float, offset: Float) {
        val path = Path()
        var x = -offset
        while (x < w + step) {
            val k = 0.7f + 0.3f * abs(sin(x / 41f))
            path.moveTo(x, base)
            path.lineTo(x + step / 2f, base - height * k)
            path.lineTo(x + step, base)
            path.close()
            x += step
        }
        path.addRect(Rect(0f, base, w, h))
        drawPath(path, color)
    }
    trees(back, h * 0.80f, h * 0.30f, w / 9f, w / 28f)
    trees(front, h * 0.93f, h * 0.22f, w / 13f, 0f)
}

@Composable
private fun HeaderStat(text: String) {
    Surface(color = Color.White.copy(alpha = 0.16f), shape = RoundedCornerShape(50)) {
        Text(
            text,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun CategoryGrid(onPick: (String) -> Unit) {
    Column(Modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Categories.all.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { c -> CategoryTile(c, Modifier.weight(1f)) { onPick(c.id) } }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun CategoryTile(c: Category, modifier: Modifier, onClick: () -> Unit) {
    val color = catColor(c.id)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = modifier
    ) {
        Column(
            Modifier.padding(vertical = 14.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(catIcon(c.id), null, tint = color)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                c.short,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FeaturedCard(b: Business) {
    Card(
        onClick = { Nav.push(Route.Detail(b.id)) },
        modifier = Modifier.width(250.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box {
            BizBanner(b, 130.dp)
            Surface(
                color = Saffron,
                shape = RoundedCornerShape(bottomStart = 14.dp),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text(
                    "ویژه",
                    color = OnSaffron,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
        Column(Modifier.padding(12.dp)) {
            Text(
                b.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                subtitleOf(b),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            OpenBadge(b)
        }
    }
}

@Composable
private fun JoinCta() {
    val ctx = LocalContext.current
    val cs = MaterialTheme.colorScheme
    val phone = Store.settings.adminPhone
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = cs.secondaryContainer)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("کسب‌وکار داری؟", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = cs.onSecondaryContainer)
            Spacer(Modifier.height(4.dp))
            Text(
                "مغازه‌ات را در گرگان‌نما ثبت کن تا مشتری‌های محله راحت پیدات کنند.",
                style = MaterialTheme.typography.bodyMedium,
                color = cs.onSecondaryContainer
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    if (phone.isBlank()) toast(ctx, "شماره پشتیبانی هنوز تنظیم نشده")
                    else Act.sms(ctx, phone, "سلام، می‌خواهم کسب‌وکارم در گرگان‌نما ثبت شود.\nاسم مغازه: \nمحله: \nشماره: ")
                }) { Text("درخواست ثبت") }
                OutlinedButton(onClick = {
                    if (phone.isBlank()) toast(ctx, "شماره پشتیبانی هنوز تنظیم نشده") else Act.dial(ctx, phone)
                }) { Text("تماس") }
            }
        }
    }
}
