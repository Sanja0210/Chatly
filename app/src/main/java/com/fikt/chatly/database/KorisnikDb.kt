package com.fikt.chatly.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity
data class KorisnikDb(
    @PrimaryKey val uid: String,
    @ColumnInfo val username: String?,
    @ColumnInfo val fullname: String?,
    @ColumnInfo val profile: String?,
    @ColumnInfo val cover: String?,
    @ColumnInfo val status: String?,
    @ColumnInfo val search: String?,
    @ColumnInfo val facebook: String?,
    @ColumnInfo val instagram: String?,
    @ColumnInfo val website: String?,
    @ColumnInfo val gender: String?,
    @ColumnInfo val timestamp: Long?,
    @ColumnInfo val dostapnost: Int?,
    @ColumnInfo val gostin: Int?,
    @ColumnInfo val kodPotvrda: String?
)