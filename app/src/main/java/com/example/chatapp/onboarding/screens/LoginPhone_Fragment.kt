package com.example.chatapp.onboarding.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.chatapp.R
import com.example.chatapp.databinding.LoginPhoneBinding
import com.example.chatapp.home.HomeActivity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class LoginPhone_Fragment : Fragment() {
    private lateinit var _binding: LoginPhoneBinding
    private val binding get() = _binding!!
    private lateinit var verificationId: String
    private lateinit var auth: FirebaseAuth
    private lateinit var mphoneNumber: String
    private var forceResendingToken: PhoneAuthProvider.ForceResendingToken? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = LoginPhoneBinding.inflate(layoutInflater, container, false)
        auth = Firebase.auth
        verificationId = ""
        mphoneNumber = ""
        binding.sendOtp.setOnClickListener {
            val phoneNumber = "+84" + binding.inputPhone.text.toString()
            onClickVerifiedPhoneNumber(phoneNumber)
            binding.sendOtp.setText("Resent Code")
        }

        binding.loginPhoneBtn.setOnClickListener {
            try {
                onClickVertifyOTP(binding.inputOTP.text.toString())
            } catch (e: Exception) {
                Log.d("Error", e.message.toString())
            }
        }

        return binding.root
    }

    //Xac thuc OTP
    private fun onClickVertifyOTP(otp: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        signInWithPhoneAuthCredential(credential)
    }

    //Dùng để gửi OTP
    private fun onClickVerifiedPhoneNumber(phoneNumber: String) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(credential)
            }
            override fun onVerificationFailed(e: FirebaseException) {
                if (e is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(
                        context, "Invalid request", Toast.LENGTH_LONG
                    )
                } else if (e is FirebaseTooManyRequestsException) {
                    Toast.makeText(
                        context,
                        "The SMS quota for the project has been exceeded",
                        Toast.LENGTH_LONG
                    )
                } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                    Toast.makeText(
                        context,
                        "reCAPTCHA verification attempted with null Activity",
                        Toast.LENGTH_LONG
                    )
                }
            }
            override fun onCodeSent(
                id: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                super.onCodeSent(id, token)
                verificationId = id
                forceResendingToken = token
            }
        }
        mphoneNumber = phoneNumber
        val options =
            PhoneAuthOptions.newBuilder(auth).setPhoneNumber(phoneNumber) // Phone number to verify
                .setTimeout(30L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(requireActivity()) // Activity (for callback binding)
                .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    //Dùng để gửi lại OTP
    private fun onClickSendOTPAgain() {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                if (e is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(
                        context, "Invalid request", Toast.LENGTH_LONG
                    )
                } else if (e is FirebaseTooManyRequestsException) {
                    Toast.makeText(
                        context,
                        "The SMS quota for the project has been exceeded",
                        Toast.LENGTH_LONG
                    )
                } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                    Toast.makeText(
                        context,
                        "reCAPTCHA verification attempted with null Activity",
                        Toast.LENGTH_LONG
                    )
                }
            }

            override fun onCodeSent(
                id: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                super.onCodeSent(id, token)
                verificationId = id
            }
        }
        val options =
            PhoneAuthOptions.newBuilder(auth).setPhoneNumber(mphoneNumber) // Phone number to verify
                .setTimeout(30L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(requireActivity()) // Activity (for callback binding)
                .setForceResendingToken(this.forceResendingToken!!)
                .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }


    //Dùng để đăng nhập
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val intent = Intent(context, HomeActivity::class.java)
                startActivity(intent)
            } else {
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(
                        context, "The verification code entered was invalid", Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}