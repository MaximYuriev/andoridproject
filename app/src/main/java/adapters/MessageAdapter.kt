package adapters

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.messanger.R
import request.Client
import request.Message


class MessageAdapter: RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    val client = Client

    var data: List<Message> = emptyList()
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    inner class MessageViewHolder(val itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = data[position]
        val messageContent: TextView = holder.itemView.findViewById(R.id.messageContent)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            weight = 1.0f
            topMargin = 15
            leftMargin = 10
            rightMargin = 10
            gravity = if (client.userId == message.senderId) Gravity.END else Gravity.START
        }
        if (client.userId == message.senderId)
            messageContent.setBackgroundResource(R.drawable.sent_msg)
        else
            messageContent.setBackgroundResource(R.drawable.receive_msg)
        messageContent.layoutParams = params
        messageContent.text = message.content
    }


}