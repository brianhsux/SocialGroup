package com.brianhsu.socialgroup.controller

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.brianhsu.socialgroup.Utilities.SharePrefs
import com.cloudinary.android.LogLevel
import com.cloudinary.android.MediaManager
import com.cloudinary.android.policy.GlobalUploadPolicy
import com.cloudinary.android.policy.UploadPolicy

/**
 * Created by brian on 2018/4/12.
 */
class App: Application() {

    private var mainThreadHandler: Handler? = null

    companion object {
        lateinit var prefs: SharePrefs
        lateinit var instance: App
    }

    override fun onCreate() {
        prefs = SharePrefs(applicationContext)
        instance = this
        mainThreadHandler = Handler(Looper.getMainLooper())

        super.onCreate()

        // This can be called any time regardless of initialization.
        MediaManager.setLogLevel(LogLevel.DEBUG)

        // Mandatory - call a flavor of init. Config can be null if cloudinary_url is provided in the manifest.
        MediaManager.init(this)

        // Optional - configure global policy.
        MediaManager.get().globalUploadPolicy = GlobalUploadPolicy.Builder()
                .maxConcurrentRequests(4)
                .networkPolicy(UploadPolicy.NetworkType.ANY)
                .build()
    }

    fun runOnMainThread(runnable: Runnable) {
        mainThreadHandler?.post(runnable)
    }
}