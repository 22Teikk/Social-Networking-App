package com.example.chatapp.onboarding

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.chatapp.Constant
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentLoginBinding
import com.example.chatapp.newsfeed.NewsfeedActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class LogIn_Fragment : Fragment() {
    private lateinit var _binding: FragmentLoginBinding
    private val args: LogIn_FragmentArgs by navArgs()
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val binding get() = _binding!!
    //For Google login
    private lateinit var client: GoogleSignInClient
    //For facebook login
    private lateinit var callBackManager: CallbackManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        auth = Firebase.auth
        getAccount()
        database = Firebase.database.reference
        FacebookSdk.sdkInitialize(requireContext());
        //Generate Option Log In Google
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        client = GoogleSignIn.getClient(requireContext(), options)
        callBackManager = CallbackManager.Factory.create()

        binding.txtSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_home_Fragment_to_signUp_Fragment)
        }

        binding.btnLogIn.setOnClickListener {
            val email = binding.inputEmail.text.toString()
            val password = binding.inputPass.text.toString()
            if (checkInput()) {
                signInWithEmail(email, password)
            }
        }

        binding.txtForgotPass.setOnClickListener {
            forgotPasswordEmail()
        }

        binding.imagePhone.setOnClickListener {
            findNavController().navigate(R.id.action_home_Fragment_to_loginPhone_Fragment)
        }

        binding.imageGoogle.setOnClickListener {
            signInWithGoogle()
        }

        binding.imageFacebook.setOnClickListener {
            signInWithFacebook()
        }

        return binding.root
    }

    private fun signInWithFacebook() {
        val accessToken = AccessToken.getCurrentAccessToken()
        if (accessToken != null && !accessToken.isExpired) {
            val intent = Intent(this.context, NewsfeedActivity::class.java)
            startActivity(intent)
        }
        LoginManager.getInstance().registerCallback(callBackManager, object :FacebookCallback<LoginResult>{
            override fun onCancel() {

            }

            override fun onError(error: FacebookException) {

            }

            override fun onSuccess(result: LoginResult) {
                val intent = Intent(context, NewsfeedActivity::class.java)
                startActivity(intent)
            }
        })

        LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile,email"))

    }

    override fun onResume() {
        if (args.email != "" && args.password != "") {
            getDataSignUp()
        }
        super.onResume()
    }

    private fun signInWithGoogle() {
        val intent = client.signInIntent
        arlGoogleAccount.launch(intent)
    }

    val arlGoogleAccount =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                if (task.isSuccessful) {
                    val account = task.getResult(ApiException::class.java)
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val intent = Intent(this.context, NewsfeedActivity::class.java)
                                startActivity(intent)
                            } else {
                                Toast.makeText(requireContext(), task.exception?.message , Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(requireContext(), "Some thing error" , Toast.LENGTH_LONG).show()
                }

            }
        }


    private fun forgotPasswordEmail() {
        val email = binding.inputEmail.text.toString()
        auth.sendPasswordResetEmail(email).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this.context, "Code was sent to your email!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveAccount() {
        val sharedPref = requireActivity().getSharedPreferences("account", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        if (binding.chkSaveAcc.isChecked) {
            editor.putString("email", binding.inputEmail.text.toString())
            editor.putString("pass", binding.inputPass.text.toString())
            editor.putBoolean("isSave", true)
        }else {
            editor.putString("email", "")
            editor.putString("pass", "")
            editor.putBoolean("isSave", false)
        }
        editor.apply()
    }

    private fun getAccount() {
        val sharedPref = requireActivity().getSharedPreferences("account", Context.MODE_PRIVATE)
        val email: String = sharedPref.getString("email", "").toString()
        val pass = sharedPref.getString("pass", "")
            binding.chkSaveAcc.isChecked = true
            binding.inputEmail.setText(email)
            binding.inputPass.setText(pass)
    }


    //Chưa biết dùng + getAccount()
    private fun getDataSignUp() {
        binding.inputEmail.setText(args.email)
        binding.inputPass.setText(args.password)
    }

    //Log in with email
    private fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveAccount()
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    createUserOnDatabase()
                    val intent = Intent(this.context, NewsfeedActivity::class.java)
                    intent.putExtra("email", user?.email)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this.context,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

    private fun createUserOnDatabase() {
        val userID = auth.uid.toString()
        database.child(Constant.USER_TABLE_NAME).child(userID)
            .setValue("Hello").addOnCompleteListener {
                if (it.isSuccessful)
                    Toast.makeText(
                        context,
                        "Data was stored!",
                        Toast.LENGTH_SHORT
                    ).show()
                else
                    Toast.makeText(context, "Error!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkInput(): Boolean {
        if (binding.inputEmail.text.toString() == "") {
            binding.layoutInputEmail.error = "Requirement field!"
            return false
        }
        if (binding.inputPass.text.toString() == "") {
            binding.layoutPass.error = "Requirement field!"
            return false
        }
        return true
    }
}