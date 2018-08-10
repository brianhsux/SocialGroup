package com.brianhsu.socialgroup.controller

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.DocumentsContract
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.brianhsu.socialgroup.R
import com.brianhsu.socialgroup.Utilities.CHOOSE_IMAGE_REQUEST_CODE
import com.brianhsu.socialgroup.Utilities.ResourceRepo
import com.brianhsu.socialgroup.Utilities.Tools
import com.brianhsu.socialgroup.Utilities.TAG
import com.brianhsu.socialgroup.model.Resource
import kotlinx.android.synthetic.main.activity_create_user.*

class CreatePostActivity : AppCompatActivity() {

    private var backgroundHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        initToolbar()
        val handlerThread = HandlerThread("MainActivityWorker")
        handlerThread.start()
        backgroundHandler = Handler(handlerThread.looper)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ImageActivity.UPLOAD_IMAGE_REQUEST_CODE) {
                Log.d(TAG, "CreatePostActivity>>>onActivityResult()-1")
                // if the user chose to upload right now we want to schedule an immediate upload:
                uploadImage(data?.getSerializableExtra(ImageActivity.RESOURCE_INTENT_EXTRA) as Resource)
            } else if (requestCode == CHOOSE_IMAGE_REQUEST_CODE && data != null) {
                Log.d(TAG, "CreatePostActivity>>>onActivityResult()-2")
                uploadImageFromIntentUri(data)
            }
        }
    }

    private fun initToolbar() {
        setSupportActionBar(mainToolbar)
        supportActionBar?.title = "New Post"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_send_post, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    fun uploadGalleryFabBtnClicked(view: View) {
        Tools.openMediaChooser(this, CHOOSE_IMAGE_REQUEST_CODE)
    }

    private fun uploadImage(resource: Resource?) {
        Log.d(TAG, "CreatePostActivity>>>uploadImage()")
        resourceUpdated(ResourceRepo.instance?.uploadResource(resource))
    }

    protected fun resourceUpdated(resource: Resource?) {
        Log.d(TAG, "CreatePostActivity>>>resourceUpdated()-1")
        runOnUiThread {
            Log.d(TAG, "CreatePostActivity>>>resourceUpdated()-2>>>run()")
//            for (fragment in getPages()) {
//                val adapter = fragment.getRecyclerView().getAdapter() as ResourcesAdapter
//                adapter.resourceUpdated(resource)
//            }
        }
    }

    private fun uploadImageFromIntentUri(data: Intent) {
        val takeFlags = data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        Log.d(TAG, "CreatePostActivity>>>uploadImageFromIntentUri()-1")
        val uri = data.data
        if (uri != null) {
            Log.d(TAG, "CreatePostActivity>>>uploadImageFromIntentUri()-2")
            handleUri(uri, takeFlags)
        } else if (data.clipData != null) {
            Log.d(TAG, "CreatePostActivity>>>uploadImageFromIntentUri()-3")
            val clip = data.clipData
            for (i in 0 until clip!!.itemCount) {
                handleUri(clip.getItemAt(i).uri, takeFlags)
            }
        }
    }

    private fun handleUri(uri: Uri, flags: Int) {
        Log.d(TAG, "CreatePostActivity>>>handleUri()-1")
        backgroundHandler?.post(Runnable {
            Log.d(TAG, "CreatePostActivity>>>handleUri()-2>>>run()")
            if (DocumentsContract.isDocumentUri(applicationContext, uri)) {
                contentResolver.takePersistableUriPermission(uri, flags)
            }

            val pair = Tools.getResourceNameAndType(applicationContext, uri)
            val resource = Resource(uri.toString(), pair.first, pair.second)
            uploadImage(resource)
        })
    }
}
