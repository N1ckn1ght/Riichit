package com.example.riichit

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Random

class SoloActivity : AppCompatActivity() {
    internal lateinit var tiles: Array<Int>
    internal var kanStatus = 0
    internal var riichiStatus = 0
    internal var tsumoStatus = 0
    private lateinit var wall: Array<Int>
    private lateinit var rtsumo: ImageView
    private lateinit var rhand: RecyclerView
    private lateinit var rdiscard: RecyclerView
    private lateinit var rindicator: RecyclerView
    private lateinit var rcalls: RecyclerView
    private lateinit var handAdapter: HandAdapter
    private lateinit var discardAdapter: DiscardAdapter
    private lateinit var indicatorAdapter: DiscardAdapter
    private lateinit var callsAdapter: DiscardAdapter
    private var size: Int = 136
    private var tilesLeft: Int = 64
    private var tsumo: Int = 136
    private lateinit var hand: MutableList<Int>
    private lateinit var computableHand: Array<Int>
    private lateinit var discard: MutableList<Int>
    private var openDoras: Int = 1
    private lateinit var indicator: Array<Int>
    private lateinit var showableIndicator: MutableList<Int>
    private lateinit var kanButton: Button
    private lateinit var riichiButton: Button
    private lateinit var tsumoButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solo)
        supportActionBar?.hide()

        tiles = arrayOf(
            R.drawable.man_1, R.drawable.man_2, R.drawable.man_3, R.drawable.man_4, R.drawable.man_5, R.drawable.man_6, R.drawable.man_7, R.drawable.man_8, R.drawable.man_9,
            R.drawable.pin_1, R.drawable.pin_2, R.drawable.pin_3, R.drawable.pin_4, R.drawable.pin_5, R.drawable.pin_6, R.drawable.pin_7, R.drawable.pin_8, R.drawable.pin_9,
            R.drawable.sou_1, R.drawable.sou_2, R.drawable.sou_3, R.drawable.sou_4, R.drawable.sou_5, R.drawable.sou_6, R.drawable.sou_7, R.drawable.sou_8, R.drawable.sou_9,
            R.drawable.wind_east, R.drawable.wind_south, R.drawable.wind_north, R.drawable.wind_west, R.drawable.dragon_red, R.drawable.dragon_white, R.drawable.dragon_green,
            R.drawable.closed)

        rtsumo = findViewById(R.id.tsumo)
        rhand = findViewById(R.id.hand)
        rdiscard = findViewById(R.id.discard)
        rindicator = findViewById(R.id.dora_indicator)
        rcalls = findViewById(R.id.calls)
        kanButton = findViewById(R.id.call_kan)
        riichiButton = findViewById(R.id.call_riichi)
        tsumoButton = findViewById(R.id.call_tsumo)

        val displayMetrics = DisplayMetrics()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val display = this.display
            @Suppress("DEPRECATION")
            display?.getMetrics(displayMetrics)
        } else {
            @Suppress("DEPRECATION")
            val display = this.windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            display.getMetrics(displayMetrics)
        }
        val handTileWidth = (displayMetrics.widthPixels * 0.068).toInt()
        val handTileHeight = (displayMetrics.widthPixels * 0.272 / 3).toInt()
        val padding = (displayMetrics.widthPixels * 0.016).toInt()
        val discardTileHeight = (min((displayMetrics.heightPixels - handTileHeight - padding * 4), handTileHeight * 3) / 3.0).toInt()
        val discardTileWidth = (min((displayMetrics.heightPixels - handTileHeight - padding * 4), handTileHeight * 3) / 4.0).toInt()
        Log.d("d/screen", "display metrics: ${displayMetrics.widthPixels}x${displayMetrics.heightPixels}, hand tile sizes: ${handTileWidth}x${handTileHeight}, padding: ${padding}, discard tile sizes: ${discardTileWidth}x${discardTileHeight}")

        rtsumo.layoutParams.width = handTileWidth
        rtsumo.setMargin(0, 0, 0, padding)
        rhand.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        handAdapter = HandAdapter(LayoutInflater.from(this), this, handTileWidth, handTileHeight, padding)
        rdiscard.layoutManager = GridLayoutManager(this, 6, RecyclerView.VERTICAL, false)
        discardAdapter = DiscardAdapter(LayoutInflater.from(this), this, discardTileWidth, discardTileHeight, padding)
        rindicator.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        indicatorAdapter = DiscardAdapter(LayoutInflater.from(this), this, handTileWidth, handTileHeight, padding)
        rcalls.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        callsAdapter = DiscardAdapter(LayoutInflater.from(this), this, handTileWidth, handTileHeight, padding)

        kanButton.setMargin(0, padding, padding, 0)
        riichiButton.setMargin(0, 1, padding, 0)
        tsumoButton.setMargin(0, 1, padding, 0)

        wall = Array(size){ i -> i }
        discard = mutableListOf()
        indicator = Array(14){size}
        for (i in 0..13) {
            indicator[i] = randomTile()
        }
        showableIndicator = mutableListOf(indicator[5], 136, 136, 136, 136)
        hand = MutableList(13){size}
        for (i in 0..12) {
            hand[i] = randomTile()
        }
        hand.sort()
        handAdapter.submitList(hand)
        rhand.adapter = handAdapter
        indicatorAdapter.submitList(showableIndicator)
        rindicator.adapter = indicatorAdapter
        tsumo = randomTile()
        rtsumo.setImageResource(tiles[tsumo / 4])
    }

    internal fun discard(toRemove: Int) {
        if (kanStatus > 0 && tsumo / 4 == toRemove / 4) {
            rtsumo.performClick()
            return
        }
        if (riichiStatus > 0) {
            Toast.makeText(baseContext, baseContext.getString(R.string.illegal_discard), Toast.LENGTH_SHORT).show()
            return
        }
        if (tilesLeft > 0) {
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
            // TODO: kan declaration (when all 4 in the hand!)
            hand[current] = tsumo
            handAdapter.submitList(hand)
            rhand.adapter = handAdapter
            tsumo = toRemove
        }
        rtsumo.performClick()
    }

    fun onClickTsumo(view: View) {
        if (tilesLeft > 0) {
            tilesLeft--
            if (kanStatus > 0) {
                var handCopy = hand
                handCopy = handCopy.filterNot {
                    it / 4 == tsumo / 4
                } as MutableList<Int>
                // TODO: check for Riichi
                if (handCopy.size == 10) {
                    hand = handCopy
                    handAdapter.submitList(hand)
                    rhand.adapter = handAdapter
                    var temp = tsumo
                    tsumo = indicator[0]
                    indicator[0] = temp
                    showableIndicator[openDoras] = indicator[5 + openDoras * 2]
                    openDoras++
                    indicatorAdapter.submitList(showableIndicator)
                    rindicator.adapter = indicatorAdapter
                } else {
                    Toast.makeText(baseContext, baseContext.getString(R.string.illegal_kan), Toast.LENGTH_SHORT).show()
                }
            } else {
                // TODO: remove after debug
                if (size > 118) {
                    discard.add(tsumo)
                    discardAdapter.submitList(discard)
                    rdiscard.adapter = discardAdapter
                }
                tsumo = randomTile()
            }
            rtsumo.setImageResource(tiles[tsumo / 4])
            return
        }
        // TODO: finish the game
        Toast.makeText(baseContext, baseContext.getString(R.string.no_tiles_left), Toast.LENGTH_SHORT).show()
    }

    fun onCallKan(view: View) {
        if (kanStatus > 0) {
            kanStatus = 0
            kanButton.text = getString(R.string.button_cancel)
            riichiButton.enable()
            tsumoButton.enable()
        } else {
            if (tilesLeft > 0) {
                tilesLeft--
                kanStatus = 1
                kanButton.text = getString(R.string.button_kan)
                riichiButton.disable()
                tsumoButton.disable()
            } else {
                Toast.makeText(baseContext, baseContext.getString(R.string.no_tiles_left), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onCallRiichi(view: View) {
        if (riichiStatus > 1) {
            // Nothing?
        } else if (riichiStatus > 0) {
            // TODO: option to revert if no tiles went to discard
        } else {
            if (tilesLeft > 0) {
                // TODO: actual Riichi logic
                // TODO: reaction with other buttons
            } else {
                Toast.makeText(baseContext, baseContext.getString(R.string.no_tiles_left), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onCallTsumo(view: View) {
        // TODO: calc the hand!!
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

    private fun transform(hand: MutableList<Int>) : Array<Int> {
        // this function should transform hand from view variation to computable
        return arrayOf()
    }

    private fun View.setMargin(left: Int, top: Int, right: Int, bottom: Int) {
        val p = this.layoutParams as ConstraintLayout.LayoutParams
        p.setMargins(left, top, right, bottom)
        this.layoutParams = p
    }

    private fun Button.enable() {
        this.isEnabled = true
        this.isClickable = true
        this.setBackgroundColor(ContextCompat.getColor(baseContext, R.color.blue))
        this.setTextColor(ContextCompat.getColor(baseContext, R.color.white))
    }

    private fun Button.disable() {
        this.isEnabled = false
        this.isClickable = false
        this.setBackgroundColor(ContextCompat.getColor(baseContext, R.color.white))
        this.setTextColor(ContextCompat.getColor(baseContext, R.color.gray))
    }

    private fun min(a: Int, b: Int) : Int {
        if (a > b) return b
        return a
    }
}