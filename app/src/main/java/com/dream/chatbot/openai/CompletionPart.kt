package com.dream.chatbot.openai

import cn.hutool.http.ContentType
import cn.hutool.http.Header
import cn.hutool.http.HttpRequest
import cn.hutool.http.HttpResponse
import com.alibaba.fastjson.JSONObject
import com.dream.chatbot.GlobalData
import com.dream.chatbot.database.completion.CompletionEntity

object CompletionPart {

    fun askQuestion(question: String, model: String, token: Int, mKey: String): CompletionEntity {
        val requestJSON = JSONObject()
        requestJSON["model"] = model
        requestJSON["prompt"] = question
        requestJSON["max_tokens"] = token
        requestJSON["temperature"] = 0
        val response: HttpResponse = HttpRequest
            .post(GlobalData.chatGPTUrl)
            .body(requestJSON.toJSONString())
            .header(Header.AUTHORIZATION.value, "Bearer $mKey")
            .header(Header.CONTENT_TYPE.value, ContentType.JSON.value)
            .execute()
        val body = response.body()
        val preData = JSONObject.parseObject(body).getJSONArray("choices").getJSONObject(0)
        return CompletionEntity(sender = "ai", chatContext = preData.getString("text"))
    }
}