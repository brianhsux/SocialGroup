package com.brianhsu.socialgroup.Adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.brianhsu.socialgroup.model.Post
import com.brianhsu.socialgroup.R
import com.brianhsu.socialgroup.Utilities.TAG
import com.brianhsu.socialgroup.Utilities.Tools
import com.cloudinary.Transformation
import com.cloudinary.Url
import com.mikhaellopez.circularimageview.CircularImageView
import com.cloudinary.android.MediaManager

class AdapterPostSectioned(private val context: Context, private val posts: List<Post>,
                           private val itemClick: (Post) -> Unit) :
        RecyclerView.Adapter<AdapterPostSectioned.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        Log.d(TAG, "AdapterPostSectioned>>>onBindViewHolder()")
        holder.bindPost(context, posts[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        Log.d(TAG, "AdapterPostSectioned>>>onCreateViewHolder()")
        val view = LayoutInflater.from(context).inflate(R.layout.post_card_list_view, parent, false)
        return ViewHolder(view, itemClick)
//        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return posts.count()
    }

    inner class ViewHolder(itemView: View?, private val itemClick: (Post) -> Unit) : RecyclerView.ViewHolder(itemView) {
//    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        var userCircularImage = itemView?.findViewById<CircularImageView>(R.id.postUserCircularImage)
        var userName = itemView?.findViewById<TextView>(R.id.postUserName)
        var timeStamp = itemView?.findViewById<TextView>(R.id.postTimeStamp)
        var postContent = itemView?.findViewById<TextView>(R.id.postContent)
        var postImage = itemView?.findViewById<ImageView>(R.id.postImage)


        fun bindPost(context: Context, posts: Post) {
//            Tools.displayImageOriginal(context, userCircularImage, posts.authorImage)
//            Tools.displayImageOriginal(context, postImage, posts.postImage)
            val authorImageId:String = posts.authorImageId
            val authorImageUrl:String = MediaManager.get().url().generate("userImage/$authorImageId.jpg")
            Tools.displayImageOriginal(context, userCircularImage, authorImageUrl)

            val postImageId:String = posts.postImageId
//            val postImageUrl:String = MediaManager.get().url().generate("$postImageId.webp")
            val transformation: Transformation<*> = MediaManager.get().url().transformation().width(250).height(250).gravity("faces").crop("fill")
            val postImageUrl:String = MediaManager.get().url().transformation(transformation).generate("$postImageId.webp")

            Tools.displayImageOriginal(context, postImage, postImageUrl)

            userName?.text = posts.authorName
            timeStamp?.text = posts.postTime
            postContent?.text = posts.postContent

//            Log.d(TAG, "AdapterPostSectioned>>>bindPost()")
        }
    }
}