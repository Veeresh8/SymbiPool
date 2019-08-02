package com.droid.symbipool

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.droid.symbipool.TicketUtils.getPreference
import com.droid.symbipool.steps.GenderStep
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_all_ticket.*
import kotlinx.android.synthetic.main.item_all_ticket.view.*

class AllTicketsFragment : Fragment() {

    private var adapter: FirestoreRecyclerAdapter<*, *>? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private var firestore: FirebaseFirestore? = null
    private var recyclerView: RecyclerView? = null

    companion object {
        fun newInstance(): AllTicketsFragment = AllTicketsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_all_tickets, container, false)
        initUI(view)
        initQuery()
        return view
    }

    private fun initUI(view: View) {
        recyclerView = view.findViewById(R.id.rvAllTickets)
        linearLayoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        recyclerView?.layoutManager = linearLayoutManager
        firestore = FirebaseFirestore.getInstance()
    }

    private fun initQuery() {
        val collection = FirebaseFirestore.getInstance().collection(DatabaseUtils.TICKET_COLLECTION)
            .whereEqualTo("date", DatabaseUtils.getCurrentDate())
            .orderBy("time", Query.Direction.ASCENDING)

        val response = FirestoreRecyclerOptions.Builder<Ticket>()
            .setQuery(collection, Ticket::class.java)
            .build()

        adapter = object : FirestoreRecyclerAdapter<Ticket, TicketViewHolder>(response) {
            @SuppressLint("SetTextI18n")
            override fun onBindViewHolder(holder: TicketViewHolder, position: Int, ticket: Ticket) {
                holder.itemView.tvDateTime.text = TicketUtils.getTimeAndDate(ticket)
                holder.itemView.tvStartAddress.text = TicketUtils.getStartAddress(ticket)
                holder.itemView.tvDestination.text = TicketUtils.getDestinationAddress(ticket)

                if (getPreference(ticket) != GenderStep.GenderPreference.NONE.name) {
                    holder.itemView.tvPreference.visibility = View.VISIBLE
                    holder.itemView.tvPreference.text = "${getPreference(ticket)} ONLY"
                } else {
                    holder.itemView.tvPreference.visibility = View.GONE
                }

                holder.itemView.tvNavStart.setOnClickListener {
                    ticket.startLocation?.run {
                        if (this.lat != null && this.longi != null) {
                            TicketUtils.launchMapsWithCoordinates(this.lat, this.longi, holder.itemView.context)
                        }
                    }
                }

                holder.itemView.tvNavEnd.setOnClickListener {
                    ticket.endLocation?.run {
                        if (this.lat != null && this.longi != null) {
                            TicketUtils.launchMapsWithCoordinates(this.lat, this.longi, holder.itemView.context)
                        }
                    }
                }
            }

            override fun onCreateViewHolder(group: ViewGroup, i: Int): TicketViewHolder {
                val view = LayoutInflater.from(group.context)
                    .inflate(R.layout.item_all_ticket, group, false)
                return TicketViewHolder(view)
            }

            override fun onError(exception: FirebaseFirestoreException) {
                Log.e("Exception parsing: ", "${exception.message}")
            }
        }

        adapter?.notifyDataSetChanged()
        recyclerView?.adapter = adapter
    }

    class TicketViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
        LayoutContainer

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }
}