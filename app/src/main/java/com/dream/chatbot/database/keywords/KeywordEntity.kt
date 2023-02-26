package com.dream.chatbot.database.keywords

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "KeywordData")
data class KeywordEntity (

    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,

    @ColumnInfo(name = "long_sentences", typeAffinity = ColumnInfo.TEXT)
    val sentences: String?,

    @ColumnInfo(name = "human_requirement", typeAffinity = ColumnInfo.TEXT)
    val keywords: String?,

    @ColumnInfo(name = "time", typeAffinity = ColumnInfo.TEXT)
    val time: String?
)