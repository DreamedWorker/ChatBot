package com.dream.chatbot.database.python

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "PythonTranslation")
data class LanguageEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,

    @ColumnInfo(name = "python_language", typeAffinity = ColumnInfo.TEXT)
    val context: String?,

    @ColumnInfo(name = "nature_language", typeAffinity = ColumnInfo.TEXT)
    val translation: String?,

    @ColumnInfo(name = "time", typeAffinity = ColumnInfo.TEXT)
    val time: String?
)
