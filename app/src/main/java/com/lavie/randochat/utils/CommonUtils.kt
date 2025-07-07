package com.lavie.randochat.utils

import android.util.Base64
import kotlinx.coroutines.TimeoutCancellationException
import timber.log.Timber
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CommonUtils {
    //NETWORK
    fun isNetworkError(exception: Exception): Boolean {

        val message = exception.message?.lowercase() ?: ""

        return message.contains("network") ||
                message.contains("timeout") ||
                message.contains("connection") ||
                message.contains("unreachable") ||
                exception is TimeoutCancellationException
    }

    // ENCRYPTION
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val IV_LENGTH = 16

    fun generateMessageKey(roomId: String, userId: String): String {
        val input = "$roomId:$userId"
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray())
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    fun encryptMessage(plainText: String, key: String): String {
        return try {
            val keyBytes = Base64.decode(key, Base64.NO_WRAP)
            val secretKeySpec = SecretKeySpec(keyBytes, "AES")

            val iv = ByteArray(IV_LENGTH)
            SecureRandom().nextBytes(iv)
            val ivParameterSpec = IvParameterSpec(iv)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)

            val encryptedBytes = cipher.doFinal(plainText.toByteArray())

            val combined = iv + encryptedBytes
            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            Timber.e("Cannot Encrypt Message: ${e.message}")

            plainText
        }
    }

    fun decryptMessage(encryptedText: String, key: String): String {
        return try {
            val keyBytes = Base64.decode(key, Base64.NO_WRAP)
            val secretKeySpec = SecretKeySpec(keyBytes, "AES")

            val combined = Base64.decode(encryptedText, Base64.NO_WRAP)

            val iv = combined.sliceArray(0 until IV_LENGTH)
            val encryptedBytes = combined.sliceArray(IV_LENGTH until combined.size)

            val ivParameterSpec = IvParameterSpec(iv)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes)
        } catch (e: Exception) {
            Timber.e("Cannot Decrypt Message: ${e.message}")

            encryptedText
        }
    }

    //INPUT VALIDATOR
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
    private val PASSWORD_REGEX = Regex("^(?=.*[A-Za-z])(?=.*\\d).{6,}$")

    fun isValidEmail(email: String): Boolean {
        return EMAIL_REGEX.matches(email)
    }

    fun isValidPassword(password: String): Boolean {
        return PASSWORD_REGEX.matches(password)
    }
}

