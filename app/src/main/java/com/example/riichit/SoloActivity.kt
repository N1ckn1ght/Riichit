package com.example.riichit

import android.app.Dialog
import android.os.Bundle
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AlignmentSpan
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.riichit.AppDatabase.Companion.instance
import com.example.riichit.Drawables.tiles
import com.example.riichit.LocaleHelper.setLocale
import com.example.riichit.Operations.addGame
import com.example.riichit.Operations.getBalance
import com.example.riichit.Operations.getStreak
import com.example.riichit.Operations.updateProfile
import com.example.riichit.Ruleset.yakuCountedCost
import com.example.riichit.Ruleset.yakuHanCost
import com.example.riichit.Ruleset.yakumanHanCost
import com.example.riichit.Utility.isEqual
import com.example.riichit.Utility.logicIncrement
import com.example.riichit.Utility.setMargin
import com.example.riichit.Utility.toInt
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Integer.min
import java.util.*


class SoloActivity : AppCompatActivity() {
    private var context = this
    private lateinit var db: AppDatabase
    private lateinit var ivTsumo: ImageView
    private lateinit var rhand: RecyclerView
    private lateinit var rdiscard: RecyclerView
    private lateinit var rindicator: RecyclerView
    private lateinit var rcalls: RecyclerView
    private lateinit var handAdapter: HandAdapter
    private lateinit var discardAdapter: DiscardAdapter
    private lateinit var indicatorAdapter: DiscardAdapter
    private lateinit var callsAdapter: CallsAdapter
    private lateinit var all: Array<Int>
    private lateinit var wall: Array<Int>
    private lateinit var hand: MutableList<Int>
    private lateinit var discard: MutableList<Int>
    private lateinit var indicator: Array<Int>
    private lateinit var showableIndicator: MutableList<Int>
    private lateinit var buttonKan: Button
    private lateinit var buttonRiichi: Button
    private lateinit var buttonTsumo: Button
    private var discardTileHeight: Int = 0
    private var discardTileWidth: Int = 0
    private var toast: Toast? = null
    private var kanStatus = 0
    private var rinshan = false
    private var riichiStatus = false
    private var riichiTile = -2
    private var gameOver = false
    private var overType = 0
    private var size: Int = 0
    private var tilesWall: Int = 0
    private var tilesLeft: Int = 0
    private var tsumo: Int = 0
    private var openDoras: Int = 1
    private var calls: MutableList<Int> = mutableListOf()
    private var waitings: MutableList<Int> = mutableListOf()
    private var postfix: String = ""
    private var yakuConditional: Map<String, Boolean>? = null
    private var mode = 0
    private var balance = 0
    private var profile = 1
    private var streak = 0
    private var save = false
    // TODO: hint button with the best efficiency move based on shanten and uke-ire calculations

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        setLocale(context)
        mode = intent.getIntExtra("mode", 0)
        val ema = intent.getBooleanExtra("ema", false)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solo)
        supportActionBar?.hide()

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
        if (ema) {
            postfix = "_ema"
        }

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        save = sharedPreferences.getBoolean("score_saving", false)
        db = instance(this)

        GlobalScope.launch(Dispatchers.IO) {
            streak = getStreak(db, profile, mode)
            if (save) {
                updateProfile(db, profile, mode, -1000, -1)
            }
            balance = getBalance(db, profile, mode)
        }

        tilesLeft = tilesWall
        tsumo = 136
        ivTsumo = findViewById(R.id.tsumo)
        rhand = findViewById(R.id.hand)
        rdiscard = findViewById(R.id.discard)
        rindicator = findViewById(R.id.dora_indicator)
        rcalls = findViewById(R.id.calls)
        buttonKan = findViewById(R.id.btnKan)
        buttonRiichi = findViewById(R.id.btnRiichi)
        buttonTsumo = findViewById(R.id.btnTsumo)

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
        discardTileHeight = (min(
            (displayMetrics.heightPixels - handTileHeight - padding * 4),
            handTileHeight * 3
        ) / 3.0).toInt()
        discardTileWidth = (min(
            (displayMetrics.heightPixels - handTileHeight - padding * 4),
            handTileHeight * 3
        ) / 4.0).toInt()
        val kanTileWidth = (handTileWidth * 3 - padding) / 4
        val kanTileHeight = kanTileWidth * 4 / 3
        Log.d(
            "d/logScreen",
            "display metrics: ${displayMetrics.widthPixels}x${displayMetrics.heightPixels}, hand tile sizes: ${handTileWidth}x${handTileHeight}, padding: ${padding}, discard tile sizes: ${discardTileWidth}x${discardTileHeight}"
        )

        ivTsumo.layoutParams.width = handTileWidth
        ivTsumo.setMargin(0, 0, 0, padding)
        rhand.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        handAdapter =
            HandAdapter(
                LayoutInflater.from(this),
                this::discard,
                handTileWidth,
                handTileHeight,
                padding
            )
        rdiscard.layoutManager = GridLayoutManager(this, 6, RecyclerView.VERTICAL, false)
        discardAdapter = DiscardAdapter(
            LayoutInflater.from(this),
            discardTileWidth,
            discardTileHeight,
            padding
        )
        rindicator.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        indicatorAdapter =
            DiscardAdapter(LayoutInflater.from(this), handTileWidth, handTileHeight, padding)
        rcalls.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        callsAdapter =
            CallsAdapter(LayoutInflater.from(this), kanTileWidth, kanTileHeight, padding)

        buttonKan.setMargin(0, padding, padding, 0)
        buttonRiichi.setMargin(0, 1, padding, 0)
        buttonTsumo.setMargin(0, 1, padding, 0)

        all = Array(size) { i -> i }
        wall = Array(tilesLeft - 1) { 136 }
        discard = mutableListOf()
        indicator = Array(14) { size }
        for (i in 0..13) {
            indicator[i] = randomTile()
        }
        showableIndicator = mutableListOf(indicator[5], 136, 136, 136, 136)
        hand = MutableList(13) { size }
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
        ivTsumo.setImageResource(tiles[tsumo / 4])

        Log.d("d/logTilesWall", "INI wall: ${wall.toIntArray().contentToString()}")
        Log.d("d/logTilesDead", "INI dead: ${indicator.toIntArray().contentToString()}")

        buttonKan.setOnClickListener {
            if (kanStatus > 0) {
                kanStatus = 0
                buttonKan.text = getString(R.string.button_kan)
                if (riichiTile < 0) {
                    buttonRiichi.enable()
                }
                buttonTsumo.enable()
            } else {
                if (tilesLeft > openDoras) {
                    kanStatus = 1
                    buttonKan.text = getString(R.string.button_cancel)
                    if (riichiTile < 0) {
                        buttonRiichi.disable()
                    }
                    buttonTsumo.disable()
                } else {
                    showToast(baseContext.getString(R.string.no_tiles_left))
                }
            }
        }
        buttonRiichi.setOnClickListener {
            if (balance < 1000) {
                showToast(getString(R.string.no_riichi_bet))
                return@setOnClickListener
            }
            if (riichiStatus) {
                riichiStatus = false
                buttonRiichi.text = getString(R.string.button_riichi)
                buttonKan.enable()
                buttonTsumo.enable()
            } else {
                if (tilesLeft > openDoras - 1) {
                    riichiStatus = true
                    buttonRiichi.text = getString(R.string.button_cancel)
                    buttonKan.disable()
                    buttonTsumo.disable()
                } else {
                    showToast(baseContext.getString(R.string.no_tiles_left))
                }
            }
        }
        buttonTsumo.setOnClickListener {
            gameOver = true
            // This condition is a mess, rework asap.
            //  Else will be triggered onGameOver() and just when no more tiles left.
            if (tsumo < 136) {
                val kanTiles = getKanTiles()
                val doraIndicators: MutableList<Int> = mutableListOf()
                yakuConditional = mapOf(
                    "riichi" to (riichiTile > 0),
                    "double_riichi" to (riichiTile == 0),
                    "ippatsu" to (tilesLeft == tilesWall - 2 - riichiTile + openDoras),
                    "rinshan_kaihou" to rinshan,
                    "haitei_raoyue" to (tilesLeft == openDoras),
                    "tenhou" to (tilesLeft == openDoras + tilesWall - 1)
                )
                for (i in 0 until openDoras) {
                    doraIndicators.add(indicator[5 + i * 2])
                    if (riichiTile > -1) {
                        doraIndicators.add(indicator[4 + i * 2])
                    }
                }

                val calc = Calc(
                    hand, tsumo, doraIndicators, kanTiles, yakuConditional!!,
                    27, 27, yakuHanCost, yakumanHanCost, yakuCountedCost
                )
                calc.calc()
                // this will also update balance points for a profile, if set to true
                showYakuList(makeOutput(calc.getYaku(), calc.getCost(), true))
                // however the game itself ill be added to database in onGameOver() function
                // some other cases of updating balance will be put in when(overType) condition
                // with the exception of overType == 0 condition, it's updating itself on game start
                // and riichi bet will be updated in setRiichi() function
                // TODO: make fragment for manual calculations; compare on game over screen

                onGameOver()
            } else {
                buttonTsumo.text = getString(R.string.button_over)
                when (overType) {
                    0 -> {
                        finish()
                    }
                    1 -> {
                        showTempai(waitings)
                        overType = 0
                        if (save) {
                            GlobalScope.launch(Dispatchers.IO) {
                                updateProfile(db, profile, mode, 1000, streak)
                            }
                        }
                    }
                    2 -> {
                        showYakuList(
                            makeOutput(
                                mutableMapOf("chombo" to 1),
                                mutableMapOf("han" to 0, "fu" to 0, "dealer" to -12000),
                                true
                            )
                        )
                        overType = 0
                    }
                    3 -> {
                        showYakuList(
                            makeOutput(
                                mutableMapOf("nagashi_mangan" to 1),
                                mutableMapOf("han" to 0, "fu" to 0, "dealer" to 12000),
                                true
                            )
                        )
                        overType = 0
                    }
                    4 -> {
                        // TODO: if tsumo-furiten is active
                        overType = 0
                    }
                }
            }
        }
        ivTsumo.setOnClickListener {
            if (gameOver) {
                return@setOnClickListener
            }
            if (tilesLeft > openDoras - 1) {
                rinshan = false
                if (riichiStatus) {
                    setRiichi()
                }
                if (kanStatus > 0) {
                    if (countTilesInHand(tsumo) == 3) {
                        if (riichiTile < 0) {
                            buttonRiichi.enable()
                        }
                        getFromDead(tsumo / 4)
                    } else {
                        showToast(baseContext.getString(R.string.illegal_kan))
                    }
                } else {
                    tilesLeft--
                    // if riichi was just declared, logicIncrement value will be added to tsumo
                    discard.add(
                        tsumo + logicIncrement * (
                                tilesLeft == openDoras + tilesWall - 2 - riichiTile).toInt()
                    )
                    discardAdapter.submitList(discard)
                    rdiscard.adapter = discardAdapter

                    if (tilesLeft > openDoras - 1) {
                        tsumo = wall[tilesLeft - 1]
                        ivTsumo.setImageResource(tiles[tsumo / 4])
                    } else {
                        tsumo = 136
                        ivTsumo.setImageResource(android.R.color.transparent)
                        onGameOver()
                    }
                }
            } else {
                showToast(baseContext.getString(R.string.no_tiles_left))
            }
        }
    }

    override fun onPause() {
        super.onPause()
        toast?.cancel()
    }

    private fun discard(toRemove: Int) {
        if (gameOver) {
            return
        }
        if (kanStatus > 0 && tsumo / 4 == toRemove / 4) {
            ivTsumo.performClick()
            return
        }
        if (riichiTile > -1) {
            showToast(baseContext.getString(R.string.illegal_discard))
            return
        }
        if (tilesLeft > openDoras - 1) {
            if (kanStatus > 0) {
                if (countTilesInHand(toRemove) < 4) {
                    showToast(baseContext.getString(R.string.illegal_kan))
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
                getFromDead(toRemove / 4)
                return
            }
            handAdapter.submitList(hand)
            rhand.adapter = handAdapter
        }
        ivTsumo.performClick()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun onGameOver() {
        buttonKan.disable()
        buttonRiichi.disable(highlight = (riichiTile > -1))

        // the result of the game will be easily calculated from this data later if needed
        if (save) {
            GlobalScope.launch(Dispatchers.IO) {
                // TODO: implement good records interface to show this statistics
                // addGame(db, profile, hand, tsumo, getKanTiles(), discard, yakuConditional)
                return@launch
            }
        }

        if (!gameOver) {
            // check if player is tempai (1 tile left for a tsumo-win call)
            // if he had tempai, it's overType 1
            // if he had riichi and is not tempai, it's overType 2
            // waitings check might be already made in setRiichi() function
            if (riichiTile < 0) {
                waitings = findWaitings(hand, getKanTiles())
                if (waitings.size > 0) {
                    buttonTsumo.text = getString(R.string.button_tempai)
                    overType = 1
                }
            } else {
                if (waitings.size > 0) {
                    buttonTsumo.text = getString(R.string.button_tempai)
                    overType = 1
                } else {
                    buttonTsumo.text = getString(R.string.chombo)
                    overType = 2
                    return
                }
            }
            // if he had nothing, but discard is nagashi mangan, it's overType 3
            // otherwise it's overType 0
            val nagashiDiscard = arrayOf(0, 8, 9, 17, 18, 26, 27, 28, 29, 30, 31, 32, 33)
            var nagashiFound = true
            for (x in discard) {
                val tile = (x - (x > logicIncrement).toInt() * logicIncrement) / 4
                if (tile !in nagashiDiscard) {
                    nagashiFound = false
                    break
                }
            }
            if (nagashiFound) {
                buttonTsumo.text = getString(R.string.button_nagashi)
                overType = 3
            }
            if (overType > 0) {
                return
            }
        }
        buttonTsumo.text = getString(R.string.button_over)
        tsumo = 136
    }

    private fun randomTile(): Int {
        if (size == 0) {
            return 136
        }
        val rnd: Int = Random().nextInt(size)
        size--
        if (size > rnd) {
            all[size] = all[rnd].also { all[rnd] = all[size] }
        }
        return all[size]
    }

    private fun getFromDead(kanItem: Int) {
        val newHand = hand.filterNot {
            it / 4 == kanItem
        } as MutableList<Int>

        // abort if under riichi and new kan changes tempai
        if (riichiTile > -1) {
            val newKanTiles = getKanTiles()
            newKanTiles.add(tsumo)
            val newWaitings = findWaitings(newHand, newKanTiles)
            if (!isEqual(waitings, newWaitings)) {
                showToast(getString(R.string.illegal_kan_riichi))
                return
            }
        }

        hand = newHand
        tsumo = indicator[openDoras - 1]
        showableIndicator[openDoras] = indicator[5 + openDoras * 2]
        openDoras++
        indicatorAdapter.submitList(showableIndicator)
        rindicator.adapter = indicatorAdapter
        kanStatus = 0
        buttonKan.text = getString(R.string.button_kan)
        if (riichiTile < 0) {
            buttonRiichi.enable()
        }
        buttonTsumo.enable()
        handAdapter.submitList(hand)
        rhand.adapter = handAdapter
        calls.add(136)
        calls.add(kanItem * 4 + 1)
        calls.add(kanItem * 4 + 2)
        calls.add(136)
        callsAdapter.submitList(calls)
        rcalls.adapter = callsAdapter
        ivTsumo.setImageResource(tiles[tsumo / 4])
        rinshan = true
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun setRiichi() {
        if (save) {
            GlobalScope.launch(Dispatchers.IO) {
                updateProfile(db, profile, mode, -1000, 0)
            }
        }
        waitings = findWaitings(hand, getKanTiles())
        riichiStatus = false
        riichiTile = tilesWall - tilesLeft + openDoras - 1
        buttonRiichi.disable(highlight = true)
        buttonRiichi.text = getString(R.string.button_riichi)
        buttonKan.enable()
        buttonTsumo.enable()
    }

    private fun getKanTiles(): MutableList<Int> {
        val kanTiles: MutableList<Int> = mutableListOf()
        for (i in 0 until (calls.size / 4)) {
            kanTiles.add(calls[i * 4 + 1] - 1)
        }
        return kanTiles
    }

    private fun countTilesInHand(tile: Int): Int {
        var count = 0
        for (it in hand) {
            if (it / 4 == tile / 4) {
                count++
            }
        }
        return count
    }

    // temporary recurrent solution without changes in Calc class
    private fun findWaitings(hand: MutableList<Int>, kanTiles: MutableList<Int>): MutableList<Int> {
        val found: MutableList<Int> = mutableListOf()
        for (tile in all.indices step 4) {
            Log.d("d/waitingIterations", "$tile in 0 until ${all.size} step 4")
            val calc = Calc(
                hand,
                tile,
                mutableListOf(),
                kanTiles,
                mapOf(),
                27,
                27,
                yakuHanCost,
                yakumanHanCost,
                yakuCountedCost
            )
            calc.calc()
            val yakuList = calc.getYaku()
            if (!yakuList.containsKey("chombo")) {
                found.add(tile)
            }
        }
        return found
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

    @Suppress("SameParameterValue")
    @OptIn(DelicateCoroutinesApi::class)
    private fun makeOutput(
        yaku: MutableMap<String, Int>,
        cost: MutableMap<String, Int>,
        saveToProfile: Boolean = false
    ): String {
        var output = ""
        for ((k, v) in yaku) {
            output += context.getString(
                context.resources.getIdentifier(
                    k + postfix,
                    "string",
                    context.packageName
                )
            )
            if (v > 1) {
                output += " $v"
            }
            output += ", "
        }
        output = "${output.dropLast(2)}\n\n" +
                "${getString(R.string.han)}: ${cost["han"]}, " +
                "${getString(R.string.fu)}: ${cost["fu"]}\n" +
                "${getString(R.string.cost)}: ${cost["dealer"]}"

        if (saveToProfile) {
            var balanceChange = cost["dealer"]?: 0
            var streakChange = -1
            if (balanceChange > 0 && !yaku.containsKey("nagashi_mangan")) {
                streakChange = 1 + streak
                if (riichiTile > -1) {
                    balanceChange += 1000
                }
            }
            if (save) {
                GlobalScope.launch(Dispatchers.IO) {
                    updateProfile(db, profile, mode, balanceChange, streakChange)
                }
            }
        }

        return output
    }

    private fun showYakuList(output: String) {
        val dialog = baseDialog()
        dialog.setContentView(R.layout.dialog_debug_yaku_list)
        val tvYakuList = dialog.findViewById<TextView>(R.id.tvYakuList)
        val btnDismiss = dialog.findViewById<Button>(R.id.btnDismiss)
        tvYakuList.text = output
        btnDismiss.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showTempai(waitings: MutableList<Int>) {
        val dialog = baseDialog()
        dialog.setContentView(R.layout.dialog_debug_tempai)
        val btnDismiss = dialog.findViewById<Button>(R.id.btnDismiss)
        val rvWaitings = dialog.findViewById<RecyclerView>(R.id.rvWaitings)
        btnDismiss.setOnClickListener {
            dialog.dismiss()
        }
        rvWaitings.layoutManager =
            GridLayoutManager(dialog.context, 8, RecyclerView.VERTICAL, false)
        val waitingsAdapter = DiscardAdapter(
            LayoutInflater.from(dialog.context),
            discardTileWidth,
            discardTileHeight,
            0
        )
        waitingsAdapter.submitList(waitings)
        rvWaitings.adapter = waitingsAdapter
        dialog.show()
    }

    private fun baseDialog(): Dialog {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog.window?.attributes?.gravity = Gravity.TOP or Gravity.START
        dialog.window?.attributes?.x = 20
        dialog.window?.attributes?.y = 20
        return dialog
    }

    private fun showToast(text: String) {
        toast?.cancel()

        val centeredText: Spannable = SpannableString(text)
        centeredText.setSpan(
            AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
            0, text.length - 1,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )

        toast = Toast.makeText(
            baseContext,
            centeredText,
            Toast.LENGTH_SHORT
        )
        toast?.show()
    }
}