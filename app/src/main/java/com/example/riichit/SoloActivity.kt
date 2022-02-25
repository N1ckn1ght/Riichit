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

        val hand = makeHand()
        var showHand = transform(hand)

        val rhand = findViewById<RecyclerView>(R.id.hand)
        Log.d("stage", "rhand was found.")
        val handAdapter = HandAdapter(LayoutInflater.from(this))
        Log.d("stage", "handAdapter was created.")
        handAdapter.submitList(showHand)
        Log.d("stage", "list of tiles was submitted.")
        rhand.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        Log.d("stage", "linear layout manager has been accepted.")
        rhand.adapter = handAdapter
        Log.d("stage", "adapter has been passed successfully!")
    }

    private fun makeHand() : Array<Int> {
        val hand = Array<Int>(34){0}
        hand[0] = 3
        hand[1] = 1
        hand[2] = 1
        hand[3] = 1
        hand[4] = 1
        hand[5] = 1
        hand[6] = 1
        hand[7] = 1
        hand[8] = 3
        return hand
    }

    private fun transform(hand: Array<Int>) : MutableList<Int> {
        var showHand = mutableListOf<Int>()
        for (i in hand.indices) {
            for (j in 1..hand[i]) {
                showHand.add(i)
            }
        }
        return showHand
    }
}