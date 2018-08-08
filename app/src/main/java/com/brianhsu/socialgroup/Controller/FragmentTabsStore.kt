package com.brianhsu.socialgroup.Controller

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.brianhsu.socialgroup.R
import com.brianhsu.socialgroup.Utilities.TAG
import com.brianhsu.socialgroup.Utilities.Tools

class FragmentTabsStore : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "FragmentTabsStore>>>onCreateView()");
        val root = inflater.inflate(R.layout.fragment_tabs_store_dark, container, false)

        if (activity != null) {
            Tools.displayImageOriginal(activity!!, root.findViewById(R.id.image_1) as ImageView, R.drawable.image_8)
            Tools.displayImageOriginal(activity!!, root.findViewById(R.id.image_2) as ImageView, R.drawable.image_9)
            Tools.displayImageOriginal(activity!!, root.findViewById(R.id.image_3) as ImageView, R.drawable.image_15)
            Tools.displayImageOriginal(activity!!, root.findViewById(R.id.image_4) as ImageView, R.drawable.image_14)
            Tools.displayImageOriginal(activity!!, root.findViewById(R.id.image_5) as ImageView, R.drawable.image_12)
            Tools.displayImageOriginal(activity!!, root.findViewById(R.id.image_6) as ImageView, R.drawable.image_2)
            Tools.displayImageOriginal(activity!!, root.findViewById(R.id.image_7) as ImageView, R.drawable.image_5)
        }

        return root
    }

    companion object {
        fun newInstance(): FragmentTabsStore {
            return FragmentTabsStore()
        }
    }
}