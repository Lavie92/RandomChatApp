package com.lavie.randochat.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.lavie.randochat.R
import com.lavie.randochat.model.User
import com.lavie.randochat.utils.Constants
import com.lavie.randochat.utils.isNetworkError
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import com.lavie.randochat.repository.UserRepository.UserResult

class UserRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val database: DatabaseReference
) : UserRepository {

    override suspend fun signInWithGoogle(idToken: String): User? {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()

            Timber.d("Sign in successful: ${result.user?.uid}")

            result.user?.let { firebaseUser ->
                User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    nickname = firebaseUser.displayName ?: "",
                    isOnline = true
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Sign in failed")
            null
        }
    }

    override suspend fun saveUserToDb(user: User): UserResult {
        var retryCount = 0
        val maxRetries = 3
        var delayTime = 1000L

        while (retryCount < maxRetries) {
            try {
                FirebaseDatabase.getInstance().goOnline()

                val disabledCheck = checkUserDisabled(user.id, Constants.SAVE_USER_TIMEOUT)
                if (disabledCheck != null) {
                    return disabledCheck
                }

                val existingUserSnapshot = withTimeout(Constants.SAVE_USER_TIMEOUT) {
                    database.child(Constants.USERS).child(user.id).get().await()
                }

                val isDisabled = if (existingUserSnapshot.exists()) {
                    val existingUserMap = existingUserSnapshot.value as? Map<*, *>
                    existingUserMap?.get(Constants.IS_DISABLED) as? Boolean ?: false
                } else {
                    user.isDisabled
                }

                val userMap = mapOf(
                    Constants.ID to user.id,
                    Constants.EMAIL to user.email,
                    Constants.NICKNAME to user.nickname,
                    Constants.IS_ONLINE to user.isOnline,
                    Constants.LAST_UPDATED to System.currentTimeMillis(),
                    Constants.IS_DISABLED to isDisabled
                )

                withTimeout(Constants.SAVE_USER_TIMEOUT) {
                    database.child(Constants.USERS).child(user.id).setValue(userMap).await()
                }

                return UserResult.Success(user.copy(isDisabled = isDisabled))

            } catch (e: TimeoutCancellationException) {
                retryCount++

                if (retryCount >= maxRetries) {
                    return UserResult.Error(R.string.save_user_data_failed)
                }

                delay(delayTime)
                delayTime *= 2

            } catch (e: Exception) {
                retryCount++

                if (isNetworkError(e) && retryCount < maxRetries) {
                    delay(delayTime)
                    delayTime *= 2
                } else {
                    if (retryCount >= maxRetries) {
                        return UserResult.Error(R.string.please_check_connection)
                    } else {
                        Timber.e(e)
                    }
                }
            }
        }

        return UserResult.Error(R.string.save_user_data_failed)
    }

    override fun getCurrentUser(): User? {
        val user = firebaseAuth.currentUser

        return user?.let {
            User(
                id = it.uid,
                email = it.email ?: "",
                nickname = it.displayName ?: "",
                isOnline = true
            )
        }
    }

    override suspend fun checkUserValid(): UserResult {
        val user = firebaseAuth.currentUser
        if (user != null) {
            try {
                withTimeout(Constants.RELOAD_USER_TIMEOUT) { user.reload().await() }

                val disabledCheck = checkUserDisabled(user.uid, Constants.RELOAD_USER_TIMEOUT)
                if (disabledCheck != null) {
                    return disabledCheck
                }

                val userSnapshot = withTimeout(Constants.RELOAD_USER_TIMEOUT) {
                    database.child(Constants.USERS).child(user.uid).get().await()
                }

                if (userSnapshot.exists()) {
                    val userMap = userSnapshot.value as? Map<*, *>
                    val isDisabled = userMap?.get(Constants.IS_DISABLED) as? Boolean ?: false

                    return UserResult.Success(
                        User(
                            id = user.uid,
                            email = userMap?.get(Constants.EMAIL) as? String ?: "",
                            nickname = userMap?.get(Constants.NICKNAME) as? String ?: "",
                            isOnline = userMap?.get(Constants.IS_ONLINE) as? Boolean ?: true,
                            isDisabled = isDisabled
                        )
                    )
                } else {
                    firebaseAuth.signOut()
                    return UserResult.Error(R.string.account_not_exist)
                }
            } catch (e: Exception) {
                Timber.e(e, "User validation failed")
                if (!isNetworkError(e)) {
                    firebaseAuth.signOut()
                }
                return UserResult.Error(R.string.network_error)
            }
        }

        return UserResult.Error(R.string.account_not_exist)
    }

    private suspend fun checkUserDisabled(userId: String, timeout: Long): UserResult? {
        return try {
            val userSnapshot = withTimeout(timeout) {
                database.child(Constants.USERS).child(userId).get().await()
            }

            if (userSnapshot.exists()) {
                val userMap = userSnapshot.value as? Map<*, *>
                val isDisabled = userMap?.get(Constants.IS_DISABLED) as? Boolean ?: false

                if (isDisabled) {
                    firebaseAuth.signOut()
                    return UserResult.Error(R.string.account_locked_full)
                }
            }

            null
        } catch (e: Exception) {
            throw e
        }
    }
}