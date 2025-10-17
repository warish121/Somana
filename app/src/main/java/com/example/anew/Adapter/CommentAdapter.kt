package com.example.anew.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.anew.DataClass.Comment
import com.example.anew.DataClass.Users
import com.example.anew.R
import com.example.anew.databinding.CommentLayoutBinding

class CommentAdapter(private val comments: MutableList<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    private val userMap = mutableMapOf<String, Users>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding =
            CommentLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        val user = userMap[comment.id] // Use comment's user ID to get user data
        holder.bind(comment, user)
    }

    override fun getItemCount(): Int = comments.size

    fun updateComments(newComments: List<Comment>) {
        comments.clear()
        comments.addAll(newComments)
        notifyDataSetChanged()
    }

    fun updateUser(userId: String, user: Users) {
        userMap[userId] = user
        notifyDataSetChanged()
    }

    inner class CommentViewHolder(private val binding: CommentLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment, user: Users?) {
            binding.username.text = comment.cName
            binding.commentText.text = comment.text

            // Load profile image if user data is available
            user?.profileImage?.let { imageUrl ->
                if (imageUrl.isNotEmpty()) {
                    Glide.with(binding.root)
                        .load(imageUrl)
                        .placeholder(R.drawable.defaultprofile)
                        .error(R.drawable.defaultprofile)
                        .fitCenter()
                        .circleCrop()
                        .into(binding.profileimage)
                } else {
                    binding.profileimage.setImageResource(R.drawable.defaultprofile)
                }
            } ?: run {
                // Set default image if user data is not available
                binding.profileimage.setImageResource(R.drawable.defaultprofile)
            }
        }
    }
}