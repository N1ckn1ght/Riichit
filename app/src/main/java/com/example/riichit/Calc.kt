package com.example.riichit

// TODO: Yaku should have their own classes as well to improve cost calculations code complexity
class Calc (private val showableHand: MutableList<Int>,
            private val showableTsumo: Int,
            private val doraIndicators: MutableList<Int>,
            private val kanTiles: MutableList<Int>,
            private val yakuConditional: Map<String, Boolean>,
            private val playerWind: Int = 27,
            private var roundWind: Int = 27) {
    class Meld (val type: Int, val tile: Int)   // 0 - chii, 1 - pon, 2 - kan

    private var yaku = mutableMapOf("13-wait kokushi musou" to 0,
        "chantaiayo" to 0,
        "chiitoitsu" to 0,
        "chinitsu" to 0,
        "chinroutou" to 0,
        "chuuren poutou" to 0,
        "daisangen" to 0,
        "daisuushii" to 0,
        "dora" to 0,
        "double kokushi musou" to 0,
        "double riichi" to 0,
        "iipeikou" to 0,
        "ippatsu" to 0,
        "ittsu" to 0,
        "junchan taiayo" to 0,
        "haitei raoyue" to 0,
        "honitsu" to 0,
        "honroutou" to 0,
        "kokushi musou" to 0,
        "menzenchin tsumohou" to 0,
        "pinfu" to 0,
        "pure chuuren poutou" to 0,
        "riichi" to 0,
        "rinshan kaihou" to 0,
        "ryanpeikou" to 0,
        "ryuuiisou" to 0,
        "sanankou" to 0,
        "sankatsu" to 0,
        "sanshoku doujun" to 0,
        "sanshoku doukou" to 0,
        "shousangen" to 0,
        "shousuushii" to 0,
        "suuankou" to 0,
        "suuankou tanki" to 0,
        "suukantsu" to 0,
        "tanyao" to 0,
        "tenhou" to 0,
        "tsuuiisou" to 0,
        "yakuhai" to 0)
    private var cost = mutableMapOf("han" to 0,
            "fu" to 0,
            "base" to 0)
    // TODO: Han and fu results should be detailed for education
    private var chombo = true

    private val hand = Array(34){-1}
    private var tsumo = -1

    fun calc() {
        clear()
        applyYakuConditional()
        decompose()
    }

    private fun clear() {
        for ((k, _) in yaku) {
            yaku[k] = 0
        }
        for ((k, _) in cost) {
            cost[k] = 0
        }
        for (i in hand.indices) {
            hand[i] = 0
        }
        tsumo = -1
        chombo = true
    }

    private fun decompose() {
        // simple check for invalid hand
        if (showableHand.size != 13 - kanTiles.size * 3) {
            return
        }

        // transform hand to readable form (without kans for now)
        tsumo = showableTsumo / 4
        for (i in showableHand) {
            hand[i / 4]++
        }
        hand[tsumo]++

        // search for special yakus
        yaku = applyYakuHanded(yaku, hand, tsumo, doraIndicators)
        if (yaku["chiitoitsu"]!! > 0 ||
            yaku["kokushi musou"]!! > 0 ||
            yaku["13-wait kokushi musou"]!! > 0) {
            chombo = false
        }

        // find all possible pairs
        val pairIndices: MutableList<Int> = mutableListOf()
        for (i in hand.indices) {
            if (hand[i] > 1) {
                pairIndices.add(i)
            }
        }

        // find all possible meld combinations
        for (pairIndex in pairIndices) {
            val currentHand = hand
            currentHand[pairIndex] -= 2

            val manCombs = findValidCombinations(currentHand, 0)
            val pinCombs = findValidCombinations(currentHand, 9)
            val souCombs = findValidCombinations(currentHand, 18)
            val honourCombs = findHonours(currentHand)

            // find valid combination that costs more
            for (man in manCombs) {
                for (pin in pinCombs) {
                    for (sou in souCombs) {
                        for (honour in honourCombs) {
                            if (man.size + pin.size + sou.size + honour.size == 4) {
                                chombo = false
                                val currentYaku = searchForYaku(man, pin, sou, honour)
                                val currentCost = calculateHandCost(currentYaku)
                                // TODO: check for yaku count in case of kazoe yakuman!!
                                if (currentCost["base"]!! > cost["base"]!!) {
                                    yaku = currentYaku
                                    cost = currentCost
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun findValidCombinations(hand: Array<Int>, start: Int) : MutableList<MutableList<Meld>> {
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
            for (j in 0..size) {
                val comb = combs[j]
                comb.add(i + 1)
                combs.add(comb)
            }
        }

        val kans: MutableList<Meld> = mutableListOf()
        for (i in kanTiles) {
            if (i >= start && i < start + 9) {
                kans.add(Meld(2, i))
            }
        }

        for (comb in combs) {
            val combHand = hand
            val combMelds: MutableList<Meld> = mutableListOf()
            for (i in comb) {
                combHand[ponIndices[i]] -= 3
                combMelds.add(Meld(1, ponIndices[i]))
            }

            var handIsValid = true
            for (i in start until start + 7) {
                for (j in 0 until combHand[i]) {
                    combMelds.add(Meld(0, combHand[i]))
                }
                combHand[i + 1] -= max(combHand[i], 0)
                combHand[i + 2] -= max(combHand[i], 0)
                combHand[i] -= max(combHand[i], 0)
            }
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

    private fun findHonours(hand: Array<Int>) : MutableList<MutableList<Meld>> {
        val meldListList: MutableList<MutableList<Meld>> = mutableListOf(mutableListOf())

        for (i in 27..33) {
            if (hand[i] == 3) {
                meldListList[0].add(Meld(1, i))
            } else if (hand[i] > 0) {
                return mutableListOf(mutableListOf())
            }
        }

        for (i in kanTiles) {
            if (i in 27..33) {
                meldListList[0].add(Meld(2, i))
            }
        }

        return meldListList
    }

    private fun applyYakuConditional() {
        for ((k, v) in yakuConditional) {
            yaku[k] = v.toInt()
        }
    }

    private fun applyYakuHanded(yaku: MutableMap<String, Int>,
                                hand: Array<Int>, tsumo: Int,
                                doraIndicators: MutableList<Int>) : MutableMap<String, Int> {
        yaku["13-wait kokushi musou"] = isKokushiMusou(hand, tsumo).toInt()
        yaku["chiitoitsu"] = isChiitoitsu(hand).toInt()
        yaku["chinitsu"] = isChinitsu(hand).toInt()
        yaku["chinroutou"] = isChinroutou(hand).toInt()
        yaku["chuuren poutou"] = isChuurenPoutou(hand, tsumo).toInt()
        yaku["daisangen"] = isDaisangen(hand).toInt()
        yaku["daisuushii"] = isDaisuushii(hand).toInt()
        yaku["honroutou"] = isHonroutou(hand).toInt()
        yaku["kokushi musou"] = isKokushiMusou(hand, tsumo).toInt()
        yaku["pure chuuren poutou"] = isPureChuurenPoutou(hand, tsumo).toInt()
        yaku["ryuuiisou"] = isRyuuiisou(hand).toInt()
        yaku["shousangen"] = isShousangen(hand).toInt()
        yaku["shousuushii"] = isShousuushii(hand).toInt()
        yaku["tanyao"] = isTanyao(hand).toInt()
        yaku["tsuuiisou"] = isTsuuiisou(hand).toInt()
        yaku["dora"] = countDora(hand, doraIndicators)
        if (yaku["chinitsu"]!! > 0) {
            yaku["honitsu"] = isHonitsu(hand).toInt()
        }
        return yaku
    }

    private fun applyYakuMelded(yaku: MutableMap<String, Int>,
                                hand: Array<Int>, tsumo: Int, pair: Int,
                                man: MutableList<Meld>,
                                pin: MutableList<Meld>,
                                sou: MutableList<Meld>,
                                honour: MutableList<Meld>) : MutableMap<String, Int> {
        // TODO: implement
        return yaku
    }

    private fun calculateHandCost(yaku: MutableMap<String, Int>) : MutableMap<String, Int> {
        // TODO: calculate hand cost logic

        return mutableMapOf()
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
        val meldListArray = arrayOf(man, pin, sou, honour)
        val possibleTilesChii = arrayOf(0, 6, 9, 15, 18, 24)
        val possibleTilesNotChii = arrayOf(0, 8, 9, 17, 18, 26, 27, 28, 29, 30, 31, 32, 33)
        for (meldList in meldListArray) {
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
        val rangeArray = arrayOf(0, 9, 17)
        for (start in rangeArray) {
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
        val meldListArray = arrayOf(man, pin, sou)
        for (meldList in meldListArray) {
            for (i in meldList.indices) {
                for (j in i + 1 until meldList.size) {
                    if (i == j) {
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
        val meldListArray = arrayOf(man, pin, sou)
        var start = 0
        for (meldList in meldListArray) {
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
        val meldListArray = arrayOf(man, pin, sou)
        val possibleTilesChii = arrayOf(0, 6, 9, 15, 18, 24)
        val possibleTilesNotChii = arrayOf(0, 8, 9, 17, 18, 26)
        for (meldList in meldListArray) {
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
        val rangeArray = arrayOf(0, 9, 17)
        var found = false
        for (start in rangeArray) {
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
        val meldListArray = arrayOf(man, pin, sou)
        var noTankiWaiting = false
        for (meldList in meldListArray) {
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
        return noTankiWaiting
    }

    private fun isPureChuurenPoutou(hand: Array<Int>, tsumo: Int) : Boolean {
        val pattern = arrayOf(3, 1, 1, 1, 1, 1, 1, 1, 3)
        val rangeArray = arrayOf(0, 9, 17)
        for (start in rangeArray) {
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
        val meldListArray = arrayOf(man, pin, sou)
        for (meldList in meldListArray) {
            for (meld in meldList) {
                if (meld.type > 0) {
                    return false
                }
            }
        }
        for (meldList in meldListArray) {
            if (meldList.size == 4) {
                return ((meldList[0] == meldList[1]) && (meldList[2] == meldList[3]))
            } else if (meldList.size == 2) {
                if (meldList[0] != meldList[1]) {
                    return false
                }
            } else if (meldList.size > 0) {
                return false
            }
        }
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
        val meldListArray = arrayOf(man, pin, sou, honour)
        var count = 0
        for (meldList in meldListArray) {
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
        val meldListArray = arrayOf(man, pin, sou, honour)
        var count = 0
        for (meldList in meldListArray) {
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
                        manMeld.tile == pinMeld.tile + 9 &&
                        manMeld.tile == souMeld.tile + 18) {
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
                        manMeld.tile == pinMeld.tile + 9 &&
                        manMeld.tile == souMeld.tile + 18) {
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
        val meldListArray = arrayOf(man, pin, sou, honour)
        for (meldList in meldListArray) {
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
        val meldListArray = arrayOf(man, pin, sou, honour)
        for (meldList in meldListArray) {
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
        val meldListArray = arrayOf(man, pin, sou, honour)
        for (meldList in meldListArray) {
            for (meld in meldList) {
                if (meld.type < 2) {
                    return false
                }
            }
        }
        return true
    }

    private fun isTanyao(hand: Array<Int>) : Boolean {
        val impossibleTiles = arrayOf(0, 8, 9, 17, 18, 26, 27, 28, 29, 30, 31, 32, 33)
        for (i in impossibleTiles) {
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