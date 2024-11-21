package com.example.messanger

import adapters.MessageAdapter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject
import request.Client
import request.Message
import java.util.concurrent.TimeUnit


class ConcreteChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_concrete_chat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        getSupportActionBar()?.hide();
        val mainHandler = Handler(Looper.getMainLooper())
        val bundle = intent.extras
        val chatName = bundle?.getString("chatName")
        val chatId = bundle?.getInt("chatId")
        val chatNameToolBar: TextView = findViewById(R.id.chatName)
        chatNameToolBar.text = chatName

        val client = Client

        val adapter = MessageAdapter()
        val manager = LinearLayoutManager(this)
        val messagesList: RecyclerView = findViewById(R.id.messages)
        messagesList.layoutManager = manager
        var messages:MutableList<Message> = mutableListOf()
        messagesList.adapter = adapter
        lifecycleScope.launch {
            messages = Client.getAllMessages(chatId!!)
            adapter.data = messages
            messagesList.scrollToPosition(adapter.itemCount - 1)
        }

        val wsClient = OkHttpClient.Builder()
            .readTimeout(3, TimeUnit.SECONDS)
            .build()
        val request = Request.Builder()
            .url("ws://192.168.0.104:8000/chat/ws/$chatId/${client.userId}")
            .build()
        val wsListener = EchoWebSocketListener(object : MessageListener{
            override fun recieveMessage(message: Message) {
                messages.add(message)
                mainHandler.post {
                    adapter.notifyItemInserted(messages.size - 1)
                    messagesList.scrollToPosition(adapter.itemCount - 1)
                }
            }
        })
        val websocket = wsClient.newWebSocket(request, wsListener)
        val goBackButton: ImageButton = findViewById(R.id.goBack)
        goBackButton.setOnClickListener{
            websocket.close(1000,"")
            finish()
        }
        val message: EditText = findViewById(R.id.messageContent)
        val sendTextMessageButton: ImageButton = findViewById(R.id.sendMessages)
        sendTextMessageButton.setOnClickListener{
            lifecycleScope.launch {
                client.sendMessage(chatId!!, message.text.toString().trim())
                message.text.clear()
            }
        }
    }
}

interface MessageListener{
    fun recieveMessage(message: Message)
}

private class EchoWebSocketListener(private val messageListener: MessageListener)
    : WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) {
        println("open")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        val json = JSONObject(text)
        val message = Message(content = json.get("content").toString(),
            messageId = null,
            senderId = json.get("sender_id").toString().toInt())
        messageListener.recieveMessage(message)
        output("Receiving : ${json.get("content")}")
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        output("Receiving bytes : " + bytes.hex())
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(NORMAL_CLOSURE_STATUS, null)
        output("Closing : $code / $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        output("Error : " + t.message)
    }

    companion object {
        private val NORMAL_CLOSURE_STATUS = 1000
    }

    private fun output(txt: String) {
        Log.v("WSS", txt)
    }
}