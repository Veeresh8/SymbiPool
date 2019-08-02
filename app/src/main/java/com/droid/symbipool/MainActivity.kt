package com.droid.symbipool

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

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

        toolbarTitle.text = getString(R.string.app_name)

        val tabLayout: TabLayout = findViewById(R.id.tabLayout)

        val viewPager: ViewPager = findViewById(R.id.viewPager)

        val adapter = ViewpagerAdapter(supportFragmentManager)

        viewPager.offscreenPageLimit = 3

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
}