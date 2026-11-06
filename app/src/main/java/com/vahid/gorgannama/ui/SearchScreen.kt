package com.vahid.gorgannama.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vahid.gorgannama.data.Business
import com.vahid.gorgannama.data.Categories
import com.vahid.gorgannama.data.Store
import com.vahid.gorgannama.util.Fa
import com.vahid.gorgannama.util.Hours
import com.vahid.gorgannama.util.Time

private enum class SortBy(val title: String) { SMART("پیشنهادی"), NAME("الفبا"), NEWEST("جدیدترین") }

@Composable
fun SearchScreen() {
    val cs = MaterialTheme.colorScheme
    var q by rememberSaveable { mutableStateOf("") }
    var openNow by rememberSaveable { mutableStateOf(false) }
    var offerOnly by rememberSaveable { mutableStateOf(false) }
    var featuredOnly by rememberSaveable { mutableStateOf(false) }
    var verifiedOnly by rememberSaveable { mutableStateOf(false) }
    var hood by rememberSaveable { mutableStateOf("") }
    var sort by remember { mutableStateOf(SortBy.SMART) }
    val cat = Nav.searchCategory
    val today = Time.today()
    val tokens = Fa.normalize(q).split(' ').filter { it.isNotBlank() }

    val filtered = Store.visible().filter { b ->
        (cat == null || b.categoryId == cat) &&
            (hood.isBlank() || b.neighborhood == hood) &&
            (!openNow || Hours.isOpen(b) == true) &&
            (!offerOnly || Store.hasOffer(b, today)) &&
            (!featuredOnly || Store.isFeatured(b, today)) &&
            (!verifiedOnly || b.verified) &&
            (tokens.isEmpty() || Store.haystack(b).let { h -> tokens.all { t -> h.contains(t) } })
    }
    val results = when (sort) {
        SortBy.SMART -> filtered.sortedWith(
            compareByDescending<Business> { Store.isFeatured(it, today) }
                .thenByDescending { Store.isPaidActive(it, today) }
                .thenByDescending { Hours.isOpen(it) == true }
                .thenBy { it.name }
        )
        SortBy.NAME -> filtered.sortedBy { it.name }
        SortBy.NEWEST -> filtered.sortedByDescending { it.createdAt }
    }

    Column(Modifier.fillMaxSize()) {
        Surface(color = cs.primary) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                TextField(
                    value = q,
                    onValueChange = { q = it },
                    placeholder = { Text("اسم مغازه، خدمت یا محله…") },
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    trailingIcon = if (q.isNotEmpty()) {
                        { IconButton(onClick = { q = "" }) { Icon(Icons.Filled.Close, "پاک کردن") } }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = cs.surface,
                        unfocusedContainerColor = cs.surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(selected = openNow, onClick = { openNow = !openNow }, label = { Text("الان باز") },
                leadingIcon = { Icon(Icons.Filled.Schedule, null, Modifier.size(18.dp)) })
            FilterChip(selected = offerOnly, onClick = { offerOnly = !offerOnly }, label = { Text("تخفیف‌دار") },
                leadingIcon = { Icon(Icons.Filled.LocalOffer, null, Modifier.size(18.dp)) })
            FilterChip(selected = featuredOnly, onClick = { featuredOnly = !featuredOnly }, label = { Text("ویژه") },
                leadingIcon = { Icon(Icons.Filled.Star, null, Modifier.size(18.dp)) })
            FilterChip(selected = verifiedOnly, onClick = { verifiedOnly = !verifiedOnly }, label = { Text("تأییدشده") },
                leadingIcon = { Icon(Icons.Filled.Verified, null, Modifier.size(18.dp)) })
        }
        Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChoiceMenu(
                "دسته",
                listOf<String?>(null) + Categories.all.map { it.id },
                cat,
                { id -> if (id == null) "همه" else Categories.title(id) },
                { Nav.searchCategory = it },
                Modifier.weight(1f)
            )
            ChoiceMenu(
                "محله",
                listOf("") + Store.neighborhoods(),
                hood,
                { it.ifBlank { "همه" } },
                { hood = it },
                Modifier.weight(1f)
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${Fa.num(results.size)} نتیجه", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
            ChoiceMenu("", SortBy.values().toList(), sort, { it.title }, { sort = it }, Modifier.width(150.dp))
        }
        if (results.isEmpty()) {
            EmptyState(Icons.Filled.SearchOff, "چیزی پیدا نشد", "فیلترها را کمتر کن یا کلمه دیگری بنویس")
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
                items(results, key = { it.id }) { BusinessCard(it) }
            }
        }
    }
}
