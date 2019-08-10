package com.droid.symbipool.filterTicket

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.droid.symbipool.MainActivity
import com.droid.symbipool.R
import com.droid.symbipool.TicketFilter
import com.droid.symbipool.creationSteps.GenderStep
import com.droid.symbipool.filterSteps.EndStep
import com.droid.symbipool.filterSteps.StartStep
import ernestoyaquello.com.verticalstepperform.listener.StepperFormListener
import kotlinx.android.synthetic.main.activity_create_ticket.*


class FilterActivity : AppCompatActivity(), StepperFormListener {

    private lateinit var genderStep: GenderStep
    private lateinit var startStep: StartStep
    private lateinit var endStep: EndStep

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)
        initStepperForm()
        initClickListeners()
    }

    private fun initClickListeners() {
        ivClose.setOnClickListener {
            finish()
        }
    }

    private fun initStepperForm() {
        val stepTitles = resources.getStringArray(R.array.steps_titles_filter)
        startStep = StartStep(stepTitles[0])
        endStep = EndStep(stepTitles[1])
        genderStep = GenderStep(stepTitles[2])
        stepperForm?.setup(this, startStep, endStep, genderStep)?.init()
    }

    override fun onCompletedForm() {
        val ticketFilter = TicketFilter(
            startLocation = startStep.stepData,
            endLocation = endStep.stepData,
            genderPreference = genderStep.stepData
        )

        Log.i(javaClass.simpleName, "Filter ticket: $ticketFilter")

        val intent = Intent()
        intent.putExtra(MainActivity.TICKET_FILTER, ticketFilter)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onCancelledForm() {
        finish()
    }

}
