package com.droid.symbipool.filterSteps

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.datePicker
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.droid.symbipool.R
import com.droid.symbipool.Ticket
import com.droid.symbipool.TicketUtils
import com.droid.symbipool.creationSteps.GenderStep
import ernestoyaquello.com.verticalstepperform.Step
import kotlinx.android.synthetic.main.step_gender.*
import java.lang.StringBuilder
import java.text.SimpleDateFormat


class EndStep(title: String) : Step<Pair<String, String>>(title) {

    private var tvEndLocation: TextView? = null
    private var endLocation: Pair<String, String>? = null
    private var locality: String = ""
    private var city: String = ""

    override fun restoreStepData(data: Pair<String, String>?) {

    }

    override fun isStepDataValid(stepData: Pair<String, String>?): IsDataValid {
        return if (endLocation != null) {
            IsDataValid(true)
        } else {
            IsDataValid(false)
        }
    }

    override fun onStepMarkedAsCompleted(animated: Boolean) {

    }

    override fun getStepDataAsHumanReadableString(): String {
        return tvEndLocation?.text.toString()
    }

    override fun createStepContentLayout(): View {
        val view = LayoutInflater.from(context).inflate(R.layout.step_filter_end_location, null, false)
        tvEndLocation = view.findViewById(R.id.tvEndLocation)
        tvEndLocation?.setOnClickListener {
            showCityDialog()
        }
        return view
    }

    private fun showLocalityDialog() {
        val localityDialog = MaterialDialog(context)
            .title(R.string.end_location)
            .listItemsSingleChoice(
                items = TicketUtils.getAllEndLocations(),
                initialSelection = 0
            ) { _, index, _ ->
                if (index == 0) {
                    endLocation = Pair(TicketUtils.ANY_LOCATION, city)
                    tvEndLocation?.text = "ANY AREA , $city"
                    markAsCompleted(false)
                    return@listItemsSingleChoice
                }
                locality = TicketUtils.getEndLocationPicked(index)
                endLocation = Pair(locality, city)
                tvEndLocation?.text = "$locality , $city"
                markAsCompleted(false)
            }
            .positiveButton(R.string.select)
        localityDialog.show()
    }

    private fun showCityDialog() {
        val cityDialog = MaterialDialog(context)
            .title(R.string.city)
            .listItemsSingleChoice(items = TicketUtils.getAllCities(), initialSelection = 0) { _, index, _ ->
                city = TicketUtils.getCityPicked(index)
                showLocalityDialog()
            }
            .positiveButton(R.string.select)
        cityDialog.show()
    }

    override fun getStepData(): Pair<String, String> {
        endLocation?.run {
            return this
        }
        return Pair("NULL", "NULL")
    }

    override fun onStepOpened(animated: Boolean) {

    }

    override fun onStepMarkedAsUncompleted(animated: Boolean) {

    }

    override fun onStepClosed(animated: Boolean) {

    }
}