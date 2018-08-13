package com.brianhsu.socialgroup.Sevices

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.util.TypedValue
import com.brianhsu.socialgroup.R
import com.brianhsu.socialgroup.Utilities.ResourceRepo
import com.brianhsu.socialgroup.Utilities.Tools
import com.brianhsu.socialgroup.controller.App
import com.brianhsu.socialgroup.controller.CreatePostActivity
import com.brianhsu.socialgroup.model.Resource

import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.ListenerService
//import com.cloudinary.android.sample.R
//import com.cloudinary.android.sample.model.Resource
//import com.cloudinary.android.sample.persist.ResourceRepo

import java.util.HashMap
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class CloudinaryService : ListenerService() {

    private val bitmaps = HashMap<String, BitmapResult?>()
    private var notificationManager: NotificationManager? = null
    private val idsProvider = AtomicInteger(1000)
    private val requestIdsToNotificationIds = ConcurrentHashMap<String, Int>()
    private var builder: Notification.Builder? = null
    private var backgroundThreadHandler: Handler? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "CloudinaryService>>>onCreate()-1")
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        builder = Notification.Builder(this)
        if (builder != null) {
            builder!!.setContentTitle("Uploading to Cloudinary...")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(PendingIntent.getActivity(this, 999,
                            Intent(this, CreatePostActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT).setAction(ACTION_STATE_IN_PROGRESS),
                            0))
                    .setOnlyAlertOnce(true)
                    .setOngoing(true)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder!!.setChannelId(App.NOTIFICATION_CHANNEL_ID)
            }
        }

        val handlerThread = HandlerThread("CloudinaryServiceBackgroundThread")
        handlerThread.start()
        backgroundThreadHandler = Handler(handlerThread.looper)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun getBuilder(requestId: String, status: Resource.UploadStatus): Notification.Builder {
        val builder = Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(this, 1234,
                        Intent(this, CreatePostActivity::class.java)
                                .setAction(actionFromStatus(status)), 0))
                .setLargeIcon(getBitmap(requestId))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(App.NOTIFICATION_CHANNEL_ID)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(resources.getColor(R.color.colorPrimary))
        }

        return builder

    }

    private fun actionFromStatus(status: Resource.UploadStatus): String {
        return when (status) {
            Resource.UploadStatus.QUEUED, Resource.UploadStatus.UPLOADING -> ACTION_STATE_IN_PROGRESS
            Resource.UploadStatus.UPLOADED -> ACTION_STATE_UPLOADED
            Resource.UploadStatus.RESCHEDULED, Resource.UploadStatus.FAILED -> ACTION_STATE_ERROR
            else -> ACTION_STATE_UPLOADED
        }
    }

    private fun getBitmap(requestId: String): Bitmap {
        var result: BitmapResult? = bitmaps[requestId]
        if (result == null) {
            synchronized(bitmaps) {
                result = bitmaps[requestId]
                if (result == null) {
                    var bitmap: Bitmap? = null
                    try {
                        val resource = ResourceRepo.instance?.getResource(requestId)
                        val uri = resource?.localUri
                        val value = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48f, resources.displayMetrics).toInt()
                        if (resource?.resourceType.equals("image")) {
                            bitmap = Tools.decodeBitmapStream(this, Uri.parse(uri), value, value)
                        }
                    } catch (e: Exception) {
                        // print but don't fail the notification
                        e.printStackTrace()
                    }

                    // bitmap can be null, save it anyway (we don't want to retry)
                    result = BitmapResult(bitmap)
                    bitmaps[requestId] = result
                }
            }
        }

        return result!!.bitmap!!
    }

    private fun cleanupBitmap(requestId: String) {
        synchronized(bitmaps) {
            bitmaps.remove(requestId)
        }
    }

    private fun cancelNotification(requestId: String) {
        val id = requestIdsToNotificationIds[requestId]
        if (id != null) {
            notificationManager!!.cancel(id)
        }
    }

    private fun sendBroadcast(updatedResource: Resource?): Boolean {
        // This is called from background threads and the main thread  may touch the resource and delete it
        // in the meantime (from the activity) - verify it's still around before sending the broadcast
        return if (updatedResource != null) {
            LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(ACTION_RESOURCE_MODIFIED).putExtra("resource", updatedResource))
        } else false

    }

    override fun onStart(requestId: String) {
        Log.d(TAG, "CloudinaryService>>>onStart()-1")
        if (backgroundThreadHandler != null) {
            Log.d(TAG, "CloudinaryService>>>onStart()-2")
            backgroundThreadHandler!!.post { sendBroadcast(ResourceRepo.instance?.resourceUploading(requestId)) }
        }

        cancelNotification(requestId)
        val id = idsProvider.incrementAndGet()
        requestIdsToNotificationIds[requestId] = id
        notificationManager?.notify(id,
                getBuilder(requestId, Resource.UploadStatus.UPLOADING)
                        .setContentTitle("Preparing upload...")
                        .build())
    }

    @Synchronized
    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
        Log.d(TAG, "CloudinaryService>>>onProgress()-1")
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(ACTION_UPLOAD_PROGRESS).putExtra("requestId", requestId).putExtra("bytes", bytes).putExtra("totalBytes", totalBytes))
        var notificationId: Int? = requestIdsToNotificationIds[requestId]

        if (notificationId == null) {
            notificationId = idsProvider.incrementAndGet()
            requestIdsToNotificationIds[requestId] = notificationId
        }

        if (totalBytes > 0) {
            val progressFraction = bytes.toDouble() / totalBytes
            val progress = Math.round(progressFraction * 1000).toInt()
            builder?.setProgress(1000, progress, false)
            builder?.setContentText(String.format("%d%% (%d KB)", (progressFraction * 100).toInt(), bytes / 1024))
        } else {
            builder?.setProgress(1000, 1000, true)
            builder?.setContentText(String.format("%d KB", bytes / 1024))
        }

        builder?.setLargeIcon(getBitmap(requestId))
        notificationManager?.notify(notificationId, builder?.build())
    }

    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
        Log.d(TAG, "CloudinaryService>>>onSuccess()-1")
        val publicId = resultData["public_id"]?.toString()
        val deleteToken = resultData["delete_token"]?.toString()


        if (backgroundThreadHandler != null) {
            Log.d(TAG, "CloudinaryService>>>onSuccess()-2, publicId: " + publicId +
            ", deleteToken: " + deleteToken)
            backgroundThreadHandler!!.post { sendBroadcast(ResourceRepo.instance?.resourceUploaded(requestId, publicId, deleteToken)) }
        }

        Log.d(TAG, "CloudinaryService>>>onSuccess()-3")
        cancelNotification(requestId)
        Log.d(TAG, "CloudinaryService>>>onSuccess()-4")
        val id = idsProvider.incrementAndGet()
        Log.d(TAG, "CloudinaryService>>>onSuccess()-5")
        requestIdsToNotificationIds[requestId] = id
        notificationManager?.notify(id,
                getBuilder(requestId, Resource.UploadStatus.UPLOADED)
                        .setContentTitle("Cloudinary Upload")
                        .setContentText("The image was uploaded successfully!")
                        .build())
        Log.d(TAG, "CloudinaryService>>>onSuccess()-6")
        cleanupBitmap(requestId)
        Log.d(TAG, "CloudinaryService>>>onSuccess()-7")
    }

    override fun onError(requestId: String, error: ErrorInfo) {
        Log.d(TAG, "CloudinaryService>>>onError()-1")
        backgroundThreadHandler?.post {
            val resource: Resource?
            if (error.code == ErrorInfo.REQUEST_CANCELLED) {
                resource = ResourceRepo.instance?.getResource(requestId)
                if (resource != null) {
                    ResourceRepo.instance?.delete(resource.localUri!!)
                    resource.status = Resource.UploadStatus.CANCELLED
                }
            } else {
                resource = ResourceRepo.instance?.resourceFailed(requestId, error.code, error.description)
            }

            sendBroadcast(resource)
        }

        cancelNotification(requestId)

        val id = idsProvider.incrementAndGet()
        requestIdsToNotificationIds[requestId] = id

        notificationManager?.notify(id,
                getBuilder(requestId, Resource.UploadStatus.FAILED)
                        .setContentTitle("Error uploading.")
                        .setContentText(error.description)
                        .setStyle(Notification.BigTextStyle()
                                .setBigContentTitle("Error uploading.")
                                .bigText(error.description))
                        .build())

        cleanupBitmap(requestId)
    }

    override fun onReschedule(requestId: String, error: ErrorInfo) {
        Log.d(TAG, "CloudinaryService>>>onReschedule()-1")
        backgroundThreadHandler?.post { sendBroadcast(ResourceRepo.instance?.resourceRescheduled(requestId, error.code, error.description)) }
        cancelNotification(requestId)
        val id = idsProvider.incrementAndGet()
        requestIdsToNotificationIds[requestId] = id
        notificationManager?.notify(id,
                getBuilder(requestId, Resource.UploadStatus.RESCHEDULED)
                        .setContentTitle("Connection issues")
                        .setContentText("The upload will resume once network is available.")
                        .build())
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "CloudinaryService>>>onDestroy()-1")
        backgroundThreadHandler?.removeCallbacksAndMessages(null)
    }

    private class BitmapResult constructor(internal val bitmap: Bitmap?)

    companion object {

        val ACTION_RESOURCE_MODIFIED = "ACTION_RESOURCE_MODIFIED"
        val ACTION_UPLOAD_PROGRESS = "ACTION_UPLOAD_PROGRESS"
        val ACTION_STATE_ERROR = "cloudinary.action_error"
        val ACTION_STATE_UPLOADED = "cloudinary.action_uploaded"
        val ACTION_STATE_IN_PROGRESS = "cloudinary.action_progress"

        private val TAG = "BBB>>>SocialGroup"
    }
}