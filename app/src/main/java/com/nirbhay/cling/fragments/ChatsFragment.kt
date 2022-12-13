package com.nirbhay.cling.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.nirbhay.cling.R
import com.nirbhay.cling.adapters.PositionCallBack
import com.nirbhay.cling.adapters.RVAdapter
import com.nirbhay.cling.daos.ContactsDao
import com.nirbhay.cling.data.ContactsData


class ChatsFragment : Fragment(R.layout.fragment_chats), PositionCallBack{

    private lateinit var db: FirebaseFirestore
    private lateinit var contactList: ArrayList<ContactsData>
    lateinit var rVadapter: RVAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var text: TextView
    private lateinit var progressBar: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        contactList = arrayListOf()

        recyclerView = view.findViewById(R.id.chat_recyclerView)
        text = view.findViewById(R.id.text)
        progressBar = view.findViewById(R.id.progressBar)
        text.visibility = View.GONE

        registerForContextMenu(recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        rVadapter = RVAdapter(requireContext(),contactList, this)
        recyclerView.adapter = rVadapter

        getUserData()


    }

    private fun getUserData(){
        db = FirebaseFirestore.getInstance()

        db.collection("Contacts").document(auth.currentUser!!.uid)
            .collection("ContactDetail")
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if (error != null){
                    return
                }
                for (dc: DocumentChange in value?.documentChanges!!){
                    if (dc.type == DocumentChange.Type.ADDED){
                        contactList.add(dc.document.toObject(ContactsData::class.java))
                        if (contactList.size >= 1){
                            recyclerView.visibility = View.VISIBLE
                            text.visibility = View.GONE
                            recyclerView.adapter = rVadapter
                        }else{
                            recyclerView.visibility = View.GONE
                            text.visibility = View.VISIBLE
                        }
                    }
                }
                progressBar.visibility = View.GONE
                rVadapter.notifyDataSetChanged()
            }
        })

    }




    override fun getPosition(position: Int, uid: String) {

        Log.d("uid", uid)

        val contactsDao = ContactsDao()
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(
            Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.custom_dialog)
        dialog.show()

        val yesButton = dialog.findViewById<TextView>(R.id.yesButton)
        val noButton = dialog.findViewById<TextView>(R.id.noButton)

        yesButton.setOnClickListener {
            contactsDao.deleteContact(uid)
            contactList.removeAt(position)
            rVadapter.notifyDataSetChanged()
            dialog.dismiss()
        }

        noButton.setOnClickListener {
            dialog.dismiss()
        }
    }


}











