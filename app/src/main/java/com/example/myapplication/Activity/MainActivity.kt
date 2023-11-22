package com.example.myapplication.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import com.example.myapplication.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var text1: TextView
    private lateinit var text2: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        getTime()
        getDelay()
    }

    private fun getTime() {
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val currentDay =
            SimpleDateFormat(" dd ' - ' MM ' - ' yyyy", Locale.getDefault())
                .format(Date())
        text1.setText(currentTime)
        text2.setText(currentDay)


    }

    private fun init() {
        text1 = findViewById(R.id.time_now)
        text2 = findViewById(R.id.date_now)
    }

    private fun getDelay() {
        Handler(Looper.getMainLooper()).postDelayed({        // su dung handler de doi mot khoang thoi gian va sau do chuyen sang man hinh khac
            val intent =
                Intent(this, Home::class.java)            // tao intent de chuyen sang man hinh khac
            startActivity(intent)
            finish() // Đóng màn hình hiện tại
        }, 2000)
    }
}