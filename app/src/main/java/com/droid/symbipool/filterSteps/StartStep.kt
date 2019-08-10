package com.droid.symbipool.filterSteps

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.droid.symbipool.R
import com.droid.symbipool.TicketUtils
import ernestoyaquello.com.verticalstepperform.Step


class StartStep(title: String) : Step<Pair<String, String>>(title) {

    private var tvStartLocation: TextView? = null
    private var startLocation: Pair<String, String>? = null
    private var locality: String = ""
    private var city: String = ""

    override fun isStepDataValid(stepData: Pair<String, String>?): IsDataValid {
        return if (startLocation != null) {
            IsDataValid(true)
        } else {
            IsDataValid(false)
        }
    }

    override fun restoreStepData(data: Pair<String, String>?) {

    }

    override fun onStepMarkedAsCompleted(animated: Boolean) {

    }

    override fun getStepDataAsHumanReadableString(): String {
        return tvStartLocation?.text.toString()
    }

    @SuppressLint("SetTextI18n")
    override fun createStepContentLayout(): View {
        val view = LayoutInflater.from(context).inflate(R.layout.step_filter_start_location, null, false)
        tvStartLocation = view.findViewById(R.id.tvStartLocation)
        tvStartLocation?.setOnClickListener {
            val localityDialog = MaterialDialog(context)
                .title(R.string.start_location)
                .listItemsSingleChoice(
                    items = TicketUtils.getAllStartLocations(),
                    initialSelection = 0
                ) { _, index, _ ->


                    if (index == 0) {
                        startLocation = Pair(TicketUtils.ANY_LOCATION, TicketUtils.ANY_LOCATION)
                        tvStartLocation?.text = TicketUtils.ANY_LOCATION
                        markAsCompleted(false)
                        return@listItemsSingleChoice
                    }

                    locality = TicketUtils.getStartLocationPicked(index)
                    showCityDialog(context)
                }
                .positiveButton(R.string.select)

            localityDialog.show()
        }
        return view
    }

    private fun showCityDialog(context: Context) {
        val cityDialog = MaterialDialog(context)
            .title(R.string.city)
            .listItemsSingleChoice(items = TicketUtils.getAllCities(), initialSelection = 0) { _, index, _ ->
                city = TicketUtils.getCityPicked(index)
                startLocation = Pair(locality, city)
                tvStartLocation?.text = "$locality , $city"
                markAsCompleted(false)
            }
            .positiveButton(R.string.select)
        cityDialog.show()
    }

    override fun getStepData(): Pair<String, String> {
        startLocation?.run {
            return this
        }
        return Pair(TicketUtils.ANY_LOCATION, TicketUtils.ANY_LOCATION)
    }

    override fun onStepOpened(animated: Boolean) {

    }

    override fun onStepMarkedAsUncompleted(animated: Boolean) {

    }

    override fun onStepClosed(animated: Boolean) {

    }
}