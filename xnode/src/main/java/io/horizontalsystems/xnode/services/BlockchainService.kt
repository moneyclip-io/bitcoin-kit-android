package io.horizontalsystems.xnode.services

import io.horizontalsystems.xnode.models.BlockchainData

class BlockchainService {

    private var storageService: StorageService? = null

    init {
        storageService = StorageService()
        storageService?.initConnection()

        storageService?.save(BlockchainData("hash1", 1))
        storageService?.save(BlockchainData("hash2", 2))
        storageService?.save(BlockchainData("hash3", 3))
        storageService?.save(BlockchainData("hash4", 4))

        println("Out 1:${storageService?.getBlockchainData("hash1")}")
        println("Out 2:${storageService?.getBlockchainData("hash2")}")
    }
}