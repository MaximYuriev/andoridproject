package request

import android.content.SharedPreferences
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.cookies.get
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.cookie
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Cookie
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.cookies
import io.ktor.http.renderSetCookieHeader
import io.ktor.http.setCookie
import io.ktor.serialization.gson.gson
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import kotlinx.coroutines.runBlocking
import java.util.Scanner


data class SuccessResponse(val detail: String, val data: String?)

data class Chat(val chatId:Int, val fullname:String, val lastMessage: String?)

data class ResponseChatLists(val detail: String, val data: MutableList<Chat>)

data class CurrentUser(val detail: String, val data: Int)

data class Message(val content: String, val messageId: Int?, val senderId: Int)

data class ResponseMessageList(val detail: String, val data: MutableList<Message>)

object Client {

    private val client = HttpClient(){
        install(ContentNegotiation) {
            gson()
        }
        install(WebSockets)
    }
    private var userSession: Cookie? = null
    var userId: Int? = null

    fun checkSession(): Boolean {
        return userSession != null
    }

    fun checkUserId(): Boolean{
        return userId != null
    }

    suspend fun authRequest(email: String, password: String): Map<String, Any> {
        val body = """{"email":"$email", "password":"$password"}"""
        val response: HttpResponse = client.post("http://192.168.0.104:8000/auth/login"){
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        if(response.status == HttpStatusCode.OK)
            userSession = response.setCookie()["web-app-session-id"]!!
        val respBody: SuccessResponse = response.body()

        return mapOf("status" to response.status, "detail" to respBody.detail)
    }
    suspend fun regRequest(email: String, username: String, password: String,
                           firstname: String?, lastname: String?): String {
        var body = """{"email":"$email", "username":"$username", "password":"$password""""
        if (firstname != "")
            body += """, "firstname":"$firstname""""
        if (lastname != "")
            body += """, "lastname":"$lastname""""
        body += "}"
        val response: HttpResponse = client.post("http://192.168.0.104:8000/auth/registration"){
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        val respBody: SuccessResponse = response.body()
        return respBody.detail
    }

    suspend fun getUserChatList(): MutableList<Chat> {
        val response: ResponseChatLists = client.get("http://192.168.0.104:8000/chat"){
            cookie(userSession!!.name, userSession!!.value)
        }.body()
        return response.data
    }

    suspend fun createChat(username: String): Map<String, Any>{
        val body = """{"username":"$username"}"""
        val response: HttpResponse = client.post("http://192.168.0.104:8000/chat"){
            cookie(userSession!!.name, userSession!!.value)
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        val responseBody: SuccessResponse = response.body()
        return mapOf("status" to response.status, "detail" to responseBody.detail)
    }

    suspend fun getUserId(){
        val response: CurrentUser = client.get("http://192.168.0.104:8000/user/id"){
            cookie(userSession!!.name, userSession!!.value)
        }.body()
        userId = response.data
    }

    suspend fun getAllMessages(chatId: Int): MutableList<Message>{
        val response: ResponseMessageList = client.get("http://192.168.0.104:8000/chat/message/$chatId"){
            cookie(userSession!!.name, userSession!!.value)
        }.body()
        return response.data
    }

    suspend fun sendMessage(chatId: Int, message: String){
        val body = """{"content":"$message"}"""
        val response: HttpResponse = client.post("http://192.168.0.104:8000/chat/message/$chatId"){
            cookie(userSession!!.name, userSession!!.value)
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
}