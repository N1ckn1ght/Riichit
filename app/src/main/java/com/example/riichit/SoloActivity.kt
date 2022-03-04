package com.example.riichit

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Random

class SoloActivity : AppCompatActivity() {
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
        makeHand()

        rtsumo.layoutParams.width = width
        rtsumo.layoutParams.height = height
        rtsumo.setPadding(0, 0,  padding / 2, 0)
        rtsumo.requestLayout()
        handAdapter = HandAdapter(LayoutInflater.from(this), this, width, height, padding)
        adapterUpdate(transform(hand))
    }

    private fun randomTile() : Int {
        if (size == 0)
        {
            return 34
        }
        var rnd: Int = Random().nextInt(size)
        size--
        wall[size] = wall[rnd] + wall[size]
        wall[rnd] = wall[size] - wall[rnd]
        wall[size] = wall[size] - wall[rnd]
        return wall[size]
    }

    private fun makeHand() {
        hand = Array(34){0}
        for (i in 1..13) {
            hand[randomTile()]++
        }
        tsumo = randomTile()
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
        tsumo = randomTile()
        adapterUpdate(transform(hand))
    }

    fun onClickTsumo(view: View) {
        tsumo = randomTile()
    }
}