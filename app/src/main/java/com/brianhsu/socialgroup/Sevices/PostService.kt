package com.brianhsu.socialgroup.Sevices

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.brianhsu.socialgroup.Utilities.TAG
import com.brianhsu.socialgroup.Utilities.URL_CREATE_POST
import com.brianhsu.socialgroup.Utilities.URL_READ_POST
import com.brianhsu.socialgroup.controller.App
import com.brianhsu.socialgroup.model.Post
import org.json.JSONException
import org.json.JSONObject

object PostService {

    val posts = ArrayList<Post>()

    fun createPost(authorEmail: String, authorName: String, authorImage: String, postImage: String,
                   postContent: String, postTime: String, complete: (Boolean) -> Unit) {
        Log.d(TAG, "PostService>>>createPost()-1")
        val jsonBody = JSONObject()

        jsonBody.put("authorEmail", authorEmail)
        jsonBody.put("authorName", authorName)
        jsonBody.put("authorImage", authorImage)
        jsonBody.put("postImage", postImage)
        jsonBody.put("postContent", postContent)
        jsonBody.put("postTime", postTime)
        val requestBody = jsonBody.toString()

        val createRequest = object : JsonObjectRequest(Method.POST, App.prefs.URL_CREATE_POST, null, Response.Listener { response ->

            try {
                complete(true)
            } catch (e: JSONException) {
                Log.d(TAG, "EXC: " + e.localizedMessage)
                complete(false)
            }
        }, Response.ErrorListener { error ->
            Log.d(TAG, "Could not add post $error")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.prefs.authToken}")
                return headers
            }
        }

        App.prefs.requestQueue.add(createRequest)
    }

    fun readAllPosts(context: Context, complete: (Boolean) -> Unit) {

        val readRequest = object: JsonArrayRequest(Method.GET, App.prefs.URL_READ_POST, null, Response.Listener { response ->
            clearPosts()

            try {
//                Log.d(TAG, "readAllPosts: $response")

                for (x in 0 until response.length()) {
                    val post = response.getJSONObject(x)

                    val authorEmail = post.getString("authorEmail")
                    val authorName = post.getString("authorName")
                    val authorImageId = post.getString("authorImage")
                    val postImageId = post.getString("postImage")
                    val postContent = post.getString("postContent")
                    val postTime = post.getString("postTime")

                    val newPost = Post(authorEmail, authorName, authorImageId, postImageId, postContent, postTime)
                    this.posts.add(newPost)
                }

                complete(true)
            } catch (e: JSONException) {
                Log.d(TAG, "EXC: " + e.localizedMessage)
                complete(false)
            }

        }, Response.ErrorListener {error ->
            Log.d(TAG, "Could not read post $error")
            complete(false)
        }) {}

        App.prefs.requestQueue.add(readRequest)
    }

    fun clearPosts() {
        posts.clear()
    }
}