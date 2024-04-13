package com.smartgeek.imageprocessingtest.views

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.smartgeek.imageprocessingtest.R
import com.smartgeek.imageprocessingtest.model.PupilDistanceModel

class MainActivity2 : AppCompatActivity() {

    private var mDistanceList: ArrayList<PupilDistanceModel> = arrayListOf()
    private lateinit var tvDisplay: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        tvDisplay = findViewById(R.id.tv_display)
        Log.v("my lists", "${mDistanceList.size}")
//        mDistanceList.clear()

        val receivedList = intent.getParcelableArrayListExtra<PupilDistanceModel>("distance")
        if (receivedList != null) {
            mDistanceList = receivedList
        }
        Log.v("my data", receivedList?.size.toString())

        var sum = 0.0f
        val length = mDistanceList.size

        for (i in mDistanceList){
            sum += i.distance
        }

        val distance = (sum/length)
        tvDisplay.text = getString(R.string.your_pupil_distance_is)+" ${String.format("%.2f", distance)}"
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this@MainActivity2, AnimationActivity::class.java))
    }
}