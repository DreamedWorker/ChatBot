package com.dream.chatbot.database.image

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ImageDAO {
    @Query("SELECT *  FROM ImageData ORDER BY ID")
    fun getAll() : List<ImageEntity>

    @Insert
    fun insertOne(imageEntity: ImageEntity)

    @Delete
    fun deleteOne(imageEntity: ImageEntity)
}