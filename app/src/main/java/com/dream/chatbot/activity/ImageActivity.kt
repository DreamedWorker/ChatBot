package com.dream.chatbot.activity

import android.app.DownloadManager
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.dream.chatbot.R
import com.dream.chatbot.adapter.ImageAdapter
import com.dream.chatbot.database.image.ImageDAO
import com.dream.chatbot.database.image.ImageDatabase
import com.dream.chatbot.database.image.ImageEntity
import com.dream.chatbot.databinding.ActivityNormalChatBinding
import com.dream.chatbot.openai.ImagePart
import com.dream.chatbot.utils.FileHandle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions

class ImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNormalChatBinding
    private lateinit var db: ImageDatabase
    private lateinit var mDao: ImageDAO
    private lateinit var mKey: String
    private lateinit var downloadLocation: String
    private lateinit var configData: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNormalChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.normalToolbar)
        mKey = intent.getStringExtra("APIKEY")!!
        configData = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        downloadLocation = configData.getString("image_download", "inner")!!
        changeUI()
        requirePermission()
        loadHistory()
        drawPicture(configData)
    }

    private fun changeUI() {
        //we share the same layout file in this activity, so the text should be changed.
        binding.normalAsk.text = resources.getText(R.string.image_submit)
        binding.normalQuestionContainer.hint = resources.getText(R.string.image_tip)
        binding.normalQuestionContainer.helperText = resources.getText(R.string.image_hint)
    }

    private fun requirePermission() {
        //we will download images to your disk, so we require permission here.
        if (!XXPermissions.isGranted(this, Permission.READ_MEDIA_IMAGES)) {
            XXPermissions.with(this)
                .permission(Permission.READ_MEDIA_IMAGES)
                .request { _, allGranted ->
                    if (!allGranted) {
                        Toast.makeText(
                            applicationContext,
                            "有权限尚未授予，此模块的下载功能可能不可用。如需授权，请前往软件权限设置页授权！",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    //--------------------------------------------------------------------------------------------//
    private fun loadHistory() {
        Thread {
            db = Room.databaseBuilder(applicationContext, ImageDatabase::class.java, "db_image")
                .build()
            mDao = db.imageDAO()
            val oldList = mDao.getAll()
            runOnUiThread { updateView(oldList) }
        }.start()
    }

    private fun updateView(newList: List<ImageEntity>){
        val newAdapter = ImageAdapter(newList, applicationContext)
        newAdapter.setOnDownloadSelectedListener(object : ImageAdapter.OnDownloadSelectedListener{
            override fun onDownloaderSelectedListener(position: Int) {
                MaterialAlertDialogBuilder(this@ImageActivity)
                    .setTitle("提示")
                    .setMessage("要删除此图像${newList[position].context}的数据吗？")
                    .setPositiveButton("确认"){_,_ ->
                        deleteSelectedItem(newList[position])
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }
        })
        val linear = LinearLayoutManager(applicationContext)
        binding.normalStage.layoutManager = linear
        binding.normalStage.addItemDecoration(
            DividerItemDecoration(
                applicationContext,
                DividerItemDecoration.VERTICAL
            )
        )
        binding.normalStage.itemAnimator = DefaultItemAnimator()
        binding.normalStage.adapter = newAdapter
        try {
            binding.normalStage.smoothScrollToPosition(newAdapter.itemCount - 1)
        } catch (e: Exception){
            Toast.makeText(applicationContext, "自动滚动失败，当前视图可能没有任何内容！", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteSelectedItem(item: ImageEntity){
        Thread{
            mDao.deleteOne(item)
            val newList = mDao.getAll()
            runOnUiThread {
                updateView(newList)
                if (configData.getBoolean("delete_sync", false)){
                    val innerFile = "${applicationContext.getExternalFilesDir("images")}/${item.context}.jpg"
                    val outerFile = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path}/ChatBot/${item.context}.jpg"
                    try {
                        FileHandle.deleteFile(innerFile)
                    } catch (e: Exception){
                        FileHandle.deleteFile(outerFile)
                    }
                }
            }
        }.start()
    }

    private fun drawPicture(configData: SharedPreferences) {
        binding.normalAsk.setOnClickListener {
            if (binding.normalQuestion.text.toString().isNotEmpty()) {
                Toast.makeText(applicationContext, "请求已发出，等待响应。", Toast.LENGTH_SHORT).show()
                Thread {
                    val result = ImagePart.requirePicture(
                        binding.normalQuestion.text.toString(),
                        configData.getString("image_definition", "256x256")!!,
                        mKey
                    )
                    mDao.insertOne(result)
                    downloadPicture(result)
                    val newList = mDao.getAll()
                    runOnUiThread {
                        updateView(newList)
                        Toast.makeText(applicationContext, "响应完成", Toast.LENGTH_SHORT).show()
                        if (configData.getBoolean("clear_input", false)) {
                            binding.normalQuestion.setText("")
                        }
                    }
                }.start()
            }
        }
    }

    private fun downloadPicture(url: ImageEntity) {
        val downloadRequire = DownloadManager.Request(Uri.parse(url.imageUrl!!))
        when(downloadLocation){
            "inner" -> {
                downloadRequire.setDestinationInExternalFilesDir(applicationContext,
                    "/images", "/${url.context!!}.jpg")
            }
            "outer" -> {
                downloadRequire.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_PICTURES, "ChatBot/${url.context!!}.jpg"
                )
            }
        }
        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(downloadRequire)
    }
}