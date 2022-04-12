package com.example.riichit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onClickSolo(view: View) {
        val intent = Intent(this, SoloActivity::class.java)
        startActivity(intent)
    }

    // TODO: mode with only one type of tiles
    // TODO: RU translation
}