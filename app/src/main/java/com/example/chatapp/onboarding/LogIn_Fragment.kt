package com.example.chatapp.onboarding

import android.content.Context
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentLoginBinding

class LogIn_Fragment : Fragment() {
    private lateinit var _binding: FragmentLoginBinding
    private val args: LogIn_FragmentArgs by navArgs()
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        getDataSignUp()

        binding.txtSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_home_Fragment_to_signUp_Fragment)
        }

        binding.btnLogIn.setOnClickListener {
            saveAccount()
        }

        return binding.root
    }

    private fun saveAccount() {
        val sharedPref = requireActivity().getSharedPreferences("account", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        if (binding.chkSaveAcc.isChecked) {
            editor.putString("email", binding.inputEmail.text.toString())
            editor.putString("pass", binding.inputPass.text.toString())
            editor.putBoolean("isSave", true)
        }else {
            editor.remove("email")
            editor.remove("pass")
            editor.remove("isSave")
        }
        editor.apply()
    }

    private fun getAccount() {
        val sharedPref = requireActivity().getSharedPreferences("account", Context.MODE_PRIVATE)
        val email: String = sharedPref.getString("email", "").toString()
        val pass = sharedPref.getString("pass", "")
        if (sharedPref.getBoolean("isSave", false) == true) {
            binding.chkSaveAcc.isChecked = true
            binding.inputEmail.setText(email)
            binding.inputPass.setText(pass)
        }
    }

    override fun onStart() {
        getAccount()
        super.onStart()
    }
    private fun getDataSignUp() {
        if (args.email != null)
        binding.inputEmail.setText(args.email)
        if (args.password != null)
        binding.inputPass.setText(args.password)
    }


}