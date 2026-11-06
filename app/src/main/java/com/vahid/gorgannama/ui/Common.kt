package com.vahid.gorgannama.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.vahid.gorgannama.data.Business
import com.vahid.gorgannama.data.Categories
import com.vahid.gorgannama.data.Store
import com.vahid.gorgannama.util.Fa
import com.vahid.gorgannama.util.Hours
import com.vahid.gorgannama.util.Time
import java.io.File

fun toast(ctx: Context, msg: String) {
    Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
}

object Act {
    private fun start(ctx: Context, intent: Intent, fail: String = "برنامه‌ای برای باز کردن پیدا نشد") {
        try {
            ctx.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: ActivityNotFoundException) {
            toast(ctx, fail)
        } catch (e: SecurityException) {
            toast(ctx, fail)
        }
    }

    private fun clean(phone: String) = Fa.latin(phone).filter { it.isDigit() || it == '+' }

    fun view(ctx: Context, url: String) = start(ctx, Intent(Intent.ACTION_VIEW, Uri.parse(url)))

    fun dial(ctx: Context, phone: String) =
        start(ctx, Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + clean(phone))))

    fun sms(ctx: Context, phone: String, body: String) =
        start(ctx, Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + clean(phone))).putExtra("sms_body", body))

    fun share(ctx: Context, text: String) = start(
        ctx,
        Intent.createChooser(
            Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text),
            "معرفی به دوستان"
        )
    )

    fun map(ctx: Context, b: Business) {
        val uri = if (b.lat != null && b.lng != null) {
            "geo:${b.lat},${b.lng}?q=${b.lat},${b.lng}(${Uri.encode(b.name)})"
        } else {
            "geo:0,0?q=" + Uri.encode("گرگان، " + b.address.ifBlank { b.name })
        }
        start(ctx, Intent(Intent.ACTION_VIEW, Uri.parse(uri)), "برنامه نقشه (نشان، بلد یا گوگل‌مپ) نصب نیست")
    }

    fun social(base: String, handle: String): String {
        val h = handle.trim()
        return if (h.startsWith("http")) h else base + h.removePrefix("@")
    }

    fun web(url: String): String = if (url.startsWith("http")) url.trim() else "https://" + url.trim()
}

fun catColor(id: String): Color {
    val palette = listOf(
        Color(0xFF1E5B4B), Color(0xFFB4442E), Color(0xFFB8801C), Color(0xFF4F6D2F),
        Color(0xFF2F5D7A), Color(0xFF7A3E5F), Color(0xFF5B4B8A)
    )
    return palette[(id.hashCode() and 0x7fffffff) % palette.size]
}

fun catIcon(id: String): ImageVector = when (id) {
    "restaurant" -> Icons.Filled.Restaurant
    "cafe" -> Icons.Filled.LocalCafe
    "grocery" -> Icons.Filled.LocalGroceryStore
    "clothing" -> Icons.Filled.Checkroom
    "beauty" -> Icons.Filled.ContentCut
    "medical" -> Icons.Filled.LocalHospital
    "auto" -> Icons.Filled.DirectionsCar
    "home" -> Icons.Filled.Weekend
    "tech" -> Icons.Filled.PhoneAndroid
    "education" -> Icons.Filled.School
    "services" -> Icons.Filled.Handyman
    "sport" -> Icons.Filled.SportsSoccer
    "tourism" -> Icons.Filled.Hotel
    "crafts" -> Icons.Filled.Palette
    else -> Icons.Filled.Storefront
}

fun imageModel(s: String): Any = if (s.startsWith("/")) File(s) else s

fun subtitleOf(b: Business): String =
    listOf(Categories.title(b.categoryId), b.neighborhood).filter { it.isNotBlank() }.joinToString(" · ")

