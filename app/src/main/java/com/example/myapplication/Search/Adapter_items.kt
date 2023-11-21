package com.example.myapplication.Search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.myapplication.R

class Adapter_items (context: Context, resource: Int, objects: List<items>) :
    ArrayAdapter<items>(context, resource, objects) {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        if (v == null) {
            val inflater = LayoutInflater.from(context)
            v = inflater.inflate(R.layout.items, null)
        }

        val items = getItem(position)
        if (items != null) {
            val txtMaNV = v?.findViewById<TextView>(R.id.item_id)
            txtMaNV?.text = "Mã nhân viên : ${items.idNv11}"

            val txtTen = v?.findViewById<TextView>(R.id.item_name)
            txtTen?.text = "Tên nhân viên : ${items.name11}"

            val txtDd = v?.findViewById<TextView>(R.id.item_onoff)
            txtDd?.text = items.onoff
        }
        return v ?: View(context)
    }

}