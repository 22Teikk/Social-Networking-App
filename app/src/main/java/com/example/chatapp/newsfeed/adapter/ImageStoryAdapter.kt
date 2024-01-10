import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.PagerAdapter
import com.example.chatapp.Constant
import com.example.chatapp.databinding.ImageStoryItemBinding
import com.example.chatapp.model.Stories
import com.example.chatapp.newsfeed.screens.ViewStoryFragmentDirections
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.util.Timer
import java.util.TimerTask

class ImageStoryAdapter(
    private val context: Context,
    private val storyList: List<Stories>,
    private val navController: NavController
) : PagerAdapter() {

    private var autoSlideTimer: Timer? = null
    private var currentViewPagerPosition = 0

    override fun getCount(): Int {
        return storyList.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val binding = ImageStoryItemBinding.inflate(inflater, container, false)
        val view = binding.root
        viewStory(storyList[0])
        if (storyList[position].uid == Firebase.auth.uid) {
            getCountViewStory(
                binding, storyList[position].storyID.toString(),
                Firebase.auth.uid.toString()
            )
        }

        Picasso.get().load(storyList[position].imageURL).into(binding.imageStoryView)

        binding.showViewer.setOnClickListener {
            // Stop auto-slide when "showViewer" is clicked
            stopAutoSlide()
            val action = storyList[position].storyID?.let { it1 ->
                ViewStoryFragmentDirections.actionViewStoryFragmentToFollowAndLikeFragment(
                    "Viewer",
                    it1,
                    storyList[position].uid!!
                )
            }
            action?.let { it1 -> navController.navigate(it1) }
        }

        binding.delStory.setOnClickListener {
            Firebase.database.reference.child(Constant.STORY_TABLE_NAME)
                .child(storyList[position].uid.toString())
                .child(storyList[position].storyID.toString()).removeValue()
            Toast.makeText(context, "Delete Success", Toast.LENGTH_SHORT).show()
            navController.navigateUp()
        }

        container.addView(view)
        return view
    }

    private fun getCountViewStory(binding: ImageStoryItemBinding, storyID: String, auth: String) {
        binding.countViewLayout.visibility = View.VISIBLE
        Firebase.database.reference.child(Constant.STORY_TABLE_NAME)
            .child(auth)
            .child(storyID)
            .child("viewer")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.childrenCount.toInt()
                    if (count > 1)
                        binding.countView.text = "${count - 1} people view your story"
                    else binding.countView.text = "None view your story"
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun stopAutoSlide() {
        autoSlideTimer?.cancel()
        autoSlideTimer = null
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        super.setPrimaryItem(container, position, `object`)
        if (currentViewPagerPosition != position) {
            currentViewPagerPosition = position
            viewStory(storyList[currentViewPagerPosition])
        }
    }

    private fun viewStory(story: Stories) {
        var newStory = story
        newStory.viewer = story.viewer ?: mutableListOf()
        val setViewer: MutableSet<String> = mutableSetOf()
        if (story.viewer!=null) setViewer.addAll(story.viewer!!)
        setViewer.add(Firebase.auth.uid.toString())
        newStory.viewer = setViewer.toList()
        Firebase.database.reference.child(Constant.STORY_TABLE_NAME).child(story.uid.toString()).child(story.storyID.toString()).setValue(newStory)
    }
}
