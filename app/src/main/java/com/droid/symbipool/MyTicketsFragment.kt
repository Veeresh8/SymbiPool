package com.droid.symbipool

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.droid.symbipool.adapters.AllTicketsAdapter
import com.google.firebase.firestore.FirebaseFirestore
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class MyTicketsFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private var adapter: AllTicketsAdapter? = null
    private var tvEmpty: TextView? = null
    private var allUserTickets: List<Ticket> = ArrayList()

    companion object {
        fun newInstance(): MyTicketsFragment = MyTicketsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_my_tickets, container, false)
        initUI(view)
        return view
    }

    private fun initUI(view: View) {
        tvEmpty = view.findViewById(R.id.tvEmpty)
        recyclerView = view.findViewById(R.id.rvMyTickets)
        context?.let { recyclerView?.withLinearLayout(it) }
        adapter = AllTicketsAdapter({ ticket ->
            deleteTicket(ticket)
        }, true)
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
        ticket.ticketID?.run {
            FirebaseFirestore.getInstance().collection(DatabaseUtils.TICKET_COLLECTION)
                .document(this)
                .delete()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.i(javaClass.simpleName, "Removed ticket: ${ticket.ticketID}")
                    } else {
                        Log.e(javaClass.simpleName, "${it.exception?.message}")
                    }
                }
        }
    }

    @Subscribe(sticky = true)
    fun onTicketAdded(ticketEvent: TicketEvent) {
        ticketEvent.tickets?.run {
            if (this.isNotEmpty()) {
                tvEmpty?.gone()
                recyclerView?.visible()
                recyclerView?.scrollToPosition(0)
                allUserTickets = this.map { it }
                Log.i(javaClass.simpleName, "User lists: ${allUserTickets.size}")
                adapter?.submitList(allUserTickets)
            } else {
                tvEmpty?.visible()
                recyclerView?.gone()
            }
        }
        EventBus.getDefault().removeStickyEvent(ticketEvent)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }
}