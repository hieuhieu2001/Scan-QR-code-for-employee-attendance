package com.example.myapplication.Fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter

class tabbar_adapter(fr:FragmentActivity) : FragmentStateAdapter(fr) {

    override fun getItemCount(): Int = 4
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> fragment_create_qr()
            2 -> fragment_scan_qr()
            3 -> fragment_attendance()
            else -> fragment_searcg()
        }
    }
}