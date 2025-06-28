package com.lavie.randochat.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.lavie.randochat.model.User
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val database: DatabaseReference
) : UserRepository {

    override suspend fun signInWithGoogle(idToken: String): User? {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()

            result.user?.let { firebaseUser ->
                User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun saveUserToDb(user: User) {
        try {
            database.child("users").child(user.id).setValue(user).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getCurrentUser(): User? {
        val user = FirebaseAuth.getInstance().currentUser

        return user?.let {
            User(id = it.uid, email = it.email ?: "")
        }
    }

    override suspend fun checkUserValid(): User? {
        val user = firebaseAuth.currentUser
        if (user != null) {
            try {
                user.reload().await()
                return firebaseAuth.currentUser?.let { User(it.uid, it.email ?: "") }
            } catch (e: Exception) {
                firebaseAuth.signOut()
            }
        }
        return null
    }
}