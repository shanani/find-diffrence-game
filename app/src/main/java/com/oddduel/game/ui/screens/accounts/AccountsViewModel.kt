package com.oddduel.game.ui.screens.accounts

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oddduel.game.data.User
import com.oddduel.game.data.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccountsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = UserRepository(app)

    val users: StateFlow<List<User>> = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var errorMessage: String? by mutableStateOf(null)
        private set

    fun createUser(name: String, avatarIndex: Int, onCreated: (User) -> Unit) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            errorMessage = "Enter a name"
            return
        }
        viewModelScope.launch {
            if (repo.nameExists(trimmed)) {
                errorMessage = "That name is already taken"
                return@launch
            }
            errorMessage = null
            val user = repo.createUser(trimmed, avatarIndex)
            onCreated(user)
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch { repo.deleteUser(user) }
    }
}
