package com.dream.chatbot.database.python

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LanguageDAO {

    @Query("SELECT *  FROM PythonTranslation ORDER BY ID")
    fun getAll(): List<LanguageEntity>

    @Insert
    fun insertOne(entity: LanguageEntity)

    @Delete
    fun deleteOne(entity: LanguageEntity)
}