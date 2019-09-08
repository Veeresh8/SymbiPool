package com.droid.symbipool

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import com.afollestad.materialdialogs.MaterialDialog
import com.droid.symbipool.creationSteps.GenderStep
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_ticket_details.*
import org.jetbrains.anko.browse

class TicketDetailsActivity : AppCompatActivity() {

    private var ticket: Ticket? = null
    private var canDelete: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_details)
        clickListeners()
        setTicketDetails(intent)
    }

    private fun setTicketDetails(intent: Intent?) {
        ticket = intent?.getParcelableExtra("ticket") as Ticket
        canDelete = intent.getBooleanExtra("can_delete", false)

        canDelete?.run {
            if (this) {
                btnTicketContact.gone()
                btnTicketDelete.visible()
            } else {
                btnTicketContact.visible()
                btnTicketDelete.gone()
            }
        }

        ticket?.run {
            tvDateTime.text = TicketUtils.getTimeAndDate(this)
            tvStartAddress.text = TicketUtils.getFullStartAddress(this)
            tvDestination.text = TicketUtils.getFullDestinationAddress(this)

            if (TicketUtils.getPreference(this) != GenderStep.GenderPreference.NONE.name) {
                tvPreference.visible()
                tvPreference.text = "${TicketUtils.getPreference(this)} ONLY"
            } else {
                tvPreference.gone()
            }
        }
    }

    private fun clickListeners() {
        ivBack.setOnClickListener { finish() }
        ivSupport.setOnClickListener {
            browse("https://www.cry.org")
        }
        btnTicketContact.setOnClickListener { ticket?.let { it1 -> launchContact(ticket = it1) } }
        btnTicketDelete.setOnClickListener {
            MaterialDialog(this).show {
                title(R.string.delete)
                cancelable(true)
                message(R.string.delete_message)
                positiveButton(R.string.delete) {
                    ticket?.let { it1 -> performDeletion(it1) }
                }
            }
        }

        tvNavStart.setOnClickListener {
            ticket?.startLocation?.run {
                if (this.lat != null && this.longi != null) {
                    TicketUtils.launchMapsWithCoordinates(
                        this.lat,
                        this.longi,
                        this@TicketDetailsActivity
                    )
                }
            }
        }

        tvNavEnd.setOnClickListener {
            ticket?.endLocation?.run {
                if (this.lat != null && this.longi != null) {
                    TicketUtils.launchMapsWithCoordinates(
                        this.lat,
                        this.longi,
                        this@TicketDetailsActivity
                    )
                }
            }
        }
    }

    private fun performDeletion(ticket: Ticket) {
        ticket.ticketID?.run {
            FirebaseFirestore.getInstance().collection(DatabaseUtils.TICKET_COLLECTION)
                .document(this)
                .delete()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.i(javaClass.simpleName, "Removed ticket: ${ticket.ticketID}")
                        finish()
                    } else {
                        Log.e(javaClass.simpleName, "${it.exception?.message}")
                        showSnack("Failed to remove ticket, try later")
                    }
                }
        }
    }

    private fun launchContact(ticket: Ticket) {
        ticket.contact?.run {
            if (this.isDigitsOnly()) {
                askPermission(Manifest.permission.CALL_PHONE) {
                    makeCall(this@TicketDetailsActivity, this)
                }.onDeclined {
                    showSnack("Grant permission to place a call")
                }.runtimePermission.onForeverDenied {
                    startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                        )
                    )
                }.onAccepted {
                    makeCall(this@TicketDetailsActivity, this)
                }
            } else {
                try {
                    val emailIntent =
                        Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", this, null))
                    emailIntent.putExtra(
                        Intent.EXTRA_SUBJECT,
                        "Regarding a ride on ${TicketUtils.getTimeAndDate(ticket)}"
                    )
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "")
                    startActivity(Intent.createChooser(emailIntent, "Send email"))
                } catch (exception: Exception) {
                    showSnack("No email clients found, please install one")
                }
            }
        }
    }

    private fun showSnack(message: String) {
        rootLayout?.run {
            Snackbar.make(
                this,
                message,
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
}
