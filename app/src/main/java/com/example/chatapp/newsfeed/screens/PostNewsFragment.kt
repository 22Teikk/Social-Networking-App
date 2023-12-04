package com.example.chatapp.newsfeed.screens

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.Constant
import com.example.chatapp.Converters
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentPostNewsBinding
import com.example.chatapp.model.Posts
import com.example.chatapp.model.Users
import com.example.chatapp.newsfeed.adapter.ListImagePostAdapter
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.bottomnavigation.BottomNavigationView
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

class PostNewsFragment : Fragment() {
    private lateinit var _binding: FragmentPostNewsBinding
    private val binding get() = _binding
    private var uriImagePost: ArrayList<Uri> = arrayListOf()
    private lateinit var imagePostAdapter: ListImagePostAdapter
    private lateinit var database: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var auth: FirebaseAuth
    private lateinit var user: Users
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPostNewsBinding.inflate(inflater, container, false)
        hideActionBar()

        auth = Firebase.auth
        database = Firebase.database.reference
        storageReference = FirebaseStorage.getInstance().getReference(Constant.POST_IMAGE_PATH)
        initUIrcv()
        initUIUser()
        binding.apply {
            backToFeeds.setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }
            btnPublish.setOnClickListener {
                publishPost()
            }
            selectImageGallery.setOnClickListener {
                selectImageFromGallery()
            }
            takePhoto.setOnClickListener {
                checkCameraPermission()
                takeAPhoto()
            }
            clearImage.setOnClickListener {
                binding.rcvImagePost.visibility = View.GONE
                binding.clearImage.visibility = View.GONE
                uriImagePost.clear()
                imagePostAdapter.notifyDataSetChanged()
            }
            locationPost.setOnClickListener {

            }
        }

        return binding.root
    }

    //For user information
    private fun initUIUser() {
        database.child(Constant.USER_TABLE_NAME).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userReceive = snapshot.child(auth.uid!!).getValue(Users::class.java)
                userReceive?.let {
                    user = it
                    binding.userName.text = user.name
                    Picasso.get().load(user.avatar).into(binding.avatarProfile)
                }
            }

            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
            }

        })
    }
    //For user information

    //For publish Post
    private fun publishPost() {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Uploading...")
        progressDialog.setMessage("Please wait...")
        progressDialog.setCancelable(false)

        if (binding.inputContentPost.text.isNotEmpty()) {
            if (uriImagePost.size != 0) {
                val postID = database.child(Constant.POST_TABLE_NAME).push().key
                if (postID != null) {
                    val imageTasks = mutableListOf<Task<Uri>>() // Danh sách các tác vụ lưu trữ ảnh

                    uriImagePost?.forEach { uri ->
                        val imageRef = storageReference.child(postID).child(uri.lastPathSegment!!)
                        val uploadTask = imageRef.putFile(uri)

                        // Thêm tác vụ lưu trữ vào danh sách
                        imageTasks.add(uploadTask.continueWithTask { task ->
                            if (!task.isSuccessful) {
                                task.exception?.let {
                                    throw it
                                }
                            }
                            // Lấy URL của ảnh sau khi lưu trữ thành công
                            imageRef.downloadUrl
                        })
                    }

                    // Sử dụng Task.whenAllSuccess để chờ tất cả các tác vụ lưu trữ hoàn tất
                    Tasks.whenAllSuccess<Uri>(imageTasks).addOnSuccessListener { downloadUrls ->
                        val listImage = downloadUrls.map { it.toString() }
                        val newPost = Posts(
                            postID,
                            binding.inputContentPost.text.toString(),
                            listImage,
                            user.uid
                        )


                        progressDialog.show()
                        database.child(Constant.POST_TABLE_NAME).child(postID)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (!snapshot.exists()) {
                                        database.child(Constant.POST_TABLE_NAME).child(postID)
                                            .setValue(newPost).addOnSuccessListener {
                                                progressDialog.dismiss()
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Publish successful!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                findNavController().navigate(R.id.feedFragment)
                                            }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(
                                        requireContext(),
                                        "Please choose the Image!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Your post needs at least 1 photo", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            Toast.makeText(requireContext(), "The title of Post is not empty!", Toast.LENGTH_SHORT)
                .show()
        }
    }
    //For publish Post

    private fun initUIrcv() {
        imagePostAdapter = ListImagePostAdapter(uriImagePost)
        binding.rcvImagePost.layoutManager = LinearLayoutManager(requireContext())
        binding.rcvImagePost.adapter = imagePostAdapter
    }

    //For take a photo
    val arl =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as Bitmap
                uriImagePost.add(Converters.getImageUriFromBitmap(requireContext(), imageBitmap))
                binding.rcvImagePost.visibility = View.VISIBLE
                binding.clearImage.visibility = View.VISIBLE
                imagePostAdapter.notifyDataSetChanged()
            }
        }

    override fun onResume() {
        hideActionBar()
        super.onResume()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.CAMERA),
                123
            )
        } else {
            takeAPhoto()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.size > 0 && grantResults[0] ==
            PackageManager.PERMISSION_GRANTED && requestCode == 123
        ) {
            //permission from popup was granted
            takeAPhoto()
        }
    }

    private fun takeAPhoto() {
        arl.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
    }
    //For take a photo


    //selectImageFromGallery
    val imagePicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data != null) {
                    binding.rcvImagePost.visibility = View.VISIBLE
                    binding.clearImage.visibility = View.VISIBLE
                    val clipData = data.clipData
                    if (clipData != null) {
                        for (i in 0 until clipData.itemCount) {
                            val imageUri = clipData.getItemAt(i).uri
                            uriImagePost.add(imageUri)
                            imagePostAdapter.notifyDataSetChanged()
                        }
                    } else {
                        val imageUri = data.data
                        if (imageUri != null) {
                            uriImagePost.add(imageUri)
                        }
                    }
                    imagePostAdapter.notifyDataSetChanged()
                }
            }
        }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        imagePicker.launch(intent)
    }
    //selectImageFromGallery

    private fun hideActionBar() {
        val actionbar = activity?.findViewById<LinearLayout>(R.id.actionbarNews)
        if (actionbar != null) {
            actionbar.visibility = View.GONE
        }
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationNews)
        if (bottomNav != null) {
            bottomNav.visibility = View.GONE
        }
    }

    override fun onStop() {
        val actionbar = activity?.findViewById<LinearLayout>(R.id.actionbarNews)
        if (actionbar != null) {
            actionbar.visibility = View.VISIBLE
        }
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationNews)
        if (bottomNav != null) {
            bottomNav.visibility = View.VISIBLE
        }
        super.onStop()
    }
}