package com.lavie.randochat.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.lavie.randochat.R
import com.lavie.randochat.model.ChatRoom
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

    override suspend fun signInWithGoogle(idToken: String): UserResult? {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()

            Timber.d("Sign in successful: ${result.user?.uid}")

            result.user?.let { firebaseUser ->
                UserResult.Success(
                    User(
                        id = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        nickname = firebaseUser.displayName ?: "",
                        isOnline = true
                    )
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Sign in failed")

            if (isNetworkError(e)) {
                UserResult.Error(R.string.network_error)
            } else {
                UserResult.Error(R.string.login_error)
            }
        }
    }

    override suspend fun saveUserToDb(user: User): UserResult {
        var retryCount = 0
        val maxRetries = 3
        var delayTime = 1000L

        while (retryCount < maxRetries) {
            try {
                FirebaseDatabase.getInstance().goOnline()

                val userSnapshot = withTimeout(Constants.SAVE_USER_TIMEOUT) {
                    database.child(Constants.USERS).child(user.id).get().await()
                }

                val userMap = userSnapshot.value as? Map<*, *>
                val isDisabled = userMap?.get(Constants.IS_DISABLED) as? Boolean ?: user.isDisabled

                if (isDisabled) {
                    firebaseAuth.signOut()
                    return UserResult.Error(R.string.account_locked_full)
                }

                val userMapToSave = mapOf(
                    Constants.ID to user.id,
                    Constants.EMAIL to user.email,
                    Constants.NICKNAME to user.nickname,
                    Constants.IS_ONLINE to user.isOnline,
                    Constants.LAST_UPDATED to System.currentTimeMillis(),
                    Constants.IS_DISABLED to isDisabled
                )

                withTimeout(Constants.SAVE_USER_TIMEOUT) {
                    database.child(Constants.USERS).child(user.id).setValue(userMapToSave).await()
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

                when {
                    isNetworkError(e) && retryCount < maxRetries -> {
                        delay(delayTime)
                        delayTime *= 2
                    }

                    retryCount >= maxRetries -> {
                        return UserResult.Error(R.string.please_check_connection)
                    }

                    else -> {
                        Timber.e(e)
                    }
                }
            }
        }

        return UserResult.Error(R.string.save_user_data_failed)
    }

    override suspend fun checkUserValid(): UserResult {
        val user = firebaseAuth.currentUser
        if (user != null) {
            try {
                withTimeout(Constants.RELOAD_USER_TIMEOUT) { user.reload().await() }

                val userSnapshot = withTimeout(Constants.RELOAD_USER_TIMEOUT) {
                    database.child(Constants.USERS).child(user.uid).get().await()
                }

                if (userSnapshot.exists()) {
                    val userMap = userSnapshot.value as? Map<*, *>
                    val isDisabled = userMap?.get(Constants.IS_DISABLED) as? Boolean ?: false

                    if (isDisabled) {
                        firebaseAuth.signOut()
                        return UserResult.Error(R.string.account_locked_full)
                    }

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

                    return UserResult.Error(R.string.login_error)
                }

                return UserResult.Error(R.string.network_error)
            }
        }

        return UserResult.Error(null)
    }

    override suspend fun getActiveRoomForUser(userId: String): ChatRoom? {
        val snapshot = database.child(Constants.CHAT_ROOMS)
            .orderByChild(Constants.ACTIVE)
            .equalTo(true)
            .get()
            .await()
        for (room in snapshot.children) {
            val chatRoom = room.getValue(ChatRoom::class.java)
            if (chatRoom != null && chatRoom.participantIds.contains(userId)) {
                return chatRoom
            }
        }

        return null
    }

    override suspend fun registerWithEmail(email: String, password: String): UserRepository.UserResult {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                Timber.d("Register success: ${firebaseUser.uid}")

                val userMap = mapOf(
                    "id" to firebaseUser.uid,
                    "email" to email,
                    "nickname" to "",
                    "isOnline" to true,
                    "lastUpdated" to System.currentTimeMillis(),
                    "isDisabled" to false
                )

                database.child(Constants.USERS).child(firebaseUser.uid).setValue(userMap).await()

                UserResult.Success(
                    User(
                        id = firebaseUser.uid,
                        email = email,
                        nickname = "",
                        isOnline = true
                    )
                )
            } else {
                UserResult.Error(R.string.account_not_exist)
            }
        } catch (e: FirebaseAuthUserCollisionException) {
            Timber.e(e, "Email already registered")
            UserResult.Error(R.string.account_already_registered)
        } catch (e: Exception) {
            Timber.e(e, "Register failed")
            UserResult.Error(R.string.login_error)
        }
    }


    override suspend fun loginWithEmail(email: String, password: String): UserResult? {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                val user = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    nickname = firebaseUser.displayName ?: "",
                    isOnline = true
                )
                UserResult.Success(user)
            } else {
                UserResult.Error(R.string.login_error)
            }
        } catch (e: Exception) {
            Timber.e(e, "Login with email failed")
            if (isNetworkError(e)) {
                UserResult.Error(R.string.network_error)
            } else {
                UserResult.Error(R.string.login_error)
            }
        }
    }


}