package com.droid.symbipool

import java.text.SimpleDateFormat
import java.util.*



object DatabaseUtils {
    const val TICKET_COLLECTION = "tickets"
    const val EMAILS_COLLECTION = "emails"
    const val TIME = "time"
    const val DATE = "date"
    private var dateFormat = SimpleDateFormat("dd-MMM-yyyy")
    var latestDate: String? = null

    fun getCurrentDate(): String {
        return dateFormat.format(Calendar.getInstance().time)
    }

    fun getNextDate(): String {

        if (latestDate == null)
            latestDate = getCurrentDate()

        val date = dateFormat.parse(latestDate)
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        return dateFormat.format(calendar.time)
    }
}