package com.dream.chatbot.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dream.chatbot.R
import com.dream.chatbot.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.loginToolbar)

        loadConfig()
//        binding.button.setOnClickListener {
//            Thread{
//                val question = Completion()
//                question.setPrompt("你能预测一下100年后的人类生活是怎么样的吗？")
//                question.setModel("text-davinci-003")
//                println(JSONUtil.toJsonStr(question))
//                val response: HttpResponse = HttpRequest
//                    .post(GlobalData.chatGPTUrl)
//                    .body("{\"model\":\"text-davinci-003\",\"prompt\":\"你能预测一下100年后的人类生活是怎么样的吗？\",\"max_tokens\":512,\"temperature\":0}")
//                    .header(Header.AUTHORIZATION.getValue(), "Bearer " + GlobalData.apiKey)
//                    .header(Header.CONTENT_TYPE.getValue(), ContentType.JSON.getValue())
//                    .execute()
//                val body = response.body()
//                println(body)
//            }.start()
//        }
        events()
    }

    private fun loadConfig() {
        val config = getSharedPreferences("global", MODE_PRIVATE)
        val isRemember = config.getBoolean("remember", false)
        binding.loginRemember.isChecked = isRemember
        if (isRemember){
            binding.loginKey.setText(config.getString("apiKey", ""))
        }
    }

    private fun events() {
        binding.loginSubmit.setOnClickListener {
            if (binding.loginKey.text.toString().isNotEmpty()){
                val intent = Intent(applicationContext, NormalActivity::class.java)
                if (binding.loginRemember.isChecked){
                    val sharedData = getSharedPreferences("global", MODE_PRIVATE).edit()
                    sharedData.putString("apiKey", binding.loginKey.text.toString())
                    sharedData.putBoolean("remember", false)
                    sharedData.apply()
                    intent.putExtra("src", binding.loginKey.text.toString())
                    startActivity(intent)
                    finish()
                } else {
                    intent.putExtra("src", binding.loginKey.text.toString())
                    startActivity(intent)
                    finish()
                }
            } else {
                Toast.makeText(applicationContext, resources.getText(R.string.login_key_empty), Toast.LENGTH_SHORT).show()
            }
        }
        binding.button3.setOnClickListener {
            val intent = Intent(applicationContext, NormalActivity::class.java)
            intent.putExtra("src", "developer")
            startActivity(intent)
            finish()
        }
    }
}