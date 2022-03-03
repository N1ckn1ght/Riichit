package com.example.riichit

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class SoloActivity : AppCompatActivity() {
    lateinit var hand: Array<Int>
    lateinit var rhand: RecyclerView
    lateinit var handAdapter: HandAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solo)

        val displayMetrics = DisplayMetrics()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val display = this.display
            display?.getRealMetrics(displayMetrics)
        } else {
            @Suppress("DEPRECATION")
            val display = this.windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            display.getMetrics(displayMetrics)
        }

        // TODO: implement tsumo tile
        hand = makeHand()

        rhand = findViewById<RecyclerView>(R.id.hand)
        Log.d("stage", "rhand was found.")
        handAdapter = HandAdapter(LayoutInflater.from(this), this, displayMetrics.widthPixels)
        Log.d("stage", "handAdapter was created.")

        adapterUpdate(transform(hand))
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

    private fun adapterUpdate(showHand: MutableList<Int>) {
        handAdapter.submitList(showHand)
        Log.d("stage", "list of tiles was submitted.")
        rhand.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        Log.d("stage", "linear layout manager has been accepted.")
        rhand.adapter = handAdapter
        Log.d("stage", "adapter has been passed successfully!")
    }

    internal fun discard(toRemove: Int) {
        // TODO: implement actual mahjong wall's logic
        hand[toRemove]--
        hand[31]++
        adapterUpdate(transform(hand))
    }
}