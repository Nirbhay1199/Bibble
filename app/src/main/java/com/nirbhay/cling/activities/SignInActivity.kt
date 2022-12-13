package com.nirbhay.cling.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.nirbhay.cling.R
import com.nirbhay.cling.data.User
import com.nirbhay.cling.daos.Dao
import com.nirbhay.cling.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var textShader: Shader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fadeAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        binding.appName.startAnimation(fadeAnim)

        window.statusBarColor = Color.parseColor("#212426")

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("764377697436-3tnqptds3b11c68o1fkql8ckoqcoffjp.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        auth = Firebase.auth

        if (auth.currentUser != null){
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }else{
            binding.signIn.setOnClickListener {
                val intent = googleSignInClient.signInIntent
                launcher.launch(intent)
            }
        }

        val title = binding.appName
        val paint = title.paint
        val width = paint.measureText(title.text.toString())
        textShader = LinearGradient(0f,0f,width,title.textSize, intArrayOf(
            Color.parseColor("#00B0FF"),
            Color.parseColor("#00E676")
        ),null, Shader.TileMode.CLAMP)
        title.paint.shader = textShader
    }

    private var launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result: ActivityResult ->
        binding.progressBar.visibility = View.VISIBLE
        if (result.resultCode == RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                task.getResult(ApiException::class.java).idToken?.let {
                    firebaseAuthWithGoogle(it)
                }
            } catch (e: ApiException) {
                e.printStackTrace()
            }

        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        auth = FirebaseAuth.getInstance()
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener{
            if (it.isSuccessful) {
                val user = auth.currentUser
                uploadUserData(user)
            }
        }
    }

    private fun uploadUserData(user: FirebaseUser?) {
        val dao = Dao()
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            val token = it.toString()
            if (user != null) {
                val userObj = User(user.uid, user.email, user.displayName, user.photoUrl.toString(), token)
                dao.addUser(userObj)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

    }


}