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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.example.chatapp.Constant
import com.example.chatapp.Converters
import com.example.chatapp.Main_Activity
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentProfileBinding
import com.example.chatapp.model.Posts
import com.example.chatapp.model.Users
import com.example.chatapp.newsfeed.adapter.OptionListProfileAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.tabs.TabLayout
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
import com.google.protobuf.Value
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
    private val args: ProfileFragmentArgs by navArgs()
    private lateinit var profileID: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        auth = Firebase.auth
        database = Firebase.database.reference
        storageRef = FirebaseStorage.getInstance().getReference(Constant.USER_IMAGE_PATH)

        if (args.profileID.equals("")) profileID = auth.uid.toString()
        else {
            profileID = args.profileID
            binding.apply {
                logOut.visibility = View.GONE
                isFollowing(profileID, changeInformation)
                hideActionBar()
                actionBarProfile.visibility = View.VISIBLE
                backToSearch.setOnClickListener {
                    findNavController().navigateUp()
                }
            }
        }
        initUI()
        countPosts()
        countFollowers()
        countFollowing()
        //Log Out
        binding.logOut.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), Main_Activity::class.java))
        }
        //Change Information
        binding.changeInformation.setOnClickListener {
            if (binding.changeInformation.contentDescription.equals("editProfile")) {
                checkCameraPermission()
                updateProfileDialog()
            }else if (binding.changeInformation.contentDescription.equals("Follow")) {
                database.child(Constant.FOLLOW_TABLE_NAME).child(auth.uid!!)
                    .child(Constant.FOLLOW_TABLE_FOLLOWING).child(
                        profileID
                    ).setValue(true)
                database.child(Constant.FOLLOW_TABLE_NAME).child(profileID)
                    .child(Constant.FOLLOW_TABLE_FOLLOWER).child(
                        auth.uid!!
                    ).setValue(true)
            }else if (binding.changeInformation.contentDescription.equals("Following")){
                database.child(Constant.FOLLOW_TABLE_NAME).child(auth.uid!!)
                    .child(Constant.FOLLOW_TABLE_FOLLOWING).child(
                        profileID
                    ).removeValue()
                database.child(Constant.FOLLOW_TABLE_NAME).child(profileID)
                    .child(Constant.FOLLOW_TABLE_FOLLOWER).child(
                        auth.uid!!
                    ).removeValue()
            }
        }

        return binding.root
    }

    private fun countFollowing() {
        database.child(Constant.FOLLOW_TABLE_NAME).child(profileID).child(Constant.FOLLOW_TABLE_FOLLOWING)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.childrenCount.toInt() == 1) binding.countFollowing.text = "0"
                    binding.countFollowing.text = (snapshot.childrenCount - 1).toString()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun countFollowers() {
        database.child(Constant.FOLLOW_TABLE_NAME).child(profileID).child(Constant.FOLLOW_TABLE_FOLLOWER)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.childrenCount.toInt() == 1) binding.countFollowers.text = "0"  
                    else binding.countFollowers.text = (snapshot.childrenCount - 1).toString()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun countPosts() {
        database.child(Constant.POST_TABLE_NAME).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var i = 0
                for (data in snapshot.children) {
                    val post = data.getValue(Posts::class.java)
                    if (post?.publisher.equals(profileID))
                        i++
                }
                binding.countPost.text = i.toString()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun isFollowing(userID: String, imageFollow: ImageView) {
        database.child(Constant.FOLLOW_TABLE_NAME).child(auth.uid!!)
            .child(Constant.FOLLOW_TABLE_FOLLOWING)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child(userID).exists()) {
                        imageFollow.setImageResource(R.drawable.baseline_done_24)
                        imageFollow.contentDescription = "Following"
                    }
                    else {
                        imageFollow.setImageResource(R.drawable.baseline_add_24)
                        imageFollow.contentDescription = "Follow"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
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
        val bio = dialog.findViewById<TextInputEditText>(R.id.inputBio)
        val spnGender = dialog.findViewById<Spinner>(R.id.spnGender)
        val spnAge = dialog.findViewById<Spinner>(R.id.spnAge)
        val save = dialog.findViewById<Button>(R.id.update)

        Picasso.get().load(userUpdate.avatar).into(imageAvatar)
        name.setText(userUpdate.name.toString())
        bio.setText(userUpdate.bio.toString())

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
                saveOnDatabase(name.text.toString(), gender, age, bio.text.toString())
            }
            dialog.dismiss()
        }
    }

    private fun saveOnDatabase(name: String, gender: String, age: Int, bio: String) {
        if (uriAvatar != null) {
            storageRef.child(uid).putFile(uriAvatar!!)
                .addOnSuccessListener { task ->
                    task.metadata!!.reference!!.downloadUrl
                        .addOnCompleteListener { tempUrl ->
                            val imgUrl = tempUrl.result.toString()
                            val contacts =
                                Users(uid, age, name, imgUrl, gender, bio = bio)
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
        }else {
            val contacts =
                Users(uid, age, name, userUpdate.avatar, gender, bio = bio)
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
                val user = snapshot.child(profileID).getValue(Users::class.java)
                user?.let {
                    userUpdate = it
                    binding.userName.text = user.name
                    Picasso.get().load(user.avatar).into(binding.avatarProfile)
                    countPosts()
                    if (user.bio == "" || user.bio.equals(null)) binding.userBio.visibility = View.GONE
                    else {
                        binding.userBio.visibility = View.VISIBLE
                        binding.userBio.text = user.bio
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
            }

        })

        binding.optionsViewProfile.addTab(binding.optionsViewProfile.newTab().setIcon(R.drawable.baseline_grid_on_24))
        if (profileID.equals(auth.uid))
            binding.optionsViewProfile.addTab(binding.optionsViewProfile.newTab().setIcon(R.drawable.outline_share_24))
        val adapter = OptionListProfileAdapter(childFragmentManager, lifecycle, profileID)
        binding.viewPager2.adapter = adapter
        binding.optionsViewProfile.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    binding.viewPager2.currentItem = tab.position
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })
        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.optionsViewProfile.selectTab(binding.optionsViewProfile.getTabAt(position))
            }
        })
    }

    override fun onStart() {
        if (profileID.equals(args.profileID))
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