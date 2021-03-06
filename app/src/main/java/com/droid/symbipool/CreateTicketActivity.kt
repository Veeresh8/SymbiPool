package com.droid.symbipool

import android.animation.Animator
import android.app.Activity
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.droid.symbipool.creationSteps.ContactStep
import com.droid.symbipool.creationSteps.DateStep
import com.droid.symbipool.creationSteps.GenderStep
import com.droid.symbipool.creationSteps.TimeStep
import com.google.android.libraries.places.api.model.Place
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rtchagas.pingplacepicker.PingPlacePicker
import ernestoyaquello.com.verticalstepperform.Step
import ernestoyaquello.com.verticalstepperform.listener.StepperFormListener
import kotlinx.android.synthetic.main.activity_create_ticket.*
import java.util.*


class CreateTicketActivity : AppCompatActivity(), StepperFormListener {

    private lateinit var timeStep: TimeStep
    private lateinit var dateStep: DateStep
    private lateinit var genderStep: GenderStep
    private lateinit var contactStep: ContactStep
    private lateinit var endLocationStep: LocationStep
    private lateinit var startLocationStep: LocationStep
    private val REQUEST_PLACE_PICKER: Int = 1337
    private var startLocation: StartLocation? = null
    private var endLocation: EndLocation? = null
    private var locationType: LocationType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_ticket)
        initStepperForm()
        initClickListeners()
    }

    private fun initClickListeners() {
        ivClose.setOnClickListener {
            finish()
        }
    }

    private fun initStepperForm() {
        val stepTitles = resources.getStringArray(R.array.steps_titles)
        dateStep = DateStep(stepTitles[0])
        contactStep = ContactStep(stepTitles[0])
        timeStep = TimeStep(stepTitles[1])
        startLocationStep = LocationStep(stepTitles[2], "")
        endLocationStep = LocationStep(stepTitles[3], null)
        genderStep = GenderStep(stepTitles[4])
        contactStep = ContactStep(stepTitles[5])
        stepperForm?.setup(this, dateStep, timeStep, startLocationStep, endLocationStep, genderStep, contactStep)
            ?.init()
    }

    override fun onCompletedForm() {

        hideSoftKeyboard()

        val collection = FirebaseFirestore.getInstance().collection(DatabaseUtils.TICKET_COLLECTION).document()

        val time = TicketUtils.getTimeStamp(timeStep.stepData.trim(), dateStep.stepData.trim())

        val ticket = Ticket(
            time = time,
            date = dateStep.stepData.trim(),
            startLocation = startLocation?.getActual(),
            endLocation = endLocation?.getActual(),
            genderPreference = genderStep.stepData.trim(),
            ticketID = collection.id,
            contact = contactStep.stepData.trim(),
            creator = FirebaseAuth.getInstance().currentUser?.email
        )

        Log.i(javaClass.simpleName, "TicketResponse: $ticket")

        stepperForm.gone()
        lottieAnimation.visible()

        collection.set(ticket).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d(javaClass.simpleName, "Saved ticket: $it")
            } else {
                Log.e(javaClass.simpleName, "Error saving ticket: ${it.exception?.message}")
                Snackbar.make(rootLayout, "Failed to create ticket", Snackbar.LENGTH_LONG).show()
                stepperForm.visible()
                lottieAnimation.gone()
            }
        }

        lottieAnimation.playAnimation()
        lottieAnimation.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {
                finish()
            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationStart(p0: Animator?) {
                Snackbar.make(rootLayout, "Created ticket successfully", Snackbar.LENGTH_LONG).show()
            }
        })

    }

    override fun onCancelledForm() {
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == REQUEST_PLACE_PICKER) && (resultCode == Activity.RESULT_OK)) {
            val place: Place? = data?.let { PingPlacePicker.getPlace(it) }
            getAddress(place?.latLng?.latitude, place?.latLng?.longitude, place?.address, place?.name)
        }
    }

    private fun getAddress(
        latitude: Double?,
        longitude: Double?,
        fullAddress: String? = "NULL",
        placeName: String? = "NULL"
    ): String {
        val result = StringBuilder()
        try {
            val geoCoder = Geocoder(this, Locale.getDefault())
            val addresses = latitude?.let { longitude?.let { it1 -> geoCoder.getFromLocation(it, it1, 10) } }
            addresses?.run {
                if (this.size > 0) {
                    val address = addresses[0]

                    updateAddress(address, addresses)

                    when (locationType) {
                        LocationType.START -> {
                            val pickedStartLocation = StartLocation()
                            pickedStartLocation.name = placeName
                            pickedStartLocation.fullAddress = fullAddress
                            pickedStartLocation.subLocality = address.subLocality
                            pickedStartLocation.locality = address.locality
                            pickedStartLocation.lat = latitude.toString()
                            pickedStartLocation.longi = longitude.toString()

                            startLocation = pickedStartLocation

                            startLocation?.fullAddress?.let {
                                startLocationStep.setLocationText(
                                    Triple(
                                        address.subLocality,
                                        address.locality,
                                        it
                                    )
                                )
                            }

                        }
                        LocationType.END -> {
                            val pickedEndLocation = EndLocation()
                            pickedEndLocation.name = placeName
                            pickedEndLocation.fullAddress = fullAddress
                            pickedEndLocation.subLocality = address.subLocality
                            pickedEndLocation.locality = address.locality
                            pickedEndLocation.lat = latitude.toString()
                            pickedEndLocation.longi = longitude.toString()

                            endLocation = pickedEndLocation

                            endLocation?.fullAddress?.let {
                                endLocationStep.setLocationText(
                                    Triple(
                                        address.subLocality,
                                        address.locality,
                                        it
                                    )
                                )
                            }

                        }
                    }

                }
            }
        } catch (exception: Exception) {
            Log.e(javaClass.simpleName, "${exception.message}")
        }
        return result.toString()
    }

    private fun updateAddress(address: Address, addresses: List<Address>) {
        if (address.subLocality == null || address.locality == null) {

            address.subLocality = TicketUtils.searchForSubLocality(addresses)
            address.locality = TicketUtils.searchForLocality(addresses)

            if (address.subLocality == null || address.locality == null) {

                address.subLocality = address.locality
                address.locality = address.subAdminArea

                if (address.locality == null && address.subAdminArea != null) {
                    address.locality = address.subAdminArea
                }

                if (address.subLocality == null && address.locality != null) {
                    address.subLocality = address.locality
                }
            }
        }
    }

    fun showPlacePicker() {
        val builder = PingPlacePicker.IntentBuilder()
        builder.setAndroidApiKey(BuildConfig.PLACES_KEY)
            .setMapsApiKey(BuildConfig.MAPS_KEY)

        try {
            val placeIntent = builder.build(this)
            startActivityForResult(placeIntent, REQUEST_PLACE_PICKER)
        } catch (exception: Exception) {
            Log.e(javaClass.simpleName, "Exception starting location picker: ${exception.message}")
        }
    }


    inner class LocationStep(title: String, private val isStart: String?) : Step<String>(title, isStart) {

        private var tvLocationText: TextView? = null

        override fun restoreStepData(data: String?) {

        }

        override fun isStepDataValid(stepData: String?): IsDataValid {
            return if (isStart != null && startLocation != null && startLocation?.subLocality != null && startLocation?.locality != null) {
                IsDataValid(true)
            } else if (endLocation != null && endLocation?.subLocality != null && endLocation?.locality != null) {
                IsDataValid(true)
            } else {
                IsDataValid(false)
            }
        }

        override fun onStepMarkedAsCompleted(animated: Boolean) {

        }

        override fun getStepDataAsHumanReadableString(): String {
            return if (isStart != null) {
                "${startLocation?.toString()}"
            } else {
                "${endLocation?.toString()}"
            }
        }

        override fun createStepContentLayout(): View {
            val view = LayoutInflater.from(context).inflate(R.layout.step_start_location, null, false)
            tvLocationText = view.findViewById(R.id.tvLocation)
            tvLocationText?.setOnClickListener {
                locationType = if (isStart != null) {
                    LocationType.START
                } else {
                    LocationType.END
                }
                showPlacePicker()
            }

            return view
        }

        override fun getStepData(): String {
            return if (isStart != null) {
                "${startLocation?.getAsString()}"
            } else {
                "${endLocation?.getAsString()}"
            }
        }

        override fun onStepOpened(animated: Boolean) {

        }

        override fun onStepMarkedAsUncompleted(animated: Boolean) {

        }

        override fun onStepClosed(animated: Boolean) {

        }

        fun setLocationText(addressPair: Triple<String?, String?, String?>) {

            var area = ""

            area = area.plus("Area: ${addressPair.first}").plus("\n\n")
                .plus("City: ${addressPair.second}").plus("\n")

            updateSubtitle(area, false)

            tvLocationText?.text = addressPair.third

            if (addressPair.first != null && addressPair.second != null && addressPair.third != null) {
                markAsCompleted(false)
            } else {
                tvLocationText?.text = "Pick some address near the current one"
            }
        }
    }


    enum class LocationType {
        START, END
    }

}