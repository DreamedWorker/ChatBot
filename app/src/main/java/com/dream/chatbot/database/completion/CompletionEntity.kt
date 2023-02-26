package com.dream.chatbot.database.completion

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CompletionData")
data class CompletionEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,

    @ColumnInfo(name = "message_sender", typeAffinity = ColumnInfo.TEXT)
    val sender: String?,

    @ColumnInfo(name = "chat_context", typeAffinity = ColumnInfo.TEXT)
    val chatContext: String?
)
