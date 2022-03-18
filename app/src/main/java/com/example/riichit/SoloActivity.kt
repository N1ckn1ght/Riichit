package com.example.riichit

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Random

class SoloActivity : AppCompatActivity() {
    private lateinit var tiles: Array<Int>
    private lateinit var wall: Array<Int>

    private lateinit var rhand: RecyclerView
    private lateinit var rtsumo: ImageView
    private lateinit var handAdapter: HandAdapter

    private lateinit var hand: MutableList<Int>
    private var size: Int = 136
    private var tsumo: Int = 34

    // this is for future yaku logic and stuff
    private lateinit var computableHand: Array<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solo)

        tiles = arrayOf(
            R.drawable.man_1, R.drawable.man_2, R.drawable.man_3, R.drawable.man_4, R.drawable.man_5, R.drawable.man_6, R.drawable.man_7, R.drawable.man_8, R.drawable.man_9,
            R.drawable.pin_1, R.drawable.pin_2, R.drawable.pin_3, R.drawable.pin_4, R.drawable.pin_5, R.drawable.pin_6, R.drawable.pin_7, R.drawable.pin_8, R.drawable.pin_9,
            R.drawable.sou_1, R.drawable.sou_2, R.drawable.sou_3, R.drawable.sou_4, R.drawable.sou_5, R.drawable.sou_6, R.drawable.sou_7, R.drawable.sou_8, R.drawable.sou_9,
            R.drawable.wind_east, R.drawable.wind_south, R.drawable.wind_north, R.drawable.wind_west, R.drawable.dragon_red, R.drawable.dragon_white, R.drawable.dragon_green,
            R.drawable.debug)

        rtsumo = findViewById(R.id.tsumo)
        rhand = findViewById(R.id.hand)

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
        rtsumo.layoutParams.width = width
        rtsumo.layoutParams.height = height + padding
        rtsumo.setPadding(0, 0, 0, padding)

        wall = Array(size){ i -> i }
        makeHand()
        handAdapter = HandAdapter(LayoutInflater.from(this), this, width, height, padding, tiles)
        adapterUpdate(hand)
        rtsumo.performClick()
    }

    private fun randomTile() : Int {
        if (size == 0)
        {
            return 136
        }
        val rnd: Int = Random().nextInt(size)
        size--
        if (size > rnd) {
            wall[size] = wall[rnd] + wall[size]
            wall[rnd] = wall[size] - wall[rnd]
            wall[size] = wall[size] - wall[rnd]
        }
        return wall[size]
    }

    private fun makeHand() {
        hand = MutableList(13){136}
        for (i in 0..12) {
            hand[i] = randomTile()
        }
        hand.sort()
    }

    private fun transform(hand: MutableList<Int>) : Array<Int> {
        // this function should transform hand from view variation to computable
        return arrayOf()
    }

    private fun adapterUpdate(showHand: MutableList<Int>) {
        handAdapter.submitList(showHand)
        rhand.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rhand.adapter = handAdapter
    }

    internal fun discard(toRemove: Int) {
        var current = hand.indexOf(toRemove)
        if (tsumo > hand[current]) {
            do {
                current++
                if (current > 12) {
                    break
                }
                hand[current - 1] = hand[current]
            } while (tsumo > hand[current])
            current--
        } else {
            do {
                current--
                if (current < 0) {
                    break
                }
                hand[current + 1] = hand[current]
            } while (tsumo < hand[current])
            current++
        }
        hand[current] = tsumo
        adapterUpdate(hand)
        rtsumo.performClick()
    }

    fun onClickTsumo(view: View) {
        tsumo = randomTile()
        rtsumo.setImageResource(tiles[tsumo / 4])
    }
}