package com.example.anew.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.anew.DataClass.Users
import com.example.anew.R
import com.example.anew.databinding.SearchItemBinding

class SearchAdapter(
    private var users: List<Users>,
) : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    // Add click listener interface
    var onItemClick: ((Users) -> Unit)? = null

    private var filteredList: List<Users> = ArrayList(users) // Initialize with copy

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val binding = SearchItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(filteredList[position])
    }

    override fun getItemCount(): Int = filteredList.size

    fun updateList(newList: List<Users>) {
        users = newList
        filteredList = ArrayList(newList) // Create a new copy
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val trimmedQuery = query.trim()
        filteredList = if (trimmedQuery.isEmpty()) {
            ArrayList(users) // Return full list
        } else {
            users.filter { user ->
                user.name?.trim()?.contains(trimmedQuery, ignoreCase = true) == true
            }
        }
        notifyDataSetChanged()
    }

    inner class SearchViewHolder(private val binding: SearchItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: Users) {
            binding.nametext.text = user.name ?: "Unknown"

            Glide.with(binding.root)
                .load(user.profileImage)
                .placeholder(R.drawable.defaultprofile)
                .error(R.drawable.defaultprofile)
                .centerCrop()
                .circleCrop()
                .into(binding.imageview)


            binding.root.setOnClickListener {
                if (user.uid != null) {
                    onItemClick?.invoke(user)
                } else {
                    Log.e("SearchAdapter", "Clicked user has no UID!")
                }
            }
        }
    }
}
