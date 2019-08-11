package com.droid.symbipool

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.droid.symbipool.filterTicket.FilterActivity
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private var adapter: ViewpagerAdapter? = null

    private var allTicketsFragment = AllTicketsFragment.newInstance()
    private var myTicketsFragment = MyTicketsFragment.newInstance()

    companion object {
        var INTENT_CODE = 1337
        var TICKET_FILTER = "ticket_filter"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViewPager()
        initClickListeners()
    }

    private fun initClickListeners() {
        fabCreateTicket.setOnClickListener {
            startActivity(Intent(this@MainActivity, CreateTicketActivity::class.java))
        }
    }

    private fun initViewPager() {

        val tabLayout: TabLayout = findViewById(R.id.tabLayout)

        val viewPager: ViewPager = findViewById(R.id.viewPager)

        adapter = ViewpagerAdapter(supportFragmentManager)

        adapter?.allFragments?.add(allTicketsFragment)
        adapter?.allFragments?.add(myTicketsFragment)

        viewPager.offscreenPageLimit = 2

        viewPager.adapter = adapter

        tabLayout.setupWithViewPager(viewPager)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })
    }

    fun startFilterActivity() {
        val intent = Intent(this, FilterActivity::class.java)
        startActivityForResult(intent, INTENT_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == INTENT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val ticketFilter = data?.getSerializableExtra(TICKET_FILTER) as TicketFilter
                allTicketsFragment.filter(ticketFilter)
            }
        }
    }
}