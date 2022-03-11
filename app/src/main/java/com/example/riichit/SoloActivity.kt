package com.example.riichit

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Random

class SoloActivity : AppCompatActivity() {
    lateinit var tiles: MutableList<Int>

    lateinit var hand: Array<Int>
    var tsumo: Int = 34

    lateinit var wall: Array<Int>
    var size: Int = 136

    lateinit var rhand: RecyclerView
    lateinit var handAdapter: HandAdapter
    lateinit var rtsumo: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solo)

        tiles = mutableListOf(
            R.drawable.man_1, R.drawable.man_2, R.drawable.man_3, R.drawable.man_4, R.drawable.man_5, R.drawable.man_6, R.drawable.man_7, R.drawable.man_8, R.drawable.man_9,
            R.drawable.pin_1, R.drawable.pin_2, R.drawable.pin_3, R.drawable.pin_4, R.drawable.pin_5, R.drawable.pin_6, R.drawable.pin_7, R.drawable.pin_8, R.drawable.pin_9,
            R.drawable.sou_1, R.drawable.sou_2, R.drawable.sou_3, R.drawable.sou_4, R.drawable.sou_5, R.drawable.sou_6, R.drawable.sou_7, R.drawable.sou_8, R.drawable.sou_9,
            R.drawable.wind_east, R.drawable.wind_south, R.drawable.wind_north, R.drawable.wind_west, R.drawable.dragon_red, R.drawable.dragon_white, R.drawable.dragon_green,
            R.drawable.debug)

        rtsumo = findViewById<ImageView>(R.id.tsumo)
        rhand = findViewById<RecyclerView>(R.id.hand)

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
        val width = (displayMetrics.widthPixels * 0.065).toInt()
        val height = (displayMetrics.widthPixels * 0.26 / 3).toInt()
        val padding = (displayMetrics.widthPixels * 0.03).toInt()

        wall = Array(size){ i -> i / 4 }
        Log.d("log-wall", "size: ${size}, wall: ${wall.joinToString(" ")}")
        makeHand()
        rtsumo.performClick()

        rtsumo.layoutParams.width = width
        rtsumo.layoutParams.height = height + padding
        rtsumo.setPadding(0, 0, 0, padding)
        //rtsumo.layoutParams = ConstraintLayout.LayoutParams(width, height)
        handAdapter = HandAdapter(LayoutInflater.from(this), this, width, height, padding, tiles)
        adapterUpdate(transform(hand))
    }

    private fun randomTile() : Int {
        if (size == 0)
        {
            return 34
        }
        var rnd: Int = Random().nextInt(size)
        size--
        if (size > rnd) {
            wall[size] = wall[rnd] + wall[size]
            wall[rnd] = wall[size] - wall[rnd]
            wall[size] = wall[size] - wall[rnd]
        }
        return wall[size]
    }

    private fun makeHand() {
        hand = Array(35){0}
        for (i in 1..13) {
            hand[randomTile()]++
            Log.d("log-wall", "size: ${size}, wall: ${wall.joinToString(" ")}")
        }
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
        rhand.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rhand.adapter = handAdapter
    }

    internal fun discard(toRemove: Int) {
        hand[toRemove]--
        hand[tsumo]++
        rtsumo.performClick()
        adapterUpdate(transform(hand))
    }

    fun onClickTsumo(view: View) {
        tsumo = randomTile()
        rtsumo.setImageResource(tiles[tsumo])
        Log.d("log-wall", "size: ${size}, wall: ${wall.joinToString(" ")}")
    }
}