@Composable
fun BizAvatar(b: Business, size: Dp) {
    val shape = RoundedCornerShape(size / 4)
    if (b.imageUrl.isNotBlank()) {
        AsyncImage(
            model = imageModel(b.imageUrl),
            contentDescription = b.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(size).clip(shape)
        )
    } else {
        Box(
            Modifier.size(size).clip(shape).background(catColor(b.categoryId)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                b.name.trim().take(1).ifBlank { "؟" },
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun BizBanner(b: Business, height: Dp) {
    if (b.imageUrl.isNotBlank()) {
        AsyncImage(
            model = imageModel(b.imageUrl),
            contentDescription = b.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(height)
        )
    } else {
        val c = catColor(b.categoryId)
        Box(
            Modifier.fillMaxWidth().height(height).background(Brush.linearGradient(listOf(c, c.copy(alpha = 0.72f)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                catIcon(b.categoryId), null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(height / 3)
            )
        }
    }
}

@Composable
fun Pill(text: String, container: Color, content: Color) {
    Surface(shape = RoundedCornerShape(50), color = container) {
        Text(
            text,
            color = content,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun OpenBadge(b: Business) {
    val cs = MaterialTheme.colorScheme
    when (Hours.isOpen(b)) {
        true -> Pill("باز", cs.primaryContainer, cs.onPrimaryContainer)
        false -> Pill("بسته", cs.surfaceVariant, cs.onSurfaceVariant)
        null -> {}
    }
}

@Composable
fun BusinessCard(b: Business, onClick: () -> Unit = { Nav.push(Route.Detail(b.id)) }) {
    val cs = MaterialTheme.colorScheme
    val today = Time.today()
    val fav = b.id in Store.favorites
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            BizAvatar(b, 60.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        b.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (b.verified) {
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Filled.Verified, "تأییدشده", tint = cs.primary, modifier = Modifier.size(18.dp))
                    }
                }
                Text(
                    subtitleOf(b),
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OpenBadge(b)
                    if (Store.isFeatured(b, today)) Pill("ویژه", cs.secondaryContainer, cs.onSecondaryContainer)
                    if (Store.hasOffer(b, today)) Pill("تخفیف", cs.tertiaryContainer, cs.onTertiaryContainer)
                }
            }
            IconButton(onClick = { Store.toggleFavorite(b.id) }) {
                Icon(
                    if (fav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    "نشان کردن",
                    tint = if (fav) cs.tertiary else cs.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 20.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (action != null) TextButton(onClick = onAction) { Text(action) }
    }
}

@Composable
fun ScreenHeader(title: String, subtitle: String) {
    val cs = MaterialTheme.colorScheme
    Surface(color = cs.primary, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = cs.onPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = cs.onPrimary.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, subtitle: String) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(72.dp).clip(CircleShape).background(cs.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = cs.onPrimaryContainer, modifier = Modifier.size(36.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
fun <T> ChoiceMenu(
    label: String,
    options: List<T>,
    selected: T,
    text: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(onClick = { open = true }, modifier = Modifier.fillMaxWidth()) {
            Text(
                if (label.isBlank()) text(selected) else "$label: ${text(selected)}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Filled.ArrowDropDown, null)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { o ->
                DropdownMenuItem(text = { Text(text(o)) }, onClick = { onSelect(o); open = false })
            }
        }
    }
}

@Composable
fun Field(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    keyboard: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
    supporting: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = minLines == 1,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        supportingText = if (supporting != null) { { Text(supporting) } } else null,
        modifier = modifier
    )
}

@Composable
fun Group(title: String, content: @Composable ColumnScope.() -> Unit) {
    val cs = MaterialTheme.colorScheme
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = cs.surface)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = cs.primary)
            content()
        }
    }
}

@Composable
fun SwitchRow(text: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
fun IconTextButton(icon: ImageVector, text: String, enabled: Boolean = true, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, modifier = Modifier.weight(1f))
    }
}

@Composable
fun HoodChips(onPick: (String) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Store.neighborhoods().forEach { n -> AssistChip(onClick = { onPick(n) }, label = { Text(n) }) }
    }
}

@Composable
fun BackTopBar(title: String, actions: @Composable RowScope.() -> Unit = {}) {
    val cs = MaterialTheme.colorScheme
    TopAppBar(
        title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            IconButton(onClick = { Nav.pop() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "بازگشت") }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = cs.primary,
            titleContentColor = cs.onPrimary,
            navigationIconContentColor = cs.onPrimary,
            actionIconContentColor = cs.onPrimary
        )
    )
}
