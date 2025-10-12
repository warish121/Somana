package com.example.anew.Adapter

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.anew.Comment_Section
import com.example.anew.DataClass.Post
import com.example.anew.DataClass.Users
import com.example.anew.Profile_
import com.example.anew.R
import com.example.anew.databinding.HomeItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PostAdapter(
    private val users: List<Users>,
    private val profileImg: List<String>,
    private val postedPost: List<Post>,
    private val onUserNameClickListener: (Users) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val database = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    init {
        if (users.size != profileImg.size || postedPost.size != users.size) {
            throw IllegalArgumentException("All lists must have the same size")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = HomeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onUserNameClickListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(users[position], profileImg[position], postedPost[position])
    }

    override fun getItemCount(): Int = postedPost.size

    inner class PostViewHolder(
        private val binding: HomeItemBinding,
        private val onUserNameClickListener: (Users) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var isLiked = false
        private var likeCount = 0
        private var shareCount = 0

        fun bind(user: Users, profileImg: String, post: Post) {
            binding.name1.text = user.name
            likeCount = post.likeCount
            binding.likeText.text = likeCount.toString()
            binding.shareText.text = shareCount.toString()
            shareCount = post.shareCount

            // Set click listener on the user name
            binding.name1.setOnClickListener {
                onUserNameClickListener(user)
            }
            binding.shareLayout.setOnClickListener {
                share()

                sharecount(post.postId, user.uid)
            }





            // Set click listener on the profile image
            binding.ImageProfile.setOnClickListener {
                onUserNameClickListener(user)
            }

            Glide.with(binding.root)
                .load(profileImg.ifEmpty { R.drawable.defaultprofile })
                .placeholder(R.drawable.defaultprofile)
                .error(R.drawable.defaultprofile)
                .circleCrop()
                .into(binding.ImageProfile)

            Glide.with(binding.root)
                .load(post.PostImg)
                .placeholder(R.drawable.defaultprofile)
                .error(R.drawable.defaultprofile)
                .into(binding.postImage)

            // Check if current user has liked this post
            checkIfLiked(post.postId, user.uid)
            fetchshareCount(post.postId, user.uid)

            binding.likeLayout.setOnClickListener {
                toggleLike(post.postId, user.uid)
            }

            binding.commentLayout.setOnClickListener {
                val intent = Intent(binding.commentLayout.context, Comment_Section::class.java)
                binding.root.context.startActivity(intent)
            }
        }

        private fun checkIfLiked(postId: String, postOwnerId: String) {
            val currentUser = auth.currentUser?.uid ?: return

            database.child("users").child(postOwnerId).child("posts").child(postId)
                .child("likes").child(currentUser)
                .get()
                .addOnSuccessListener { snapshot ->
                    isLiked = snapshot.exists()
                    val likeCount = snapshot.child("likeCount").getValue(Int::class.java) ?: 0
                    binding.likeText.text = likeCount.toString()
                    updateLikeButtonUI()
                }
                .addOnFailureListener { exception ->
                    // Handle error
                }
        }

        private fun toggleLike(postId: String, postOwnerId: String) {
            val currentUser = auth.currentUser?.uid ?: return
            val postRef = database.child("users").child(postOwnerId).child("posts").child(postId)
                .child("likes").child(currentUser)

            if (isLiked) {
                postRef.child("likeCount").setValue(likeCount--)
                    .addOnSuccessListener {
                        likeCount > 0
                        isLiked = false
                        updateLikeButtonUI()
                    }
            } else {
                postRef.child("likeCount").setValue(likeCount + 1)
                    .addOnSuccessListener {
                        isLiked = true
                        updateLikeButtonUI()
                    }
            }
        }

        private fun updateLikeButtonUI() {
            binding.likeImg.setImageResource(
                if (isLiked) R.drawable.likedicon else R.drawable.like
            )
        }

        private fun share(){
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, "Check out this awesome app!")
            binding.root.context.startActivity(Intent.createChooser(intent, "Share via"))
        }
        private fun sharecount(postId: String, postOwnerId: String) {
            val currentUser = auth.currentUser?.uid ?: return
            val postRef = database.child("users").child(postOwnerId).child("posts").child(postId)
                .child("shares").child(currentUser)
            postRef.child("shareCount").setValue(shareCount + 1)

        }
        private fun fetchshareCount(postId: String, postOwnerId: String) {
            val currentUser = auth.currentUser?.uid ?: return

            database.child("users").child(postOwnerId).child("posts").child(postId)
                .child("shares").child(currentUser)
                .get()
                .addOnSuccessListener { snapshot ->

                    val shareCount = snapshot.child("shareCount").getValue(Int::class.java) ?: 0
                    binding.shareText.text = shareCount.toString()
                    updateLikeButtonUI()
                }
                .addOnFailureListener { exception ->
                    // Handle error
                }
        }

    }
}