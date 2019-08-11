package com.droid.symbipool

import java.text.SimpleDateFormat
import java.util.*

object DatabaseUtils {
    const val TICKET_COLLECTION = "tickets"
    const val EMAILS_COLLECTION = "emails"
    const val TIME = "time"
    const val DATE = "date"
    const val TICKET_ID = "ticketID"
    var dateFormat = SimpleDateFormat("dd-MMM-yyyy")

    fun getCurrentDate(): String {
        return dateFormat.format(Calendar.getInstance().time)
    }
}