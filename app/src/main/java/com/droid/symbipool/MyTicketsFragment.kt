package com.droid.symbipool

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class MyTicketsFragment : Fragment() {

    companion object {
        fun newInstance(): MyTicketsFragment = MyTicketsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_my_tickets, container, false)
        return view
    }
}