package com.fikt.chatly.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface KorisnikDao {
    @Query("SELECT * FROM KorisnikDb")
    fun getAll(): List<KorisnikDb>

    @Query("SELECT * FROM KorisnikDb WHERE uid = :term")
    fun searchFor(term: String): List<KorisnikDb>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg users: KorisnikDb)
}