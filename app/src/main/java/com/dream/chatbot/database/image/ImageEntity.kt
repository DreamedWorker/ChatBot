package com.dream.chatbot.database.image

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ImageData")
data class ImageEntity (

    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,

    @ColumnInfo(name = "message_sender", typeAffinity = ColumnInfo.TEXT)
    val sender: String?,

    @ColumnInfo(name = "human_requirement", typeAffinity = ColumnInfo.TEXT)
    val context: String?,

    @ColumnInfo(name = "image_url", typeAffinity = ColumnInfo.TEXT)
    val imageUrl: String?
)