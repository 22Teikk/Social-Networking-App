package com.example.chatapp.onboarding

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.chatapp.databinding.FragmentSignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUp_Fragment : Fragment() {
    private lateinit var _binding: FragmentSignUpBinding
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignUpBinding.inflate(layoutInflater, container, false)
        auth = Firebase.auth
        binding.btnSignUp.setOnClickListener {
            if (checkInput()) {
                val email = binding.inputEmailSignUp.text.toString()
                val pass = binding.inputPassword.text.toString()
                signUpOnFirebase(email, pass)
            }
        }
        return binding.root
    }

    private fun signUpOnFirebase(email: String, pass: String) {
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val action =
                    SignUp_FragmentDirections.actionSignUpFragmentToHomeFragment(auth.currentUser?.email!!, pass)
                findNavController().navigate(action)
            } else {
                Toast.makeText(
                    this.context,
                    "Authentication failed.",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    private fun checkInput(): Boolean {
        val email = binding.inputEmailSignUp.text.toString()
        val pass = binding.inputPassword.text.toString()
        val confirmPass = binding.inputConfirmPass.text.toString()
        if (email == "") {
            binding.layoutInputEmail.error = "Required Input Email"
            return false
        }
        if (pass == "") {
            binding.layoutPassword.error = "Required Input Password"
            return false
        }
        if (confirmPass == "") {
            binding.layoutConfirmPass.error = "Required Input Password"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutInputEmail.error = "Please check your email format"
            return false
        }
        if (!confirmPass.trim().equals(pass.trim())) {
            binding.layoutConfirmPass.error = "Password do not match!"
            return false
        }
        return true
    }

}