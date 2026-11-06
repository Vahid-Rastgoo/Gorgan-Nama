package com.vahid.gorgannama.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vahid.gorgannama.data.Business
import com.vahid.gorgannama.data.Store
import com.vahid.gorgannama.util.Fa
import com.vahid.gorgannama.util.Time

@Composable
fun OffersScreen() {
    val today = Time.today()
    val list = Store.visible().filter { Store.hasOffer(it, today) }.sortedWith(
        compareByDescending<Business> { Store.isFeatured(it, today) }.thenBy { it.offerUntil ?: Long.MAX_VALUE }
    )
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("تخفیف‌های گرگان", "پیشنهادهای فعال مغازه‌ها، همین الان")
        if (list.isEmpty()) {
            EmptyState(Icons.Filled.LocalOffer, "فعلاً تخفیفی نیست", "مغازه‌ها که تخفیف بگذارند، اینجا می‌بینی")
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)) {
                items(list, key = { it.id }) { OfferCard(it, today) }
            }
        }
    }
}

@Composable
private fun OfferCard(b: Business, today: Long) {
    val cs = MaterialTheme.colorScheme
    Card(
        onClick = { Nav.push(Route.Detail(b.id)) },
        colors = CardDefaults.cardColors(containerColor = cs.tertiaryContainer),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocalOffer, null, tint = cs.onTertiaryContainer)
                Spacer(Modifier.width(8.dp))
                Text(
                    b.offerTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = cs.onTertiaryContainer
                )
            }
            if (b.offerDetail.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(b.offerDetail, style = MaterialTheme.typography.bodyMedium, color = cs.onTertiaryContainer)
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                BizAvatar(b, 40.dp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(b.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, color = cs.onTertiaryContainer)
                    Text(subtitleOf(b), style = MaterialTheme.typography.bodySmall, color = cs.onTertiaryContainer,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                val until = b.offerUntil
                if (until != null) {
                    Text(
                        if (until == today) "فقط امروز" else "تا " + Fa.date(until),
                        style = MaterialTheme.typography.labelLarge,
                        color = cs.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun FavoritesScreen() {
    val list = Store.businesses.filter { it.id in Store.favorites }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("نشان‌شده‌ها", "مغازه‌هایی که دوست داری دوباره سراغشان بروی")
        if (list.isEmpty()) {
            EmptyState(Icons.Filled.FavoriteBorder, "هنوز چیزی نشان نکردی", "روی قلب کنار هر مغازه بزن")
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)) {
                items(list, key = { it.id }) { BusinessCard(it) }
            }
        }
    }
}
