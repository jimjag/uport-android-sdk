package me.uport.sdk.identity

import android.support.annotation.Keep

@Keep
enum class AccountType {
    KeyPair,
    MetaIdentityManager,
    Proxy,
    Device,
    IdentityManager,
}