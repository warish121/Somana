package com.example.anew

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide

class PostGridAdapter(
    private val context: Context,
    private val postUrls: List<String>
) : BaseAdapter() {

    override fun getCount(): Int = postUrls.size

    override fun getItem(position: Int): String = postUrls[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val imageView = convertView as? ImageView ?: ImageView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            adjustViewBounds = true
        }

        try {
            Glide.with(context)
                .load(postUrls[position])
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(imageView)
        } catch (e: Exception) {
            Log.e("GlideError", "Error loading image: ${e.message}")
        }

        return imageView
    }
}