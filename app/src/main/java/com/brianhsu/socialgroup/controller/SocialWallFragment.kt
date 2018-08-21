package com.brianhsu.socialgroup.controller

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.brianhsu.socialgroup.Adapters.AdapterPostSectioned
import com.brianhsu.socialgroup.model.Post
import com.brianhsu.socialgroup.R
import com.brianhsu.socialgroup.Sevices.PostService
import com.brianhsu.socialgroup.Utilities.TAG
import java.util.ArrayList

class SocialWallFragment : Fragment() {

    var recyclerView: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        Log.d(TAG, "SocialWallFragment>>>onCreateView()");
        val root = inflater.inflate(R.layout.fragment_social_wall, container, false)

//        val recyclerView = root.findViewById<RecyclerView>(R.id.social_wall_recycler_view)
        recyclerView = root.findViewById<RecyclerView>(R.id.social_wall_recycler_view)
        recyclerView?.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.isNestedScrollingEnabled = false

        if (activity != null) {
            val testString = "There are many variations of passages of Lorem Ipsum available, but the majority have suffered alteration in some form, by injected humour, or randomised words which don't look even slightly believable. If you are going to use a passage of Lorem Ipsum, you need to be sure there isn't anything embarrassing hidden in the middle of text. All the Lorem Ipsum generators on the Internet tend to repeat predefined chunks as necessary, making this the first true generator on the Internet. It uses a dictionary of over 200 Latin words, combined with a handful of model sentence structures, to generate Lorem Ipsum which looks reasonable. The generated Lorem Ipsum is therefore always free from repetition, injected humour, or non-characteristic words etc."
            val posts = PostService.posts

            if (posts.size == 0) {
                posts.add(Post("", "Brian",  "photo_male_1", "bcnkrwniruyzworepuuf", testString, "Aug 20, 2018 18:20:20"))
                posts.add(Post("", "Joanna",  "photo_female_3", "edpftd8fyhgd4pmq1xof", testString, "Sep 20, 2018 18:20:20"))
                posts.add(Post("", "Odin",  "photo_male_3", "qenujzureigckg1hcgtm", testString,"Dec 20, 2018 18:20:20" ))
                posts.add(Post("", "Bradley",  "photo_male_4", "hcu80lmm3ssjtoos6e0i", testString,"July 20, 2018 18:20:20"))
            }

//            posts.reverse()

            //set data and list adapter
            val gridSectionAdapter = AdapterPostSectioned(activity!!, posts) {
                post ->
                Toast.makeText(activity!!, post.authorName, Toast.LENGTH_LONG).show()
//            val productDetailIntent = Intent(this, ProductDetailActivity::class.java)
//            productDetailIntent.putExtra(EXTRA_PRODUCT_DETAIL, item)
//            startActivity(productDetailIntent)
            }

            recyclerView?.adapter = gridSectionAdapter
        }

        return root
    }

    fun refresh() {
        recyclerView?.adapter?.notifyDataSetChanged()
        if (fragmentManager != null) {
            val ft = fragmentManager!!.beginTransaction()
            ft.detach(this).attach(this).commit()
        }
    }

    companion object {

        fun newInstance(): SocialWallFragment {
            return SocialWallFragment()
        }
    }
}