package com.nirbhay.cling.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nirbhay.cling.R
import com.nirbhay.cling.activities.ChatActivity
import com.nirbhay.cling.activities.SearchActivity
import com.nirbhay.cling.daos.ContactsDao
import com.nirbhay.cling.daos.MessagesDao
import com.nirbhay.cling.data.SearchData

class SearchRvAdapter(private val c: Context, private val usersList: ArrayList<SearchData>) : RecyclerView.Adapter<SearchRvAdapter.ViewHolder>() {
    private val contactsDao = ContactsDao()

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val userImage: ImageView = view.findViewById(R.id.searchProfileImg)
        val userName: TextView = view.findViewById(R.id.searchUserName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder = ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.search_view,parent,false))
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = usersList[position]
        holder.userName.text = currentItem.userName

        Glide
            .with(holder.userImage.context)
            .load(currentItem.imageUrl)
            .centerCrop()
            .placeholder(R.drawable.img)
            .into(holder.userImage)

        holder.userName.setOnClickListener {
//            contactsDao.addContact(currentItem.uid, currentItem.userName!!, currentItem.email!!, currentItem.imageUrl!!)
            val intent = Intent(c, ChatActivity::class.java)
                .putExtra("userName", currentItem.userName)
                .putExtra("uid", currentItem.uid)
                .putExtra("email", currentItem.email)
                .putExtra("imageUrl", currentItem.imageUrl)
            c.startActivity(intent)
            (c as SearchActivity).finish()

        }

    }

    override fun getItemCount(): Int {
        return usersList.size
    }


}