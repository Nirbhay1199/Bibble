package com.nirbhay.cling.activities

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.nirbhay.cling.adapters.MessageRvAdapter
import com.nirbhay.cling.daos.ContactsDao
import com.nirbhay.cling.daos.MessagesDao
import com.nirbhay.cling.data.ChatDetails
import com.nirbhay.cling.data.Message
import com.nirbhay.cling.data.NotificationData
import com.nirbhay.cling.data.PushNotification
import com.nirbhay.cling.databinding.ActivityChatBinding
import com.nirbhay.cling.instance.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var messagesDao: MessagesDao
    private var adapter: MessageRvAdapter? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var documentName: String
    private lateinit var token: String
    private lateinit var db: FirebaseFirestore
    private lateinit var user: String
    private lateinit var user2Name: String
    private lateinit var user2Email: String
    private lateinit var user2uid: String
    private lateinit var user2photoUrl: String
    private lateinit var messageId: String
    val messagesTDList = mutableListOf<String>()


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = Color.parseColor("#212426")

        messagesDao = MessagesDao()
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.backBtn.setOnClickListener{
            finish()
        }

        (binding.messageRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false


        user2uid = intent.getStringExtra("uid").toString()
        user2Email = intent.getStringExtra("email").toString()
        user2Name = intent.getStringExtra("userName").toString()
        user2photoUrl = intent.getStringExtra("imageUrl").toString()

        db.collection("Users").document(user2uid).get().addOnSuccessListener {
            token = it.data?.get("token").toString()
        }
        binding.chatActionBar.text = user2Name

        try {
//            layoutManager = LinearLayoutManager(this)
//            layoutManager.orientation = LinearLayoutManager.VERTICAL

//            layoutManager.reverseLayout = true
//            binding.messageRecyclerView.layoutManager = layoutManager
            layoutManager = LinearLayoutManagerWrapper(this, LinearLayoutManager.VERTICAL, true)
            layoutManager.stackFromEnd = true
            binding.messageRecyclerView.layoutManager = layoutManager

            binding.sendMessageBtn.setOnClickListener {
                val message = binding.message.text.toString()
                val trimmedMsg = message.trim()

                if (!isOnline(this)){
                    Toast.makeText(this, "No Internet !", Toast.LENGTH_SHORT).show()
                }
                else if(message.trim().isEmpty()){
                    Toast.makeText(this,"Can't send empty message", Toast.LENGTH_SHORT).show()
                }
                else {
                    messagesDao.addMessage(trimmedMsg,
                        user2uid,
                        documentName)

                    messagesDao.messageCollection.document(documentName).get().addOnCompleteListener {
                        val res = it.result!!
                        if (res["user1"] == "" || res["user2"] == ""){
                            PushNotification(
                                NotificationData(
                                    auth.currentUser!!.displayName.toString(),
                                    trimmedMsg,
                                    auth.currentUser!!.uid
                                ),
                                token
                            ).also { item ->
                                sendNotification(item)
                            }
                        }
                    }
                    binding.message.text.clear()
                }
            }

        }catch (e: Exception){
            e.printStackTrace()
        }


        docExists()

//        binding.messageRecyclerView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
//            if (bottom < oldBottom) {
//                binding.messageRecyclerView.smoothScrollToPosition(0)
//            }
//        }
        binding.deleteMsg.setOnClickListener {
            messagesDao.deleteMessage(documentName,messagesTDList)
            messagesTDList.clear()
            binding.deleteMsg.visibility = View.GONE
        }


    }

    fun onLongClick() {
        binding.deleteMsg.visibility = View.VISIBLE
    }

    fun removeDeleteBtn(){
        binding.deleteMsg.visibility = View.GONE
    }

    class LinearLayoutManagerWrapper : LinearLayoutManager {
        constructor(context: Context?, orientation: Int, reverseLayout: Boolean) : super(
            context,
            orientation,
            reverseLayout
        )

        override fun supportsPredictiveItemAnimations(): Boolean {
            return false
        }
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if(response.isSuccessful){
//                Log.d("tag", "Response: ${Gson().toJson(response)}")
            }else{
                Log.e("tag", response.errorBody().toString())
            }
        }catch (e: Exception){
            Log.e("tag", e.toString() )
        }
    }

    private fun loadMessages(doc: String){

        try {

            val messageCollection = messagesDao.messageCollection.document(doc)
            val chatCollection = messageCollection.collection("Chats")

            val query = chatCollection.orderBy("createdAt", Query.Direction.DESCENDING)

            val recyclerOptions = FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(query, Message::class.java).build()

            adapter = MessageRvAdapter(recyclerOptions, this)

            binding.messageRecyclerView.adapter = adapter

            adapter!!.startListening()

            query.addSnapshotListener { _, _ ->
                layoutManager.scrollToPositionWithOffset(0,0)
            }

            binding.progressBar.visibility = View.GONE
        }catch (e: Exception){
            e.printStackTrace()
        }

    }

    private fun docExists(){

        val docName = auth.currentUser!!.uid + user2uid
        val docName2 = user2uid + auth.currentUser!!.uid

        messagesDao.messageCollection.document(docName).get().addOnCompleteListener { it ->
            if (it.isSuccessful){
                val doc = it.result
                if (doc.exists()){
                    documentName = docName
                    addContact(documentName)
                    loadMessages(documentName)
                    messagesDao.messageCollection.document(documentName).update("user1", "true")
                    user = "user1"
                }else{
                    messagesDao.messageCollection.document(docName2).get().addOnCompleteListener {
                        if (it.isSuccessful){
                            val doc2 = it.result
                            if (doc2.exists()){
                                documentName = docName2
                                addContact(documentName)
                                loadMessages(documentName)
                                messagesDao.messageCollection.document(documentName).update("user2", "true")
                                user = "user2"
                            } else{
                                documentName = docName
                                addContact(documentName)
                                loadMessages(documentName)
                                messagesDao.messageCollection.document(documentName).update("user1", "true")
                                user = "user1"
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            adapter?.startListening()
            messagesDao.messageCollection.document(documentName).update(user, "true")
        }catch(e: Exception){
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            adapter?.stopListening()
            messagesDao.messageCollection.document(documentName).update(user, "")
        }catch (e: Exception){
            e.printStackTrace()
        }
    }



    override fun onStop() {
        super.onStop()
        try {
            adapter?.stopListening()
            messagesDao.messageCollection.document(documentName).update(user, "")
        }catch (e: Exception){
            e.printStackTrace()
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                return true
            }
        }
        return false
    }


    private fun addContact(doc: String){
        val contactsDao = ContactsDao()
        val currentUser = auth.currentUser!!
        contactsDao.contactsCollection.document(currentUser.uid)
            .collection("ContactDetail").document(user2uid)
            .get().addOnCompleteListener {
            if (it.isSuccessful && !it.result.exists()){
                contactsDao.addContact(currentUser.uid,user2uid, user2Name, user2Email, user2photoUrl)
                if (user == "user1") {
                    messagesDao.messageCollection.document(doc).set(ChatDetails("true", ""))
                }
            }
        }

        contactsDao.contactsCollection.document(user2uid)
            .collection("ContactDetail").document(auth.currentUser!!.uid)
            .get().addOnCompleteListener {
                if (it.isSuccessful && !it.result.exists()){
                    contactsDao.addContact(user2uid,currentUser.uid, currentUser.displayName!!,
                        currentUser.email!!, currentUser.photoUrl.toString())
                    if (user == "user2") {
                        messagesDao.messageCollection.document(doc).set(ChatDetails("", "true"))
                    }
                }
            }


    }

}