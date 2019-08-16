package com.droid.symbipool

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.afollestad.materialdialogs.MaterialDialog
import com.droid.symbipool.filterTicket.FilterActivity
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.libraries.places.api.Places
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
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
        date.text = DatabaseUtils.getCurrentDate()
    }

    private fun initPermissions() {
        askPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION) {
        }.onDeclined {
            askPermissionsAgain()
        }.runtimePermission.onForeverDenied {
            startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                )
            )
        }.onAccepted {
            launchCreateTicket()
        }
    }

    private fun askPermissionsAgain() {
        val snackBar = Snackbar.make(main_content, "Location permission needed to continue", Snackbar.LENGTH_LONG)
        snackBar.setAction("Grant") {
            initPermissions()
        }
        snackBar.setActionTextColor(resources.getColor(R.color.colorAccent)).show()
    }

    private fun hasGrantedPermissions(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fineLocation == PackageManager.PERMISSION_GRANTED && coarseLocation == PackageManager.PERMISSION_GRANTED
    }

    private fun initClickListeners() {
        fabCreateTicket.setOnClickListener {
            if (!hasGrantedPermissions()) {
                initPermissions()
            } else {
                launchCreateTicket()
            }
        }

        logout.setOnClickListener {
            MaterialDialog(this).show {
                title(R.string.logout)
                cancelable(true)
                message(R.string.logout_message)
                positiveButton(R.string.logout) {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this@MainActivity, AuthenticationActivity::class.java))
                    finish()
                }
            }
        }

        date.setOnClickListener {
            allTicketsFragment.onDatePicked()
        }
    }

    private fun launchCreateTicket() {
        if (!isLocationEnabled(this)) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
            return
        }
        startActivity(Intent(this@MainActivity, CreateTicketActivity::class.java))
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