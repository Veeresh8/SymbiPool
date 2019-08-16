package com.droid.symbipool.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.droid.symbipool.*
import com.droid.symbipool.TicketUtils.getPreference
import com.droid.symbipool.creationSteps.GenderStep
import kotlinx.android.synthetic.main.item_all_ticket.view.*

class AllTicketsAdapter(private val clickListener: (Ticket) -> Unit, private val canDelete: Boolean) :
    androidx.recyclerview.widget.ListAdapter<Ticket, AllTicketsAdapter.ViewHolder>(TicketDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_all_ticket, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener, canDelete)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(ticket: Ticket, clickListener: (Ticket) -> Unit, canDelete: Boolean) {
            itemView.tvDateTime.text = TicketUtils.getTimeAndDate(ticket)
            itemView.tvStartAddress.text = TicketUtils.getStartAddress(ticket)
            itemView.tvDestination.text = TicketUtils.getDestinationAddress(ticket)

            if (getPreference(ticket) != GenderStep.GenderPreference.NONE.name) {
                itemView.tvPreference.visible()
                itemView.tvPreference.text = "${getPreference(ticket)} ONLY"
            } else {
                itemView.tvPreference.gone()
            }

            if (canDelete) {
                itemView.btnContact.invisible()
                itemView.btnDelete.visible()
            } else {
                itemView.btnContact.visible()
                itemView.btnDelete.gone()
            }

            itemView.tvNavStart.setOnClickListener {
                ticket.startLocation?.run {
                    if (this.lat != null && this.longi != null) {
                        TicketUtils.launchMapsWithCoordinates(this.lat, this.longi, itemView.context)
                    }
                }
            }

            itemView.tvNavEnd.setOnClickListener {
                ticket.endLocation?.run {
                    if (this.lat != null && this.longi != null) {
                        TicketUtils.launchMapsWithCoordinates(this.lat, this.longi, itemView.context)
                    }
                }
            }

            itemView.btnContact.setOnClickListener { clickListener(ticket) }
            itemView.btnDelete.setOnClickListener { clickListener(ticket) }
        }
    }
}