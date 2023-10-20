package com.example.chatapp.onboarding

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentSignUpBinding

class SignUp_Fragment : Fragment() {
    private lateinit var _binding: FragmentSignUpBinding
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignUpBinding.inflate(layoutInflater, container, false)

        binding.btnSignUp.setOnClickListener {
            if (checkInput()) {
                val action = SignUp_FragmentDirections.actionSignUpFragmentToHomeFragment(
                    binding.inputEmailSignUp.text.toString(),
                    binding.inputPassword.text.toString())
                findNavController().navigate(action)
            }
        }
        return binding.root
    }

    private fun checkInput(): Boolean {
        val email = binding.inputEmailSignUp.text.toString()
        val pass = binding.inputPassword.text.toString()
        val confirmPass = binding.inputConfirmPass.text.toString()
        if (email == "" ) {
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
        if (!confirmPass.equals(pass)) {
            Toast.makeText(this.context, "Password do not match!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

}