package com.droid.symbipool

import com.google.firebase.firestore.Exclude
import com.google.gson.Gson
import java.io.Serializable

data class StartLocation(
    var name: String? = null,
    var subLocality: String? = null,
    var locality: String? = null,
    var fullAddress: String? = null,
    var lat: String? = null,
    var longi: String? = null
) {

    @Exclude
    fun getAsString(): String {
        return Gson().toJson(this)
    }

    @Exclude
    fun getActual(): StartLocation {
        return Gson().fromJson(getAsString(), StartLocation::class.java)
    }

    override fun toString(): String {
        return fullAddress.toString()
    }
}

data class EndLocation(
    var name: String? = null,
    var subLocality: String? = null,
    var locality: String? = null,
    var fullAddress: String? = null,
    var lat: String? = null,
    var longi: String? = null
) {

    @Exclude
    fun getAsString(): String {
        return Gson().toJson(this)
    }

    @Exclude
    fun getActual(): EndLocation {
        return Gson().fromJson(getAsString(), EndLocation::class.java)
    }

    override fun toString(): String {
        return fullAddress.toString()
    }
}

data class Ticket(
    var ticketID: String? = null,
    var startLocation: StartLocation? = null,
    var endLocation: EndLocation? = null,
    var date: String? = null,
    var time: Long? = null,
    var genderPreference: String? = null,
    var contact: String? = null
)

data class TicketFilter(
    var startLocation: Pair<String, String>? = null,
    var endLocation: Pair<String, String>? = null,
    var genderPreference: String? = null
) : Serializable
