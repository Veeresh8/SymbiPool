package com.droid.symbipool

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.datePicker
import com.droid.symbipool.TicketUtils.addLocations
import com.droid.symbipool.TicketUtils.addTicket
import com.droid.symbipool.TicketUtils.allEndLocations
import com.droid.symbipool.TicketUtils.allStartLocations
import com.droid.symbipool.TicketUtils.allTickets
import com.droid.symbipool.TicketUtils.appendTickets
import com.droid.symbipool.TicketUtils.clearAllLists
import com.droid.symbipool.TicketUtils.distinctTicketsSorted
import com.droid.symbipool.TicketUtils.endCityCheck
import com.droid.symbipool.TicketUtils.endLocalityCheck
import com.droid.symbipool.TicketUtils.filteredList
import com.droid.symbipool.TicketUtils.genderCheck
import com.droid.symbipool.TicketUtils.removeLocations
import com.droid.symbipool.TicketUtils.removeTicket
import com.droid.symbipool.TicketUtils.replaceTicket
import com.droid.symbipool.TicketUtils.setLatestDate
import com.droid.symbipool.TicketUtils.startCityCheck
import com.droid.symbipool.TicketUtils.startLocalityCheck
import com.droid.symbipool.adapters.AllTicketsAdapter
import com.droid.symbipool.creationSteps.DateStep
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class AllTicketsFragment : Fragment() {

    private var isModified: Boolean? = null
    private var paginationHint: TextView? = null
    private var sheetView: View? = null
    private var mBottomSheetDialog: BottomSheetDialog? = null
    private var listener: ListenerRegistration? = null
    private var adapter: AllTicketsAdapter? = null
    private var firestore: FirebaseFirestore? = null
    private var recyclerView: RecyclerView? = null
    private var tvEmpty: TextView? = null
    private var progressBar: ProgressBar? = null
    private var rootLayout: RelativeLayout? = null
    private var cpResetFilter: Chip? = null
    private var cpNextDate: Chip? = null
    private var cpFilter: Chip? = null
    private var lastPickedDate: String? = null
    private var TAG = javaClass.simpleName

    companion object {
        fun newInstance(): AllTicketsFragment = AllTicketsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_all_tickets, container, false)
        initUI(view)
        showProgress(true)
        initQuery(DatabaseUtils.getCurrentDate())
        initClicks()
        return view
    }

    private fun onDatePicked() {
        MaterialDialog(requireContext()).show {
            datePicker { _, date ->
                val datePicked = DateStep.dateFormat.format(date.time)
                if (datePicked != lastPickedDate) {
                    performPagination(datePicked)
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
        cpFilter = view.findViewById(R.id.cpFilter)
        cpNextDate = view.findViewById(R.id.cpNextDate)
        cpResetFilter = view.findViewById(R.id.cpResetFilter)
        progressBar = view.findViewById(R.id.progressBar)
        rootLayout = view.findViewById(R.id.rootLayout)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        context?.let { recyclerView?.withLinearLayout(it) }
        adapter = AllTicketsAdapter({ ticket -> launchContact(ticket) },
            false,
            object : AllTicketsAdapter.ClickInterface {
                override fun loadTicketDetails(ticket: Ticket) {
                    navigateToTicketDetails(ticket)
                }

                override fun loadMoreTickets() {
                    performPagination()
                }

                override fun changeDate() {
                    MaterialDialog(requireContext()).show {
                        datePicker { _, date ->
                            val datePicked = DateStep.dateFormat.format(date.time)
                            if (datePicked != lastPickedDate) {
                                performPagination(datePicked)
                            }
                        }
                    }
                }
            })

        recyclerView?.adapter = adapter
        firestore = FirebaseFirestore.getInstance()
        mBottomSheetDialog = activity?.let { BottomSheetDialog(it) }
        mBottomSheetDialog?.window?.setDimAmount(0.9F)
        sheetView = layoutInflater.inflate(
            R.layout.bs_pagination_loading,
            null
        )
        sheetView?.run {
            paginationHint = this.findViewById(R.id.tvSupportHint) as TextView
            mBottomSheetDialog?.setContentView(this)
        }
    }

    private fun navigateToTicketDetails(ticket: Ticket) {
        val activity = activity as MainActivity
        activity.startTicketDetailsActivity(false, ticket)
    }

    private fun performPagination() {
        paginationHint?.text = "Loading tickets for \n\n${DatabaseUtils.getNextDate()}"
        showPaginationProgress(true)
        Handler().postDelayed({
            getPaginatedResultsForDate(DatabaseUtils.getNextDate())
        }, 1500)
    }

    private fun performPagination(date: String) {
        paginationHint?.text = "Loading tickets for \n\n${date}"
        showPaginationProgress(true)
        Handler().postDelayed({
            getPaginatedResultsForDate(date)
        }, 1000)
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

        cpNextDate?.run {
            this.setOnClickListener {
                onDatePicked()
            }
        }
    }

    private fun showPaginationProgress(show: Boolean) {
        if (show) {
            mBottomSheetDialog?.show()
        } else {
            mBottomSheetDialog?.dismiss()
        }
    }

    private fun getPaginatedResultsForDate(date: String) {
        Log.i(
            javaClass.simpleName,
            "Performing pagination for date $date"
        )
        FirebaseFirestore.getInstance().collection(DatabaseUtils.TICKET_COLLECTION)
            .whereEqualTo(DatabaseUtils.DATE, date)
            .orderBy(DatabaseUtils.TIME, Query.Direction.ASCENDING)
            .addSnapshotListener { querySnapshot, error ->
                querySnapshot?.run {
                    if (error != null) {
                        Log.e(javaClass.simpleName, "Pagination query error: ${error.message}")
                        return@addSnapshotListener
                    }

                    Log.i(
                        javaClass.simpleName,
                        "Pagination result size ${querySnapshot.documentChanges.size}"
                    )

                    if (querySnapshot.documentChanges.size == 0) {

                        DatabaseUtils.latestDate = date

                        adapter?.notifyItemChanged(allTickets.size - 1)
                        showPaginationProgress(false)
                        val snackBar = rootLayout?.let {
                            Snackbar
                                .make(
                                    it,
                                    "No results for $date \nCheck ${DatabaseUtils.getNextDate()}",
                                    5000
                                )
                                .setAction("Load") {
                                    performPagination()
                                }
                        }
                        snackBar?.show()
                        return@addSnapshotListener
                    }

                    for (document in querySnapshot.documentChanges) {
                        val ticket = document.document.toObject(Ticket::class.java)
                        when (document.type) {
                            DocumentChange.Type.ADDED -> {
                                Log.i(TAG, "New ticket: $ticket")
                                addLocations(ticket)
                                addTicket(ticket)
                            }
                            DocumentChange.Type.MODIFIED -> {
                                Log.i(TAG, "Modified ticket: $ticket")
                                addLocations(ticket)
                                replaceTicket(ticket)
                            }
                            DocumentChange.Type.REMOVED -> {
                                Log.i(TAG, "Removed ticket: $ticket")
                                removeLocations(ticket)
                                removeTicket(ticket)
                            }
                        }
                    }

                    if (allTickets.contains(TicketUtils.getPaginationTicket()))
                        removeTicket(TicketUtils.getPaginationTicket())

                    val filteredList = allTickets.distinctBy { ticket ->
                        ticket.ticketID
                    }

                    allTickets = filteredList as ArrayList<Ticket>

                    allTickets.add(TicketUtils.getPaginationTicket())

                    allTickets.sortBy {
                        it.time
                    }

                    if (allTickets.size >= 2) {
                        DatabaseUtils.latestDate = allTickets[allTickets.size - 2].date
                    }

                    adapter?.run {
                        submitList(allTickets.map { it })
                    }

                    EventBus.getDefault()
                        .postSticky(UserTicketEvent(TicketUtils.getUserCreatedTickets()))

                    Handler().postDelayed({
                        Log.i(
                            javaClass.simpleName,
                            "(PAGINATION) Changing latest date to: ${DatabaseUtils.latestDate} || ${DatabaseUtils.getNextDate()}}"
                        )
                        adapter?.notifyItemChanged(allTickets.size - 1)
                    }, 500)

                    showSnack(rootLayout, "Found ${querySnapshot.documentChanges.size} results")

                    showPaginationProgress(false)
                }
            }
    }

    private fun initQuery(date: String) {
        clearAllLists()
        lastPickedDate = date
        listener = FirebaseFirestore.getInstance().collection(DatabaseUtils.TICKET_COLLECTION)
            .whereEqualTo(DatabaseUtils.DATE, date)
            .orderBy(DatabaseUtils.TIME, Query.Direction.ASCENDING)
            .addSnapshotListener { querySnapshot, error ->
                querySnapshot?.run {
                    if (error != null) {
                        Log.e(javaClass.simpleName, "Query error: ${error.message}")
                        return@addSnapshotListener
                    }

                    isModified = false

                    for (document in querySnapshot.documentChanges) {
                        val ticket = document.document.toObject(Ticket::class.java)
                        when (document.type) {
                            DocumentChange.Type.ADDED -> {
                                Log.i(TAG, "New ticket: $ticket")
                                addLocations(ticket)
                                allTickets.add(ticket)
                            }
                            DocumentChange.Type.MODIFIED -> {
                                isModified = true
                                Log.i(TAG, "Modified ticket: $ticket")
                                addLocations(ticket)
                                allTickets.forEachIndexed { index, currentTicket ->
                                    if (ticket.ticketID == currentTicket.ticketID) {
                                        allTickets[index] = ticket
                                    }
                                }
                            }
                            DocumentChange.Type.REMOVED -> {
                                isModified = true
                                Log.i(TAG, "Removed ticket: $ticket")
                                removeLocations(ticket)
                                allTickets.remove(ticket)
                            }
                        }
                    }

                    if (allTickets.contains(TicketUtils.getPaginationTicket()))
                        allTickets.remove(TicketUtils.getPaginationTicket())

                    val filteredList = allTickets.distinctBy { ticket -> ticket.ticketID }

                    allTickets = filteredList as ArrayList<Ticket>

                    isModified?.run {
                        if (!this) {
                            allTickets.sortBy {
                                it.time
                            }
                        }
                    }

                    allTickets.add(TicketUtils.getPaginationTicket())

                    if (allTickets.size >= 2) {
                        DatabaseUtils.latestDate = allTickets[allTickets.size - 2].date
                    }

                    adapter?.submitList(allTickets.map { it })

                    Log.i(
                        javaClass.simpleName,
                        "Start locations: $allStartLocations || End locations: $allEndLocations"
                    )

                    showCount(allTickets)

                    showProgress(false)

                    EventBus.getDefault()
                        .postSticky(UserTicketEvent(TicketUtils.getUserCreatedTickets()))

                    Handler().postDelayed({
                        Log.i(
                            javaClass.simpleName,
                            "Changing latest date to: ${DatabaseUtils.latestDate} || ${DatabaseUtils.getNextDate()}}"
                        )
                        adapter?.notifyItemChanged(allTickets.size - 1)
                    }, 500)
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

            filteredList = allTickets.map { it }
                .filter { startCityCheck(it, ticketFilter) && endCityCheck(it, ticketFilter) }

            Log.i(javaClass.simpleName, "(CITY) Filter ticket ${filteredList.size}: $filteredList")

            if (startLocality == TicketUtils.ANY_LOCATION && endLocality != TicketUtils.ANY_LOCATION) {
                filteredList = filteredList
                    .filter { endLocalityCheck(it, ticketFilter) && genderCheck(it, ticketFilter) }
                Log.i(
                    javaClass.simpleName,
                    "(END LOCATION) Filter ticket ${filteredList.size}: $filteredList"
                )
                return@run
            }

            if (startLocality != TicketUtils.ANY_LOCATION && endLocality == TicketUtils.ANY_LOCATION) {
                filteredList = filteredList
                    .filter {
                        startLocalityCheck(it, ticketFilter) && genderCheck(
                            it,
                            ticketFilter
                        )
                    }
                Log.i(
                    javaClass.simpleName,
                    "(START LOCATION) Filter ticket ${filteredList.size}: $filteredList"
                )
                return@run
            }

            if (startLocality == TicketUtils.ANY_LOCATION && endLocality == TicketUtils.ANY_LOCATION) {
                filteredList = filteredList
                    .filter { genderCheck(it, ticketFilter) }
                Log.i(
                    javaClass.simpleName,
                    "(GENDER) Filter ticket ${filteredList.size}: $filteredList"
                )
                return@run
            }

            if (startLocality != TicketUtils.ANY_LOCATION && endLocality != TicketUtils.ANY_LOCATION) {
                filteredList = filteredList
                    .filter {
                        startLocalityCheck(it, ticketFilter) &&
                                endLocalityCheck(it, ticketFilter) &&
                                genderCheck(it, ticketFilter)
                    }
                Log.i(
                    javaClass.simpleName,
                    "(FILTERED) Filter ticket ${filteredList.size}: $filteredList"
                )
                return@run
            }
        }

        adapter?.submitList(filteredList)

        cpResetFilter?.run {
            this.visible()
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
                showEmptyLayout(list)
            }
        }, 1000)
    }

    private fun launchContact(ticket: Ticket) {
        ticket.contact?.run {
            if (this.isDigitsOnly()) {
                askPermission(Manifest.permission.CALL_PHONE) {
                    activity?.let { it1 -> makeCall(it1, this) }
                }.onDeclined {
                    showSnack(rootLayout, "Grant permission to place a call")
                }.runtimePermission.onForeverDenied {
                    startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                        )
                    )
                }.onAccepted {
                    activity?.let { it1 -> makeCall(it1, this) }
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
                    showSnack(rootLayout, "No email clients found, please install one")
                }
            }
        }
    }

    override fun onDestroy() {
        Log.i(javaClass.simpleName, "Removing listener")
        listener?.remove()
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Subscribe(sticky = true)
    fun onTicketAdded(userTicketEvent: AllUserTicketEvent) {

        Log.i(TAG, "Add tickets Event: ${userTicketEvent.tickets?.size}")

        appendTickets(userTicketEvent.tickets)

        val filteredList = distinctTicketsSorted()

        adapter?.submitList(filteredList)

        Handler().postDelayed({
            setLatestDate(filteredList)
            adapter?.notifyItemChanged(filteredList.size - 1)
        }, 1000)

        EventBus.getDefault().removeStickyEvent(userTicketEvent)
    }

    @Subscribe(sticky = true)
    fun onTicketDeleted(ticketEvent: AllTicketsDeleteEvent) {

        Log.i(TAG, "Delete ticket Event: ${ticketEvent.ticket.date}")

        removeTicket(ticketEvent.ticket)

        val filteredList = distinctTicketsSorted()

        adapter?.submitList(filteredList)

        EventBus.getDefault().removeStickyEvent(ticketEvent)
    }
}