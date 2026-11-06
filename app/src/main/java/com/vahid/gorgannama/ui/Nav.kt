package com.vahid.gorgannama.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

sealed interface Route {
    data object Main : Route
    data object Admin : Route
    data class Detail(val id: String) : Route
    data class EditBusiness(val id: String?, val fromLeadId: String? = null) : Route
    data class EditLead(val id: String?) : Route
}

object Nav {
    val stack = mutableStateListOf<Route>(Route.Main)
    var tab by mutableIntStateOf(0)
    var searchCategory by mutableStateOf<String?>(null)

    fun push(r: Route) {
        stack.add(r)
    }

    fun pop() {
        if (stack.size > 1) stack.removeAt(stack.lastIndex)
    }

    /** after deleting a business, close every screen that showed it */
    fun closeBusiness(id: String) {
        stack.removeAll { (it is Route.Detail && it.id == id) || (it is Route.EditBusiness && it.id == id) }
        if (stack.isEmpty()) stack.add(Route.Main)
    }
}
