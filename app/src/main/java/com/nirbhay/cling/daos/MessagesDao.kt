package com.nirbhay.cling.daos

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.nirbhay.cling.data.*
import kotlin.math.log


class MessagesDao {
    private val db = FirebaseFirestore.getInstance()
    private val userDao = Dao()
    private val auth = Firebase.auth
    val messageCollection = db.collection("Messages")


    fun addMessage(message: String, messageTo: String, doc: String){
            val createdAt = System.currentTimeMillis()

            lateinit var createdBy: User
            userDao.getUserById(auth.currentUser!!.uid).addOnCompleteListener { user ->
                if (user.isSuccessful){
                    createdBy = user.result.toObject(User::class.java)!!
                    val messageObj = Message(createdBy, createdAt, message, messageTo)
                    messageCollection.document(doc)
                        .collection("Chats").document().set(messageObj)

                }
            }

    }

    fun deleteMessage(doc: String, id: List<String>){
        for (i in id){
            messageCollection.document(doc).collection("Chats").document(i).delete()
        }
    }

//    fun addDoc(doc: String, seen: String){
////        val docObj = ChatDetails(seen)
//        messageCollection.document(doc).set(docObj)
//    }


}