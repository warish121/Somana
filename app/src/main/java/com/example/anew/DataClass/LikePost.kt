package com.example.anew.DataClass

import android.widget.TextView

data class LikePost(

    val likeCount: Int = 0,
    val postId: String = "",  // Make sure this field exists
    val PostImg: String = "",

)
