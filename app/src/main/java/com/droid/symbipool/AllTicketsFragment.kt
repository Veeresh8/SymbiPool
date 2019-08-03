package com.droid.symbipool

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.droid.symbipool.TicketUtils.addLocations
import com.droid.symbipool.TicketUtils.allEndLocations
import com.droid.symbipool.TicketUtils.allStartLocations
import com.droid.symbipool.TicketUtils.removeLocations
import com.droid.symbipool.adapters.AllTicketsAdapter
import com.droid.symbipool.filterTicket.FilterActivity
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_all_tickets.*
import java.io.Serializable


class AllTicketsFragment : Fragment() {

    private var adapter: AllTicketsAdapter? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private var firestore: FirebaseFirestore? = null
    private var recyclerView: RecyclerView? = null
    private var tvFilter: TextView? = null
    private var cpDate: Chip? = null

    private val allTickets: ArrayList<Ticket> = ArrayList()

    companion object {
        fun newInstance(): AllTicketsFragment = AllTicketsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_all_tickets, container, false)
        initUI(view)
        initQuery()
        initClicks()
        return view
    }

    private fun initClicks() {
        tvFilter?.run {
            this.setOnClickListener {
                val mainActivity = activity as MainActivity
                mainActivity.startFilterActivity()
            }
        }
    }

    private fun initUI(view: View) {
        recyclerView = view.findViewById(R.id.rvAllTickets)
        cpDate = view.findViewById(R.id.cpDate)
        tvFilter = view.findViewById(R.id.tvFilter)
        linearLayoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        recyclerView?.layoutManager = linearLayoutManager
        adapter = AllTicketsAdapter {
            Snackbar.make(requireView(), "${it.startLocation?.subLocality}", Snackbar.LENGTH_LONG).show()
        }
        recyclerView?.adapter = adapter
        firestore = FirebaseFirestore.getInstance()
        cpDate?.text = DatabaseUtils.getCurrentDate()
    }

    private fun initQuery() {
        FirebaseFirestore.getInstance().collection(DatabaseUtils.TICKET_COLLECTION)
            .whereEqualTo(DatabaseUtils.DATE, DatabaseUtils.getCurrentDate())
            .orderBy(DatabaseUtils.TIME, Query.Direction.ASCENDING)
            .addSnapshotListener { querySnapshot, error ->
                querySnapshot?.run {
                    if (error != null) {
                        Log.e(TAG, "Query error: ${error.message}")
                        return@addSnapshotListener
                    }

                    for (document in querySnapshot.documentChanges) {
                        val ticket = document.document.toObject(Ticket::class.java)
                        when (document.type) {
                            DocumentChange.Type.ADDED -> {
                                Log.i(TAG, "New ticket: $ticket")
                                addLocations(ticket)
                                allTickets.add(ticket)
                            }
                            DocumentChange.Type.MODIFIED -> {
                                Log.i(TAG, "Modified ticket: $ticket")
                                addLocations(ticket)
                                allTickets.forEachIndexed { index, currentTicket ->
                                    if (ticket.ticketID == currentTicket.ticketID) {
                                        allTickets[index] = ticket
                                    }
                                }
                            }
                            DocumentChange.Type.REMOVED -> {
                                Log.i(TAG, "Removed ticket: $ticket")
                                removeLocations(ticket)
                                allTickets.remove(ticket)
                            }
                        }
                    }

                    allTickets.sortBy {
                        it.time
                    }

                    adapter?.submitList(allTickets.toList())

                    Log.i(javaClass.simpleName, "Start locations: $allStartLocations")
                    Log.i(javaClass.simpleName, "End locations: $allEndLocations")

                    Snackbar.make(requireView(), "Found ${allTickets.size} results", Snackbar.LENGTH_LONG).show()
                }
            }
    }

    fun filter(ticketFilter: TicketFilter?) {
        Log.i(javaClass.simpleName, "Filter ticket: $ticketFilter")
    }
}