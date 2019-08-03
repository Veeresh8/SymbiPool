package com.droid.symbipool

import android.animation.Animator
import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.droid.symbipool.creationSteps.DateStep
import com.droid.symbipool.creationSteps.GenderStep
import com.droid.symbipool.creationSteps.TimeStep
import com.google.android.libraries.places.api.model.Place
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
        timeStep = TimeStep(stepTitles[1])
        startLocationStep = LocationStep(stepTitles[2], "")
        endLocationStep = LocationStep(stepTitles[3], null)
        genderStep = GenderStep(stepTitles[4])
        stepperForm?.setup(this, dateStep, timeStep, startLocationStep, endLocationStep, genderStep)?.init()
    }

    override fun onCompletedForm() {
        val ticket = Ticket(
            time = timeStep.stepData.toLong(),
            date = dateStep.stepData.trim(),
            startLocation = startLocation?.getActual(),
            endLocation = endLocation?.getActual(),
            genderPreference = genderStep.stepData.trim(),
            ticketID = FirebaseFirestore.getInstance().collection(DatabaseUtils.TICKET_COLLECTION).document().id
        )
        Log.i(javaClass.simpleName, "TicketResponse: $ticket")

        FirebaseFirestore.getInstance().collection(DatabaseUtils.TICKET_COLLECTION).add(ticket)
            .addOnSuccessListener {
                Log.d(javaClass.simpleName, "Saved ticket: ${it.id}")
            }
            .addOnFailureListener {
                Log.e(javaClass.simpleName, "Error saving ticket: ${it.message}")
            }

        stepperForm.visibility = View.GONE
        lottieAnimation.visibility = View.VISIBLE

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
            val addresses = latitude?.let { longitude?.let { it1 -> geoCoder.getFromLocation(it, it1, 1) } }
            addresses?.run {
                if (this.size > 0) {
                    val address = addresses[0]

                    if (address.subLocality == null || address.locality == null) {
                        MaterialDialog(this@CreateTicketActivity).show {
                            title(R.string.error_area_not_found_title)
                            cancelable(false)
                            message(R.string.error_area_not_found_message)
                            positiveButton(R.string.got_it) {
                                showPlacePicker()
                            }
                        }
                    }

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