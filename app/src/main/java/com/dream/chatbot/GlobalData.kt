package com.dream.chatbot

import android.content.Context
import androidx.room.Room
import com.dream.chatbot.database.python.LanguageDatabase

object GlobalData {
    const val chatGPTUrl = "https://api.openai.com/v1/completions"
    const val chatImageUrl = "https://api.openai.com/v1/images/generations"
    const val apiKey = "sk-abcdefg"
    lateinit var languageDatabase: LanguageDatabase

    fun initDatabase(mContext: Context){
        languageDatabase = Room.databaseBuilder(mContext, LanguageDatabase::class.java, "db_python").build()
    }
}