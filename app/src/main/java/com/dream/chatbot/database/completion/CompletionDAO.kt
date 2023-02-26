package com.dream.chatbot.database.completion

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CompletionDAO {
    //we do not provide an updateMethod because it is unnecessary.

    @Query("SELECT *  FROM CompletionData ORDER BY ID")
    fun getAll() : List<CompletionEntity>

    @Insert
    fun insertOne(singleMessage: CompletionEntity)

    @Delete
    fun deleteOne(request: CompletionEntity)
}