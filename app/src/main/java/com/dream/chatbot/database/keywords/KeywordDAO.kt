package com.dream.chatbot.database.keywords

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface KeywordDAO {

    @Query("SELECT *  FROM KeywordData ORDER BY ID")
    fun getAll(): List<KeywordEntity>

    @Insert
    fun insertOne(entity: KeywordEntity)

    @Delete
    fun deleteOne(entity: KeywordEntity)
}