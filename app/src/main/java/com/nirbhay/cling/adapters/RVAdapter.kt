package com.nirbhay.cling.adapters

import android.content.Context
import android.content.Intent
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nirbhay.cling.R
import com.nirbhay.cling.activities.ChatActivity
import com.nirbhay.cling.daos.ContactsDao
import com.nirbhay.cling.daos.MessagesDao
import com.nirbhay.cling.data.ContactsData


class RVAdapter(private val c : Context, private val list: ArrayList<ContactsData>, private val positionCallBack: PositionCallBack) : RecyclerView.Adapter<RVAdapter.ViewHolder>(){


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var profile: ImageView
        var userName: TextView
        private var options: ImageView
        init {
            profile = itemView.findViewById(R.id.f_profile)
            userName = itemView.findViewById(R.id.userName)
            options = itemView.findViewById(R.id.options)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.chat_view, parent, false)
        )


    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentUser = list[position]

        holder.userName.text = currentUser.contactName
        holder.userName.setOnClickListener {
            val intent = Intent(c,ChatActivity::class.java)
            intent
                .putExtra("userName", currentUser.contactName)
                .putExtra("uid", currentUser.contactUid)
                .putExtra("email", currentUser.contactMail)
                .putExtra("imageUrl", currentUser.contactImg)

            c.startActivity(intent)
        }

        holder.userName.setOnLongClickListener{

            positionCallBack.getPosition(holder.absoluteAdapterPosition, currentUser.contactUid)

            true
        }

        Glide
            .with(holder.profile.context)
            .load(currentUser.contactImg)
            .centerCrop()
            .placeholder(R.drawable.img)
            .into(holder.profile)


//        val messagesDao = MessagesDao()
        try {

        }catch (e: Exception){
            e.printStackTrace()
        }

    }


    override fun getItemCount(): Int {
        return list.size
    }


}

interface PositionCallBack{
    fun getPosition(position: Int, uid: String)
}