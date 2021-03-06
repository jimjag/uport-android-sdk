package me.uport.sdk.core

import android.support.annotation.VisibleForTesting
import android.support.annotation.VisibleForTesting.PRIVATE
import kotlinx.coroutines.Dispatchers
import org.kethereum.extensions.toHexStringNoPrefix
import org.spongycastle.util.encoders.Base64
import org.walleth.khex.clean0xPrefix
import org.walleth.khex.prepend0xPrefix
import java.math.BigInteger
import java.nio.charset.Charset
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.Dispatchers.Main as mainLooperContext

/**
 * Shorthand for a mockable UI context in unit tests
 */
val UI by lazy { coroutineUiContextInitBlock() }

private var coroutineUiContextInitBlock: () -> CoroutineContext = { Dispatchers.Main }

/**
 * Call this in @Before methods where you need to interact with UI context
 */
@VisibleForTesting(otherwise = PRIVATE)
fun stubUiContext() {
    coroutineUiContextInitBlock = { EmptyCoroutineContext }
}

//using spongy castle implementation because the android one can't be mocked in tests
/**
 * Creates a base64 representation of the given byteArray, without padding
 */
fun ByteArray.toBase64(): String = Base64.toBase64String(this).replace("=", "")

/**
 * Creates a base64 representation of the byteArray that backs this string, without padding
 */
fun String.toBase64() = this.toByteArray().toBase64()

/**
 * pads a base64 string with a proper number of '='
 */
fun String.padBase64() = this.padEnd(this.length + (4 - this.length % 4) % 4, '=')

fun String.toBase64UrlSafe() = this.toBase64().replace('+', '-').replace('/', '_')
fun ByteArray.toBase64UrlSafe() = this.toBase64().replace('+', '-').replace('/', '_')

fun String.decodeBase64(): ByteArray = this
        //force non-url safe and add padding so that it can be applied to all b64 formats
        .replace('-', '+')
        .replace('_', '/')
        .padBase64()
        .let {
            if (it.isEmpty())
                byteArrayOf()
            else
                Base64.decode(it)
        }


fun String.hexToBytes32() = clean0xPrefix().padStart(64, '0').prepend0xPrefix()
fun BigInteger.toBytes32String() = toHexStringNoPrefix().padStart(64, '0').prepend0xPrefix()

val utf8: Charset = Charset.forName("UTF-8")
fun ByteArray.bytes32ToString() = this.toString(utf8)