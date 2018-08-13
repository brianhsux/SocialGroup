package com.brianhsu.socialgroup.Adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.brianhsu.socialgroup.R
import com.brianhsu.socialgroup.Utilities.TAG
import com.brianhsu.socialgroup.model.Resource

import com.cloudinary.android.MediaManager
import com.cloudinary.android.ResponsiveUrl
import com.squareup.picasso.Picasso

import java.util.ArrayList

internal class ResourcesAdapter(val context: Context, resources: List<Resource>, val requiredSize: Int, val validStatuses: List<Resource.UploadStatus>, val listener: ImageClickedListener?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val resources: MutableList<ResourceWithMeta>

    init {
        val cardImageWidth: Int = context.resources.getDimensionPixelSize(R.dimen.card_image_width)
        val cardImageHeight: Int = context.resources.getDimensionPixelSize(R.dimen.card_height)

        this.resources = ArrayList(resources.size)
//        for (resource in resources) {
//            this.resources.add(ResourceWithMeta(resource))
//        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_REGULAR) {
            createRegularViewHolder(parent)
        } else {
            createFailedViewHolder(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_REGULAR) {
            Log.d(TAG, "ResourcesAdapter>>>onBindViewHolder()-1")
            bindRegularView(holder as ResourceViewHolder, position)
        } else {
            Log.d(TAG, "ResourcesAdapter>>>onBindViewHolder()-2")
            bindErrorView(holder as FailedResourceViewHolder, position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val status = resources[0].resource.status
        return if (status === Resource.UploadStatus.FAILED || status === Resource.UploadStatus.RESCHEDULED) {
            TYPE_ERROR
        } else TYPE_REGULAR

    }

    override fun getItemCount(): Int {
        return resources.size
    }

    private fun createFailedViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val viewGroup: ViewGroup = LayoutInflater.from(parent.context).inflate(R.layout.item_main_gallery_error, parent, false) as ViewGroup
        viewGroup.setOnClickListener { v ->
            if (listener != null) {
                val resource = v.tag as Resource
                listener.onImageClicked(resource)
            }
        }

        val retryButton = viewGroup.findViewById<TextView>(R.id.retryButton)
        retryButton.setOnClickListener({ v ->
            if (listener != null) {
                val resource = v.tag as Resource
                listener.onRetryClicked(resource)
            }
        })

        val cancelButton = viewGroup.findViewById<ImageView>(R.id.cancelButton)
        cancelButton.setOnClickListener({ v ->
            if (listener != null) {
                val resource = v.tag as Resource
                listener.onDeleteClicked(resource, false)
            }
        })

        return FailedResourceViewHolder(viewGroup, viewGroup.findViewById(R.id.filename) as TextView, viewGroup.findViewById(R.id.image_view) as ImageView, retryButton, cancelButton, viewGroup.findViewById(R.id.rescheduleLabel), viewGroup.findViewById(R.id.errorDescription) as TextView)
    }

    private fun createRegularViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val viewGroup: ViewGroup = LayoutInflater.from(parent.context).inflate(R.layout.item_main_gallery, parent, false) as ViewGroup
        viewGroup.layoutParams.height = requiredSize
        viewGroup.setOnClickListener { v ->
            if (listener != null) {
                val resource = v.tag as Resource
                listener.onImageClicked(resource)
            }
        }

        val deleteButton = viewGroup.findViewById<ImageView>(R.id.deleteButton)
        deleteButton.setOnClickListener({ v ->
            if (listener != null) {
                val resource = v.tag as Resource
                listener.onDeleteClicked(resource, false)
            }
        })

        val cancelRequest = viewGroup.findViewById<ImageView>(R.id.buttonClear)
        cancelRequest.setOnClickListener({ v ->
            if (listener != null) {
                val resource = v.tag as Resource
                listener.onCancelClicked(resource)
            }
        })
        return ResourceViewHolder(viewGroup, viewGroup.findViewById<ImageView>(R.id.image_view), viewGroup.findViewById(R.id.statusText) as TextView,
                deleteButton, viewGroup.findViewById(R.id.buttonsContainer), viewGroup.findViewById(R.id.videoIcon), viewGroup.findViewById(R.id.uploadProgress) as ProgressBar,
                viewGroup.findViewById(R.id.black_overlay), viewGroup.findViewById(R.id.filename) as TextView, cancelRequest)
    }

    private fun bindErrorView(holder: FailedResourceViewHolder, position: Int) {
        val resourceWithMeta = resources[position]
        val resource = resourceWithMeta.resource
        holder.errorDescription.setText(resource.lastErrorDesc)
        val isVideo = resource.resourceType.equals("video")
        val placeHolder = if (isVideo) R.drawable.video_placeholder else R.drawable.placeholder
        Picasso.get().load(resource.localUri).placeholder(placeHolder).centerCrop().resizeDimen(R.dimen.card_image_width, R.dimen.card_height).into(holder.imageView)
        holder.retryButton.tag = resource
        holder.cancelButton.tag = resource
        holder.rescheduleLabel.visibility = if (resource.status === Resource.UploadStatus.RESCHEDULED) View.VISIBLE else View.GONE
        holder.filename.setText(resource.name)
    }

    private fun bindRegularView(holder: ResourceViewHolder, position: Int) {
        Log.d(TAG, "ResourcesAdapter>>>bindRegularView()-1, position: " + position)
        val resourceWithMeta = resources[position]
        val resource = resourceWithMeta.resource

        // setup default values for more readable code:
        holder.itemView.tag = resource
        holder.deleteButton.tag = resource
        holder.cancelRequest.tag = resource
        holder.deleteButton.visibility = View.VISIBLE
        holder.progressBar.progress = 0
        holder.progressBar.visibility = View.INVISIBLE
        holder.buttonsContainer.visibility = View.GONE
        holder.cancelRequest.visibility = View.GONE
        holder.statusText.text = null
        holder.name.text = null
        var local = true
        val isVideo = resource.resourceType.equals("video")
        Log.d(TAG, "ResourcesAdapter>>>bindRegularView()-2, resource.status: " + resource.status)
        when (resource.status) {

            Resource.UploadStatus.QUEUED -> {
                holder.blackOverlay.animate().cancel()
                holder.blackOverlay.visibility = View.GONE
                holder.cancelRequest.visibility = View.VISIBLE
            }
            Resource.UploadStatus.UPLOADING -> {
                holder.cancelRequest.visibility = View.VISIBLE
                val progressFraction = resourceWithMeta.bytes.toDouble() / resourceWithMeta.totalBytes
                val progress = Math.round(progressFraction * 1000).toInt()
                holder.progressBar.visibility = View.VISIBLE
                holder.progressBar.max = 1000
                holder.progressBar.progress = progress
                if (holder.blackOverlay.visibility != View.VISIBLE) {
                    holder.blackOverlay.alpha = 0f
                    holder.blackOverlay.visibility = View.VISIBLE
                    holder.blackOverlay.animate().alpha(1f)
                }
                if (isVideo) {
                    holder.name.setText(resource.name)
                }
                setProgressText(progressFraction, holder.statusText)
            }
            Resource.UploadStatus.UPLOADED -> {
                holder.blackOverlay.animate().cancel()
                holder.blackOverlay.visibility = View.GONE
                holder.videoIcon.visibility = if (isVideo) View.VISIBLE else View.GONE
                holder.buttonsContainer.visibility = View.VISIBLE
                local = false
            }
            Resource.UploadStatus.RESCHEDULED -> {
                holder.blackOverlay.animate().cancel()
                holder.blackOverlay.visibility = View.GONE
            }
            Resource.UploadStatus.FAILED -> {
                holder.blackOverlay.animate().cancel()
                holder.blackOverlay.visibility = View.GONE
            }

            else -> {
                Log.d(TAG, "ResourcesAdapter>>>bindRegularView()-2-1, resource.localUri: " + resource.localUri)
                holder.blackOverlay.animate().cancel()
                holder.blackOverlay.visibility = View.GONE
                holder.cancelRequest.visibility = View.VISIBLE
            }
        }

        val placeholder = if (resource.resourceType.equals("image")) R.drawable.placeholder else R.drawable.video_placeholder

        if (local) {
            Log.d(TAG, "ResourcesAdapter>>>bindRegularView()-3, local is true: ")
            Picasso.get().load(resource.localUri).placeholder(placeholder).centerCrop().resize(requiredSize, requiredSize).into(holder.imageView)
        } else {
            Log.d(TAG, "ResourcesAdapter>>>bindRegularView()-3, local is false: ")
            val publicId = resource.cloudinaryPublicId
            val url = MediaManager.get().url().publicId(publicId).resourceType(resource.resourceType).format("webp")
            MediaManager.get().responsiveUrl(ResponsiveUrl.Preset.AUTO_FILL)
                    .generate(url, holder.imageView) {
                        url -> Picasso.get().load(url.generate()).placeholder(placeholder).into(holder.imageView)
                    }
        }

        if (resourceWithMeta.totalBytes > 0) {
            val progressFraction = resourceWithMeta.bytes.toDouble() / resourceWithMeta.totalBytes
            val progress = Math.round(progressFraction * 1000).toInt()
            holder.progressBar.visibility = View.VISIBLE
            holder.progressBar.max = 1000
            holder.progressBar.progress = progress
        } else {
            holder.progressBar.progress = 0
            holder.progressBar.visibility = View.INVISIBLE
        }
    }

    private fun setProgressText(progressFraction: Double, statusText: TextView) {
        val progressStr = Math.round(progressFraction * 100).toString()
        val text = statusText.context.getString(R.string.uploading, progressStr)
        val spannableString = SpannableString(text)
        spannableString.setSpan(ForegroundColorSpan(statusText.context.resources.getColor(R.color.buttonColor)), text.indexOf(progressStr), text.length, 0)
        statusText.text = spannableString
    }

    private fun addResource(resource: Resource) {
        resources.add(0, ResourceWithMeta(resource))
        notifyItemInserted(0)
    }

    fun replaceImages(resources: List<Resource>) {
        this.resources.clear()
        for (resource in resources) {
            this.resources.add(ResourceWithMeta(resource))
        }

        notifyDataSetChanged()
    }

    fun removeResource(resourceId: String?): Resource? {
        var toRemove: Resource? = null
        for (i in resources.indices) {
            val resource = resources[i].resource
            if (resource.localUri.equals(resourceId)) {
                toRemove = resource
                resources.removeAt(i)
                notifyItemRemoved(i)
                break
            }
        }

        return toRemove
    }

    fun resourceTempUpdated(updated: Resource) {
        Log.d(TAG, "ResourcesAdapter>>>resourceTempUpdated()-1, updated.status: " + updated.status
                + ", validStatuses: " + validStatuses + ", name: " + updated.name + ", localUri: " +
                updated.localUri)

        addResource(updated)
    }

    fun resourceUpdated(updated: Resource) {
        Log.d(TAG, "ResourcesAdapter>>>resourceUpdated()-1, updated.status: " + updated.status
        + ", validStatuses: " + validStatuses)
        if (!validStatuses.contains(updated.status)) {
            Log.d(TAG, "ResourcesAdapter>>>resourceUpdated()-2")
            removeResource(updated.localUri)
        } else {
            Log.d(TAG, "ResourcesAdapter>>>resourceUpdated()-3")
            var found = false
            for (i in resources.indices) {
                Log.d(TAG, "ResourcesAdapter>>>resourceUpdated()-4")
                val resourceWithMeta = resources[i]
                val resource = resourceWithMeta.resource
                if (resource.requestId.equals(updated.requestId)) {
                    Log.d(TAG, "ResourcesAdapter>>>resourceUpdated()-5")
                    Resource.copyFields(updated, resource)
                    resourceWithMeta.bytes = 0
                    resourceWithMeta.totalBytes = 0
                    notifyItemChanged(i)
                    found = true
                    break
                }
            }

            if (!found) {
                Log.d(TAG, "ResourcesAdapter>>>resourceUpdated()-6")
                // not found but status is valid - it should be added here.
                addResource(updated)
            }
        }
    }

    fun progressUpdated(requestId: String, bytes: Long, totalBytes: Long) {
        Log.d(TAG, "ResourcesAdapter>>>progressUpdated()-1, requestId: " + requestId +
        ", bytes: " + bytes + ", totalBytes: " + totalBytes)
        for (i in resources.indices) {
            val resource = resources[i]
            if (resource.resource.requestId.equals(requestId)) {
                resource.bytes = bytes
                resource.totalBytes = totalBytes
                notifyItemChanged(i)
                break
            }
        }
    }

    internal interface ImageClickedListener {
        fun onImageClicked(resource: Resource)

        fun onDeleteClicked(resource: Resource, recent: Boolean?)

        fun onRetryClicked(resource: Resource)

        fun onCancelClicked(resource: Resource)
    }

    inner class ResourceViewHolder internal constructor(itemView: View, val imageView: ImageView, val statusText: TextView, val deleteButton: View, val buttonsContainer: View, val videoIcon: View, val progressBar: ProgressBar, val blackOverlay: View, val name: TextView, val cancelRequest: View) : RecyclerView.ViewHolder(itemView)

    inner class FailedResourceViewHolder internal constructor(itemView: View, val filename: TextView, val imageView: ImageView, val cancelButton: View, val retryButton: View, val rescheduleLabel: View, val errorDescription: TextView) : RecyclerView.ViewHolder(itemView)

    private class ResourceWithMeta internal constructor(internal val resource: Resource) {
        internal var bytes: Long = 0
        internal var totalBytes: Long = 0
    }

    companion object {
        private const val TYPE_REGULAR = 0
        private const val TYPE_ERROR = 1
    }
}