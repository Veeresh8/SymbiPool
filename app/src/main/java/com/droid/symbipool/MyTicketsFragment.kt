package com.droid.symbipool

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.droid.symbipool.adapters.AllTicketsAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_my_tickets.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class MyTicketsFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private var adapter: AllTicketsAdapter? = null
    private var tvEmpty: TextView? = null
    private var allUserTickets: ArrayList<Ticket> = ArrayList()
    private var TAG = javaClass.simpleName

    companion object {
        fun newInstance(): MyTicketsFragment = MyTicketsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_tickets, container, false)
        initUI(view)
        getUserTickets()
        return view
    }

    private fun initUI(view: View) {
        tvEmpty = view.findViewById(R.id.tvEmpty)
        recyclerView = view.findViewById(R.id.rvMyTickets)
        context?.let { recyclerView?.withLinearLayout(it) }
        adapter = AllTicketsAdapter({ ticket ->
            deleteTicket(ticket)
        }, true, object : AllTicketsAdapter.ClickInterface {
            override fun loadTicketDetails(ticket: Ticket) {
                val activity = activity as MainActivity
                activity.startTicketDetailsActivity(true, ticket)
            }

            override fun loadMoreTickets() {

            }

            override fun changeDate() {

            }

        })
        recyclerView?.adapter = adapter
    }

    private fun deleteTicket(ticket: Ticket) {
        activity?.let {
            MaterialDialog(it).show {
                title(R.string.delete)
                cancelable(true)
                message(R.string.delete_message)
                positiveButton(R.string.delete) {
                    performDeletion(ticket)
                }
            }
        }
    }

    private fun performDeletion(ticket: Ticket) {
        showSnack("Removing ticket")
        ticket.ticketID?.run {
            FirebaseFirestore.getInstance().collection(DatabaseUtils.TICKET_COLLECTION)
                .document(this)
                .delete()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.i(TAG, "Removed ticket successfully: $ticket")
                    } else {
                        showSnack("Failed to delete ticket")
                        Log.e(TAG, "${it.exception?.message}")
                    }
                }
        }
    }

    private fun getUserTickets() {
        FirebaseFirestore.getInstance().collection(DatabaseUtils.TICKET_COLLECTION)
            .whereEqualTo(DatabaseUtils.CREATOR, FirebaseAuth.getInstance().currentUser?.email)
            .orderBy(DatabaseUtils.TIME, Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, error ->
                querySnapshot?.run {
                    if (error != null) {
                        Log.e(TAG, "My tickets query error: ${error.message}")
                        return@addSnapshotListener
                    }

                    Log.i(TAG, "Document size: ${querySnapshot.documentChanges.size}")

                    val distinctList = allUserTickets.distinctBy { ticket ->
                        ticket.ticketID
                    } as ArrayList<Ticket>

                    allUserTickets = distinctList

                    for (document in querySnapshot.documentChanges) {
                        val ticket = document.document.toObject(Ticket::class.java)
                        when (document.type) {
                            DocumentChange.Type.ADDED -> {
                                Log.i(TAG, "New ticket: $ticket")
                                allUserTickets.add(ticket)
                            }

                            DocumentChange.Type.MODIFIED -> {
                                Log.i(TAG, "Modified ticket: $ticket")
                                allUserTickets.forEachIndexed { index, currentTicket ->
                                    if (ticket.ticketID == currentTicket.ticketID) {
                                        allUserTickets[index] = ticket
                                    }
                                }
                            }

                            DocumentChange.Type.REMOVED -> {
                                Log.i(TAG, "Removed ticket: $ticket")
                                EventBus.getDefault().postSticky(AllTicketsDeleteEvent(ticket))
                                allUserTickets.remove(ticket)
                            }
                        }
                    }

                    Log.i(TAG, "User tickets size: ${allUserTickets.size}")

                    val filteredList = allUserTickets.distinctBy { ticket ->
                        ticket.ticketID
                    } as ArrayList<Ticket>

                    filteredList.sortByDescending {
                        it.time
                    }

                    adapter?.submitList(filteredList.map { it })

                    showCount(allUserTickets)

                    val userList = allUserTickets.filter {
                        it.time!! >= TicketUtils.getCurrentTime()
                    } as ArrayList<Ticket>

                    EventBus.getDefault()
                        .postSticky(AllUserTicketEvent(userList))
                }
            }
    }

    private fun showCount(list: List<Ticket>) {
        Handler().postDelayed({
            rootLayout?.run {
                showEmptyLayout(list)
            }
        }, 1000)
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

    @Subscribe(sticky = true)
    fun onTicketDeleted(ticketEvent: TicketEvent) {

        Log.i(TAG, "Delete ticket Event: ${ticketEvent.ticket}")

        allUserTickets.remove(ticketEvent.ticket)

        val filteredList = allUserTickets.distinctBy { ticket ->
            ticket.ticketID
        } as ArrayList<Ticket>

        filteredList.sortByDescending {
            it.time
        }

        adapter?.submitList(filteredList)

        EventBus.getDefault().removeStickyEvent(ticketEvent)
    }

    @Subscribe(sticky = true)
    fun onTicketAdded(userTicketEvent: UserTicketEvent) {

        Log.i(TAG, "Add tickets Event: ${userTicketEvent.tickets?.size}")

        userTicketEvent.tickets?.let { allUserTickets.addAll(it) }

        val filteredList = allUserTickets.distinctBy { ticket ->
            ticket.ticketID
        } as ArrayList<Ticket>

        filteredList.sortByDescending {
            it.time
        }

        adapter?.submitList(filteredList)

        EventBus.getDefault().removeStickyEvent(userTicketEvent)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
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