package com.droid.symbipool.steps

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.datePicker
import com.afollestad.materialdialogs.datetime.timePicker
import com.droid.symbipool.R
import ernestoyaquello.com.verticalstepperform.Step
import java.text.SimpleDateFormat


class TimeStep(title: String) : Step<String>(title) {

    private var timePicked: String? = null
    private var timestamp: Long? = null
    var timeFormat = SimpleDateFormat("hh:mm aa")

    override fun restoreStepData(data: String?) {

    }

    override fun isStepDataValid(stepData: String?): IsDataValid {
        return if (timePicked != null) {
            IsDataValid(true)
        } else {
            IsDataValid(false)
        }
    }

    override fun onStepMarkedAsCompleted(animated: Boolean) {

    }

    override fun getStepDataAsHumanReadableString(): String {
        return "$timePicked"
    }

    override fun createStepContentLayout(): View {
        val view = LayoutInflater.from(context).inflate(R.layout.step_time, null, false)
        val tvTime = view.findViewById<TextView>(R.id.tvTime)
        tvTime.setOnClickListener {
            MaterialDialog(context).show {
                timePicker(show24HoursView = false) { _, datetime ->
                    val formattedDate = timeFormat.format(datetime.time)
                    timePicked = formattedDate
                    tvTime.text = timePicked
                    timestamp = datetime.timeInMillis
                    markAsCompleted(false)
                }
            }
        }

        return view
    }

    override fun getStepData(): String {
        return "$timestamp"
    }

    override fun onStepOpened(animated: Boolean) {

    }

    override fun onStepMarkedAsUncompleted(animated: Boolean) {

    }

    override fun onStepClosed(animated: Boolean) {

    }
}