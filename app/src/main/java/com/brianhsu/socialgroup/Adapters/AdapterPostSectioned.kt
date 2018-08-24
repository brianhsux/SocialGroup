package com.brianhsu.socialgroup.Adapters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.brianhsu.socialgroup.model.Post
import com.brianhsu.socialgroup.R
import com.brianhsu.socialgroup.Utilities.BROADCAST_POST_MORE_INFO_DIALOG
import com.brianhsu.socialgroup.Utilities.TAG
import com.brianhsu.socialgroup.Utilities.Tools
import com.cloudinary.Transformation
import com.mikhaellopez.circularimageview.CircularImageView
import com.cloudinary.android.MediaManager
import java.text.SimpleDateFormat
import java.util.*

class AdapterPostSectioned(private val context: Context, private val posts: List<Post>,
                           private val itemClick: (Post) -> Unit) :
        RecyclerView.Adapter<AdapterPostSectioned.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindPost(context, posts[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
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
        var postMoreInfoBtn = itemView?.findViewById<ImageButton>(R.id.postMoreInfoBtn)


        fun bindPost(context: Context, posts: Post) {
            val authorImageId:String = posts.authorImageId
            val authorImageUrl:String = MediaManager.get().url().generate("userImage/$authorImageId.jpg")
            Tools.displayImageOriginal(context, userCircularImage, authorImageUrl)

            val postImageSourcePath:String = posts.postImageId
            val transformation: Transformation<*> = MediaManager.get().url().transformation().width(250).height(250).gravity("faces").crop("fill")
            val postImageUrl:String = MediaManager.get().url().transformation(transformation).generate("$postImageSourcePath.webp")

            Tools.displayImageOriginal(context, postImage, postImageUrl)

            userName?.text = posts.authorName
            timeStamp?.text = formatPostDate(posts.postTime)
            postContent?.text = posts.postContent

            postMoreInfoBtn?.setOnClickListener {
                Toast.makeText(context, "Ｍore info with postId: ${posts.postId}", Toast.LENGTH_LONG).show()

                val sendPostAction = Intent(BROADCAST_POST_MORE_INFO_DIALOG)
                val bundle = Bundle()
                bundle.putString("EXTRA_POST_ID", posts.postId)
                sendPostAction.putExtra("EXTRA_BUNDLE", bundle)

                LocalBroadcastManager.getInstance(context).sendBroadcast(sendPostAction)
            }
        }

        private fun formatPostDate(postDate: String?): String {
            var finalStr = ""
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
                inputFormat.timeZone = TimeZone.getTimeZone("GMT")
                val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
                finalStr = outputFormat.format(inputFormat.parse(postDate))
            } catch (e:Exception) {
                Log.d(TAG, "AdapterPostSectioned(), error: ${e.printStackTrace()}")
            }

            return finalStr
        }
    }
}