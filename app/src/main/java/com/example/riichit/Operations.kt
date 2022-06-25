package com.example.riichit

import android.util.Log
import com.example.riichit.Ruleset.newBalance
import com.example.riichit.Utility.operationsLimit
import com.example.riichit.Utility.toInt

object Operations {
    fun addGame(
        db: AppDatabase,
        profile: Int,
        hand: MutableList<Int>,
        tsumo: Int,
        kanTiles: MutableList<Int>,
        discard: MutableList<Int>,
        yakuConditional: Map<String, Boolean>? = null
    ) {
        val yakuConditionalArray = arrayOf(0, 0, 0, 0, 0, 0)
        yakuConditional?.let {
            var i = 0
            for ((_, v) in it) {
                yakuConditionalArray[i++] = v.toInt()
            }
        }

        val handString = hand.toIntArray().contentToString()
        val kanTilesString = kanTiles.toIntArray().contentToString()
        val discardString = discard.toIntArray().contentToString()
        val yakuConditionalString = yakuConditionalArray.contentToString()

        db.gamesDao().addGame(
            profile,
            handString,
            tsumo,
            kanTilesString,
            discardString,
            yakuConditionalString
        )
    }

    fun updateProfile(
        db: AppDatabase,
        profile: Int,
        mode: Int,
        balanceChange: Int,
        streakChange: Int
    ) {
        val data = getProfile(db, profile)

        if (data.isEmpty()) {
            Log.d("d/operations", "Profile $profile not found in database $db!")
            return
        }

        var mahjongBalance = data[0].mahjongBalance
        var mahjongStreak = data[0].mahjongStreak
        var manBalance = data[0].manBalance
        var manStreak = data[0].manStreak
        when (mode) {
            0 -> {
                mahjongBalance = newBalance(mahjongBalance, balanceChange)
                mahjongStreak = when {
                    streakChange < 0 -> 0
                    streakChange > 0 -> streakChange
                    else -> mahjongStreak
                }
            }
            1 -> {
                manBalance = newBalance(manBalance, balanceChange)
                manStreak = when {
                    streakChange < 0 -> 0
                    streakChange > 0 -> streakChange
                    else -> manStreak
                }
            }
        }
        updateProfile(db, profile, mahjongBalance, mahjongStreak, manBalance, manStreak)
    }

    fun createProfile(
        db: AppDatabase,
        profile: Int
    ) {
        var recursionLevel = 0

        var data = getProfile(db, profile)
        while (data.isEmpty() && recursionLevel++ < operationsLimit) {
            db.profilesDao().createProfile()
            data = getProfile(db, profile)
        }
    }

    fun getBalance(
        db: AppDatabase,
        profile: Int,
        mode: Int
    ): Int {
        val data = getProfile(db, profile)

        if (data.isEmpty()) {
            Log.d("d/operations", "Profile $profile not found in database $db!")
            return 0
        }

        when (mode) {
            0 -> {
                return data[0].mahjongBalance
            }
            1 -> {
                return data[0].manBalance
            }
        }
        return 0
    }

    fun getStreak(
        db: AppDatabase,
        profile: Int,
        mode: Int
    ): Int {
        val data = getProfile(db, profile)

        if (data.isEmpty()) {
            Log.d("d/operations", "Profile $profile not found in database $db!")
            return 0
        }

        when (mode) {
            0 -> {
                return data[0].mahjongStreak
            }
            1 -> {
                return data[0].manStreak
            }
        }
        return 0
    }

    private fun getProfile(
        db: AppDatabase,
        profile: Int
    ): List<ProfileEntity> {
        return db.profilesDao().getProfile(profile)
    }

    private fun updateProfile(
        db: AppDatabase,
        profile: Int,
        mahjongBalance: Int,
        mahjongStreak: Int,
        manBalance: Int,
        manStreak: Int
    ) {
        db.profilesDao().updateProfile(profile, mahjongBalance, manBalance, mahjongStreak, manStreak)
    }
}