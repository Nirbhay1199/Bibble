package com.nirbhay.cling.daos

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.nirbhay.cling.data.ContactsData

class ContactsDao {
    private val userDao = Dao()
    private val auth = Firebase.auth
    private val db = FirebaseFirestore.getInstance()
    val contactsCollection = db.collection("Contacts")

    fun addContact(uid: String,contactUid: String, contactName: String, contactMail: String, contactImg: String){
        userDao.getUserById(auth.currentUser!!.uid).addOnCompleteListener {
            if (it.isSuccessful){
                val contactObj = ContactsData(contactUid,contactName,contactMail, contactImg)
                contactsCollection.document(uid)
                    .collection("ContactDetail").document(contactUid).set(contactObj)
            }
        }
    }

    fun deleteContact(contactUid: String){
        contactsCollection.document(auth.currentUser!!.uid)
            .collection("ContactDetail").document(contactUid).delete()
    }

}