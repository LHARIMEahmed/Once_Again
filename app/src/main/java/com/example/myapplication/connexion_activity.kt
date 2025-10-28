package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityConnexionBinding
import com.google.firebase.auth.FirebaseAuth

class connexion_activity : AppCompatActivity() {
    private lateinit var binding : ActivityConnexionBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        binding = ActivityConnexionBinding.inflate(layoutInflater)

        setContentView(binding.root)
        firebaseAuth= FirebaseAuth.getInstance()
        binding.connexion.setOnClickListener{
            val intent = Intent(this,inscription_activity::class.java)
            startActivity(intent)
        }
        binding.button.setOnClickListener{
            val email= binding.emailR.text.toString()
            val password= binding.passwordR.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val intent = Intent(this,MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "les champs sont vides", Toast.LENGTH_SHORT).show()
            }
        }
    }
}