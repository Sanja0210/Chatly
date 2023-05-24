package com.fikt.chatly.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [KorisnikDb::class, PorakaDb::class], version = 5)
abstract class AppDatabase : RoomDatabase() {
    abstract fun korisnikDao(): KorisnikDao
    abstract fun porakaDao(): PorakaDao
}