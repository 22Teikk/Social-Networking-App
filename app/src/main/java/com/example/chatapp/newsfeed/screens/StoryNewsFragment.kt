package com.example.chatapp.newsfeed.screens

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.example.chatapp.Constant
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentStoryNewsBinding
import com.example.chatapp.model.Posts
import com.example.chatapp.model.Stories
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class StoryNewsFragment : Fragment() {
    private lateinit var _binding: FragmentStoryNewsBinding
    private val binding get() = _binding
    private var uriImage: Uri? = null
    private lateinit var database: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var auth: FirebaseAuth

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let {
            binding.imgStory.setImageURI(it)
            uriImage = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        hideActionBar()
        _binding = FragmentStoryNewsBinding.inflate(layoutInflater, container, false)
        pickImage.launch("image/*")
        auth = com.google.firebase.ktx.Firebase.auth
        database = com.google.firebase.ktx.Firebase.database.reference
        storageReference = FirebaseStorage.getInstance().getReference(Constant.STORY_IMAGE_PATH)
        binding.apply {
            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }

            customViewStory.setOnClickListener {

            }

            changeImage.setOnClickListener {
                pickImage.launch("image/*")
            }

            btnPreview.setOnClickListener {
                if (btnPreview.contentDescription == "showPreview") {
                    btnPreview.setImageResource(R.drawable.hidepreview)
                    btnPreview.contentDescription = "hidePreview"
                    btnBack.visibility = View.GONE
                    customViewStory.visibility = View.GONE
                    changeImage.visibility = View.GONE
                    btnShare.visibility = View.GONE
                } else {
                    btnPreview.setImageResource(R.drawable.showpreview)
                    btnPreview.contentDescription = "showPreview"
                    btnBack.visibility = View.VISIBLE
                    customViewStory.visibility = View.VISIBLE
                    changeImage.visibility = View.VISIBLE
                    btnShare.visibility = View.VISIBLE
                }

            }

            btnShare.setOnClickListener {
                uploadingStory()
            }
        }
        return binding.root
    }

    private fun uploadingStory() {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Publishing...")
        progressDialog.setMessage("Please wait...")
        progressDialog.setCancelable(false)
        val postID = database.child(Constant.POST_TABLE_NAME).push().key
        val imageTasks = mutableListOf<Task<Uri>>() // Danh sách các tác vụ lưu trữ ảnh
        if (uriImage != null) {
            val imageRef = storageReference.child(System.currentTimeMillis().toString() + ".jpg")
            val uploadTask = imageRef.putFile(uriImage!!)
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
            val listImage = downloadUrls[0].toString()
            val storyID = database.push().key.toString()
            val newStory = Stories(
                listImage,
                System.currentTimeMillis(),
                System.currentTimeMillis() + 86400000,
                storyID,
                auth.uid
            )
            progressDialog.show()
            database.child(Constant.STORY_TABLE_NAME).child(storyID)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.exists()) {
                            database.child(Constant.STORY_TABLE_NAME).child(storyID)
                                .setValue(newStory).addOnSuccessListener {
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

override fun onStart() {
    hideActionBar()
    super.onStart()
}

override fun onPause() {
    val actionbar = activity?.findViewById<LinearLayout>(R.id.actionbarNews)
    if (actionbar != null) {
        actionbar.visibility = View.VISIBLE
    }
    val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationNews)
    if (bottomNav != null) {
        bottomNav.visibility = View.VISIBLE
    }
    super.onPause()
}

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

}