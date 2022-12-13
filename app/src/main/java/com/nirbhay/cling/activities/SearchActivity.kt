package com.nirbhay.cling.activities

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.*
import com.nirbhay.cling.R
import com.nirbhay.cling.adapters.SearchRvAdapter
import com.nirbhay.cling.data.SearchData
import com.nirbhay.cling.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var searchRvAdapter: SearchRvAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var userList: ArrayList<SearchData>
    private lateinit var binding: ActivitySearchBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = Color.parseColor("#212426")

        db = FirebaseFirestore.getInstance()
        userList = arrayListOf()

        val searchBar = binding.searchBar
        val recyclerView = binding.listView

        recyclerView.visibility = View.GONE

        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        searchRvAdapter = SearchRvAdapter(this,userList)
        recyclerView.adapter = searchRvAdapter


        searchBar.addTextChangedListener (object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().trim().isEmpty()){
                    recyclerView.visibility = View.GONE
                }else{
                    search(p0.toString().trim())
                    recyclerView.visibility = View.VISIBLE
                }
            }
            override fun afterTextChanged(p0: Editable?) {}

        })

    }

    private fun search(s :String?) {
        db.collection("Users").orderBy("email").startAt(s).endAt(s+"\uf8ff")
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null){
                        return
                    }
                    for (dc: DocumentChange in value?.documentChanges!!){
                        if (dc.type == DocumentChange.Type.ADDED) {
                            userList.clear()
                            userList.add(dc.document.toObject(SearchData::class.java))
                        }
                    }
                    searchRvAdapter.notifyDataSetChanged()
                }

            })
    }


}