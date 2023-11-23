package com.example.myapplication.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.Fragments.fragment_attendance
import com.example.myapplication.Fragments.fragment_create_qr
import com.example.myapplication.Fragments.fragment_scan_qr
import com.example.myapplication.Fragments.fragment_searcg
import com.example.myapplication.Fragments.tabbar_adapter
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityHomeBinding
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class Home : AppCompatActivity() {
    private lateinit var navigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        anhxa()
        setUpViewPager(fragment_create_qr())
        navigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.homee -> setUpViewPager(fragment_create_qr())
                R.id.gearr -> setUpViewPager(fragment_scan_qr())
                R.id.setting -> setUpViewPager(fragment_attendance())
                R.id.search -> setUpViewPager(fragment_searcg())

                else -> {
                }
            }

            true

        }

    }

    private fun setUpViewPager(fragment: Fragment) {
        val fragmentManager = supportFragmentManager;
        val fragemtTransaction = fragmentManager.beginTransaction()
        fragemtTransaction.replace(R.id.framee_layout, fragment)
        fragemtTransaction.commit()
    }

    private fun anhxa() {
        navigationView = findViewById(R.id.bottombar)
    }
}