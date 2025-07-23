package com.lavie.randochat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.ValueEventListener
import com.lavie.randochat.repository.MatchRepository
import com.lavie.randochat.R
import com.lavie.randochat.utils.ChatType
import com.lavie.randochat.utils.Constants
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MatchViewModel(
    private val matchRepository: MatchRepository,
    private val chatViewModel: ChatViewModel,
) : ViewModel() {

    private var timeoutJob: Job? = null

    sealed class MatchState {
        object Idle : MatchState()
        object Matching : MatchState()
        data class Matched(val roomId: String, val partnerId: String) : MatchState()
        data class Error(val messageResId: Int) : MatchState()
        object Waiting : MatchState()
    }

    private val _matchState = MutableStateFlow<MatchState>(MatchState.Idle)
    val matchState: StateFlow<MatchState> = _matchState

    private var matchedListener: ValueEventListener? = null
    private var currentUserId: String? = null

    fun startMatching(myUserId: String, chatType: ChatType) {
        _matchState.value = MatchState.Matching
        currentUserId = myUserId

        timeoutJob?.cancel()

        listenMatchNotifications(myUserId)

        viewModelScope.launch {
            when (val result = matchRepository.findUserForMatch(myUserId, chatType)) {
                is MatchRepository.MatchResult.Matched -> {
                    _matchState.value = MatchState.Matched(result.roomId, result.otherUserId)
                    chatViewModel.sendWelcomeMessage(result.roomId)
                    removeMatchedListener()
                }
                is MatchRepository.MatchResult.Waiting -> {
                    _matchState.value = MatchState.Waiting

                    timeoutJob = viewModelScope.launch {
                        delay(Constants.MATCH_TIMEOUT)

                        if (_matchState.value is MatchState.Waiting) {
                            cancelWaiting()
                            _matchState.value = MatchState.Error(R.string.match_timeout)
                        }
                    }
                }
                is MatchRepository.MatchResult.Error -> {
                    _matchState.value = MatchState.Error(result.messageId ?: R.string.match_failed)

                    timeoutJob?.cancel()
                    removeMatchedListener()
                }
                MatchRepository.MatchResult.Cancelled -> {
                    removeMatchedListener()
                }
            }
        }
    }

    private fun listenMatchNotifications(userId: String) {
        removeMatchedListener()
        matchedListener = matchRepository.observeMatchedUser(userId) { matched, matchedWith, roomId ->
            if (matched && matchedWith != null && roomId != null) {
                _matchState.value = MatchState.Matched(roomId, matchedWith)
                removeMatchedListener()
            }
        }
    }

    fun cancelWaiting() {
        currentUserId?.let { userId ->
            removeMatchedListener()
            timeoutJob?.cancel()
            viewModelScope.launch {
                matchRepository.cancelWaiting(userId)
                _matchState.value = MatchState.Idle
            }
        }
    }

    private fun removeMatchedListener() {
        currentUserId?.let { userId ->
            matchedListener?.let {
                matchRepository.removeMatchedListener(userId, it)
                matchedListener = null
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timeoutJob?.cancel()
        removeMatchedListener()
    }
}