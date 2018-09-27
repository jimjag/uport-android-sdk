package me.uport.sdk.core

import org.kethereum.functions.encodeRLP
import org.kethereum.model.SignatureData
import org.kethereum.model.Transaction
import kotlin.coroutines.experimental.suspendCoroutine

/**
 * Callback type for signature results.
 */
typealias SignatureCallback = (err: Exception?, sigData: SignatureData) -> Unit

/**
 * An interface used to sign transactions or messages for uport specific operations
 */
interface Signer {

    fun signETH(rawMessage: ByteArray, callback: SignatureCallback)
    fun signJWT(rawPayload: ByteArray, callback: SignatureCallback)

    fun getAddress() : String

    fun signRawTx(
            unsignedTx: Transaction,
            callback: (err: Exception?,
                       signedEncodedTransaction: ByteArray) -> Unit) = signETH(unsignedTx.encodeRLP())
    { err, sig ->
        if (err != null) {
            return@signETH callback(err, byteArrayOf())
        }
        return@signETH callback(null, unsignedTx.encodeRLP(sig))
    }

    companion object {
        /**
         * A useless signer that calls back with empty signature and has no associated address
         */
        val blank = object : Signer {
            override fun signETH(rawMessage: ByteArray, callback: SignatureCallback)  = callback(null, SignatureData())
            override fun signJWT(rawPayload: ByteArray, callback: SignatureCallback)  = callback(null, SignatureData())
            override fun getAddress(): String = ""
        }
    }
}


////////////////////////////////////////////////////
// signer extensions - wrap callbacks as coroutines
////////////////////////////////////////////////////

suspend fun Signer.signRawTx(unsignedTx: Transaction): ByteArray = suspendCoroutine { continuation ->
    this.signRawTx(unsignedTx) { err, signedEncodedTransaction ->
        if (err != null) {
            continuation.resumeWithException(err)
        } else {
            continuation.resume(signedEncodedTransaction)
        }
    }
}

suspend fun Signer.signETH(rawMessage: ByteArray): SignatureData = suspendCoroutine { continuation ->
    this.signETH(rawMessage) { err, sigData ->
        if (err != null) {
            continuation.resumeWithException(err)
        } else {
            continuation.resume(sigData)
        }
    }
}

suspend fun Signer.signJWT(rawMessage: ByteArray): SignatureData = suspendCoroutine { continuation ->
    this.signJWT(rawMessage) { err, sigData ->
        if (err != null) {
            continuation.resumeWithException(err)
        } else {
            continuation.resume(sigData)
        }
    }
}