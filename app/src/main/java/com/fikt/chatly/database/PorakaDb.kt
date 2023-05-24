package com.fikt.chatly.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PorakaDb(
    @PrimaryKey val porakaId: String,
    @ColumnInfo val isprakjac: String?,
    @ColumnInfo val poraka: String?,
    @ColumnInfo val primac: String?,
    @ColumnInfo val seen: Boolean?,
    @ColumnInfo val url: String?,
    @ColumnInfo val isprateno: Boolean?
)