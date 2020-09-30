package io.horizontalsystems.xnode.server

import io.horizontalsystems.xnode.services.BlockchainService
import java.util.logging.Level
import java.util.logging.Logger
import io.horizontalsystems.bitcoinkit.BitcoinKit


fun main() {
    Logger.getLogger("").level = Level.SEVERE

    XNodeServer()
}

class XNodeServer {
    init {
        BlockchainService()
    }
}

