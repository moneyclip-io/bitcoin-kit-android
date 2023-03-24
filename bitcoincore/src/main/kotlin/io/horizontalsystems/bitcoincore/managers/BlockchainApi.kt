package io.horizontalsystems.bitcoincore.managers

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import io.horizontalsystems.bitcoincore.core.IInitialSyncApi
import java.util.logging.Logger

class BlockchainApi(host: String, authKey: String) : IInitialSyncApi {
    private val apiManager = ApiManager(host)
    private val jwtApiManager = ApiManager(host)
    private val authKey = authKey
    private val logger = Logger.getLogger("BlockchainApi")

    override fun getTransactions(addresses: List<String>): List<TransactionItem> {

        val jwtResponse = jwtApiManager.get("authentication/requestJWT", authKey).asObject()

        val jwt = jwtResponse.get("data").asObject().get("token").asString()

        val requestData = JsonObject().apply {
            this["addresses"] = Json.array(*addresses.toTypedArray())
        }

        logger.info("Request transactions for ${addresses.size} addresses: [${addresses.first()}, ...]")

        val response = apiManager.post("bitcoin/testnet/getTransactions", requestData.toString(), jwt).asArray()

        logger.info("Got ${response.size()} transactions for requested addresses")

        val transactions = mutableListOf<TransactionItem>()

        for (txItem in response) {
            val tx = txItem.asObject()

            val blockHashJson = tx["block"] ?: continue
            val blockHash = if (blockHashJson.isString) blockHashJson.asString() else continue

            val outputs = mutableListOf<TransactionOutputItem>()
            for (outputItem in tx["outputs"].asArray()) {
                val outputJson = outputItem.asObject()

                val scriptJson = outputJson["script"] ?: continue
                val addressJson = outputJson["address"] ?: continue

                if (scriptJson.isString && addressJson.isString) {
                    outputs.add(TransactionOutputItem(scriptJson.asString(), addressJson.asString()))
                }
            }

            transactions.add(TransactionItem(blockHash, tx["height"].asInt(), outputs))
        }

        return transactions
    }


}
