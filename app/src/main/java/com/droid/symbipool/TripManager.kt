package com.droid.symbipool

import com.google.firebase.firestore.Exclude
import com.google.gson.Gson
import java.io.Serializable
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class StartLocation(
    var name: String? = null,
    var subLocality: String? = null,
    var locality: String? = null,
    var fullAddress: String? = null,
    var lat: String? = null,
    var longi: String? = null
) : Parcelable {

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

@Parcelize
data class EndLocation(
    var name: String? = null,
    var subLocality: String? = null,
    var locality: String? = null,
    var fullAddress: String? = null,
    var lat: String? = null,
    var longi: String? = null
) : Parcelable {

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

@Parcelize
data class Ticket(
    var ticketID: String? = null,
    var startLocation: StartLocation? = null,
    var endLocation: EndLocation? = null,
    var date: String? = null,
    var time: Long? = null,
    var genderPreference: String? = null,
    var contact: String? = null,
    var creator: String? = null,
    @Exclude var isPaginationTicket: Boolean = false
) : Parcelable

data class TicketFilter(
    var startLocation: Pair<String, String>? = null,
    var endLocation: Pair<String, String>? = null,
    var genderPreference: String? = null
) : Serializable

data class TicketEvent(
    var ticket: Ticket
)

data class UserTicketEvent(
    var tickets: ArrayList<Ticket>? = null
)

data class AllTicketsDeleteEvent(
    var ticket: Ticket
)

data class AllUserTicketEvent(
    var tickets: ArrayList<Ticket>? = null
)
