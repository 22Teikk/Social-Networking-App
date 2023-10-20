package com.example.chatapp.onboarding

import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentLoginBinding

class LogIn_Fragment : Fragment() {
    private lateinit var _binding: FragmentLoginBinding
    private val args:LogIn_FragmentArgs by navArgs()
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

        return binding.root
    }

    private fun getDataSignUp() {
        binding.inputEmail.setText(args.email)
        binding.inputPass.setText(args.password)
    }


}