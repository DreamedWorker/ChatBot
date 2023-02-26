package com.dream.chatbot.database.python

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dream.chatbot.database.keywords.KeywordDAO
import com.dream.chatbot.database.keywords.KeywordEntity

@Database(entities = [LanguageEntity::class], version = 1, exportSchema = false)
abstract class LanguageDatabase : RoomDatabase() {
    abstract fun getLanguageDAO(): LanguageDAO
}