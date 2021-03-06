package me.uport.sdk.uportdid

import android.support.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import me.uport.sdk.serialization.moshi
import me.uport.sdk.universaldid.*
import me.uport.sdk.uportdid.UportDIDResolver.Companion.parseDIDString
import org.walleth.khex.clean0xPrefix

/**
 * A class that encapsulates the DID document
 *
 * See [identity_document spec](https://github.com/uport-project/specs/blob/develop/pki/identitydocument.md)
 *
 */
data class UportIdentityDocument(
        @Json(name = "@context")
        val context: String?, //ex: "http://schema.org"

        @Json(name = "@type")
        val type: String, //ex: "Organization", "Person"

        @Json(name = "publicKey")
        val publicKey: String?,  //ex: "0x04613bb3a4874d27032618f020614c21cbe4c4e4781687525f6674089f9bd3d6c7f6eb13569053d31715a3ba32e0b791b97922af6387f087d6b5548c06944ab062"

        @Json(name = "publicEncKey")
        val publicEncKey: String?,  //ex: "0x04613bb3a4874d27032618f020614c21cbe4c4e4781687525f6674089f9bd3d6c7f6eb13569053d31715a3ba32e0b791b97922af6387f087d6b5548c06944ab062"

        @Json(name = "image")
        val image: ProfilePicture?,     //ex: {"@type":"ImageObject","name":"avatar","contentUrl":"/ipfs/QmSCnmXC91Arz2gj934Ce4DeR7d9fULWRepjzGMX6SSazB"}

        @Json(name = "name")
        val name: String?, //ex: "uPort @ Devcon3" , "Vitalik Buterout"

        @Json(name = "description")
        val description: String? // ex: "uPort Attestation"
) {

    fun convertToDIDDocument(did: String): DIDDocument {

        val normalizedDid = normalizeDID(did)

        val publicVerificationKey = PublicKeyEntry(
                id = "$normalizedDid#keys-1",
                type = DelegateType.Secp256k1VerificationKey2018,
                owner = normalizedDid,
                publicKeyHex = this.publicKey?.clean0xPrefix()
        )
        val authEntries = listOf(AuthenticationEntry(
                type = DelegateType.Secp256k1SignatureAuthentication2018,
                publicKey = "$normalizedDid#keys-1")
        )

        val pkEntries = listOf(publicVerificationKey).toMutableList()

        if (publicEncKey != null) {
            pkEntries.add(PublicKeyEntry(
                    id = "$normalizedDid#keys-2",
                    type = DelegateType.Curve25519EncryptionPublicKey,
                    owner = normalizedDid,
                    publicKeyBase64 = publicEncKey)
            )
        }

        return UportDIDDocument(
                context = "https://w3id.org/did/v1",
                id = normalizedDid,
                publicKey = pkEntries,
                authentication = authEntries,
                uportProfile = copy(
                        context = null,
                        publicEncKey = null,
                        publicKey = null
                ))
    }

    private fun normalizeDID(did: String): String {
        val (_, mnid) = parseDIDString(did)
        return "did:uport:$mnid"
    }

    fun toJson(): String = jsonAdapter.toJson(this)

    companion object {
        private val jsonAdapter: JsonAdapter<UportIdentityDocument> by lazy {
            moshi.adapter(UportIdentityDocument::class.java)
        }

        fun fromJson(json: String): UportIdentityDocument? = jsonAdapter.fromJson(json)
    }
}

class ProfilePicture(
        @Json(name = "@type")
        val type: String? = "ImageObject",

        @Json(name = "name")
        val name: String? = "avatar",

        @Json(name = "contentUrl")
        val contentUrl: String? = ""
)

@Keep
data class UportDIDDocument(
        override val id: String,
        override val publicKey: List<PublicKeyEntry>,
        override val authentication: List<AuthenticationEntry>,
        override val service: List<ServiceEntry> = emptyList(),

        @Json(name = "@context")
        override val context: String = "https://w3id.org/did/v1",

        val uportProfile: UportIdentityDocument
) : DIDDocument