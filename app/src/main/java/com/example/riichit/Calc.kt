package com.example.riichit

import android.util.Log

// TODO: Yaku should have their own classes as well, this will help future optional rules
//  implementation and hand calculation in case of win by Ron instead of Tsumo
class Calc (private val showableHand: MutableList<Int>,
            private val showableTsumo: Int,
            private val doraIndicators: MutableList<Int>,
            private val kanTiles: MutableList<Int>,
            private val yakuConditional: Map<String, Boolean>,
            private val playerWind: Int,
            private val roundWind: Int,
            private val yakuHanCost: Map<String, Int>,
            private val yakumanHanCost: Map<String, Int>,
            private val yakuContedCost: Map<String, Int>) {
    class Meld (val type: Int, val tile: Int)
    // 0 - chii, 1 - pon, 2 - kan

    private var yaku = (yakuHanCost + yakumanHanCost + yakuCountedCost).toMutableMap()
    private var cost = mutableMapOf("han" to 0, "fu" to 0, "dealer" to 0, "yakumaned" to 0)
    // TODO: Han and fu results should be detailed for education purposes
    private val hand = Array(34){0}
    private var tsumo = -1

    fun getYaku(): MutableMap<String, Int> {
        return yaku
    }

    fun getCost(): MutableMap<String, Int> {
        return cost
    }

    fun calc() {
        // remove han cost from yaku
        for ((k, _) in yaku) {
            yaku[k] = 0
        }

        // simple check in case of invalid hand size
        if (showableHand.size != 13 - kanTiles.size * 3) {
            return
        }

        // transform hand to a readable form
        tsumo = showableTsumo / 4
        for (i in showableHand) {
            hand[i / 4]++
        }
        hand[tsumo]++
        val handWithNoCalls = hand.clone()
        for (i in kanTiles) {
            hand[i / 4] = 4
        }

        // search for special yaku
        applyYakuConditional(yaku, yakuConditional)
        applyYakuHanded(yaku, hand, tsumo, doraIndicators)
        if (yaku["chiitoitsu"]!! > 0 ||
            yaku["kokushi_musou"]!! > 0 ||
            yaku["thirteen_wait_kokushi_musou"]!! > 0) {
            yaku["menzenchin_tsumohou"] = 1
            cost = calculateHandCost(yaku, 25)
        }

        // find all possible pairs
        val pairIndices: MutableList<Int> = mutableListOf()
        for (i in handWithNoCalls.indices) {
            if (handWithNoCalls[i] > 1) {
                pairIndices.add(i)
            }
        }

        Log.d("d/logCalcMain", "hand: ${hand.contentToString()}")
        Log.d("d/logCalcMain", "pairIndices: ${pairIndices.toIntArray().contentToString()}")
        // deconstruct hand on melds and find all possible meld combinations
        for (pairIndex in pairIndices) {
            handWithNoCalls[pairIndex] -= 2
            val manCombs = findValidCombinations(handWithNoCalls, kanTiles, 0)
            val pinCombs = findValidCombinations(handWithNoCalls, kanTiles, 9)
            val souCombs = findValidCombinations(handWithNoCalls, kanTiles, 18)
            val honourCombs = findHonours(handWithNoCalls, kanTiles)

            // find valid combination that costs more
            Log.d("d/logCalcMain", "\t\tpair $pairIndex, comb sizes: ${manCombs.size}, ${pinCombs.size}, ${souCombs.size}, ${honourCombs.size}")
            for (man in manCombs) {
                for (pin in pinCombs) {
                    for (sou in souCombs) {
                        for (honour in honourCombs) {
                            Log.d("d/logCalcMain", "\t\t\t${(man.size + pin.size + sou.size + honour.size)}")
                            if (man.size + pin.size + sou.size + honour.size == 4) {
                                yaku["menzenchin_tsumohou"] = 1
                                val currentYaku = addYakuMelded(yaku,
                                    tsumo, pairIndex, man, pin, sou, honour, playerWind, roundWind)
                                val currentFu = calculateFu(yaku,
                                    tsumo, pairIndex, man, pin, sou, honour, playerWind, roundWind)
                                val currentCost = calculateHandCost(currentYaku, currentFu)
                                if (isFirstHandBetter(currentCost, cost)) {
                                    yaku = currentYaku
                                    cost = currentCost
                                }
                                Log.d("d/logCalcMain", "\t\t\taccurate! on $pairIndex pair, cost is $currentCost, and yaku is $currentYaku")
                            }
                        }
                    }
                }
            }
            handWithNoCalls[pairIndex] += 2
        }
    }

    private fun findValidCombinations(hand: Array<Int>, kanTiles: MutableList<Int>, start: Int) : MutableList<MutableList<Meld>> {
        val meldListList: MutableList<MutableList<Meld>> = mutableListOf()

        val ponIndices: MutableList<Int> = mutableListOf()
        for (i in start until start + 9) {
            if (hand[i] >= 3) {
                ponIndices.add(i)
            }
        }

        val combs: MutableList<MutableList<Int>> = mutableListOf(mutableListOf())
        for (i in 0 until ponIndices.size) {
            val size = combs.size
            for (j in 0 until size) {
                val comb = combs[j].toMutableList()
                comb.add(i)
                combs.add(comb)
                Log.d("d/logCalcCombs", "\t\tcomb:\t${comb.toIntArray().contentToString()}")
            }
        }
        Log.d("d/logCalcCombs", "\tcombs size = ${combs.size}")

        val kans: MutableList<Meld> = mutableListOf()
        for (i in kanTiles) {
            val tile34 = i / 4
            if (tile34 >= start && tile34 < start + 9) {
                kans.add(Meld(2, tile34))
            }
        }

        for (comb in combs) {
            val combHand = hand.clone()
            val combMelds: MutableList<Meld> = mutableListOf()
            for (i in comb) {
                combHand[ponIndices[i]] -= 3
                combMelds.add(Meld(1, ponIndices[i]))
            }

            Log.d("d/logCalcCombs", "\tbefore:\t${combHand.contentToString()}")
            var handIsValid = true
            for (i in start until start + 7) {
                for (j in 0 until combHand[i]) {
                    combMelds.add(Meld(0, i))
                }
                combHand[i + 1] -= max(combHand[i], 0)
                combHand[i + 2] -= max(combHand[i], 0)
                combHand[i] -= max(combHand[i], 0)
            }
            Log.d("d/logCalcCombs", "\tafter:\t${combHand.contentToString()}")
            for (i in start until start + 9) {
                if (combHand[i] != 0) {
                    handIsValid = false
                }
            }

            if (handIsValid) {
                for (kan in kans) {
                    combMelds.add(kan)
                }
                meldListList.add(combMelds)
            }
        }

        return meldListList
    }

    private fun findHonours(hand: Array<Int>, kanTiles: MutableList<Int>) : MutableList<MutableList<Meld>> {
        val meldListList: MutableList<MutableList<Meld>> = mutableListOf(mutableListOf())

        for (i in 27..33) {
            if (hand[i] == 3) {
                meldListList[0].add(Meld(1, i))
            } else if (hand[i] > 0) {
                return mutableListOf(mutableListOf())
            }
        }

        for (i in kanTiles) {
            val tile34 = i / 4
            if (tile34 in 27..33) {
                meldListList[0].add(Meld(2, tile34))
            }
        }

        return meldListList
    }

    private fun applyYakuConditional(yaku: MutableMap<String, Int>,
                                     yakuConditional: Map<String, Boolean>) {
        for ((k, v) in yakuConditional) {
            yaku[k] = v.toInt()
        }
    }

    private fun applyYakuHanded(yaku: MutableMap<String, Int>,
                                hand: Array<Int>, tsumo: Int,
                                doraIndicators: MutableList<Int>) {
        yaku["thirteen_wait_kokushi_musou"] = is13waitKokushiMusou(hand, tsumo).toInt()
        yaku["chinroutou"] = isChinroutou(hand).toInt()
        yaku["chuuren_poutou"] = isChuurenPoutou(hand, tsumo).toInt()
        yaku["daisangen"] = isDaisangen(hand).toInt()
        yaku["daisuushii"] = isDaisuushii(hand).toInt()
        yaku["kokushi_musou"] = isKokushiMusou(hand, tsumo).toInt()
        yaku["pure_chuuren_poutou"] = isPureChuurenPoutou(hand, tsumo).toInt()
        yaku["ryuuiisou"] = isRyuuiisou(hand).toInt()
        yaku["tsuuiisou"] = isTsuuiisou(hand).toInt()

        yaku["chiitoitsu"] = isChiitoitsu(hand).toInt()
        yaku["chinitsu"] = isChinitsu(hand).toInt()
        yaku["honitsu"] = isHonitsu(hand).toInt()
        yaku["honroutou"] = isHonroutou(hand).toInt()
        yaku["shousangen"] = isShousangen(hand).toInt()
        yaku["shousuushii"] = isShousuushii(hand).toInt()
        yaku["tanyao"] = isTanyao(hand).toInt()

        if (yaku["chinitsu"]!! > 0) {
            yaku["honitsu"] = 0
        }

        yaku["dora"] = countDora(hand, doraIndicators)
    }

    // must be applied strictly after applyYakuHanded method!
    private fun addYakuMelded(yaku: MutableMap<String, Int>,
                                tsumo: Int, pair: Int,
                                man: MutableList<Meld>,
                                pin: MutableList<Meld>,
                                sou: MutableList<Meld>,
                                honour: MutableList<Meld>,
                                playerWind: Int, roundWind: Int) : MutableMap<String, Int> {
        val yakuCopy = yaku.toMutableMap()
        yakuCopy["suuankou"] = isSuuankou(man, pin, sou, honour, pair, tsumo).toInt()
        yakuCopy["suuankou_tanki"] = isSuuankouTanki(man, pin, sou, honour, pair, tsumo).toInt()
        yakuCopy["suukantsu"] = isSuukantsu(man, pin, sou, honour).toInt()

        yakuCopy["chantaiayo"] = isChantaiayo(man, pin, sou, honour, pair).toInt()
        yakuCopy["iipeikou"] = isIipeikou(man, pin, sou).toInt()
        yakuCopy["ittsu"] = isIttsu(man, pin, sou).toInt()
        yakuCopy["junchan_taiayo"] = isJunchanTaiayo(man, pin, sou, honour, pair).toInt()
        yakuCopy["pinfu"] = isPinfu(man, pin, sou, pair, tsumo).toInt()
        yakuCopy["ryanpeikou"] = isRyanpeikou(man, pin, sou).toInt()
        yakuCopy["sanankou"] = isSanankou(man, pin, sou, honour).toInt()
        yakuCopy["sankantsu"] = isSankantsu(man, pin, sou, honour).toInt()
        yakuCopy["sanshoku_doujun"] = isSanshokuDoujun(man, pin, sou).toInt()
        yakuCopy["sanshoku_doukou"] = isSanshokuDoukou(man, pin, sou).toInt()

        if (yakuCopy["junchan_taiayo"]!! > 0) {
            yakuCopy["chantaiayo"] = 0
        }
        if (yakuCopy["honroutou"]!! > 0) {
            yakuCopy["junchan_taiayo"] = 0
        }
        if (yakuCopy["ryanpeikou"]!! > 0) {
            yakuCopy["chiitoitsu"] = 0
            yakuCopy["iipeikou"] = 0
        }

        yakuCopy["yakuhai"] = countYakuhai(honour, playerWind, roundWind)
        return yakuCopy
    }

    private fun calculateHandCost(yaku: MutableMap<String, Int>, fu: Int) : MutableMap<String, Int> {
        val cost = mutableMapOf("han" to 0, "fu" to fu, "dealer" to 0, "yakumaned" to 0)
        for ((k, v) in yakumanHanCost) {
            cost["han"] = cost["han"]!! + yaku[k]!! * v
        }
        if (cost["han"]!! > 0) {
            cost["yakumaned"] = 1
            cost["dealer"] = cost["han"]!! / 13 * 48000
            cost["fu"] = 0
        } else {
            for ((k, v) in yakuHanCost) {
                cost["han"] = cost["han"]!! + yaku[k]!! * v
            }
            for ((k, v) in yakuContedCost) {
                cost["han"] = cost["han"]!! + yaku[k]!! * v
            }
            when {
                cost["han"]!! > 12 -> {
                    cost["dealer"] = 48000
                }
                cost["han"]!! in 11..12 -> {
                    cost["dealer"] = 36000
                }
                cost["han"]!! in 8..10 -> {
                    cost["dealer"] = 24000
                }
                cost["han"]!! in 6..7 -> {
                    cost["dealer"] = 18000
                }
                cost["han"]!! == 5 -> {
                    cost["dealer"] = 12000
                }
                cost["han"]!! == 4 -> {
                    when (cost["fu"]!!) {
                        20 -> cost["dealer"] = 7800
                        25 -> cost["dealer"] = 9600
                        30 -> cost["dealer"] = 11700
                        else -> cost["dealer"] = 12000
                    }
                }
                cost["han"]!! == 3 -> {
                    when (cost["fu"]!!) {
                        20 -> cost["dealer"] = 3900
                        25 -> cost["dealer"] = 4800
                        30 -> cost["dealer"] = 6000
                        40 -> cost["dealer"] = 7800
                        50 -> cost["dealer"] = 9600
                        60 -> cost["dealer"] = 11700
                        else -> cost["dealer"] = 12000
                    }
                }
                cost["han"]!! == 2 -> {
                    when (cost["fu"]!!) {
                        20 -> cost["dealer"] = 2100
                        30 -> cost["dealer"] = 3000
                        40 -> cost["dealer"] = 3900
                        50 -> cost["dealer"] = 4800
                        60 -> cost["dealer"] = 6000
                        70 -> cost["dealer"] = 6900
                        80 -> cost["dealer"] = 7800
                        90 -> cost["dealer"] = 8700
                        100 -> cost["dealer"] = 9600
                        110 -> cost["dealer"] = 10800
                    }
                }
                cost["han"]!! == 1 -> {
                    when (cost["fu"]!!) {
                        30 -> cost["dealer"] = 1500
                        40 -> cost["dealer"] = 2100
                        50 -> cost["dealer"] = 2400
                        60 -> cost["dealer"] = 3000
                        70 -> cost["dealer"] = 3600
                        80 -> cost["dealer"] = 3900
                        90 -> cost["dealer"] = 4500
                        100 -> cost["dealer"] = 4800
                        110 -> cost["dealer"] = 5400
                    }
                }
            }
        }
        return cost
    }

    private fun calculateFu(yaku: MutableMap<String, Int>,
                            tsumo: Int, pair: Int,
                            man: MutableList<Meld>,
                            pin: MutableList<Meld>,
                            sou: MutableList<Meld>,
                            honour: MutableList<Meld>,
                            playerWind: Int, roundWind: Int) : Int {
        if (yaku["pinfu"]!! > 0) {
            return 20
        }
        var count = 22
        var wait = false
        for (meldList in arrayOf(man, pin, sou, honour)) {
            for (meld in meldList) {
                if (meld.type == 0) {
                    if (wait) {
                        continue
                    }
                    if ((tsumo == meld.tile + 1) ||
                            ((tsumo == meld.tile + 2) && (meld.tile % 9 == 0)) ||
                            ((tsumo == meld.tile) && (meld.tile % 9 == 6))) {
                        wait = true
                        Log.d("d/logCalc", "\t\t\tfu: +2 wait true!")
                    }
                }
                count += (meld.type * meld.type * 4 *
                        ((meld.tile % 9 == 0) || (meld.tile % 9 == 8)).toInt())
            }
        }
        if (pair == playerWind) {
            count += 2
        }
        if (pair == roundWind) {
            count += 2
        }
        if (pair in 31..33) {
            count += 2
        }
        if ((tsumo == pair) xor wait) {
            count += 2
        }
        Log.d("d/logCalc", "\t\t\tfu: pre-ceil is $count")
        if (count % 10 > 0) {
            count /= 10
            count++
            count *= 10
        }
        return count
    }

    private fun isFirstHandBetter(costFirstHand: MutableMap<String, Int>,
                                  costSecondHand: MutableMap<String, Int>) : Boolean{
        if (costFirstHand["yakumaned"]!! > costSecondHand["yakumaned"]!!) {
            return true
        }
        if (costFirstHand["han"]!! > costSecondHand["han"]!!) {
            return true
        }
        return ((costFirstHand["han"] == costSecondHand["han"]) &&
                (costFirstHand["fu"] == costSecondHand["fu"]))
    }

    // yaku boolean functions, they have different input and don't have checks for hand validity
    private fun is13waitKokushiMusou(hand: Array<Int>, tsumo: Int) : Boolean {
        val pattern = arrayOf(0, 8, 9, 17, 18, 26, 27, 28, 29, 30, 31, 32, 33)
        for (i in pattern) {
            if (hand[i] == 0) {
                return false
            }
        }
        return (hand[tsumo] > 1)
    }

    private fun isChantaiayo(man: MutableList<Meld>,
                             pin: MutableList<Meld>,
                             sou: MutableList<Meld>,
                             honour: MutableList<Meld>,
                             pair: Int) : Boolean {
        val possibleTilesChii = arrayOf(0, 6, 9, 15, 18, 24)
        val possibleTilesNotChii = arrayOf(0, 8, 9, 17, 18, 26, 27, 28, 29, 30, 31, 32, 33)
        for (meldList in arrayOf(man, pin, sou, honour)) {
            for (meld in meldList) {
                if (meld.type > 0) {
                    if (meld.tile in possibleTilesNotChii) {
                        continue
                    }
                } else {
                    if (meld.tile in possibleTilesChii) {
                        continue
                    }
                }
                return false
            }
        }
        return (pair in possibleTilesNotChii)
    }

    private fun isChiitoitsu(hand: Array<Int>) : Boolean {
        var pairs = 0
        for (i in hand.indices) {
            if (hand[i] == 2) {
                pairs++
            }
        }
        return (pairs == 7)
    }

    private fun isChinitsu(hand: Array<Int>) : Boolean {
        val rangeArray = arrayOf(0, 9, 17)
        for (start in rangeArray) {
            var count = 0
            for (i in start until start + 9) {
                count += hand[i]
            }
            if (count > 0) {
                return (count > 13)
            }
        }
        return false
    }

    private fun isChinroutou(hand: Array<Int>) : Boolean {
        val possibleTiles = arrayOf(0, 8, 9, 17, 18, 26)
        var tiles = 0
        for (i in possibleTiles) {
            tiles += hand[i]
        }
        return (tiles > 13)
    }

    private fun isChuurenPoutou(hand: Array<Int>, tsumo: Int) : Boolean {
        val pattern = arrayOf(3, 1, 1, 1, 1, 1, 1, 1, 3)
        for (start in arrayOf(0, 9, 17)) {
            var hasYaku = true
            for (i in pattern.indices) {
                if (hand[start + i] < pattern[i]) {
                    hasYaku = false
                    break
                }
            }
            if (hasYaku) {
                return (hand[tsumo] == pattern[tsumo - start])
            }
        }
        return false
    }

    private fun isDaisangen(hand: Array<Int>) : Boolean {
        for (i in 31..33) {
            if (hand[i] < 3) {
                return false
            }
        }
        return true
    }

    private fun isDaisuushii(hand: Array<Int>) : Boolean {
        for (i in 27..30) {
            if (hand[i] < 3) {
                return false
            }
        }
        return true
    }

    private fun isIipeikou(man: MutableList<Meld>,
                           pin: MutableList<Meld>,
                           sou: MutableList<Meld>) : Boolean {
        for (meldList in arrayOf(man, pin, sou)) {
            for (i in meldList.indices) {
                for (j in i + 1 until meldList.size) {
                    if ((meldList[i].tile == meldList[j].tile) &&
                            (meldList[i].type == 0 && meldList[j].type == 0)) {
                                return true
                    }
                }
            }
        }
        return false
    }

    private fun isIttsu(man: MutableList<Meld>,
                        pin: MutableList<Meld>,
                        sou: MutableList<Meld>) : Boolean {
        var start = 0
        for (meldList in arrayOf(man, pin, sou)) {
            val booleanArray = arrayOf(false, false, false)
            for (meld in meldList) {
                if (meld.type == 0) {
                    when (meld.tile) {
                        start -> booleanArray[0] = true
                        start + 3 -> booleanArray[1] = true
                        start + 6 -> booleanArray[2] = true
                    }
                }
                if (booleanArray[0] && booleanArray[1] && booleanArray[2]) {
                    return true
                }
            }
            start += 9
        }
        return false
    }

    private fun isJunchanTaiayo(man: MutableList<Meld>,
                                pin: MutableList<Meld>,
                                sou: MutableList<Meld>,
                                honour: MutableList<Meld>,
                                pair: Int) : Boolean {
        if (honour.size > 0) {
            return false
        }
        val possibleTilesChii = arrayOf(0, 6, 9, 15, 18, 24)
        val possibleTilesNotChii = arrayOf(0, 8, 9, 17, 18, 26)
        for (meldList in arrayOf(man, pin, sou)) {
            for (meld in meldList) {
                if (meld.type > 0) {
                    if (meld.tile in possibleTilesNotChii) {
                        continue
                    }
                } else {
                    if (meld.tile in possibleTilesChii) {
                        continue
                    }
                }
                return false
            }
        }
        return (pair in possibleTilesNotChii)
    }

    private fun isHonitsu(hand: Array<Int>) : Boolean {
        var found = false
        for (start in arrayOf(0, 9, 17)) {
            for (i in start until start + 9) {
                if (hand[i] > 0) {
                    if (found) {
                        return false
                    } else {
                        found = true
                        break
                    }
                }
            }
        }
        return true
    }

    private fun isHonroutou(hand: Array<Int>) : Boolean {
        val possibleTiles = arrayOf(0, 8, 9, 17, 18, 26, 27, 28, 29, 30, 31, 32, 33)
        var tiles = 0
        for (i in possibleTiles) {
            tiles += hand[i]
        }
        return (tiles > 13)
    }

    private fun isKokushiMusou(hand: Array<Int>, tsumo: Int) : Boolean {
        val pattern = arrayOf(0, 8, 9, 17, 18, 26, 27, 28, 29, 30, 31, 32, 33)
        for (i in pattern) {
            if (hand[i] == 0) {
                return false
            }
        }
        return (hand[tsumo] == 1)
    }

    private fun isPinfu(man: MutableList<Meld>,
                        pin: MutableList<Meld>,
                        sou: MutableList<Meld>,
                        pair: Int,
                        tsumo: Int) : Boolean {
        if (man.size + pin.size + sou.size < 4) {
            return false
        }
        var noTankiWaiting = false
        for (meldList in arrayOf(man, pin, sou)) {
            for (meld in meldList) {
                if (meld.type > 0) {
                    return false
                } else if ((tsumo == meld.tile && meld.tile % 9 != 6) ||
                    (tsumo == meld.tile + 2 && meld.tile % 9 != 0)) {
                    noTankiWaiting = true
                }
            }
        }
        if (pair in 31..33 || pair == playerWind || pair == roundWind) {
            return false
        }
        Log.d("d/logCalcSpecific", "\t\t\tpinfu, noTankiWaiting: $noTankiWaiting")
        return noTankiWaiting
    }

    private fun isPureChuurenPoutou(hand: Array<Int>, tsumo: Int) : Boolean {
        val pattern = arrayOf(3, 1, 1, 1, 1, 1, 1, 1, 3)
        for (start in arrayOf(0, 9, 17)) {
            var hasYaku = true
            for (i in pattern.indices) {
                if (hand[start + i] < pattern[i]) {
                    hasYaku = false
                    break
                }
            }
            if (hasYaku) {
                return (hand[tsumo] > pattern[tsumo - start])
            }
        }
        return false
    }

    private fun isRyanpeikou(man: MutableList<Meld>,
                             pin: MutableList<Meld>,
                             sou: MutableList<Meld>) : Boolean {
        for (meldList in arrayOf(man, pin, sou)) {
            for (meld in meldList) {
                if (meld.type > 0) {
                    return false
                }
            }
        }
        for (meldList in arrayOf(man, pin, sou)) {
            if (meldList.size == 4) {
                Log.d("d/logCalcSpecific", "\t\t\tRyanpeikou, patch of size 4")
                Log.d("d/logCalcSpecific", "\t\t\t(${meldList[0].type} ${meldList[0].tile}); (${meldList[1].type} ${meldList[1].tile}); (${meldList[2].type} ${meldList[2].tile}); (${meldList[3].type} ${meldList[3].tile})")
                return ((meldList[0].tile == meldList[1].tile) && (meldList[2].tile == meldList[3].tile))
            } else if (meldList.size == 2) {
                Log.d("d/logCalcSpecific", "\t\t\tRyanpeikou, patch of size 2")
                if (meldList[0].tile != meldList[1].tile) {
                    return false
                }
            } else if (meldList.size > 0) {
                Log.d("d/logCalcSpecific", "\t\t\tRyanpeikou, patch of size 1 or 3")
                return false
            }
        }
        Log.d("d/logCalcSpecific", "\t\t\tRyanpeikou found!")
        return true
    }

    private fun isRyuuiisou(hand: Array<Int>) : Boolean {
        val possibleTiles = arrayOf(19, 20, 21, 23, 25, 33)
        var count = 0
        for (i in possibleTiles) {
            count += hand[i]
        }
        return (count > 13)
    }

    private fun isSanankou(man: MutableList<Meld>,
                           pin: MutableList<Meld>,
                           sou: MutableList<Meld>,
                           honour: MutableList<Meld>) : Boolean {
        var count = 0
        for (meldList in arrayOf(man, pin, sou, honour)) {
            for (meld in meldList) {
                if (meld.type > 0) {
                    count++
                }
            }
        }
        return (count == 3)
    }

    private fun isSankantsu(man: MutableList<Meld>,
                            pin: MutableList<Meld>,
                            sou: MutableList<Meld>,
                            honour: MutableList<Meld>) : Boolean {
        var count = 0
        for (meldList in arrayOf(man, pin, sou, honour)) {
            for (meld in meldList) {
                if (meld.type > 1) {
                    count++
                }
            }
        }
        return (count == 3)
    }

    private fun isSanshokuDoujun(man: MutableList<Meld>,
                                 pin: MutableList<Meld>,
                                 sou: MutableList<Meld>) : Boolean {
        for (manMeld in man) {
            for (pinMeld in pin) {
                for (souMeld in sou) {
                    if (manMeld.type == 0 &&
                        pinMeld.type == 0 &&
                        souMeld.type == 0 &&
                        manMeld.tile == pinMeld.tile - 9 &&
                        manMeld.tile == souMeld.tile - 18) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun isSanshokuDoukou(man: MutableList<Meld>,
                                 pin: MutableList<Meld>,
                                 sou: MutableList<Meld>) : Boolean {
        for (manMeld in man) {
            for (pinMeld in pin) {
                for (souMeld in sou) {
                    if (manMeld.type > 0 &&
                        pinMeld.type > 0 &&
                        souMeld.type > 0 &&
                        manMeld.tile == pinMeld.tile - 9 &&
                        manMeld.tile == souMeld.tile - 18) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun isShousangen(hand: Array<Int>) : Boolean {
        var count = 0
        for (i in 31..33) {
            count += min(hand[i], 3)
        }
        return (count == 8)
    }

    private fun isShousuushii(hand: Array<Int>) : Boolean {
        var count = 0
        for (i in 27..30) {
            count += min(hand[i], 3)
        }
        return (count == 11)
    }

    private fun isSuuankou(man: MutableList<Meld>,
                           pin: MutableList<Meld>,
                           sou: MutableList<Meld>,
                           honour: MutableList<Meld>,
                           pair: Int,
                           tsumo: Int) : Boolean {
        for (meldList in arrayOf(man, pin, sou, honour)) {
            for (meld in meldList) {
                if (meld.type < 1) {
                    return false
                }
            }
        }
        return (pair != tsumo)
    }

    private fun isSuuankouTanki(man: MutableList<Meld>,
                           pin: MutableList<Meld>,
                           sou: MutableList<Meld>,
                           honour: MutableList<Meld>,
                           pair: Int,
                           tsumo: Int) : Boolean {
        for (meldList in arrayOf(man, pin, sou, honour)) {
            for (meld in meldList) {
                if (meld.type < 1) {
                    return false
                }
            }
        }
        return (pair == tsumo)
    }

    private fun isSuukantsu(man: MutableList<Meld>,
                            pin: MutableList<Meld>,
                            sou: MutableList<Meld>,
                            honour: MutableList<Meld>) : Boolean {
        for (meldList in arrayOf(man, pin, sou, honour)) {
            for (meld in meldList) {
                if (meld.type < 2) {
                    return false
                }
            }
        }
        return true
    }

    private fun isTanyao(hand: Array<Int>) : Boolean {
        for (i in arrayOf(0, 8, 9, 17, 18, 26, 27, 28, 29, 30, 31, 32, 33)) {
            if (hand[i] > 0) {
                return false
            }
        }
        return true
    }

    private fun isTsuuiisou(hand: Array<Int>) : Boolean {
        for (i in 0..26) {
            if (hand[i] > 0) {
                return false
            }
        }
        return true
    }

    private fun countDora(hand: Array<Int>, doraIndicators: MutableList<Int>) : Int {
        var count = 0
        for (tile in hand.indices) {
            for (dora in doraIndicators) {
                if (tile == (dora / 4 + 1)) {
                    count += hand[tile]
                }
            }
        }
        return count
    }

    private fun countYakuhai(honour: MutableList<Meld>, playerWind: Int, roundWind: Int) : Int {
        var count = 0
        for (meld in honour) {
            if (meld.tile in 31..33 || meld.tile == playerWind || meld.tile == roundWind) {
                count++
            }
        }
        return count
    }
}