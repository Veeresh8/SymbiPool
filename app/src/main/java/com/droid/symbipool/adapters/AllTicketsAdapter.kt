package com.droid.symbipool.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.droid.symbipool.*
import com.droid.symbipool.TicketUtils.getPreference
import com.droid.symbipool.creationSteps.GenderStep
import kotlinx.android.synthetic.main.item_all_ticket.view.*
import kotlinx.android.synthetic.main.item_pagination_ticket.view.*

class AllTicketsAdapter(
    private val clickListener: (Ticket) -> Unit,
    private val canDelete: Boolean,
    private val clickInterface: ClickInterface
) :
    androidx.recyclerview.widget.ListAdapter<Ticket, AllTicketsAdapter.ViewHolder>(TicketDiff()) {

    private val TYPE_PAGINATION_TICKET: Int = 0
    private val TYPE_TICKET: Int = 1

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isPaginationTicket) {
            TYPE_PAGINATION_TICKET
        } else {
            TYPE_TICKET
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == TYPE_TICKET) {
            val inflater = LayoutInflater.from(parent.context)
            ViewHolder(inflater.inflate(R.layout.item_all_ticket, parent, false))
        } else {
            val inflater = LayoutInflater.from(parent.context)
            ViewHolder(inflater.inflate(R.layout.item_pagination_ticket, parent, false))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ticket = getItem(position) as Ticket

        if (ticket.isPaginationTicket) {
            holder.bindPaginationTicket(clickInterface)
        } else {
            holder.bind(ticket, clickListener, canDelete, clickInterface)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindPaginationTicket(clickInterface: ClickInterface) {
            itemView.tvNextDate.text = "Load ${DatabaseUtils.getNextDate()} tickets?"
            itemView.btnLoadMore.setOnClickListener { clickInterface.loadMoreTickets() }
            itemView.btnChangeDate.setOnClickListener { clickInterface.changeDate() }
        }

        fun bind(ticket: Ticket, clickListener: (Ticket) -> Unit, canDelete: Boolean, clickInterface: ClickInterface) {

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

            DatabaseUtils.latestDate = ticket.date

            itemView.setOnClickListener { clickInterface.loadTicketDetails(ticket)}
            itemView.btnContact.setOnClickListener { clickListener(ticket) }
            itemView.btnDelete.setOnClickListener { clickListener(ticket) }
        }

    }

    interface ClickInterface {
        fun loadMoreTickets()
        fun changeDate()
        fun loadTicketDetails(ticket: Ticket)
    }
}