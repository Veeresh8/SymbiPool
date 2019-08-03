package com.droid.symbipool

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.datePicker
import com.droid.symbipool.TicketUtils.addLocations
import com.droid.symbipool.TicketUtils.allEndLocations
import com.droid.symbipool.TicketUtils.allStartLocations
import com.droid.symbipool.TicketUtils.allTickets
import com.droid.symbipool.TicketUtils.clearAllLists
import com.droid.symbipool.TicketUtils.endLocalityCheck
import com.droid.symbipool.TicketUtils.filteredList
import com.droid.symbipool.TicketUtils.genderCheck
import com.droid.symbipool.TicketUtils.removeLocations
import com.droid.symbipool.TicketUtils.startLocalityCheck
import com.droid.symbipool.adapters.AllTicketsAdapter
import com.droid.symbipool.creationSteps.DateStep
import com.droid.symbipool.filterTicket.FilterActivity
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query


class AllTicketsFragment : Fragment() {

    private var listener: ListenerRegistration? = null
    private var adapter: AllTicketsAdapter? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private var firestore: FirebaseFirestore? = null
    private var recyclerView: RecyclerView? = null
    private var tvEmpty: TextView? = null
    private var progressBar: ProgressBar? = null
    private var rootLayout: RelativeLayout? = null
    private var cpResetFilter: Chip? = null
    private var cpDate: Chip? = null
    private var cpFilter: Chip? = null
    private var lastPickedDate: String? = null

    companion object {
        fun newInstance(): AllTicketsFragment = AllTicketsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_all_tickets, container, false)
        initUI(view)
        initQuery(DatabaseUtils.getCurrentDate())
        initClicks()
        return view
    }

    private fun initClicks() {
        cpResetFilter?.run {
            this.setOnClickListener {
                resetFilter()
            }
            this.setOnCloseIconClickListener {
                resetFilter()
            }
        }

        cpFilter?.run {
            this.setOnClickListener {

                if (tvEmpty?.visibility == View.VISIBLE) {
                    Snackbar.make(this, "No results to filter", Snackbar.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val activity = activity as MainActivity
                activity.startFilterActivity()
            }
        }

        cpDate?.run {
            this.setOnClickListener {
                onDatePicked()
            }
            this.setOnCloseIconClickListener {
                onDatePicked()
            }
        }
    }

    private fun onDatePicked() {
        MaterialDialog(requireContext()).show {
            datePicker { _, date ->
                val datePicked = DateStep.dateFormat.format(date.time)
                if (datePicked != lastPickedDate) {
                    listener?.remove()
                    cpDate?.text = datePicked
                    showProgress(true)
                    clearAllLists()
                    initQuery(datePicked)
                    showProgress(false)
                }
            }
        }
    }

    private fun Chip.resetFilter() {
        showProgress(true)
        adapter?.submitList(allTickets)
        this.visibility = View.GONE
        showProgress(false)
        showCount(allTickets)
        recyclerView?.scrollToPosition(0)
    }

    private fun showProgress(showProgress: Boolean) {
        if (showProgress) {
            progressBar?.visibility = View.VISIBLE
            tvEmpty?.visibility = View.GONE
            recyclerView?.visibility = View.GONE
        } else {
            Handler().postDelayed({
                progressBar?.visibility = View.GONE
                recyclerView?.visibility = View.VISIBLE
            }, 2000)
        }
    }

    private fun initUI(view: View) {
        recyclerView = view.findViewById(R.id.rvAllTickets)
        cpDate = view.findViewById(R.id.cpDate)
        cpFilter = view.findViewById(R.id.cpFilter)
        cpResetFilter = view.findViewById(R.id.cpResetFilter)
        progressBar = view.findViewById(R.id.progressBar)
        rootLayout = view.findViewById(R.id.rootLayout)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        linearLayoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        recyclerView?.layoutManager = linearLayoutManager
        adapter = AllTicketsAdapter {
            Snackbar.make(requireView(), "${it.startLocation?.subLocality}", Snackbar.LENGTH_LONG).show()
        }
        recyclerView?.adapter = adapter
        firestore = FirebaseFirestore.getInstance()
        cpDate?.text = DatabaseUtils.getCurrentDate()
    }

    private fun initQuery(date: String) {
        lastPickedDate = date
        listener = FirebaseFirestore.getInstance().collection(DatabaseUtils.TICKET_COLLECTION)
            .whereEqualTo(DatabaseUtils.DATE, date)
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

                    adapter?.submitList(allTickets.map { it })

                    Log.i(javaClass.simpleName, "Start locations: $allStartLocations")
                    Log.i(javaClass.simpleName, "End locations: $allEndLocations")

                    recyclerView?.scrollToPosition(0)

                    showCount(allTickets)
                }
            }
    }

    fun filter(ticketFilter: TicketFilter?) {
        ticketFilter?.run {

            filteredList = ArrayList()

            showProgress(true)

            val startLocality = this.startLocation?.first
            val startCity = this.startLocation?.second

            val endLocality = this.endLocation?.first
            val endCity = this.endLocation?.second

            val genderPreference = this.genderPreference

            Log.i(
                javaClass.simpleName,
                "$startLocality || $startCity -> $endLocality || $endCity -> $genderPreference"
            )

            if (startLocality == TicketUtils.ANY_LOCATION && endLocality != TicketUtils.ANY_LOCATION) {
                filteredList = allTickets.map { it }
                    .filter { endLocalityCheck(it, ticketFilter) && genderCheck(it, ticketFilter) }
                Log.i(javaClass.simpleName, "(END LOCATION) Filter ticket ${filteredList.size}: $filteredList")
                return@run
            }

            if (startLocality != TicketUtils.ANY_LOCATION && endLocality == TicketUtils.ANY_LOCATION) {
                filteredList = allTickets.map { it }
                    .filter { startLocalityCheck(it, ticketFilter) && genderCheck(it, ticketFilter) }
                Log.i(javaClass.simpleName, "(START LOCATION) Filter ticket ${filteredList.size}: $filteredList")
                return@run
            }

            if (startLocality == TicketUtils.ANY_LOCATION && endLocality == TicketUtils.ANY_LOCATION) {
                filteredList = allTickets.map { it }
                    .filter { genderCheck(it, ticketFilter) }
                Log.i(javaClass.simpleName, "(GENDER) Filter ticket ${filteredList.size}: $filteredList")
                return@run
            }

            if (startLocality != TicketUtils.ANY_LOCATION && endLocality != TicketUtils.ANY_LOCATION) {
                filteredList = allTickets.map { it }
                    .filter {
                        startLocalityCheck(it, ticketFilter) &&
                                endLocalityCheck(it, ticketFilter) &&
                                genderCheck(it, ticketFilter)
                    }
                Log.i(javaClass.simpleName, "(FILTERED) Filter ticket ${filteredList.size}: $filteredList")
                return@run
            }
        }

        adapter?.submitList(filteredList)

        cpResetFilter?.run {
            this.visibility = View.VISIBLE
        }

        showProgress(false)

        showCount(filteredList)
    }

    private fun showEmptyLayout(list: List<Ticket>) {
        if (list.isEmpty()) {
            tvEmpty?.visibility = View.VISIBLE
            recyclerView?.visibility = View.GONE
        } else {
            tvEmpty?.visibility = View.GONE
            recyclerView?.visibility = View.VISIBLE
        }
    }

    private fun showCount(list: List<Ticket>) {
        Handler().postDelayed({
            rootLayout?.run {
                Snackbar.make(this, "Found ${list.size} results", Snackbar.LENGTH_LONG).show()
                showEmptyLayout(list)
            }
        }, 1500)
    }
}