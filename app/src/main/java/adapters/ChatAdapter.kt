package adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.messanger.R
import request.Chat

interface ChatActionListener {
    fun onChatGetId(chat: Chat)
}

class ChatAdapter(private val chatActionListener: ChatActionListener) :
        RecyclerView.Adapter<ChatAdapter.ChatViewHolder>(), View.OnClickListener {

    var data: List<Chat> = emptyList()
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    inner class ChatViewHolder(val itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        view.setOnClickListener(this)
        return ChatViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = data[position]
        val fullname: TextView = holder.itemView.findViewById(R.id.fullname)
        val lastMsg: TextView = holder.itemView.findViewById(R.id.lastMessageText)
        val unreadMessageView: TextView = holder.itemView.findViewById(R.id.unreadMessages)
        holder.itemView.tag = chat
        fullname.text = chat.fullname
        if (chat.lastMessage != null)
            lastMsg.text = chat.lastMessage
        if (chat.countUnreadMessages == null) {
            unreadMessageView.text = ""
            unreadMessageView.setBackgroundResource(R.drawable.unread_message_count_zero)
        }
        else{
            unreadMessageView.text = chat.countUnreadMessages.toString()
            unreadMessageView.setBackgroundResource(R.drawable.unread_message_count)
        }
    }

    override fun onClick(v: View) {
        val chat: Chat = v.tag as Chat
        chatActionListener.onChatGetId(chat)
    }

}