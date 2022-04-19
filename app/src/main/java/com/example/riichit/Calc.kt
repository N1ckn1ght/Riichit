package com.example.riichit

// TODO: Yaku should have their own classes as well to improve cost calculations code complexity
class Calc (val showableHand: MutableList<Int>, val showableTsumo: Int, val doraIndicators: MutableList<Int>, val kanTiles: MutableList<Int>, val yakuConditional: Map<String, Boolean>) {

    val yaku = mutableMapOf("chantaiayo" to 0,
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
    val cost = mutableMapOf("han" to 0,
            "fu" to 0,
            "base" to 0)
    // TODO: Han and fu results should be detailed for education

    private var chombo = false
    private val hand = Array(14){-1}
    private var tsumo = -1
    private val melds = Array(5){Meld(-1, -1)}

    fun calc() {
        decompose()
        applyYakuConditional()
    }

    private fun decompose() {
        // transform hand to readable form
        var index = 0
        while (showableTsumo > showableHand[index] && index < 13) {
            index++
        }
        tsumo = showableTsumo / 4
        hand[index] = tsumo
        for (i in 0 until index) {
            hand[i] = showableHand[i] / 4
        }
        for (i in index+1 until 14) {
            hand[i] = showableHand[i - 1] / 4
        }

        // get melds and pair from hand
        for (i in 1 until 14) {
            if ()
        }
    }

    private fun applyYakuConditional() {
        for ((k, v) in yakuConditional) {
            yaku[k] = v.toInt()
        }
    }
}

class Meld (val type: Int, val tile: Int, val aka: Int = 0)     // 0 - chii, 1 - pon, 2 - kan
class Pair (val tile: Int, val aka: Int = 0)