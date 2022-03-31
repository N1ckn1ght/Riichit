package com.example.riichit

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Random

class SoloActivity : AppCompatActivity() {
    lateinit var tiles: Array<Int>

    private lateinit var wall: Array<Int>
    private lateinit var rtsumo: ImageView
    private lateinit var rhand: RecyclerView
    private lateinit var rdiscard: RecyclerView

    private lateinit var handAdapter: HandAdapter
    private lateinit var discardAdapter: DiscardAdapter

    private var size: Int = 136
    private var tsumo: Int = 136
    private lateinit var hand: MutableList<Int>
    private lateinit var discard: MutableList<Int>

    // this is for future yaku logic and stuff
    private lateinit var computableHand: Array<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solo)
        supportActionBar?.hide()

        tiles = arrayOf(
            R.drawable.man_1, R.drawable.man_2, R.drawable.man_3, R.drawable.man_4, R.drawable.man_5, R.drawable.man_6, R.drawable.man_7, R.drawable.man_8, R.drawable.man_9,
            R.drawable.pin_1, R.drawable.pin_2, R.drawable.pin_3, R.drawable.pin_4, R.drawable.pin_5, R.drawable.pin_6, R.drawable.pin_7, R.drawable.pin_8, R.drawable.pin_9,
            R.drawable.sou_1, R.drawable.sou_2, R.drawable.sou_3, R.drawable.sou_4, R.drawable.sou_5, R.drawable.sou_6, R.drawable.sou_7, R.drawable.sou_8, R.drawable.sou_9,
            R.drawable.wind_east, R.drawable.wind_south, R.drawable.wind_north, R.drawable.wind_west, R.drawable.dragon_red, R.drawable.dragon_white, R.drawable.dragon_green,
            R.drawable.debug)

        rtsumo = findViewById(R.id.tsumo)
        rhand = findViewById(R.id.hand)
        rdiscard = findViewById(R.id.discard)

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
        val handTileWidth = (displayMetrics.widthPixels * 0.068).toInt()
        val handTileHeight = (displayMetrics.widthPixels * 0.272 / 3).toInt()
        val padding = (displayMetrics.widthPixels * 0.016).toInt()

        // TODO: get real sizes
        val discardTileHeight = (min((displayMetrics.heightPixels - handTileHeight * 2 - padding * 3), handTileHeight * 3) / 3.0).toInt()
        val discardTileWidth = (min((displayMetrics.heightPixels - handTileHeight * 2 - padding * 3), handTileHeight * 3) / 4.0).toInt()
        Log.d("d/screen", "display metrics: ${displayMetrics.widthPixels}x${displayMetrics.heightPixels}, hand tile sizes: ${handTileWidth}x${handTileHeight}, padding: ${padding}, discard tile sizes: ${discardTileWidth}x${discardTileHeight}")

        rtsumo.layoutParams.width = handTileWidth
        rtsumo.setMargin(0, 0, 0, padding)
        rhand.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        handAdapter = HandAdapter(LayoutInflater.from(this), this, handTileWidth, handTileHeight, padding)
        rdiscard.layoutManager = GridLayoutManager(this, 6, RecyclerView.VERTICAL, false)
        discardAdapter = DiscardAdapter(LayoutInflater.from(this), this, discardTileWidth, discardTileHeight, padding)

        wall = Array(size){ i -> i }
        makeHand()
        discard = mutableListOf()
        handAdapter.submitList(hand)
        rhand.adapter = handAdapter
        tsumo = randomTile()
        rtsumo.setImageResource(tiles[tsumo / 4])
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
        handAdapter.submitList(hand)
        rhand.adapter = handAdapter
        tsumo = toRemove
        rtsumo.performClick()
    }

    fun onClickTsumo(view: View) {
        discard.add(tsumo)
        discardAdapter.submitList(discard)
        rdiscard.adapter = discardAdapter
        tsumo = randomTile()
        rtsumo.setImageResource(tiles[tsumo / 4])
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

    private fun View.setMargin(left: Int, top: Int, right: Int, bottom: Int) {
        val p = this.layoutParams as ConstraintLayout.LayoutParams
        p.setMargins(left, top, right, bottom)
        this.layoutParams = p
    }

    private fun min(a: Int, b: Int) : Int {
        if (a > b) return b
        return a
    }
}