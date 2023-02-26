package com.dream.chatbot.database.keywords

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [KeywordEntity::class], version = 1, exportSchema = false)
abstract class KeywordDatabase : RoomDatabase(){
    abstract fun getKeywordDAO(): KeywordDAO
}