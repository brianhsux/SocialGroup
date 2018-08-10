//package com.brianhsu.socialgroup.Adapters
//
//import android.content.Context
//import android.support.v7.widget.RecyclerView
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import com.brianhsu.socialgroup.R
//import com.brianhsu.socialgroup.model.EffectData
//
//import com.cloudinary.Url
//import com.cloudinary.android.MediaManager
//import com.cloudinary.android.ResponsiveUrl
//import com.cloudinary.android.sample.R
//import com.cloudinary.android.sample.model.EffectData
//import com.squareup.picasso.Picasso
//
//import com.cloudinary.android.ResponsiveUrl.Preset.AUTO_FILL
//
//internal class EffectsGalleryAdapter(private val context: Context, private val images: List<EffectData>, private val requiredSize: Int, private val listener: ItemClickListener?) : RecyclerView.Adapter<EffectsGalleryAdapter.ImageViewHolder>() {
//    private var selected: EffectData? = null
//
//    init {
//
//        if (images.size > 0) {
//            selected = images[0]
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EffectsGalleryAdapter.ImageViewHolder {
//        val viewGroup = LayoutInflater.from(parent.context).inflate(R.layout.item_effects_gallery, parent, false) as ViewGroup
//        viewGroup.setOnClickListener { v ->
//            selected = v.tag as EffectData
//            listener?.onClick(selected)
//
//            notifyDataSetChanged()
//        }
//
//        val imageView = viewGroup.findViewById(R.id.image_view) as ImageView
//        imageView.layoutParams.height = requiredSize
//        return EffectsGalleryAdapter.ImageViewHolder(viewGroup, imageView, viewGroup.findViewById(R.id.selected_indicator), viewGroup.findViewById(R.id.effectName) as TextView)
//    }
//
//    override fun onBindViewHolder(holder: EffectsGalleryAdapter.ImageViewHolder, position: Int) {
//        val data = images[position]
//        holder.itemView.tag = images[position]
//        holder.nameTextView.setText(data.getName())
//
//        val baseUrl = MediaManager.get().url().publicId(data.getPublicId()).transformation(data.getTransformation())
//        MediaManager.get().responsiveUrl(AUTO_FILL)
//                .stepSize(50)
//                .generate(baseUrl, holder.imageView) { url -> Picasso.get().load(url.generate()).placeholder(R.drawable.placeholder).into(holder.imageView) }
//
//        if (selected != null && selected!!.equals(data)) {
//            holder.selection.visibility = View.VISIBLE
//        } else {
//            holder.selection.visibility = View.INVISIBLE
//        }
//    }
//
//    override fun getItemCount(): Int {
//        return images.size
//    }
//
//    interface ItemClickListener {
//        fun onItemClick(data: EffectData?)
//    }
//
//    class ImageViewHolder internal constructor(itemView: View, private val imageView: ImageView, private val selection: View, private val nameTextView: TextView) : RecyclerView.ViewHolder(itemView)
//}