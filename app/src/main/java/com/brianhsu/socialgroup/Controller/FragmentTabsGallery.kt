package com.brianhsu.socialgroup.Controller

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
import com.brianhsu.socialgroup.Model.SectionImage
import com.brianhsu.socialgroup.R
import com.brianhsu.socialgroup.Utilities.DataGenerator
import com.brianhsu.socialgroup.Utilities.TAG

import java.util.ArrayList

class FragmentTabsGallery : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_tabs_gallery, container, false)

        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.setHasFixedSize(true)
        recyclerView.isNestedScrollingEnabled = false

        val itemsImg = DataGenerator.getNatureImages(activity!!)
        itemsImg.addAll(DataGenerator.getNatureImages(activity!!))
        itemsImg.addAll(DataGenerator.getNatureImages(activity!!))
        itemsImg.addAll(DataGenerator.getNatureImages(activity!!))
        itemsImg.addAll(DataGenerator.getNatureImages(activity!!))

        val items = ArrayList<SectionImage>()
        for (i in itemsImg) {
            items.add(SectionImage(i, "IMG_$i.jpg", false))
        }

//        Log.v(TAG, "items: " + items.size + ", items: " + items.toString())

        var sect_count = 0
        var sect_idx = 0
        val months = DataGenerator.getStringsMonth(activity!!)
        for (i in 0 until items.size / 10) {
            Log.v(TAG, "forLoop: " + i + ", items.size" + items.size)
            items.add(sect_count, SectionImage(-1, months.get(sect_idx), true))
            sect_count = sect_count + 10
            sect_idx++
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
        return root
    }

    companion object {

        fun newInstance(): FragmentTabsGallery {
            return FragmentTabsGallery()
        }
    }
}