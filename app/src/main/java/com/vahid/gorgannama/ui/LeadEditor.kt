package com.vahid.gorgannama.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vahid.gorgannama.data.Categories
import com.vahid.gorgannama.data.Lead
import com.vahid.gorgannama.data.LeadStatus
import com.vahid.gorgannama.data.Store
import com.vahid.gorgannama.util.Fa
import com.vahid.gorgannama.util.Time

@Composable
fun LeadEditor(id: String?) {
    val ctx = LocalContext.current
    val today = Time.today()
    val existing = remember { if (id == null) null else Store.leads.firstOrNull { it.id == id } }
    var leadId by remember { mutableStateOf(existing?.id ?: Store.newId()) }
    var shop by remember { mutableStateOf(existing?.shopName ?: "") }
    var owner by remember { mutableStateOf(existing?.ownerName ?: "") }
    var phone by remember { mutableStateOf(existing?.phone ?: "") }
    var hood by remember { mutableStateOf(existing?.neighborhood ?: "") }
    var cat by remember { mutableStateOf(existing?.categoryId ?: "") }
    var address by remember { mutableStateOf(existing?.address ?: "") }
    var status by remember { mutableStateOf(existing?.status ?: LeadStatus.INTERESTED) }
    var follow by remember { mutableStateOf(existing?.followUpDay) }
    var note by remember { mutableStateOf(existing?.note ?: "") }
    var confirmDelete by remember { mutableStateOf(false) }

    fun save(): Boolean {
        if (shop.isBlank()) {
            toast(ctx, "اسم مغازه را بنویس")
            return false
        }
        val old = Store.leads.firstOrNull { it.id == leadId }
        Store.upsertLead(
            Lead(
                id = leadId,
                shopName = shop.trim(),
                ownerName = owner.trim(),
                phone = Fa.latin(phone).trim(),
                neighborhood = hood.trim(),
                categoryId = cat,
                address = address.trim(),
                status = status,
                note = note.trim(),
                visitDay = old?.visitDay ?: today,
                followUpDay = follow,
                businessId = old?.businessId
            )
        )
        return true
    }

    Scaffold(topBar = {
        BackTopBar(if (existing == null) "ثبت ویزیت" else "ویرایش ویزیت") {
            if (existing != null) IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Filled.Delete, "حذف") }
        }
    }) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Group("مغازه") {
                Field(shop, { shop = it }, "اسم مغازه *")
                Field(owner, { owner = it }, "اسم صاحب مغازه")
                Field(phone, { phone = it }, "شماره تماس", keyboard = KeyboardType.Phone)
                Field(hood, { hood = it }, "محله")
                HoodChips { hood = it }
                ChoiceMenu(
                    "دسته",
                    listOf("") + Categories.all.map { it.id },
                    cat,
                    { if (it.isBlank()) "انتخاب نشده" else Categories.title(it) },
                    { cat = it }
                )
                Field(address, { address = it }, "آدرس")
            }
            Group("نتیجه ویزیت") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LeadStatus.values().forEach { st ->
                        FilterChip(selected = status == st, onClick = { status = st }, label = { Text(st.title) })
                    }
                }
                Text("پیگیری بعدی", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf<Pair<Long?, String>>(
                        null to "ندارد", 1L to "فردا", 3L to "۳ روز دیگر", 7L to "یک هفته", 14L to "دو هفته", 30L to "یک ماه"
                    ).forEach { (d, t) ->
                        FilterChip(
                            selected = if (d == null) follow == null else follow == today + d,
                            onClick = { follow = if (d == null) null else today + d },
                            label = { Text(t) }
                        )
                    }
                }
                val f = follow
                if (f != null) Text("تاریخ پیگیری: " + Fa.longDate(f), style = MaterialTheme.typography.bodySmall)
                Field(note, { note = it }, "یادداشت (چه گفت؟ کی برگردم؟)", minLines = 3)
            }
            Button(onClick = { if (save()) { toast(ctx, "ذخیره شد"); Nav.pop() } }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text("ذخیره")
            }
            if (existing == null) {
                OutlinedButton(onClick = {
                    if (save()) {
                        val n = Store.leads.count { it.visitDay == today }
                        toast(ctx, "ثبت شد · امروز ${Fa.num(n)} ویزیت")
                        leadId = Store.newId()
                        shop = ""; owner = ""; phone = ""; address = ""; note = ""
                        status = LeadStatus.INTERESTED
                        follow = null
                    }
                }, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("ذخیره و مغازه بعدی") }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("حذف ویزیت") },
            text = { Text("این ویزیت حذف شود؟") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    Store.deleteLead(leadId)
                    Nav.pop()
                }) { Text("حذف") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("انصراف") } }
        )
    }
}
