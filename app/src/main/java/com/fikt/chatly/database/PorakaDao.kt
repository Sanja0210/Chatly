package com.fikt.chatly.database

import androidx.room.*

@Dao
interface PorakaDao {
    @Query("SELECT * FROM PorakaDb")
    fun getAll(): List<PorakaDb>

    @Query("SELECT * FROM PorakaDb WHERE porakaId = :term")
    fun searchFor(term: String): List<PorakaDb>

    @Query("SELECT * FROM PorakaDb WHERE isprakjac = :term")
    fun najdiSoIsprakjac(term: String): List<PorakaDb>

    @Query("SELECT * FROM PorakaDb WHERE primac = :term")
    fun najdiSoPrimac(term: String): List<PorakaDb>

    @Update()
    fun namestiIsprateno(poraka: PorakaDb): Int

    @Query("SELECT * FROM PorakaDb WHERE (isprakjac = :term OR primac = :term) AND (isprakjac = :term1 OR primac = :term1)")
    fun najdiPorakiChat(term: String, term1 : String): List<PorakaDb>

    @Query("SELECT * FROM PorakaDb WHERE (isprakjac = :term OR primac = :term) AND (isprakjac = :term1 OR primac = :term1)")
    fun najdiNeisprateniPorakiChat(term: String, term1 : String): List<PorakaDb>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg users: PorakaDb)
}