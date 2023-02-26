package com.dream.chatbot.database.completion

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CompletionEntity::class], version = 1, exportSchema = false)
abstract class CompletionDatabase : RoomDatabase() {
    abstract fun completionDAO() : CompletionDAO
}