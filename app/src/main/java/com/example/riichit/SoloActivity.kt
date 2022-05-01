package com.example.riichit

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class SoloActivity : AppCompatActivity() {
    internal lateinit var tiles: Array<Int>

    private var context = this

    private lateinit var rtsumo: ImageView
    private lateinit var rhand: RecyclerView
    private lateinit var rdiscard: RecyclerView
    private lateinit var rindicator: RecyclerView
    private lateinit var rcalls: RecyclerView
    private lateinit var handAdapter: HandAdapter
    private lateinit var discardAdapter: DiscardAdapter
    private lateinit var indicatorAdapter: DiscardAdapter
    private lateinit var callsAdapter: CallsAdapter

    private var kanStatus = 0
    private var rinshan = false
    private var riichiStatus = false
    private var riichiTile = -2
    private var gameOver = false

    private var size: Int = 0
    private var tilesWall: Int = 0
    private var tilesLeft: Int = 0
    private var tsumo: Int = 0
    private var openDoras: Int = 1
    private var mode: Int = 0
    private var ema: Int = 0

    private lateinit var all: Array<Int>
    private lateinit var wall: Array<Int>
    private lateinit var hand: MutableList<Int>
    private lateinit var discard: MutableList<Int>
    private lateinit var indicator: Array<Int>
    private lateinit var showableIndicator: MutableList<Int>
    private lateinit var calls: MutableList<Int>
    private lateinit var kanButton: Button
    private lateinit var riichiButton: Button
    private lateinit var tsumoButton: Button
    // TODO: hint button with the best efficiency move based on shanten and uke-ire calculations

    override fun onCreate(savedInstanceState: Bundle?) {
        mode = intent.getIntExtra("mode", 0)
        ema = intent.getIntExtra("ema", 0)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solo)
        supportActionBar?.hide()

        tiles = arrayOf(
            R.drawable.man_1, R.drawable.man_2, R.drawable.man_3, R.drawable.man_4, R.drawable.man_5, R.drawable.man_6, R.drawable.man_7, R.drawable.man_8, R.drawable.man_9,
            R.drawable.pin_1, R.drawable.pin_2, R.drawable.pin_3, R.drawable.pin_4, R.drawable.pin_5, R.drawable.pin_6, R.drawable.pin_7, R.drawable.pin_8, R.drawable.pin_9,
            R.drawable.sou_1, R.drawable.sou_2, R.drawable.sou_3, R.drawable.sou_4, R.drawable.sou_5, R.drawable.sou_6, R.drawable.sou_7, R.drawable.sou_8, R.drawable.sou_9,
            R.drawable.wind_east, R.drawable.wind_south, R.drawable.wind_west, R.drawable.wind_north, R.drawable.dragon_red, R.drawable.dragon_white, R.drawable.dragon_green,
            R.drawable.closed)

        when (mode) {
            0 -> {
                size = 136
                tilesWall = 18
            }
            1 -> {
                size = 36
                tilesWall = 5
            }
        }

        tilesLeft = tilesWall
        tsumo = 136
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
        val kanTileWidth = (handTileWidth * 3 - padding) / 4
        val kanTileHeight = kanTileWidth * 4 / 3
        Log.d("d/logScreen", "display metrics: ${displayMetrics.widthPixels}x${displayMetrics.heightPixels}, hand tile sizes: ${handTileWidth}x${handTileHeight}, padding: ${padding}, discard tile sizes: ${discardTileWidth}x${discardTileHeight}")

        rtsumo.layoutParams.width = handTileWidth
        rtsumo.setMargin(0, 0, 0, padding)
        rhand.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        handAdapter = HandAdapter(LayoutInflater.from(this), this, handTileWidth, handTileHeight, padding)
        rdiscard.layoutManager = GridLayoutManager(this, 6, RecyclerView.VERTICAL, false)
        discardAdapter = DiscardAdapter(LayoutInflater.from(this), this, discardTileWidth, discardTileHeight, padding)
        rindicator.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        indicatorAdapter = DiscardAdapter(LayoutInflater.from(this), this, handTileWidth, handTileHeight, padding)
        rcalls.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        callsAdapter = CallsAdapter(LayoutInflater.from(this), this, kanTileWidth, kanTileHeight, padding)

        kanButton.setMargin(0, padding, padding, 0)
        riichiButton.setMargin(0, 1, padding, 0)
        tsumoButton.setMargin(0, 1, padding, 0)

        all = Array(size){ i -> i }
        wall = Array(tilesLeft - 1){ 136 }
        discard = mutableListOf()
        indicator = Array(14){size}
        calls = mutableListOf()
        for (i in 0..13) {
            indicator[i] = randomTile()
        }
        showableIndicator = mutableListOf(indicator[5], 136, 136, 136, 136)
        hand = MutableList(13){size}
        for (i in 0..12) {
            hand[i] = randomTile()
        }
        for (i in 0..tilesLeft - 2) {
            wall[i] = randomTile()
        }
        hand.sort()
        handAdapter.submitList(hand)
        rhand.adapter = handAdapter
        indicatorAdapter.submitList(showableIndicator)
        rindicator.adapter = indicatorAdapter
        tsumo = randomTile()
        rtsumo.setImageResource(tiles[tsumo / 4])

        Log.d("d/logTilesWall", "INI wall: ${wall.toIntArray().contentToString()}")
        Log.d("d/logTilesDead", "INI dead: ${indicator.toIntArray().contentToString()}")
    }

    internal fun discard(toRemove: Int) {
        if (gameOver) {
            return
        }
        if (kanStatus > 0 && tsumo / 4 == toRemove / 4) {
            rtsumo.performClick()
            return
        }
        if (riichiTile > -1) {
            Toast.makeText(baseContext, baseContext.getString(R.string.illegal_discard), Toast.LENGTH_SHORT).show()
            return
        }
        if (riichiStatus) {
            setRiichi()
        }
        if (tilesLeft > openDoras - 1) {
            if (kanStatus > 0) {
                var count = 0
                for (it in hand) {
                    if (it / 4 == toRemove / 4) {
                        count++
                    }
                }
                if (count < 4) {
                    Toast.makeText(baseContext, baseContext.getString(R.string.illegal_kan), Toast.LENGTH_SHORT).show()
                    return
                }
            }
            var current = hand.indexOf(toRemove)
            if (tsumo > hand[current]) {
                do {
                    current++
                    if (current == hand.size) {
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
            tsumo = toRemove
            if (kanStatus > 0) {
                hand = hand.filterNot {
                    it / 4 == tsumo / 4
                } as MutableList<Int>
                getFromDead(toRemove / 4)
                return
            }
            handAdapter.submitList(hand)
            rhand.adapter = handAdapter
        }
        rtsumo.performClick()
    }

    fun onClickTsumo(view: View) {
        if (gameOver) {
            return
        }
        if (tilesLeft > openDoras - 1) {
            rinshan = false
            if (riichiStatus) {
                setRiichi()
            }
            if (kanStatus > 0) {
                var handCopy = hand.toMutableList()
                handCopy = handCopy.filterNot {
                    it / 4 == tsumo / 4
                } as MutableList<Int>
                if (handCopy.size == hand.size - 3) {
                    if (riichiTile > -1) {
                        // TODO: in future there must be a check on if it changes riichi waiting!
                    }
                    hand = handCopy.toMutableList()
                    getFromDead(tsumo / 4)
                } else {
                    Toast.makeText(baseContext, baseContext.getString(R.string.illegal_kan), Toast.LENGTH_SHORT).show()
                }
            } else {
                // TODO: Make riichi tile rotated on 90
                discard.add(tsumo)
                discardAdapter.submitList(discard)
                rdiscard.adapter = discardAdapter
                tilesLeft--

                if (tilesLeft > openDoras - 1) {
                    tsumo = wall[tilesLeft - 1]
                    rtsumo.setImageResource(tiles[tsumo / 4])
                } else {
                    onGameOver()
                    rtsumo.setImageResource(android.R.color.transparent)
                    onGameOver()
                }
            }
        } else {
            Toast.makeText(baseContext, baseContext.getString(R.string.no_tiles_left), Toast.LENGTH_SHORT).show()
        }
    }

    fun onCallKan(view: View) {
        if (kanStatus > 0) {
            kanStatus = 0
            kanButton.text = getString(R.string.button_kan)
            if (riichiTile == -1) {
                riichiButton.enable()
            }
            tsumoButton.enable()
        } else {
            if (tilesLeft > openDoras - 1) {
                kanStatus = 1
                kanButton.text = getString(R.string.button_cancel)
                if (riichiTile == -1) {
                    riichiButton.disable()
                }
                tsumoButton.disable()
            } else {
                Toast.makeText(baseContext, baseContext.getString(R.string.no_tiles_left), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onCallRiichi(view: View) {
        if (riichiStatus) {
            riichiStatus = false
            riichiButton.text = getString(R.string.button_riichi)
            kanButton.enable()
            tsumoButton.enable()
        } else {
            if (tilesLeft > openDoras - 1) {
                riichiStatus = true
                riichiButton.text = getString(R.string.button_cancel)
                kanButton.disable()
                tsumoButton.disable()
            } else {
                Toast.makeText(baseContext, baseContext.getString(R.string.no_tiles_left), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onCallTsumo(view: View) {
        gameOver = true
        if (tsumo < 136) {
            val doraIndicators: MutableList<Int> = mutableListOf()
            val kanTiles: MutableList<Int> = mutableListOf()
            val yakuConditional = mapOf("riichi" to (riichiTile > -1),
                    "double_riichi" to (riichiTile == 0),
                    "ippatsu" to (tilesLeft == openDoras + tilesWall - 2 - riichiTile),
                    "rinshan" to rinshan,
                    "haitei_raoyue" to (tilesLeft == openDoras),
                    "tenhou" to (tilesLeft == openDoras + tilesWall - 1))
            for (i in 0 until openDoras) {
                doraIndicators.add(indicator[5 + i * 2])
                if (riichiTile > -1) {
                    doraIndicators.add(indicator[4 + i * 2])
                }
            }
            for (i in 0 until (calls.size / 4)) {
                kanTiles.add(calls[i * 4 + 1] - 1)
            }

            val calc = Calc(hand, tsumo, doraIndicators, kanTiles, yakuConditional,
                    27, 27, yakuHanCost, yakumanHanCost, yakuCountedCost)
            calc.calc()

            // this output is only for debug purposes!
            val yakuList = calc.getYaku()
            val handCost = calc.getCost()
            var refinedYakuList: MutableList<String> = mutableListOf()

            var postfix = ""
            if (ema > 0) {
                postfix = "_ema"
            }

            if (yakuList["menzenchin_tsumohou"]!! > 0) {
                val yakumaned = handCost["yakumaned"]!! > 0
                for ((k, v) in yakuList) {
                    if (v > 0 && (!yakumaned || k in yakumanHanCost)) {
                        Log.d("d/logYakuCall", "${k} | ${k + postfix} | search in strings...")
                        var name = context.getString(context.resources.getIdentifier(
                                k + postfix, "string", context.packageName))
                        if (k in yakuCountedCost) {
                            refinedYakuList.add("$name $v")
                        } else {
                            refinedYakuList.add(name)
                        }
                    }
                }
            } else {
                refinedYakuList.add("Chombo")
                handCost["dealer"] = -12000
            }

            var output = ""
            for (i in refinedYakuList) {
                output += "${i}, "
            }
            output = output.dropLast(2)
            output += "\n\n"
            handCost.remove("yakumaned")
            for ((k, v) in handCost) {
                output += "${k}: ${v}, "
            }
            output = output.dropLast(2)

            Log.d("d/logCalcOutput", refinedYakuList.toString())
            Log.d("d/logCalcOutput", calc.getCost().toString())
            showYakuList(output)
            onGameOver()
            // TODO: make fragment for manual calculations; compare on game over screen
        } else {
            // TODO: scan for nagashi mangan
            finish()
        }
    }

    private fun onGameOver() {
        kanButton.disable()
        riichiButton.disable(highlight = (riichiTile > -1))
        tsumoButton.text = getString(R.string.game_over)
        tsumo = 136
    }

    private fun randomTile() : Int {
        if (size == 0)
        {
            return 136
        }
        val rnd: Int = Random().nextInt(size)
        size--
        if (size > rnd) {
            all[size] = all[rnd] + all[size]
            all[rnd] = all[size] - all[rnd]
            all[size] = all[size] - all[rnd]
        }
        return all[size]
    }

    private fun getFromDead(kanItem: Int) {
        tsumo = indicator[openDoras - 1]
        showableIndicator[openDoras] = indicator[5 + openDoras * 2]
        openDoras++
        indicatorAdapter.submitList(showableIndicator)
        rindicator.adapter = indicatorAdapter
        kanStatus = 0
        kanButton.text = getString(R.string.button_kan)
        riichiButton.enable()
        tsumoButton.enable()
        handAdapter.submitList(hand)
        rhand.adapter = handAdapter
        calls.add(136)
        calls.add(kanItem * 4 + 1)
        calls.add(kanItem * 4 + 2)
        calls.add(136)
        callsAdapter.submitList(calls)
        rcalls.adapter = callsAdapter
        rtsumo.setImageResource(tiles[tsumo / 4])
        rinshan = true
    }

    private fun setRiichi() {
        riichiStatus = false
        riichiTile = tilesWall - tilesLeft + openDoras - 1
        riichiButton.disable(highlight = true)
        riichiButton.text = getString(R.string.button_riichi)
        kanButton.enable()
        tsumoButton.enable()
    }

    private fun Button.enable() {
        this.isEnabled = true
        this.isClickable = true
        this.background.setTint(ContextCompat.getColor(baseContext, R.color.blue))
        this.setTextColor(ContextCompat.getColor(baseContext, R.color.white))
    }

    private fun Button.disable(highlight: Boolean = false) {
        this.isEnabled = false
        this.isClickable = false
        if (highlight) {
            this.background.setTint(ContextCompat.getColor(baseContext, R.color.orange))
        } else {
            this.background.setTint(ContextCompat.getColor(baseContext, R.color.light_gray))
        }
        this.setTextColor(ContextCompat.getColor(baseContext, R.color.white))
    }

    private fun showYakuList(output: String) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_debug_yaku_list)
        val tvYakuList = dialog.findViewById<TextView>(R.id.tvYakuList)
        val btnDismiss = dialog.findViewById<Button>(R.id.btnDismiss)
        tvYakuList.text = output
        btnDismiss.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}