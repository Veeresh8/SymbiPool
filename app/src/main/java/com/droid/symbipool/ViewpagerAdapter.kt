package com.droid.symbipool

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewpagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment = when (position) {
        0 -> AllTicketsFragment.newInstance()
        1 -> MyTicketsFragment.newInstance()
        2 -> RequestsFragment.newInstance()
        else -> AllTicketsFragment.newInstance()
    }

    override fun getPageTitle(position: Int): CharSequence = when (position) {
        0 -> "All Tickets"
        1 -> "My Tickets"
        2 -> "Requests"
        else -> ""
    }

    override fun getCount(): Int = 3
}