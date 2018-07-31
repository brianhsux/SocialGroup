package com.brianhsu.socialgroup.Adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.brianhsu.socialgroup.Model.Post
import com.brianhsu.socialgroup.R
import com.mikhaellopez.circularimageview.CircularImageView

class AdapterPostSectioned(private val context: Context, private val posts: List<Post>,
                           private val itemClick: (Post) -> Unit) :
        RecyclerView.Adapter<AdapterPostSectioned.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindPost(context, posts[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.post_card_list_view, parent, false)
        return ViewHolder(view, itemClick)
    }

    override fun getItemCount(): Int {
        return posts.count()
    }

    inner class ViewHolder(itemView: View?, private val itemClick: (Post) -> Unit) : RecyclerView.ViewHolder(itemView) {

        var userCircularImage = itemView?.findViewById<CircularImageView>(R.id.postUserCircularImage)
        var userName = itemView?.findViewById<TextView>(R.id.postUserName)
        var timeStamp = itemView?.findViewById<TextView>(R.id.postTimeStamp)
        var postContent = itemView?.findViewById<TextView>(R.id.postContent)
        var postImage = itemView?.findViewById<ImageView>(R.id.postImage)

        fun bindPost(context: Context, posts: Post) {

        }
    }
}