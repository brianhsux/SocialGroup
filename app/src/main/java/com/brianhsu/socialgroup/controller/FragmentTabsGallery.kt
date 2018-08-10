package com.brianhsu.socialgroup.controller

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.brianhsu.socialgroup.Adapters.AdapterGridSectioned
import com.brianhsu.socialgroup.model.SectionImage
import com.brianhsu.socialgroup.R
import com.brianhsu.socialgroup.Utilities.DataGenerator
import com.brianhsu.socialgroup.Utilities.TAG

import java.util.ArrayList

class FragmentTabsGallery : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "FragmentTabsStore>>>onCreateView()")
        val root = inflater.inflate(R.layout.fragment_tabs_gallery, container, false)

        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.setHasFixedSize(true)
        recyclerView.isNestedScrollingEnabled = false

        if (activity != null) {
            val itemsImg = DataGenerator.getNatureImages(activity!!)
            itemsImg.addAll(DataGenerator.getNatureImages(activity!!))
            itemsImg.addAll(DataGenerator.getNatureImages(activity!!))
            itemsImg.addAll(DataGenerator.getNatureImages(activity!!))
            itemsImg.addAll(DataGenerator.getNatureImages(activity!!))

            val items = ArrayList<SectionImage>()
            for (i in itemsImg) {
                items.add(SectionImage(i, "IMG_$i.jpg", false))
            }

            var sectCount = 0
            var sectIdx = 0
            val months = DataGenerator.getStringsMonth(activity!!)
            for (i in 0 until items.size / 10) {
                Log.v(TAG, "forLoop: " + i + ", items.size" + items.size)
                items.add(sectCount, SectionImage(-1, months[sectIdx], true))
                sectCount = sectCount.plus(10)
                sectIdx = sectIdx.inc()
            }

            //set data and list adapter
            val gridSectionAdapter = AdapterGridSectioned(activity!!, items) {
                item ->
                Toast.makeText(activity!!, item.title, Toast.LENGTH_LONG).show()
//            val productDetailIntent = Intent(this, ProductDetailActivity::class.java)
//            productDetailIntent.putExtra(EXTRA_PRODUCT_DETAIL, item)
//            startActivity(productDetailIntent)
            }

            recyclerView.adapter = gridSectionAdapter
        }

        return root
    }

    companion object {

        fun newInstance(): FragmentTabsGallery {
            return FragmentTabsGallery()
        }
    }
}