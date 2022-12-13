package com.nirbhay.cling.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Shader.TileMode
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.nirbhay.cling.R
import com.nirbhay.cling.databinding.ActivityMainBinding
import com.nirbhay.cling.fragments.ChatsFragment


class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    private lateinit var textShader: Shader
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = Color.parseColor("#212426")
        auth = FirebaseAuth.getInstance()


        val chatFragment = ChatsFragment()
        supportFragmentManager.beginTransaction().apply {
            add(R.id.frameLayout, chatFragment)
            commit()
        }

        binding.addBtn.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        val title =  binding.actionBar
        val paint = title.paint
        val width = paint.measureText(title.text.toString())
        textShader = LinearGradient(0f,0f,width,title.textSize, intArrayOf(
            Color.parseColor("#00B0FF"),
            Color.parseColor("#00E676")
        ),null, TileMode.CLAMP)
        title.paint.shader = textShader

    }

//    private fun userStatus(){
//        dao.getUserById(auth.currentUser!!.uid).addOnCompleteListener {
//            if (it.isSuccessful){
//                val user = it.result.toObject(User::class.java)
//            }
//        }
//    }

}