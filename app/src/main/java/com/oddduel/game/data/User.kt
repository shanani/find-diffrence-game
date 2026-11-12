package com.oddduel.game.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val avatarIndex: Int,
    val bestScore: Int = 0,
    val totalWins: Int = 0,
    val totalMatches: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
