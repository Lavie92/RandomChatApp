package com.lavie.randochat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DatabaseReference
import com.lavie.randochat.repository.MatchRepository
import com.lavie.randochat.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MatchViewModel(
    private val matchRepository: MatchRepository,
) : ViewModel() {

    sealed class MatchState {
        object Idle : MatchState()
        object Matching : MatchState()
        data class Matched(val roomId: String, val partnerId: String) : MatchState()
        data class Error(val messageResId: Int) : MatchState()
    }

    private val _matchState = MutableStateFlow<MatchState>(MatchState.Idle)
    val matchState: StateFlow<MatchState> = _matchState

    fun startMatching(myUserId: String) {
        _matchState.value = MatchState.Matching

        viewModelScope.launch {
            val result = matchRepository.findUserForMatch(myUserId)

            _matchState.value = when (result) {
                is MatchRepository.MatchResult.Matched -> {
                    MatchState.Matched(result.roomId, result.otherUserId)
                }
                is MatchRepository.MatchResult.Waiting -> {
                    MatchState.Matching
                }
                is MatchRepository.MatchResult.Error -> {
                    MatchState.Error(result.messageId ?: R.string.match_failed)
                }

                MatchRepository.MatchResult.Cancelled -> {
                    TODO()
                }
            }
        }
    }

    fun resetState() {
        _matchState.value = MatchState.Idle
    }
}
