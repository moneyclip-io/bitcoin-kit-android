package io.horizontalsystems.xnode.services

import com.google.gson.Gson
import com.mongodb.*
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters.eq
import io.horizontalsystems.xnode.models.BlockchainData
import org.bson.Document
import java.util.logging.Logger


class StorageService (){

    private var mongoClient: MongoClient? = null
    private var db:  MongoDatabase? = null

    fun initConnection(){
        try {
            mongoClient = MongoClient("127.0.0.1", 27017)
            db = mongoClient?.getDatabase("temp")
            db?.let {
                try{
                    it.getCollection("blockchain")
                } catch (e: IllegalArgumentException){
                    it.createCollection("blockchain")
                }
            }

        } catch (e: MongoException) {
            e.printStackTrace()
        }
    }

    fun stopConnection(){
        mongoClient?.close()
    }

    fun save(data: BlockchainData){
        try {
            val document = db?.getCollection("blockchain")
            document?.insertOne(Document.parse(Gson().toJson(data)))

        }catch (e: Exception){
            Logger.getLogger("").severe("Error when saving to db:${e}")
        }
    }

    fun getBlockchainData(hash: String): String {
        val document = db?.getCollection("blockchain")
        val data = document?.find(eq("hash", hash))
        return Gson().toJson(data?.first())
    }

}