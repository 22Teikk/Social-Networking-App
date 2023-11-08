package com.example.chatapp.newsfeed.screens

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.chatapp.Constant
import com.example.chatapp.Converters
import com.example.chatapp.Main_Activity
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentProfileBinding
import com.example.chatapp.model.Users
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso


class ProfileFragment : Fragment() {
    private lateinit var _binding: FragmentProfileBinding
    private lateinit var imageAvatar: ShapeableImageView
    private val binding get() = _binding
    private lateinit var auth: FirebaseAuth
    private val uid get() = auth.uid!!
    private lateinit var database: DatabaseReference
    private lateinit var userUpdate: Users
    private var uriAvatar: Uri? = null
    private lateinit var storageRef: StorageReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        auth = Firebase.auth
        database = Firebase.database.reference
        storageRef = FirebaseStorage.getInstance().getReference(Constant.USER_IMAGE_PATH)

        initUI()
        //Log Out
        binding.logOut.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), Main_Activity::class.java))
        }
        //Change Information
        binding.changeInformation.setOnClickListener {
            checkCameraPermission()
            updateProfileDialog()
        }

        return binding.root
    }

    private fun updateProfileDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_update_profile)

        val window = dialog.window
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height =
            WindowManager.LayoutParams.WRAP_CONTENT // Hoặc MATCH_PARENT nếu bạn muốn chiếm hết màn hình
        window?.attributes = params
        val rootView = activity?.findViewById<View>(R.id.overlayLayout)
        rootView?.let {
            it.visibility = View.VISIBLE
        }
        setUIandEventDialog(dialog, rootView)
        dialog.show()
    }

    private fun setUIandEventDialog(dialog: Dialog, rootView: View?) {
        val genders = arrayOf("Male", "Female", "Other")
        val ages = (1..100).map { it.toString() }.toTypedArray()

        val close = dialog.findViewById<ImageButton>(R.id.ibClose)
        val cancel = dialog.findViewById<Button>(R.id.cancel)
        val imageGallery = dialog.findViewById<ShapeableImageView>(R.id.selectImageGallery)
        imageAvatar = dialog.findViewById<ShapeableImageView>(R.id.avatarProfile)
        val imageCamera = dialog.findViewById<ShapeableImageView>(R.id.takePhoto)
        val name = dialog.findViewById<TextInputEditText>(R.id.inputName)
        val spnGender = dialog.findViewById<Spinner>(R.id.spnGender)
        val spnAge = dialog.findViewById<Spinner>(R.id.spnAge)
        val save = dialog.findViewById<Button>(R.id.update)

        Picasso.get().load(userUpdate.avatar).into(imageAvatar)
        name.setText(userUpdate.name.toString())

        //Select Image From Galary
        imageGallery.setOnClickListener {
            selectImage()
        }
        //Take a photo
        imageCamera.setOnClickListener {
            takePhoto()
        }

        //For Gender
        var gender = ""
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, genders)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spnGender.adapter = adapter
        spnGender.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                gender = genders[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                gender = "None"
            }
        }

        //For Age
        var age = 0
        val adapterAge = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, ages)
        adapterAge.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnAge.adapter = adapterAge
        spnAge.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                age = ages[position].toInt()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                age = 0
            }
        }


        close.setOnClickListener {
            dialog.dismiss()
            rootView?.let {
                it.visibility = View.GONE
            }
        }

        cancel.setOnClickListener {
            dialog.dismiss()
            rootView?.let {
                it.visibility = View.GONE
            }
        }

        save.setOnClickListener {
            rootView?.let {
                it.visibility = View.GONE
            }
            if (!name.text.isNullOrEmpty()) {
                saveOnDatabase(name.text.toString(), gender, age)
            }
            dialog.dismiss()
        }
    }

    private fun saveOnDatabase(name: String, gender: String, age: Int) {
        uriAvatar?.let {
            storageRef.child(uid).putFile(it)
                .addOnSuccessListener { task ->
                    task.metadata!!.reference!!.downloadUrl
                        .addOnCompleteListener { tempUrl ->
                            val imgUrl = tempUrl.result.toString()
                            val contacts =
                                Users(uid, age, name, imgUrl, gender)
                            database.child(Constant.USER_TABLE_NAME).child(uid)
                                .setValue(contacts).addOnCompleteListener {
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
                }
        }
    }

    val arl = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap
            uriAvatar = Converters.getImageUriFromBitmap(requireContext(), imageBitmap)
            imageAvatar.setImageURI(uriAvatar)
        }
    }
    private fun takePhoto() {
        arl.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
//        startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), 567)
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(android.Manifest.permission.CAMERA),
                123)
        } else {
            takePhoto()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.size > 0 && grantResults[0] ==
            PackageManager.PERMISSION_GRANTED){
            //permission from popup was granted
            takePhoto()
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        imageAvatar.setImageURI(it)
        uriAvatar = it
    }

    private fun selectImage() {
        pickImage.launch("image/*")
    }

    private fun initUI() {
        database.child(Constant.USER_TABLE_NAME).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.child(uid).getValue(Users::class.java)
                user?.let {
                    userUpdate = it
                    binding.userName.text = user.name
                    Picasso.get().load(user.avatar).into(binding.avatarProfile)
                    binding.countPost.text = user.nPosts.toString()
                    binding.countFriend.text = user.nFriends.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
            }

        })
    }

}