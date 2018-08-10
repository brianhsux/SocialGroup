package com.brianhsu.socialgroup.Adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.brianhsu.socialgroup.model.SectionImage
import com.brianhsu.socialgroup.R
import com.brianhsu.socialgroup.Utilities.TAG
import com.brianhsu.socialgroup.Utilities.Tools

private const val VIEW_ITEM = 1
private const val VIEW_SECTION = 0

class AdapterGridSectioned(private val context: Context, private val items: List<SectionImage>,
                           private val itemClick: (SectionImage) -> Unit) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d(TAG, "AdapterGridSectioned>>>onBindViewHolder()")
        if (holder is OriginalViewHolder) {
            holder.bindSectionImage(items[position], context)
        } else if (holder is SectionViewHolder) {
            holder.bindSectionImage(items[position], context)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d(TAG, "AdapterGridSectioned>>>onCreateViewHolder()")
        return when (viewType) {
            VIEW_ITEM -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_grid_image_sectioned, parent, false)
                OriginalViewHolder(view, itemClick)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_section, parent, false)
                SectionViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    inner class OriginalViewHolder(itemView: View?, private val itemClick: (SectionImage) -> Unit) : RecyclerView.ViewHolder(itemView) {
        var image = itemView?.findViewById<ImageView>(R.id.image)
        var lytParent = itemView?.findViewById<View>(R.id.lyt_parent)

        fun bindSectionImage(sImage: SectionImage, context: Context) {
            Tools.displayImageOriginal(context, image, sImage.image)
            lytParent?.setOnClickListener { itemClick(sImage) }

            val layoutParams = itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
            layoutParams.isFullSpan = false
            Log.v(TAG, "section = false: " + layoutParams.isFullSpan)
        }
    }

    inner class SectionViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        var titleSection = itemView?.findViewById<TextView>(R.id.title_section)

        fun bindSectionImage(sImage: SectionImage, context: Context) {
            Log.v(TAG, "item.image: " + sImage.image)
            titleSection?.text = sImage.title

            val layoutParams = itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
            layoutParams.isFullSpan = true
            Log.v(TAG, "section = true: " + layoutParams.isFullSpan)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (this.items[position].section) VIEW_SECTION else VIEW_ITEM
    }
}