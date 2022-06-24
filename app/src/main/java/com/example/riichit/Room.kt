package com.example.riichit

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) var id: Int,
    @ColumnInfo(name = "profile", defaultValue = "0") var profile: Int,
    @ColumnInfo(name = "hand", defaultValue = "[]") var hand: String,
    @ColumnInfo(name = "tsumo", defaultValue = "136") var tsumo: Int,
    @ColumnInfo(name = "kan_tiles", defaultValue = "[]") var kanTiles: String,
    @ColumnInfo(name = "discard", defaultValue = "[]") var discard: String,
    @ColumnInfo(name = "yaku_conditional", defaultValue = "[0, 0, 0, 0, 0, 0]") var yakuConditional: String,
    @ColumnInfo(name = "datetime", defaultValue = "CURRENT_TIMESTAMP") var datetime: String,
)

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) var id: Int,
    @ColumnInfo(name = "mahjong_balance", defaultValue = "0") var mahjongBalance: Int,
    @ColumnInfo(name = "man_balance", defaultValue = "0") var manBalance: Int,
    @ColumnInfo(name = "mahjong_streak", defaultValue = "0") var mahjongStreak: Int,
    @ColumnInfo(name = "man_streak", defaultValue = "0") var manStreak: Int
)

@Dao
interface GamesDao {
    @Query("INSERT INTO games (profile, hand, tsumo, kan_tiles, discard, yaku_conditional) VALUES (:profile, :hand, :tsumo, :kanTiles, :discard, :yakuConditional)")
    fun addGame(profile: Int, hand: String, tsumo: Int, kanTiles: String, discard: String, yakuConditional: String)
}

@Dao
interface ProfilesDao {
    @Query("INSERT INTO profiles (mahjong_balance, man_balance) VALUES (:mahjongBalance, :manBalance)")
    fun createProfile(mahjongBalance: Int = 25000, manBalance: Int = 25000)

    @Query("SELECT * FROM profiles WHERE id = (:profile)")
    fun getProfile(profile: Int): List<ProfileEntity>

    @Query("UPDATE profiles SET mahjong_balance = (:mahjongBalance), mahjong_streak = (:mahjongStreak), man_balance = (:manBalance), man_streak = (:manStreak) WHERE id = (:profile)")
    fun updateProfile(profile: Int, mahjongBalance: Int, manBalance: Int, mahjongStreak: Int, manStreak: Int)
}

@Database(entities = [GameEntity::class, ProfileEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gamesDao(): GamesDao
    abstract fun profilesDao(): ProfilesDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        internal fun instance(context: Context): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE =
                    Room.databaseBuilder(context, AppDatabase::class.java, "quotations.db").build()
            }
            return INSTANCE!!
        }
    }
}