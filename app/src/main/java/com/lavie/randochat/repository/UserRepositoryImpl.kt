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

    override suspend fun saveUserToDb(user: User): SaveUserResult {
        var retryCount = 0
        val maxRetries = 3
        var delayTime = 1000L

        while (retryCount < maxRetries) {
            try {

                FirebaseDatabase.getInstance().goOnline()

                val userMap = mapOf(
                    "id" to user.id,
                    "email" to user.email,
                    "nickname" to user.nickname,
                    "isOnline" to user.isOnline,
                    "lastUpdated" to System.currentTimeMillis()
                )

                withTimeout(Constants.SAVE_USER_TIMEOUT) {
                    database.child("users").child(user.id).setValue(userMap).await()
                }

                return SaveUserResult.Success

            } catch (e: TimeoutCancellationException) {
                retryCount++

                if (retryCount >= maxRetries) {
                    return SaveUserResult.Error(R.string.save_user_data_failed)
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
                        return SaveUserResult.Error(R.string.please_check_connection)
                    } else {
                        Timber.e(e)
                    }
                }
            }
        }

        return  SaveUserResult.Error(R.string.save_user_data_failed)
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

    override suspend fun checkUserValid(): User? {
        val user = firebaseAuth.currentUser

        if (user != null) {
            try {

                withTimeout(Constants.RELOAD_USER_TIMEOUT) {
                    user.reload().await()
                }

                return firebaseAuth.currentUser?.let {
                    User(
                        id = it.uid,
                        email = it.email ?: "",
                        nickname = it.displayName ?: "",
                        isOnline = true
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "User validation failed")

                if (!isNetworkError(e)) {
                    firebaseAuth.signOut()
                }
            }
        }

        return null
    }

    sealed class SaveUserResult {
        data object Success : SaveUserResult()
        data class Error(val messageId: Int) : SaveUserResult()
    }
}