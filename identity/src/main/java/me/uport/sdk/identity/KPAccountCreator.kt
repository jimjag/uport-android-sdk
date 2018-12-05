package me.uport.sdk.identity

import android.content.Context
import com.uport.sdk.signer.UportHDSigner
import com.uport.sdk.signer.UportHDSigner.Companion.GENERIC_DEVICE_KEY_DERIVATION_PATH
import com.uport.sdk.signer.computeAddressForPath
import com.uport.sdk.signer.createHDSeed
import com.uport.sdk.signer.encryption.KeyProtection
import com.uport.sdk.signer.importHDSeed
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.uport.sdk.core.UI

class KPAccountCreator(private val appContext: Context) : AccountCreator {

    val signer = UportHDSigner()

    private fun createOrImportAccount(networkId: String, phrase: String?, callback: AccountCreatorCallback) {
        GlobalScope.launch {
            try {
                val (handle, _) = if (phrase.isNullOrBlank()) {
                    signer.createHDSeed(appContext, KeyProtection.Level.SIMPLE)
                } else {
                    signer.importHDSeed(appContext, KeyProtection.Level.SIMPLE, phrase)
                }
                val (deviceAddress, _) = signer.computeAddressForPath(appContext,
                        handle,
                        GENERIC_DEVICE_KEY_DERIVATION_PATH,
                        "")
                val account = Account(
                        handle,
                        deviceAddress,
                        networkId,
                        deviceAddress,
                        "",
                        "",
                        "",
                        AccountType.KeyPair
                )

                launch(UI) { callback(null, account) }
            } catch (err: Exception) {
                launch(UI) { callback(err, Account.blank) }
            }

        }
    }

    override fun createAccount(networkId: String, forceRestart: Boolean, callback: AccountCreatorCallback) {
        createOrImportAccount(networkId, null, callback)
    }

    override fun importAccount(networkId: String, seedPhrase: String, forceRestart: Boolean, callback: AccountCreatorCallback) {
        createOrImportAccount(networkId, seedPhrase, callback)
    }

    override fun deleteAccount(handle: String) {
        signer.deleteSeed(appContext, handle)
    }

}