package com.example.riichit

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class SoloActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solo)

        val tiles = mutableListOf(R.drawable.dragon_red, R.drawable.dragon_white, R.drawable.dragon_green)
        val hand = mutableListOf(1, 1, 1)

        
    }


}