package com.droid.symbipool.adapters

import androidx.recyclerview.widget.DiffUtil
import com.droid.symbipool.Ticket

class TicketDiff : DiffUtil.ItemCallback<Ticket>() {

    override fun areItemsTheSame(oldItem: Ticket, newItem: Ticket): Boolean {
        return oldItem.ticketID == newItem.ticketID
    }

    override fun areContentsTheSame(oldItem: Ticket, newItem: Ticket): Boolean {
        return oldItem == newItem
    }
}