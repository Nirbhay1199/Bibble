package com.nirbhay.cling.daos

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.nirbhay.cling.data.User
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Dao {
    private val db = FirebaseFirestore.getInstance()
    private val userCollection = db.collection("Users")


    fun addUser(user: User?){
        user?.let {
            userCollection.document(user.uid).set(it)
        }
    }

    fun getUserById(uid: String): Task<DocumentSnapshot> {
        return userCollection.document(uid).get()
    }

}