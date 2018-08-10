package com.brianhsu.socialgroup.Utilities

import com.brianhsu.socialgroup.controller.App
import com.brianhsu.socialgroup.model.Resource
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.sample.core.CloudinaryHelper
import com.cloudinary.utils.StringUtils

class ResourceRepo private constructor() {
    private val helper: CloudinarySqliteHelper

    init {
        this.helper = CloudinarySqliteHelper(App.instance)
    }

    fun resourceRescheduled(requestId: String, error: Int, errorDesc: String): Resource? {
        helper.setUploadResultParams(requestId, null, null, Resource.UploadStatus.RESCHEDULED, error, errorDesc)
        return helper.findByRequestId(requestId)
    }

    fun resourceFailed(requestId: String, error: Int, errorDesc: String): Resource? {
        helper.setUploadResultParams(requestId, null, null, Resource.UploadStatus.FAILED, error, errorDesc)
        return helper.findByRequestId(requestId)
    }

    fun resourceUploaded(requestId: String, publicId: String, deleteToken: String): Resource? {
        helper.setUploadResultParams(requestId, publicId, deleteToken, Resource.UploadStatus.UPLOADED, ErrorInfo.NO_ERROR, null)
        return helper.findByRequestId(requestId)
    }

    private fun resourceQueued(resource: Resource?): Resource? {
        val localUri = resource?.localUri
        val requestId = resource?.requestId
        helper.insertOrUpdateQueuedResource(localUri, resource?.name, requestId, resource?.resourceType, Resource.UploadStatus.QUEUED)
        return helper.findByRequestId(requestId)
    }

    fun resourceUploading(requestId: String): Resource? {
        helper.setUploadResultParams(requestId, null, null, Resource.UploadStatus.UPLOADING, ErrorInfo.NO_ERROR, null)
        return helper.findByRequestId(requestId)
    }

    fun listAll(): List<Resource> {
        return helper.listAll()
    }

    fun clear() {
        helper.deleteAllImages()
    }

    fun listRecent(): List<Resource> {
        return helper.listAllUploadedAfter(System.currentTimeMillis() - RECENT_DELTA)
    }

    fun getLocalUri(requestId: String): String? {
        return helper.getLocalUri(requestId)
    }

    fun getResource(requestId: String): Resource? {
        return helper.findByRequestId(requestId)
    }

    fun delete(imageLocalId: String) {
        helper.delete(imageLocalId)
    }

    fun list(statuses: List<Resource.UploadStatus>): List<Resource> {
        val strStatuses = arrayOfNulls<String>(statuses.size)
        for (i in statuses.indices) {
            strStatuses[i] = statuses[i].name
        }

        return helper.list(strStatuses)
    }

    fun uploadResource(resource: Resource?): Resource? {
        if (StringUtils.isNotBlank(resource?.requestId)) {
            // cancel previous upload requests for this resource:
            MediaManager.get().cancelRequest(resource?.requestId)
        }

        val requestId = CloudinaryHelper.uploadResource(resource, resource?.resourceType.equals("image"))
        resource?.requestId = requestId

        return resourceQueued(resource)
    }

    companion object {
        private val lockObject = Any()
        private val RECENT_DELTA = (10 * 60 * 1000).toLong()
        private var _instance: ResourceRepo? = null

        val instance: ResourceRepo?
            get() {
                if (_instance == null) {
                    synchronized(lockObject) {
                        if (_instance == null) {
                            _instance = ResourceRepo()
                        }
                    }
                }

                return _instance
            }
    }
}

