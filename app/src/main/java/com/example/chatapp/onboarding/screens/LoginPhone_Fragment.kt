package com.example.chatapp.onboarding.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.chatapp.Constant
import com.example.chatapp.databinding.LoginPhoneBinding
import com.example.chatapp.model.Users
import com.example.chatapp.newsfeed.NewsfeedActivity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class LoginPhone_Fragment : Fragment() {
    private lateinit var _binding: LoginPhoneBinding
    private val binding get() = _binding!!
    private lateinit var verificationId: String
    private lateinit var auth: FirebaseAuth
    private lateinit var mphoneNumber: String
    private var forceResendingToken: PhoneAuthProvider.ForceResendingToken? = null
    private lateinit var database: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = LoginPhoneBinding.inflate(layoutInflater, container, false)
        auth = Firebase.auth
        database = Firebase.database.reference
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
                createUserOnDatabase(binding.inputPhone.text.toString())
                loginSuccess()
            } else {
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(
                        context, "The verification code entered was invalid", Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun loginSuccess() {
        val intent = Intent(context, NewsfeedActivity::class.java)
        startActivity(intent)
    }

    private fun createUserOnDatabase(name: String) {
        val userID = auth.uid.toString()
        val user = Users(
            userID,
            0,
            name,
            "https://firebasestorage.googleapis.com/v0/b/chat-application-2ee31.appspot.com/o/images%2Favatar.png?alt=media&token=dff0b5ac-8fbf-4c3f-bd2b-e9dac6ea1bf5&_gl=1*4fv8yt*_ga*NDEzMzYzMTQyLjE2OTcyNjIzMTk.*_ga_CW55HF8NVT*MTY5ODc5OTQ3OC4xNy4xLjE2OTg4MDAwMjEuNTQuMC4w",
            "None",
            0,
            0,
            ""
        )

        database.child(Constant.USER_TABLE_NAME).child(userID).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    database.child(Constant.USER_TABLE_NAME).child(userID).setValue(user)
                    database.child(Constant.FOLLOW_TABLE_NAME).child(auth.uid!!)
                        .child(Constant.FOLLOW_TABLE_FOLLOWING).child(auth.uid!!)
                        .setValue(true)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}