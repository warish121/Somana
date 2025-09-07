package com.example.anew.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.anew.DataClass.Comment
import com.example.anew.DataClass.Users
import com.example.anew.databinding.CommentLayoutBinding

class CommentAdapter(private val comments: MutableList<Comment>, private val user: List<Users>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding =
            CommentLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position], user[position])
    }

    override fun getItemCount(): Int = comments.size

    fun updateComments(newComments: List<Comment>) {
        comments.clear()
        comments.addAll(newComments)
        notifyDataSetChanged()
    }

    inner class CommentViewHolder(private val binding: CommentLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment, user: Users) {
            binding.username.text = comment.cName
            binding.commentText.text = comment.text

            Glide.with(binding.root)
                .load(user.profileImage)
                .into(binding.profileImage)


        }
    }
}