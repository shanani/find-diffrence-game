package com.oddduel.game.data

/** Either a persisted account or a throwaway guest - both can play a duel the same way. */
sealed class PlayerProfile {
    abstract val key: String
    abstract val displayName: String
    abstract val avatarIndex: Int

    data class Registered(val user: User) : PlayerProfile() {
        override val key get() = "user_${user.id}"
        override val displayName get() = user.name
        override val avatarIndex get() = user.avatarIndex
    }

    data class Guest(val label: String, override val avatarIndex: Int) : PlayerProfile() {
        override val key get() = "guest_$label"
        override val displayName get() = label
    }
}
