package com.example.riichit

import java.lang.Integer.max

object Ruleset {
    val yakuCountedCost = mapOf(
        "dora" to 1,
        "yakuhai" to 1
    )

    val yakuHanCost = mapOf(
        "chantaiayo" to 2,
        "chiitoitsu" to 2,
        "chinitsu" to 6,
        "chombo" to 0,
        "double_riichi" to 2,
        "iipeikou" to 1,
        "ippatsu" to 1,
        "ittsu" to 2,
        "junchan_taiayo" to 3,
        "haitei_raoyue" to 1,
        "honitsu" to 3,
        "honroutou" to 2,
        "menzenchin_tsumohou" to 1,
        "pinfu" to 1,
        "riichi" to 1,
        "rinshan_kaihou" to 1,
        "ryanpeikou" to 3,
        "sanankou" to 2,
        "sankantsu" to 2,
        "sanshoku_doujun" to 2,
        "sanshoku_doukou" to 2,
        "shousangen" to 2,
        "tanyao" to 1
    )

    val yakumanHanCost = mapOf(
        "thirteen_wait_kokushi_musou" to 26,
        "chinroutou" to 13,
        "chuuren_poutou" to 13,
        "daisangen" to 13,
        "daisuushii" to 26,
        "kokushi_musou" to 13,
        "pure_chuuren_poutou" to 26,
        "ryuuiisou" to 13,
        "shousuushii" to 13,
        "suuankou" to 13,
        "suuankou_tanki" to 26,
        "suukantsu" to 13,
        "tenhou" to 13,
        "tsuuiisou" to 13
    )

    val achievements = arrayOf(
        "mentanpin",
        "chantaiayo",
        "iipeikou",
        "tsumo_only",
        "chiitoitsu",
        "sanshoku_doujun",
        "ittsu",
        "ippatsu_haitei",
        "sanshoku_doukou",
        "streak_22",
        "dora_13",
        "hand_of_god",
        "kokushi_musou"
    )

    fun newBalance(currentBalance: Int, change: Int): Int {
        var balance = currentBalance
        if (change < 0) {
            balance = max(0, balance + change)
        } else {
            val modif = (100000.0 / (max(25000, balance) + 75000).toDouble())
            balance += (change * modif).toInt()
        }
        return balance
    }
}