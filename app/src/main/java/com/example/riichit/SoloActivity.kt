package com.example.riichit

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SoloActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solo)

        val tiles = mutableListOf(R.drawable.dragon_red, R.drawable.dragon_white, R.drawable.dragon_green)
        // val hand = mutableListOf(1, 1, 1)

        val rhand = findViewById<RecyclerView>(R.id.hand)
        Log.d("stage", "rhand was found.")
        val handAdapter = HandAdapter(LayoutInflater.from(this))
        Log.d("stage", "handAdapter was created.")
        handAdapter.submitList(tiles)
        Log.d("stage", "list of tiles was submitted.")
        rhand.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        Log.d("stage", "linear layout manager has been accepted.")
        rhand.adapter = handAdapter
        Log.d("stage", "adapter has been passed successfully!")
    }
}