package com.droid.symbipool

import android.content.Context
import android.content.Intent
import android.location.Address
import android.net.Uri
import android.util.Log
import com.droid.symbipool.creationSteps.GenderStep
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


object TicketUtils {

    var allStartLocations: Set<String> = HashSet()
    var allEndLocations: Set<String> = HashSet()
    var allCities: Set<String> = HashSet()


    var filteredList: List<Ticket> = ArrayList()
    var userCreatedList: List<Ticket> = ArrayList()
    var allTickets: ArrayList<Ticket> = ArrayList()

    const val ANY_LOCATION = "All"

    fun clearAllLists() {
        filteredList = ArrayList()
        allTickets = ArrayList()

        allCities = HashSet()
        allEndLocations = HashSet()
        allStartLocations = HashSet()
    }

    fun getUserCreatedTickets(): List<Ticket> {
        userCreatedList = allTickets.map { it }
            .filter { ticket -> ticket.creator == FirebaseAuth.getInstance().currentUser?.email }
        Log.i(javaClass.simpleName, "User lists: ${userCreatedList.size}")
        return userCreatedList
    }

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
            startAddressBuilder.append("${this.subLocality}, \t ${this.locality}")
        }

        return startAddressBuilder.toString()
    }

    fun getFullStartAddress(ticket: Ticket): String {
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

    fun getFullDestinationAddress(ticket: Ticket): String {
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

    fun getDestinationAddress(ticket: Ticket): String {
        val destinationAddressBuilder = StringBuilder()

        ticket.endLocation?.run {
            destinationAddressBuilder.append("${this.subLocality}, \t ${this.locality}")
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
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?q=loc:$lat,$longi")
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setPackage("com.google.android.apps.maps")
            context.startActivity(intent)
        } catch (exception: Exception) {
            Log.e(javaClass.simpleName, "No intent found: ${exception.message}")
        }
    }

    fun getPaginationTicket(): Ticket {
        return Ticket(isPaginationTicket = true, ticketID = "Pagination Ticket", date = "08-Sep-2030", time = 10436668200000)
    }

    fun getAllStartLocations(): List<String> {
        return allStartLocations.toList()
    }

    fun getAllEndLocations(): List<String> {
        return allEndLocations.toList()
    }

    fun removeLocations(ticket: Ticket) {
        ticket.startLocation?.subLocality?.run {
            allStartLocations = allStartLocations.minus(this)
        }

        ticket.startLocation?.locality?.run {
            allCities = allCities.minus(this)
        }

        ticket.endLocation?.subLocality?.run {
            allEndLocations = allEndLocations.minus(this)
        }

        ticket.endLocation?.locality?.run {
            allCities = allCities.minus(this)
        }
    }

    fun getStartLocationPicked(index: Int): String {
        return allStartLocations.elementAt(index)
    }

    fun getCityPicked(index: Int): String {
        return allCities.elementAt(index)
    }

    fun getEndLocationPicked(index: Int): String {
        return allEndLocations.elementAt(index)
    }

    fun getAllCities(): List<String> {
        return allCities.toList()
    }

    fun addLocations(ticket: Ticket) {

        if (!allEndLocations.contains(ANY_LOCATION)) {
            allEndLocations = allEndLocations.plus(ANY_LOCATION)
        }

        if (!allStartLocations.contains(ANY_LOCATION)) {
            allStartLocations = allStartLocations.plus(ANY_LOCATION)
        }

        ticket.startLocation?.subLocality?.run {
            allStartLocations = allStartLocations.plus(this)
        }

        ticket.startLocation?.locality?.run {
            allCities = allCities.plus(this)
        }

        ticket.endLocation?.subLocality?.run {
            allEndLocations = allEndLocations.plus(this)
        }

        ticket.endLocation?.locality?.run {
            allCities = allCities.plus(this)
        }
    }

    fun endLocalityCheck(ticket: Ticket, ticketFilter: TicketFilter): Boolean {
        return ticket.endLocation?.subLocality == ticketFilter.endLocation?.first &&
                ticket.endLocation?.locality == ticketFilter.endLocation?.second
    }

    fun genderCheck(ticket: Ticket, ticketFilter: TicketFilter): Boolean {
        return ticket.genderPreference == ticketFilter.genderPreference
    }

    fun startCityCheck(ticket: Ticket, ticketFilter: TicketFilter): Boolean {
        return ticket.startLocation?.locality == ticketFilter.startLocation?.second
    }

    fun endCityCheck(ticket: Ticket, ticketFilter: TicketFilter): Boolean {
        return ticket.endLocation?.locality == ticketFilter.endLocation?.second
    }

    fun startLocalityCheck(ticket: Ticket, ticketFilter: TicketFilter): Boolean {
        return ticket.startLocation?.subLocality == ticketFilter.startLocation?.first &&
                ticket.startLocation?.locality == ticketFilter.startLocation?.second
    }

    fun searchForSubLocality(addresses: List<Address>): String? {
        addresses.forEach { address ->
            if (address.subLocality != null)
                return address.subLocality
        }
        return null
    }

    fun searchForLocality(addresses: List<Address>): String? {
        addresses.forEach { address ->
            if (address.locality != null)
                return address.locality
        }
        return null
    }

    fun getTimeStamp(time: String, date: String): Long {
        val format = SimpleDateFormat("dd-MMM-yyyy hh:mm aa")
        val dateInString = "$date $time"
        val formattedDate = format.parse(dateInString) as Date
        val calendar = Calendar.getInstance()
        calendar.time = formattedDate
        return calendar.timeInMillis
    }
}