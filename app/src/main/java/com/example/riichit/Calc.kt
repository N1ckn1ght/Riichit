package com.example.riichit

// TODO: Yaku should have their own classes as well to improve cost calculations code complexity
class Calc (private val showableHand: MutableList<Int>,
            private val showableTsumo: Int,
            private val doraIndicators: MutableList<Int>,
            private val kanTiles: MutableList<Int>,
            private val yakuConditional: Map<String, Boolean>) {
    class Meld (val type: Int, val tile: Int)   // 0 - chii, 1 - pon, 2 - kan

    var yaku = mutableMapOf("chantaiayo" to 0,
            "chiitoitsu" to 0,
            "chinitsu" to 0,
            "chinroutou" to 0,
            "chuuren potou" to 0,
            "daisangen" to 0,
            "daisuushii" to 0,
            "dora" to 0,
            "double chuuren poutou" to 0,
            "double kokushi musou" to 0,
            "double riichi" to 0,
            "iipeikou" to 0,
            "ippatsu" to 0,
            "ittsu" to 0,
            "junchan taiayo" to 0,
            "haitei raoyue" to 0,
            "honitsu" to 0,
            "honroutou" to 0,
            "kazoe yakuman" to 0,
            "kokushi musou" to 0,
            "menzenchin tsumohou" to 0,
            "pinfu" to 0,
            "riichi" to 0,
            "rinshan kaihou" to 0,
            "ryanpeikou" to 0,
            "ryuuiisou" to 0,
            "sanankou" to 0,
            "sankatsu" to 0,
            "sanshoku doukou" to 0,
            "shousangen" to 0,
            "shousuushii" to 0,
            "suuankou" to 0,
            "suuankou tanki" to 0,
            "tanyao" to 0,
            "tenhou" to 0,
            "tsuuiisou" to 0,
            "yakuhai" to 0)
    var cost = mutableMapOf("han" to 0,
            "fu" to 0,
            "base" to 0)
    // TODO: Han and fu results should be detailed for education
    var chombo = true

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

    private fun applyYakuConditional() {
        for ((k, v) in yakuConditional) {
            yaku[k] = v.toInt()
        }
    }

    private fun decompose() {
        // simple check for invalid hand
        if (showableHand.size != 13 - kanTiles.size * 3) {
            chombo = true
            return
        }

        // transform hand to readable form (without kans for now)
        tsumo = showableTsumo / 4
        for (i in showableHand) {
            hand[i / 4]++
        }
        hand[tsumo]++

        // find all possible pairs
        val pairIndices: MutableList<Int> = mutableListOf()
        for (i in hand.indices) {
            if (hand[i] > 1) {
                pairIndices.add(i)
            }
        }

        // special case: all 7 pairs
        if (pairIndices.size == 7) {
            yaku["chiitoitsu"] = 1
            // TODO: check for ryanpeikou!!
            return
        } else if (pairIndices.size != 1) {
            chombo = true
            return
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

    private fun searchForYaku(man: MutableList<Meld>,
                              pin: MutableList<Meld>,
                              sou: MutableList<Meld>,
                              honour: MutableList<Meld>) : MutableMap<String, Int> {
        // TODO: search for yaku logic

        return mutableMapOf()
    }

    private fun calculateHandCost(yaku: MutableMap<String, Int>) : MutableMap<String, Int> {
        // TODO: calculate hand cost logic

        return mutableMapOf()
    }
}