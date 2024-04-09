package com.smartgeek.imageprocessingtest.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.smartgeek.imageprocessingtest.R
import com.smartgeek.imageprocessingtest.model.PupilDistanceModel

class MainActivity2 : AppCompatActivity() {

    private var mDistanceList: ArrayList<PupilDistanceModel> = arrayListOf()
    private lateinit var tvDisplay: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        tvDisplay = findViewById(R.id.tv_display)

        val receivedList = intent.getParcelableArrayListExtra<PupilDistanceModel>("distance")
        if (receivedList != null) {
            mDistanceList = receivedList
        }
        Log.v("data", receivedList.toString())

        var sum = 0.0f
        val length = mDistanceList.size

        for (i in mDistanceList){
            sum += i.distance
        }

        tvDisplay.text = (sum/length).toString()
    }
}