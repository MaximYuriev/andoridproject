package com.example.messanger

import adapters.ChatActionListener
import adapters.ChatAdapter
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import request.Chat
import request.Client
import kotlin.concurrent.fixedRateTimer

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val client = Client
        val intent = Intent(this, AuthActivity::class.java)
        if (!client.checkSession()){
            startActivity(intent)
            finish()
        }
        if (client.checkSession() and !client.checkUserId())
            lifecycleScope.launch { client.getUserId() }
        val intentChat = Intent(this, ConcreteChatActivity::class.java)
        val userChatsList: RecyclerView = findViewById(R.id.userChats)
        val addButton: FloatingActionButton = findViewById(R.id.addNewChat)
        var chats: MutableList<Chat>
        val adapter = ChatAdapter(object : ChatActionListener{
            override fun onChatGetId(chat: Chat) {
                intentChat.putExtra("chatId", chat.chatId)
                intentChat.putExtra("chatName", chat.fullname)
                startActivity(intentChat)
            }
        })
        val manager = LinearLayoutManager(this)
        userChatsList.layoutManager = manager
        if (client.checkSession())
            lifecycleScope.launch {
                chats = Client.getUserChatList()
                adapter.data = chats
            }
        userChatsList.adapter = adapter
        addButton.setOnClickListener{
            val dialogBinding = layoutInflater.inflate(R.layout.dialog, null)
            val dialog = Dialog(this)
            dialog.setContentView(dialogBinding)
            dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            dialog.show()
            val addChat: Button = dialogBinding.findViewById(R.id.createChat)
            val username: EditText = dialogBinding.findViewById(R.id.username)
            val response: TextView = dialogBinding.findViewById(R.id.response)
            addChat.setOnClickListener{
                if (username.text.toString() == "")
                    response.text = "Имя пользователя должно быть задано!"
                else
                    lifecycleScope.launch{
                        val createChatResponse = client.createChat(username.text.toString())
                        if (createChatResponse["status"] != HttpStatusCode.OK)
                            response.text = createChatResponse["detail"].toString()
                        else
                            recreate()
                    }
            }
        }
        fixedRateTimer("timer", false, 0L, 3000){
            this@MainActivity.runOnUiThread {
                if (client.checkSession())
                    lifecycleScope.launch {
                        chats = Client.getUserChatList()
                        adapter.data = chats
                    }
            }
        }
    }

}