package com.dream.chatbot.activity

import android.content.ClipDescription
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
import com.dream.chatbot.adapter.PythonAdapter
import com.dream.chatbot.database.python.LanguageDAO
import com.dream.chatbot.database.python.LanguageDatabase
import com.dream.chatbot.database.python.LanguageEntity
import com.dream.chatbot.databinding.ActivityPythonTranslationBinding
import com.dream.chatbot.databinding.WindowShowCodeBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PythonTranslationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPythonTranslationBinding
    private lateinit var mKey: String
    private lateinit var mDao: LanguageDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPythonTranslationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.py2zhToolbar)
        mKey = intent.getStringExtra("APIKEY")!!
        loadHistory()
        requireExplain()
    }

    private fun loadHistory() {
        Thread {
//            val db =
//                Room.databaseBuilder(applicationContext, LanguageDatabase::class.java, "db_python")
//                    .build()
            GlobalData.initDatabase(applicationContext)
            mDao = GlobalData.languageDatabase.getLanguageDAO()
            val oldList = mDao.getAll()
            runOnUiThread { updateView(oldList, false) }
        }.start()
    }

    private fun updateView(newList: List<LanguageEntity>, needScroll: Boolean) {
        val mAdapter = PythonAdapter(newList)
        mAdapter.setOnCodeRecordClickListener(object : PythonAdapter.OnCodeRecordClickListener{
            override fun onCodeRecordClickListener(position: Int) {
                val selectedEntity = newList[position]
                val detailWindow = WindowShowCodeBinding.inflate(layoutInflater)
                val window = PopupWindow(detailWindow.root, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true)
                detailWindow.py2zhWindowQuestion.text = selectedEntity.context
                detailWindow.py2zhWindowAnswer.text = selectedEntity.translation
                window.showAtLocation(binding.root, Gravity.FILL, 0, 0)
            }
        })
        mAdapter.setOnCodeLongClickListener(object : PythonAdapter.OnCodeLongClickListener{
            override fun onCodeLongClickListener(position: Int) {
                val targetEntity = newList[position]
                MaterialAlertDialogBuilder(this@PythonTranslationActivity)
                    .setTitle("提示")
                    .setMessage("要删除标题为「${targetEntity.time}」的条目吗？")
                    .setNegativeButton("取消", null)
                    .setPositiveButton("确认"){_, _ ->
                        deleteEntity(targetEntity)
                        Toast.makeText(applicationContext, "条目删除完成", Toast.LENGTH_SHORT).show()
                    }
                    .show()
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

    private fun requireExplain() {
        binding.py2zhAdd.setOnClickListener {
            val codeInput = EditText(this@PythonTranslationActivity)
            codeInput.textSize = 14f
            MaterialAlertDialogBuilder(this@PythonTranslationActivity)
                .setTitle("提示")
                .setMessage("你可以在下方输入代码，也可以将剪贴板中位置为0的内容导入以进行解析。")
                .setView(codeInput)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定") { _, _ ->
                    Thread {
                        val result = pythonMeaning(codeInput.text.toString())
                        mDao.insertOne(result)
                        val newList = mDao.getAll()
                        runOnUiThread { updateView(newList, true) }
                    }.start()
                }
                .setNeutralButton("从剪贴板导入"){_, _ ->
                    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    if (clipboardManager.hasPrimaryClip() && clipboardManager.primaryClipDescription?.hasMimeType(
                            ClipDescription.MIMETYPE_TEXT_PLAIN) == true) {
                        val clipData = clipboardManager.primaryClip
                        val text = clipData?.getItemAt(0)?.text?.toString()
                        Thread {
                            val result = pythonMeaning(text!!)
                            mDao.insertOne(result)
                            val newList = mDao.getAll()
                            runOnUiThread { updateView(newList, true) }
                        }.start()
                    }
                }
                .show()
        }
    }

    private fun deleteEntity(entity: LanguageEntity){
        Thread{
            mDao.deleteOne(entity)
            val newList = mDao.getAll()
            runOnUiThread { updateView(newList, true) }
        }.start()
    }

    private fun pythonMeaning(code: String): LanguageEntity {
        val jsonNeeded = JSONObject()
        jsonNeeded["model"] = "text-davinci-003"
        jsonNeeded["prompt"] = "解释下面提供的代码：\n${code}"
        jsonNeeded["temperature"] = 0
        jsonNeeded["max_tokens"] = 256
        jsonNeeded["top_p"] = 1.0
        jsonNeeded["frequency_penalty"] = 0.0
        jsonNeeded["presence_penalty"] = 0.0
        val response: HttpResponse = HttpRequest
            .post(GlobalData.chatGPTUrl)
            .body(jsonNeeded.toJSONString())
            .header(Header.AUTHORIZATION.value, "Bearer $mKey")
            .header(Header.CONTENT_TYPE.value, ContentType.JSON.value)
            .execute()
        val body = response.body()
        val preData = JSONObject.parseObject(body).getJSONArray("choices").getJSONObject(0)
        return LanguageEntity(context = code, translation = preData.getString("text"), time = System.currentTimeMillis().toString())
    }
}