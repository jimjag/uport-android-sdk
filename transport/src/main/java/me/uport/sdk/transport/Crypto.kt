package me.uport.sdk.transport

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON
import me.uport.knacl.nacl
import me.uport.sdk.core.decodeBase64
import me.uport.sdk.core.toBase64

/**
 * This class exposes methods to encrypt and decrypt messages according to the uPort spec at
 * https://github.com/uport-project/specs/blob/develop/messages/encryption.md
 */
object Crypto {

    /**
     * This class encapsulates an encrypted message that was produced using
     * https://github.com/uport-project/specs/blob/develop/messages/encryption.md
     */
    @Serializable
    data class EncryptedMessage(
            @SerialName("version")
            val version: String = ASYNC_ENC_ALGORITHM,
            @SerialName("nonce")
            val nonce: String,
            @SerialName("ephemPublicKey")
            val ephemPublicKey: String,
            @SerialName("ciphertext")
            val ciphertext: String
    ) {
        fun toJson(): String = JSON.stringify(EncryptedMessage.serializer(), this)

        companion object {
            fun fromJson(json: String): EncryptedMessage? = JSON.nonstrict.parse(EncryptedMessage.serializer(), json)
        }
    }

    /**
     * Calculates the publicKey usable for encryption corresponding to the given [secretKey]
     */
    fun getEncryptionPublicKey(secretKey: ByteArray): ByteArray {
        val (pk, _) = nacl.boxKeyPairFromSecretKey(secretKey)
        return pk
    }

    /**
     *  Encrypts a message
     *
     *  @param message the plaintext message to be encrypted
     *  @param boxPub  the public encryption key of the receiver, encoded as a base64 [String]
     *  @return an [EncryptedMessage] containing a `version`, `nonce`, `ephemPublicKey` and `ciphertext`
     */
    fun encryptMessage(message: String, boxPub: String): EncryptedMessage {

        val (publicKey, secretKey) = nacl.boxKeyPair()
        val nonce = nacl.randomBytes(nacl.crypto_box_NONCEBYTES)
        val padded = message.pad()
        val ciphertext = nacl.box(padded.toByteArray(Charsets.UTF_8), nonce, boxPub.decodeBase64(), secretKey)
        return EncryptedMessage(
                version = ASYNC_ENC_ALGORITHM,
                nonce = nonce.toBase64(),
                ephemPublicKey = publicKey.toBase64(),
                ciphertext = ciphertext.toBase64())
    }


    /**
     *  Decrypts a message
     *
     *  @param encrypted The [EncryptedMessage] containing `version`, `nonce`, `ephemPublicKey` and `ciphertext`
     *  @param secretKey The secret key as a [ByteArray]
     *  @return The decrypted plaintext [String]
     */
    fun decryptMessage(encrypted: EncryptedMessage, secretKey: ByteArray): String {
        if (encrypted.version != ASYNC_ENC_ALGORITHM) throw IllegalArgumentException("Unsupported encryption algorithm: ${encrypted.version}")
        if (encrypted.ciphertext.isBlank() || encrypted.nonce.isBlank() || encrypted.ephemPublicKey.isBlank()) throw IllegalArgumentException("Invalid encrypted message")
        val decrypted = nacl.boxOpen(
                encrypted.ciphertext.decodeBase64(),
                encrypted.nonce.decodeBase64(),
                encrypted.ephemPublicKey.decodeBase64(),
                secretKey) ?: throw RuntimeException("Could not decrypt message")
        return decrypted.toString(Charsets.UTF_8).unpad()
    }

    private const val ASYNC_ENC_ALGORITHM = "x25519-xsalsa20-poly1305"

}