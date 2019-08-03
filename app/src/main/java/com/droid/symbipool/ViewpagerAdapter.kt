package com.droid.symbipool

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewpagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    val allFragments: ArrayList<Fragment> = ArrayList()

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                allFragments[0]
            }
            1 -> {
                allFragments[1]
            }
            2 -> {
                allFragments[2]
            }
            else -> allFragments[0]
        }
    }

    override fun getPageTitle(position: Int): CharSequence = when (position) {
        0 -> "All Tickets"
        1 -> "My Tickets"
        2 -> "Requests"
        else -> ""
    }

    override fun getCount(): Int = 3
}