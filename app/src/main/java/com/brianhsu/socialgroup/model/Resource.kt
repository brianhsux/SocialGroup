package com.brianhsu.socialgroup.model

import java.io.Serializable
import java.util.Date

class Resource : Serializable {
    var localUri: String? = null
    var name: String? = null
    var cloudinaryPublicId: String? = null
    var requestId: String? = null
    var deleteToken: String? = null
    var statusTimestamp: Date? = null
    var resourceType: String? = null
    var lastErrorDesc: String? = null
    var lastErrorCode: Int = 0
    var status: UploadStatus? = null

    constructor()

    constructor(localUri: String, name: String?, type: String?) {
        this.localUri = localUri
        this.name = name
        this.resourceType = type
    }

    fun info(): String {
        return "localUri: $localUri, name: $name, type: $resourceType, " +
                "cloudinaryPublicId: $cloudinaryPublicId, requestId: $requestId, " +
                "deleteToken: $deleteToken, statusTimestamp: $statusTimestamp, " +
                "lastErrorDesc: $lastErrorDesc, lastErrorCode: $lastErrorCode, status: $status"
    }

    fun setLastError(errorCode: Int) {
        lastErrorCode = errorCode
    }

    enum class UploadStatus {
        QUEUED,
        UPLOADING,
        UPLOADED,
        RESCHEDULED,
        FAILED,
        CANCELLED
    }

    companion object {

        fun copyFields(src: Resource, dest: Resource) {
            dest.status = src.status
            dest.lastErrorDesc = src.lastErrorDesc
            dest.deleteToken = src.deleteToken
            dest.resourceType = src.resourceType
            dest.cloudinaryPublicId = src.cloudinaryPublicId
            dest.localUri = src.localUri
            dest.requestId = src.requestId
            dest.statusTimestamp = src.statusTimestamp
            dest.name = src.name
        }
    }
}
