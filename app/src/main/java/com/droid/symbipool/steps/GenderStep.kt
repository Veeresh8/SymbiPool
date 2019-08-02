package com.droid.symbipool.steps

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.droid.symbipool.R
import ernestoyaquello.com.verticalstepperform.Step


class GenderStep(title: String) : Step<String>(title) {

    var genderPreference: GenderPreference? = null

    override fun restoreStepData(data: String?) {

    }

    override fun isStepDataValid(stepData: String?): IsDataValid {
        return if (genderPreference != null) {
            IsDataValid(true)
        } else {
            IsDataValid(false)
        }
    }

    override fun onStepMarkedAsCompleted(animated: Boolean) {

    }

    override fun getStepDataAsHumanReadableString(): String {

        val toDisplay = StringBuilder()

        when (genderPreference) {
            GenderPreference.MALE -> {
                toDisplay.append("Male Only")
            }

            GenderPreference.FEMALE -> {
                toDisplay.append("Female Only")
            }

            GenderPreference.NONE -> {
                toDisplay.append("None")
            }
        }

        return toDisplay.toString()
    }

    override fun createStepContentLayout(): View {
        val view = LayoutInflater.from(context).inflate(R.layout.step_gender, null, false)
        val myItems = listOf("Male Only", "Female Only", "None")
        val tvGender = view.findViewById<TextView>(R.id.tvGender)
        genderPreference = GenderPreference.NONE
        tvGender.setOnClickListener {
            MaterialDialog(context).show {
                title(R.string.gender_preference)
                listItemsSingleChoice(items = myItems, initialSelection = 3) { _, index, _ ->
                    when (index) {
                        0 -> genderPreference = GenderPreference.MALE
                        1 -> genderPreference = GenderPreference.FEMALE
                        2 -> genderPreference = GenderPreference.NONE
                    }
                    tvGender.text = genderPreference?.name
                    markAsCompleted(false)
                }
                positiveButton(R.string.select)
            }
        }
        return view
    }

    override fun getStepData(): String {
        return genderPreference?.name.toString()
    }

    override fun onStepOpened(animated: Boolean) {

    }

    override fun onStepMarkedAsUncompleted(animated: Boolean) {

    }

    override fun onStepClosed(animated: Boolean) {

    }

    enum class GenderPreference {
        MALE,
        FEMALE,
        NONE
    }
}