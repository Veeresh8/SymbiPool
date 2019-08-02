package com.droid.symbipool

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class RequestsFragment : Fragment() {

    companion object {
        fun newInstance(): RequestsFragment = RequestsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_requests, container, false)
        return view
    }
}