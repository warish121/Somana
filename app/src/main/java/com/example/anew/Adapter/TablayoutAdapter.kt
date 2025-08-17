package com.example.anew.Adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.anew.DataClass.TabData
import com.example.anew.R

class TablayoutAdapter(
    private val context: Context,
    fragmentManager: FragmentManager,
    val list: ArrayList<Fragment>
) : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return list[position]
    }

    override fun getCount(): Int {
        return list.size
    }

    // For page title section (optional if you only want icons)
    override fun getPageTitle(position: Int): CharSequence? {
        return title[position]
    }

    // Companion object with tab data
    companion object {
        val tabData = listOf(
            TabData("Post", R.drawable.post),
            TabData("About", R.drawable.about_icon)
        )
        val title = tabData.map { it.title }
    }
}