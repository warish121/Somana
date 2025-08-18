package com.example.anew

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.anew.Adapter.CommentAdapter
import com.example.anew.DataClass.Comment
import com.example.anew.databinding.ActivityCommentSectionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Comment_Section : AppCompatActivity() {

    private lateinit var binding: ActivityCommentSectionBinding
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var postId: String
    private val commentList = mutableListOf<Comment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentSectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        postId = intent.getStringExtra("postId") ?: ""

        commentAdapter = CommentAdapter(commentList)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@Comment_Section)
            adapter = commentAdapter
        }

        loadComments()

        binding.commentsend.setOnClickListener {
            uploadComment()
        }
    }

    private fun loadComments() {
        FirebaseDatabase.getInstance().getReference("comments")
            .child(postId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tempList = mutableListOf<Comment>()
                    for (data in snapshot.children) {
                        val comment = data.getValue(Comment::class.java)
                        comment?.let { tempList.add(it) }
                    }
                    commentAdapter.updateComments(tempList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@Comment_Section,
                        "Failed to load comments",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun uploadComment() {
        val commentText = binding.editText.text.toString().trim()
        if (commentText.isEmpty()) {
            Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = auth.currentUser ?: run {
            Toast.makeText(this, "You need to login first", Toast.LENGTH_SHORT).show()
            return
        }

        // Get user name from database
        FirebaseDatabase.getInstance().getReference("users")
            .child(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userName = snapshot.child("name").value.toString()
                    val commentId = FirebaseDatabase.getInstance().reference
                        .child("comments")
                        .child(postId)
                        .push().key

                    val comment = Comment(
                        text = commentText,
                        cName = userName,
                        id = currentUser.uid

                    )

                    FirebaseDatabase.getInstance().getReference("comments")
                        .child(postId)
                        .child(commentId!!)
                        .setValue(comment)
                        .addOnSuccessListener {
                            binding.editText.text.clear()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this@Comment_Section,
                                "Failed to post comment",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@Comment_Section,
                        "Failed to get user info",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}