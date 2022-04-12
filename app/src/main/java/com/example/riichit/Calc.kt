package com.example.riichit

// TODO: Yaku should have their own classes as well to improve cost calculations code complexity
class Calc (hand: MutableList<Int>, tsumo: Int, doraIndicators: MutableList<Int>, kanTiles: MutableList<Int>, yakuConditional: MutableList<Boolean>) {
    // chanta, chiitoitsu, chinitsu, TODO: continue the list
    // + yakuhai, dora
    // + riichi, doubleRiichi, ippatsu, rinshan, haitei, tenhou
    val yakuList: Array<Int> = arrayOf(0)
    // han, fu, cost
    val handCost: Array<Int> = arrayOf(0, 0, 0)
    // TODO: Han and fu results should be detailed for education

    fun calc() {
        yakuList[0] = 1
    }
}
