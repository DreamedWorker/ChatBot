package com.dream.chatbot.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager.LayoutParams
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import cn.hutool.http.ContentType
import cn.hutool.http.Header
import cn.hutool.http.HttpRequest
import cn.hutool.http.HttpResponse
import com.alibaba.fastjson.JSONObject
import com.dream.chatbot.GlobalData
import com.dream.chatbot.adapter.KeywordAdapter
import com.dream.chatbot.database.keywords.KeywordDAO
import com.dream.chatbot.database.keywords.KeywordDatabase
import com.dream.chatbot.database.keywords.KeywordEntity
import com.dream.chatbot.databinding.ActivityPythonTranslationBinding
import com.dream.chatbot.databinding.WindowKeywordsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class KeywordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPythonTranslationBinding
    private lateinit var mKey: String
    private lateinit var mDao: KeywordDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPythonTranslationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.py2zhToolbar)
        mKey = intent.getStringExtra("APIKEY")!!
        loadHistory()
        askQuestion()
    }

    private fun loadHistory(){
        Thread{
            val db = Room.databaseBuilder(applicationContext, KeywordDatabase::class.java, "db_keywords").build()
            mDao = db.getKeywordDAO()
            val oldList = mDao.getAll()
            runOnUiThread { updateView(oldList, false) }
        }.start()
    }

    private fun askQuestion(){
        binding.py2zhAdd.setOnClickListener {
            val requiredSentences = EditText(this@KeywordActivity)
            requiredSentences.textSize = 14f
            MaterialAlertDialogBuilder(this@KeywordActivity)
                .setTitle("提示")
                .setMessage("输入要提炼关键词的句子")
                .setView(requiredSentences)
                .setNegativeButton("取消", null)
                .setPositiveButton("确认"){_, _ ->
                    Thread{
                        mDao.insertOne(requireKeywords(requiredSentences.text.toString()))
                        val newList = mDao.getAll()
                        runOnUiThread { updateView(newList, true) }
                    }.start()
                }.show()
        }
    }

    private fun updateView(newList: List<KeywordEntity>, needScroll: Boolean){
        val mAdapter = KeywordAdapter(newList)
        mAdapter.setOnKeywordClickListener(object : KeywordAdapter.OnKeywordClickListener{
            override fun onKeywordClickListener(position: Int) {
                val targetEntity = newList[position]
                val windowContainer = WindowKeywordsBinding.inflate(layoutInflater)
                val window = PopupWindow(windowContainer.root,LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, false)
                windowContainer.keywordsOrigin.text = targetEntity.sentences
                windowContainer.keywordsContainer.text = targetEntity.keywords
                windowContainer.keywordsCopy.setOnClickListener {
                    val manager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clipData = ClipData.newPlainText("Label", targetEntity.keywords!!)
                    manager.setPrimaryClip(clipData)
                    Toast.makeText(applicationContext, "已将内容复制到剪贴板上", Toast.LENGTH_SHORT).show()
                    window.dismiss()
                }
                windowContainer.keywordsBack.setOnClickListener { window.dismiss() }
                windowContainer.keywordsDelete.setOnClickListener {
                    deleteItem(targetEntity)
                }
                window.showAtLocation(binding.root, Gravity.FILL, 0, 0)
            }
        })
        val linear = LinearLayoutManager(applicationContext)
        binding.py2zhList.layoutManager = linear
        binding.py2zhList.addItemDecoration(
            DividerItemDecoration(
                applicationContext,
                DividerItemDecoration.VERTICAL
            )
        )
        binding.py2zhList.itemAnimator = DefaultItemAnimator()
        binding.py2zhList.adapter = mAdapter
        if (needScroll) {
            try {
                binding.py2zhList.smoothScrollToPosition(mAdapter.itemCount - 1)
            } catch (e: Exception) {
                Toast.makeText(applicationContext, "自动滚动失败，当前视图可能没有任何内容！", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun deleteItem(entity: KeywordEntity){
        Thread{
            mDao.deleteOne(entity)
            val newList = mDao.getAll()
            runOnUiThread {
                updateView(newList, false)
                Toast.makeText(applicationContext, "删除完成", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    private fun requireKeywords(sentences: String): KeywordEntity{
        val jsonNeeded = JSONObject()
        jsonNeeded["model"] = "text-davinci-003"
        jsonNeeded["prompt"] = "从下面的文段中提炼出关键词：\n\n$sentences"
        jsonNeeded["temperature"] = 0.5
        jsonNeeded["max_tokens"] = 60
        jsonNeeded["top_p"] = 1.0
        jsonNeeded["frequency_penalty"] = 0.8
        jsonNeeded["presence_penalty"] = 0.0
        val response: HttpResponse = HttpRequest
            .post(GlobalData.chatGPTUrl)
            .body(jsonNeeded.toJSONString())
            .header(Header.AUTHORIZATION.value, "Bearer $mKey")
            .header(Header.CONTENT_TYPE.value, ContentType.JSON.value)
            .execute()
        val body = response.body()
        val jsonData = JSONObject.parseObject(body)
        return KeywordEntity(
            sentences = sentences,
            keywords = jsonData.getJSONArray("choices").getJSONObject(0).getString("text"),
            time = System.currentTimeMillis().toString()
        )
    }

}