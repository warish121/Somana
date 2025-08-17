package com.example.anew.DataClass

data class Post(
    val PostImg: String = "",
    val likes: Map<String, Boolean>? = null,
    val postId: String = "", // 🔥 Needed to reference the post
    val likeCount: Int = 0
)
