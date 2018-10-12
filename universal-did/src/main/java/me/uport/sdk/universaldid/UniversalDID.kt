package me.uport.sdk.universaldid

import android.support.annotation.VisibleForTesting
import android.support.annotation.VisibleForTesting.PRIVATE
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

/**
 * A class to abstract resolving Decentralized Identity (DID) documents
 * from specific implementations based on the [method] component of a DID [String]
 *
 * [DIDResolver] implementations need to be registered using [registerResolver]
 *
 * Known implementations of [DIDResolver] are [ethr-did] and [uport-did]
 */
object UniversalDID : DIDResolver {


    private val resolvers = mapOf<String, DIDResolver>().toMutableMap()

    /**
     * Register a resolver for a particular DID [method]
     */
    fun registerResolver(resolver: DIDResolver) {
        if (resolver.method.isBlank()) {
            return
        }
        resolvers[resolver.method] = resolver
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun clearResolvers() = resolvers.clear()

    override val method: String = ""

    /**
     * checks if any of the registered resolvers can resolve
     */
    override fun canResolve(potentialDID: String): Boolean {
        val resolver = resolvers.values.find {
            it.canResolve(potentialDID)
        }
        return (resolver != null)
    }

    override suspend fun resolve(did: String): DIDDocument {
        val (method, _) = parse(did)

        if (method.isBlank()) {
            val resolver = resolvers.values.find {
                it.canResolve(did)
            }
            return resolver?.resolve(did)
                    ?: throw IllegalArgumentException("The provided did ($did) could not be resolved by any of the ${resolvers.size} registered resolvers")
        }  //no else clause, carry on

        if (resolvers.containsKey(method)) {
            return resolvers[method]?.resolve(did) ?: throw IllegalStateException("There DIDResolver for '$method' failed to resolve '$did' for an unknown reason.")
        } else {
            throw IllegalStateException("There is no DIDResolver registered to resolve '$method' DIDs and none of the other ${resolvers.size} registered ones can do it.")
        }
    }

    @VisibleForTesting(otherwise = PRIVATE)
    internal fun parse(did: String): Pair<String, String> {
        val matchResult = didPattern.find(did) ?: return ("" to "")
        val (method, identifier) = matchResult.destructured
        return (method to identifier)
    }

    //language=RegExp
    private val didPattern = "^did:(.*?):(.+)".toRegex()
}

/**
 * Abstraction of various methods of resolving DIDs
 *
 * Each resolver should know the [method] it is supposed to resolve
 * and implement a [resolve] coroutine to eventually return a [DIDDocument] or throw an error
 */
interface DIDResolver {
    /**
     * The DID method that a particular implementation can resolve
     */
    val method: String

    /**
     * Resolve a given [did] in a coroutine and return the [DIDDocument] or throw an error
     */
    suspend fun resolve(did: String): DIDDocument

    /**
     * Check if the [potentialDID] can be resolved by this resolver.
     */
    fun canResolve(potentialDID: String): Boolean
}

/**
 * Abstraction for DID documents
 */
interface DIDDocument {

    companion object {
        val blank = object : DIDDocument {}
    }
}