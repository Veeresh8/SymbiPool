package com.droid.symbipool

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.droid.symbipool.steps.GenderStep
import java.text.SimpleDateFormat
import java.util.*


object TicketUtils {


    fun getTimeAndDate(ticket: Ticket): String {
        val timeStringBuilder = StringBuilder()

        ticket.time?.run {
            val time = Date(this)
            val formattedDate = SimpleDateFormat("hh:mm aa").format(time)
            timeStringBuilder.append(formattedDate)
            timeStringBuilder.append(" | ")
        }

        ticket.date?.run {
            timeStringBuilder.append(this)
        }

        return timeStringBuilder.toString()
    }

    fun getStartAddress(ticket: Ticket): String {
        val startAddressBuilder = StringBuilder()

        ticket.startLocation?.run {
            startAddressBuilder.append(this.name)
            startAddressBuilder.append("\n")
            startAddressBuilder.append(this.fullAddress)
            startAddressBuilder.append("\n\n")
            startAddressBuilder.append("Area - ${this.subLocality}")
            startAddressBuilder.append("\n\n")
            startAddressBuilder.append("City - ${this.locality}")
        }

        return startAddressBuilder.toString()
    }

    fun getDestinationAddress(ticket: Ticket): String {
        val destinationAddressBuilder = StringBuilder()

        ticket.endLocation?.run {
            destinationAddressBuilder.append(this.name)
            destinationAddressBuilder.append("\n")
            destinationAddressBuilder.append(this.fullAddress)
            destinationAddressBuilder.append("\n\n")
            destinationAddressBuilder.append("Area - ${this.subLocality}")
            destinationAddressBuilder.append("\n\n")
            destinationAddressBuilder.append("City - ${this.locality}")
        }
        return destinationAddressBuilder.toString()
    }

    fun getPreference(ticket: Ticket): String {
        return when (ticket.genderPreference) {
            GenderStep.GenderPreference.MALE.name -> GenderStep.GenderPreference.MALE.name
            GenderStep.GenderPreference.FEMALE.name -> GenderStep.GenderPreference.FEMALE.name
            GenderStep.GenderPreference.NONE.name -> GenderStep.GenderPreference.NONE.name
            else -> GenderStep.GenderPreference.NONE.name
        }
    }

    fun launchMapsWithCoordinates(lat: String?, longi: String?, context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?q=loc:$lat,$longi"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setPackage("com.google.android.apps.maps")
            context.startActivity(intent)
        } catch (exception: Exception) {
            Log.e(javaClass.simpleName, "No intent found: ${exception.message}")
        }
    }
}