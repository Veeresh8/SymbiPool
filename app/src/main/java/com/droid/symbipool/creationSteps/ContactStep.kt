package com.droid.symbipool.creationSteps

import android.content.Context
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.datePicker
import com.droid.symbipool.R
import com.droid.symbipool.gone
import com.droid.symbipool.visible
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import ernestoyaquello.com.verticalstepperform.Step
import java.text.SimpleDateFormat


class ContactStep(title: String) : Step<String>(title) {

    private var inputPhone: TextInputLayout? = null
    private var etPhone: TextInputEditText? = null
    private var radioContact: RadioGroup? = null
    private var radioEmail: RadioButton? = null
    private var contactPicked: String? = null

    override fun restoreStepData(data: String?) {

    }

    override fun isStepDataValid(stepData: String?): IsDataValid {
        return if (contactPicked != null) {
            IsDataValid(true)
        } else {
            IsDataValid(false)
        }
    }

    override fun onStepMarkedAsCompleted(animated: Boolean) {

    }

    override fun getStepDataAsHumanReadableString(): String {
        return "$contactPicked"
    }

    override fun createStepContentLayout(): View {
        val view = LayoutInflater.from(context).inflate(R.layout.step_phone, null, false)
        inputPhone = view.findViewById(R.id.inputPhone)
        radioContact = view.findViewById(R.id.radioContact)
        radioEmail = view.findViewById(R.id.radioEmail)
        etPhone = view.findViewById(R.id.etPhone)
        contactPicked = FirebaseAuth.getInstance().currentUser?.email

        updateContact()

        radioContact?.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.radioPhone -> {
                    updateSubtitle("", false)
                    markAsUncompleted("Please enter a phone number", false)
                    inputPhone?.visible()
                }
                R.id.radioEmail -> {
                    contactPicked = FirebaseAuth.getInstance().currentUser?.email
                    inputPhone?.gone()
                    markAsCompleted(false)
                }
            }
        }

        etPhone?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(characters: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (characters?.length == 10) {
                    etPhone?.run {
                        val inputMethodManager =
                            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputMethodManager.hideSoftInputFromWindow(this.windowToken, 0)

                    }
                    updateSubtitle(characters.toString(), false)
                    contactPicked = characters.toString()
                    markAsCompleted(false)
                } else {
                    updateSubtitle("", false)
                    markAsUncompleted("Please enter a phone number", false)
                }
            }
        })

        return view
    }

    private fun updateContact() {
        Handler().postDelayed({
            updateSubtitle(FirebaseAuth.getInstance().currentUser?.email, false)
        }, 2000)
    }

    override fun getStepData(): String {
        return "$contactPicked"
    }

    override fun onStepOpened(animated: Boolean) {
        radioEmail?.isChecked = true
        updateContact()
        markAsCompleted(false)
    }

    override fun onStepMarkedAsUncompleted(animated: Boolean) {

    }

    override fun onStepClosed(animated: Boolean) {

    }
}