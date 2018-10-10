package me.uport.sdk.did

import me.uport.mnid.Account
import me.uport.sdk.jsonrpc.EthCall
import org.junit.Assert.assertEquals
import org.junit.Test

class DIDResolverTest {

    //TODO: add tests that call mock server instead of actually calling IPFS and ETH nets

    @Test
    @Throws(Exception::class)
    fun encapsulates_json_rpc() {

        val expectedPayload = "{\"method\":\"eth_call\",\"params\":[{\"to\":\"0xaddress\",\"data\":\"some0xdatastring\"},\"latest\"],\"id\":1,\"jsonrpc\":\"2.0\"}"

        val payload = EthCall("0xaddress", "some0xdatastring").toJsonRpc()

        assertEquals(expectedPayload, payload)
    }

    @Test
    fun encodes_eth_call() {
        val expectedEncoding = "0x447885f075506f727450726f66696c654950465331323230000000000000000000000000000000000000000000000000f12c30cd32b4a027710c150ae742f50db0749213000000000000000000000000f12c30cd32b4a027710c150ae742f50db0749213"
        val acc = Account.from(network = "0x04", address = "0xf12c30cd32b4a027710c150ae742f50db0749213")
        val encoding = DIDResolver().encodeRegistryFunctionCall("uPortProfileIPFS1220", acc, acc)

        assertEquals(expectedEncoding, encoding)
    }

    @Test
    fun calls_registry() {

        val expectedDocAddress = "QmWzBDtv8m21ph1aM57yVDWxdG7LdQd3rNf5xrRiiV2D2E"
        val docAddressHex = DIDResolver().getIpfsHashSync("2ozs2ntCXceKkAQKX4c9xp2zPS8pvkJhVqC")

        assertEquals(expectedDocAddress, docAddressHex)
    }

    @Test
    fun getJsonDIDSync() {

        val expectedDDO = DDO(
                context = "http://schema.org",
                type = "Person",
                publicKey = "0x04e8989d1826cd6258906cfaa71126e2db675eaef47ddeb9310ee10db69b339ab960649e1934dc1e1eac1a193a94bd7dc5542befc5f7339845265ea839b9cbe56f",
                publicEncKey = "k8q5G4YoIMP7zvqMC9q84i7xUBins6dXGt8g5H007F0=",
                description = null,
                image = null,
                name = null
        )

        val ddo = DIDResolver().getProfileDocumentSync("2ozs2ntCXceKkAQKX4c9xp2zPS8pvkJhVqC")

        assertEquals(expectedDDO, ddo)
    }


}