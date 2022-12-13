package com.nirbhay.cling.adapters

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.nirbhay.cling.R
import com.nirbhay.cling.activities.ChatActivity
import com.nirbhay.cling.data.Message
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class MessageRvAdapter(options: FirestoreRecyclerOptions<Message>, private val positionCallBack: ChatActivity):
    FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder>(options){
    private val auth = FirebaseAuth.getInstance()
    private var isSelected = false


//    private val selectedMessages = mutableListOf<SelectedMessages>()

    class MessageViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val message: TextView = view.findViewById(R.id.message)
        val time: TextView = view.findViewById(R.id.time)
        val seen: ImageView = view.findViewById(R.id.seen)
        val mainLayout: LinearLayout = view.findViewById(R.id.mainLinearLayout)
        val tick1: ImageView = view.findViewById(R.id.tick)



    }

    class MessageViewHolder2(view: View): RecyclerView.ViewHolder(view) {
        val message: TextView = view.findViewById(R.id.message2)
        val time: TextView = view.findViewById(R.id.time2)
        val mainLayout2: ConstraintLayout = view.findViewById(R.id.mainLinearLayout2)
        val tick2: ImageView = view.findViewById(R.id.tick2)

    }

    override fun getItemViewType(position: Int): Int {
        if (snapshots[position].userDetails.uid == auth.currentUser!!.uid){
            return 0
        }
        return 1
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            0 -> MessageViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.sendmessage, parent, false)

            )

            1 -> MessageViewHolder2(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.recievemessage, parent, false)
            )
            else -> {
                Toast.makeText(parent.context,"Some error occurred!", Toast.LENGTH_SHORT).show()
                MessageViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.sendmessage, parent, false))
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: Message) {
        when (holder.itemViewType){
            0 -> {

                val holder1 = holder as MessageViewHolder

                holder1.message.text = model.message!!
                val dateFormat: DateFormat = SimpleDateFormat("h:mm a")
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = model.createdAt
                holder1.time.text = dateFormat.format(calendar.time)
                holder1.tick1.visibility = View.INVISIBLE
                holder1.mainLayout.setBackgroundColor(Color.parseColor("#00FFFFFF"))


                holder1.message.setOnLongClickListener {
                    val messageId =  snapshots.getSnapshot(position).id
                    if (!positionCallBack.messagesTDList.contains(messageId)) {
                        positionCallBack.onLongClick()
                        selectMessages(position, messageId)
                        holder1.mainLayout.setBackgroundColor(Color.parseColor("#073229"))
                        holder1.tick1.visibility = View.VISIBLE
                    }
                    true
                }


                holder1.message.setOnClickListener {
                    val messageId =  snapshots.getSnapshot(position).id
                    if (positionCallBack.messagesTDList.contains(messageId)){
                        holder1.mainLayout.setBackgroundColor(Color.parseColor("#00FFFFFF"))
                        holder1.tick1.visibility = View.INVISIBLE
                        positionCallBack.messagesTDList.remove(messageId)
                        if (positionCallBack.messagesTDList.isEmpty()){
                            positionCallBack.removeDeleteBtn()
                            isSelected = false
                        }

                    }
                    else if (isSelected) {
                        positionCallBack.messagesTDList.add(messageId)
                        holder1.mainLayout.setBackgroundColor(Color.parseColor("#073229"))
                        holder1.tick1.visibility = View.VISIBLE
                    }

                }

            }
            1->{
                val holder2 = holder as MessageViewHolder2
                holder2.message.text = model.message!!
                val dateFormat: DateFormat = SimpleDateFormat("h:mm a")
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = model.createdAt
                holder2.time.text = dateFormat.format(calendar.time)
                holder2.tick2.visibility = View.INVISIBLE
                holder2.mainLayout2.setBackgroundColor(Color.parseColor("#00FFFFFF"))

                holder2.message.setOnLongClickListener {
                    val messageId =  snapshots.getSnapshot(position).id
                    if (!positionCallBack.messagesTDList.contains(messageId)) {
                        positionCallBack.onLongClick()
                        selectMessages(position, messageId)
                        holder2.mainLayout2.setBackgroundColor(Color.parseColor("#0B2551"))
                        holder2.tick2.visibility = View.VISIBLE
                    }
                    true
                }


                holder2.message.setOnClickListener {
                    val messageId2 =  snapshots.getSnapshot(position).id
                    if (positionCallBack.messagesTDList.contains(messageId2)){
                        holder2.mainLayout2.setBackgroundColor(Color.parseColor("#00FFFFFF"))
                        holder2.tick2.visibility = View.INVISIBLE
                        positionCallBack.messagesTDList.remove(messageId2)
                        if (positionCallBack.messagesTDList.isEmpty()){
                            positionCallBack.removeDeleteBtn()
                            isSelected = false
                        }
                    }
                    else if (isSelected) {
                        holder2.mainLayout2.setBackgroundColor(Color.parseColor("#0B2551"))
                        holder2.tick2.visibility = View.VISIBLE
                        selectMessages(position, messageId2)
                    }
                }

            }
        }

    }

    private fun selectMessages(position: Int, messageId: String) {
        isSelected = true
        positionCallBack.messagesTDList.add(messageId)
    }


}