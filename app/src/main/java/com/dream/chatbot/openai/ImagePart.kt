package com.dream.chatbot.openai

import cn.hutool.http.ContentType
import cn.hutool.http.Header
import cn.hutool.http.HttpRequest
import cn.hutool.http.HttpResponse
import com.alibaba.fastjson.JSONObject
import com.dream.chatbot.GlobalData
import com.dream.chatbot.database.image.ImageEntity

object ImagePart {

    fun requirePicture(requiredSentence: String, size: String, mKey: String): ImageEntity {
        val response: HttpResponse = HttpRequest
            .post(GlobalData.chatImageUrl)
            .body("{\"prompt\":\"$requiredSentence\",\"n\":1,\"size\":\"$size\"}")
            .header(Header.AUTHORIZATION.value, "Bearer $mKey")
            .header(Header.CONTENT_TYPE.value, ContentType.JSON.value)
            .execute()
        val body = response.body()
        val preData = JSONObject.parseObject(body).getJSONArray("data").getJSONObject(0)
        return ImageEntity(sender = "ai", imageUrl = preData.getString("url"), context = requiredSentence)
    }
}