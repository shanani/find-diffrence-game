package com.oddduel.game.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class UserRepository(context: Context) {
    private val dao = AppDatabase.get(context).userDao()

    fun observeAll(): Flow<List<User>> = dao.observeAll()

    suspend fun createUser(name: String, avatarIndex: Int): User {
        val id = dao.upsert(User(name = name, avatarIndex = avatarIndex))
        return dao.getById(id)!!
    }

    suspend fun recordResult(user: User, score: Int, won: Boolean) {
        dao.update(
            user.copy(
                bestScore = maxOf(user.bestScore, score),
                totalWins = user.totalWins + if (won) 1 else 0,
                totalMatches = user.totalMatches + 1
            )
        )
    }

    suspend fun deleteUser(user: User) = dao.delete(user)

    suspend fun resetAllScores() = dao.resetAllScores()

    suspend fun nameExists(name: String): Boolean = dao.countByName(name) > 0
}
