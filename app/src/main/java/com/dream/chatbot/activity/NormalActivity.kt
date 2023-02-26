package com.dream.chatbot.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.dream.chatbot.GlobalData
import com.dream.chatbot.R
import com.dream.chatbot.adapter.CompletionAdapter
import com.dream.chatbot.database.completion.CompletionDAO
import com.dream.chatbot.database.completion.CompletionDatabase
import com.dream.chatbot.database.completion.CompletionEntity
import com.dream.chatbot.databinding.ActivityNormalChatBinding
import com.dream.chatbot.openai.CompletionPart

class NormalActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNormalChatBinding
    private lateinit var db: CompletionDatabase
    private lateinit var mDao: CompletionDAO
    private lateinit var mList: ArrayList<CompletionEntity>
    private lateinit var mKey: String
    private lateinit var mAdapter: CompletionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNormalChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.normalToolbar)
        val lastMessage = intent.getStringExtra("src")!!
        mKey = if (lastMessage == "developer"){
            GlobalData.apiKey
        } else {
            lastMessage
        }
        loadHistory()
        askQuestion()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_normal, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.normal_settings -> {
                startActivity(Intent(applicationContext, SettingsActivity::class.java))
            }
            R.id.normal_image -> {
                val intent = Intent(applicationContext, ImageActivity::class.java)
                intent.putExtra("APIKEY", mKey)
                startActivity(intent)
            }
            R.id.normal_py2zh -> {
                val intent = Intent(applicationContext, PythonTranslationActivity::class.java)
                intent.putExtra("APIKEY", mKey)
                startActivity(intent)
            }
            R.id.normal_keywords -> {
                val intent = Intent(applicationContext, KeywordActivity::class.java)
                intent.putExtra("APIKEY", mKey)
                startActivity(intent)
            }
            R.id.normal_about -> {
                startActivity(Intent(applicationContext, AboutActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun askQuestion() {
        val configData = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        binding.normalAsk.setOnClickListener{
            if (binding.normalQuestion.text.toString().isNotEmpty()){
                Toast.makeText(applicationContext, "请求已发出，等待响应。", Toast.LENGTH_SHORT).show()
                Thread{
                    val human = CompletionEntity(sender = "man", chatContext = binding.normalQuestion.text.toString())
                    mDao.insertOne(human)
                    val aiAnswer = CompletionPart.askQuestion(binding.normalQuestion.text.toString(),
                    configData.getString("gpt_model", "text-davinci-003")!!, configData.getString("gpt_length", "512")!!.toInt(), mKey)
                    mDao.insertOne(aiAnswer)
                    runOnUiThread{
                        mAdapter.insertData(human)
                        mAdapter.insertData(aiAnswer)
                        binding.normalStage.smoothScrollToPosition(mAdapter.itemCount - 1)
                        Toast.makeText(applicationContext, "响应完成", Toast.LENGTH_SHORT).show()
                        if (configData.getBoolean("clear_input", false)){
                            binding.normalQuestion.setText("")
                        }
                    }
                }.start()
            }
        }
    }

    private fun loadHistory() {//operate database in a new Thread
        Thread{
            db = Room.databaseBuilder(applicationContext, CompletionDatabase::class.java, "db_completion").build()
            mDao = db.completionDAO()
            mList = ArrayList()
            val oldList = mDao.getAll()
            for (singleData in oldList){
                mList.add(singleData)
            }
            runOnUiThread {
                mAdapter = CompletionAdapter(mList)
                mAdapter.setOnCompletionSelectedListener(object : CompletionAdapter.OnCompletionSelectedListener{
                    override fun onCompletionSelectedListener(position: Int) {
                        val selectedEntity = mAdapter.queryData(position)
                        val clipManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val mData = ClipData.newPlainText("Label", selectedEntity.chatContext!!)
                        clipManager.setPrimaryClip(mData)
                        Toast.makeText(applicationContext, "已将此内容复制到剪贴板上", Toast.LENGTH_SHORT).show()
                    }
                })
                val linear = LinearLayoutManager(applicationContext)
                binding.normalStage.layoutManager = linear
                binding.normalStage.addItemDecoration(DividerItemDecoration(applicationContext,
                    DividerItemDecoration.VERTICAL))
                binding.normalStage.itemAnimator = DefaultItemAnimator()
                binding.normalStage.adapter = mAdapter
            }
        }.start()
    }
}