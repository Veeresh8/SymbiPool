package com.droid.symbipool.steps

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.datePicker
import com.droid.symbipool.R
import ernestoyaquello.com.verticalstepperform.Step
import java.text.SimpleDateFormat


class DateStep(title: String) : Step<String>(title) {

    private var datePicked: String? = null
    var dateFormat = SimpleDateFormat("dd-MMM-yyyy")

    override fun restoreStepData(data: String?) {

    }

    override fun isStepDataValid(stepData: String?): IsDataValid {
        return if (datePicked != null) {
            IsDataValid(true)
        } else {
            IsDataValid(false)
        }
    }

    override fun onStepMarkedAsCompleted(animated: Boolean) {

    }

    override fun getStepDataAsHumanReadableString(): String {
        return "$datePicked"
    }

    override fun createStepContentLayout(): View {
        val view = LayoutInflater.from(context).inflate(R.layout.step_date, null, false)
        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        tvDate.setOnClickListener {
            MaterialDialog(context).show {
                datePicker { _, date ->
                    val formattedDate = dateFormat.format(date.time)
                    datePicked = formattedDate
                    tvDate.text = datePicked
                    markAsCompleted(false)
                }
            }
        }

        return view
    }

    override fun getStepData(): String {
        return "$datePicked"
    }

    override fun onStepOpened(animated: Boolean) {

    }

    override fun onStepMarkedAsUncompleted(animated: Boolean) {

    }

    override fun onStepClosed(animated: Boolean) {

    }
}