package com.droid.symbipool

import java.text.SimpleDateFormat
import java.util.*

object DatabaseUtils {
    const val TICKET_COLLECTION = "tickets"
    var dateFormat = SimpleDateFormat("dd-MMM-yyyy")

    fun getCurrentDate(): String {
        return dateFormat.format(Calendar.getInstance().time)
    }
}