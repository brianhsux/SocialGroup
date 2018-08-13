package com.brianhsu.socialgroup.controller

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.DocumentsContract
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import com.brianhsu.socialgroup.Adapters.ResourcesAdapter
import com.brianhsu.socialgroup.R
import com.brianhsu.socialgroup.Sevices.CloudinaryService
import com.brianhsu.socialgroup.Utilities.*
import com.brianhsu.socialgroup.model.Resource
import kotlinx.android.synthetic.main.activity_create_user.*
import java.util.*

class CreatePostActivity : AppCompatActivity() {

    private var backgroundHandler: Handler? = null

    private var recyclerView: RecyclerView? = null
    private var dividerSize: Int = 0
    private val statuses = listOf(Resource.UploadStatus.QUEUED)
//    private val statuses = Arrays.asList(Resource.UploadStatus.QUEUED, Resource.UploadStatus.UPLOADING, Resource.UploadStatus.UPLOADED)
    private var receiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        initToolbar()
        prepareRecyclerView()
        val handlerThread = HandlerThread("MainActivityWorker")
        handlerThread.start()
        backgroundHandler = Handler(handlerThread.looper)
        registerLocalReceiver()
        startService(Intent(this, CloudinaryService::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterLocalReceiver()
    }

    public override fun onResume() {
        super.onResume()
        Log.d(TAG, "CreatePostActivity>>>onResume()-1")
        if (recyclerView != null) {
            Log.d(TAG, "CreatePostActivity>>>onResume()-2")
            if (recyclerView!!.width > 0) {
                Log.d(TAG, "CreatePostActivity>>>onResume()-3")
                initThumbSizeAndLoadData()
            } else {
                Log.d(TAG, "CreatePostActivity>>>onResume()-4")
                recyclerView!!.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        Log.d(TAG, "CreatePostActivity>>>onResume()>>onPreDraw()")
                        recyclerView!!.viewTreeObserver.removeOnPreDrawListener(this)
                        initThumbSizeAndLoadData()
                        return true
                    }
                })
            }
        }
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

    private fun unregisterLocalReceiver() {
        if (receiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver!!)
        }
    }

    private fun registerLocalReceiver() {
        val filter = IntentFilter(CloudinaryService.ACTION_RESOURCE_MODIFIED)
        filter.addAction(CloudinaryService.ACTION_UPLOAD_PROGRESS)
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d(TAG, "CreatePostActivity>>>onReceive()-1")
                if (!isFinishing) {
                    Log.d(TAG, "CreatePostActivity>>>onReceive()-2")
                    if (CloudinaryService.ACTION_RESOURCE_MODIFIED == intent.action) {
                        Log.d(TAG, "CreatePostActivity>>>onReceive()-3")
                        val resource = intent.getSerializableExtra("resource") as Resource
                        resourceUpdated(resource)
                    } else if (CloudinaryService.ACTION_UPLOAD_PROGRESS == intent.action) {
                        Log.d(TAG, "CreatePostActivity>>>onReceive()-4")
                        val requestId = intent.getStringExtra("requestId")
                        val bytes = intent.getLongExtra("bytes", 0)
                        val totalBytes = intent.getLongExtra("totalBytes", 0)

                        val adapter = recyclerView?.adapter as ResourcesAdapter
                        adapter.progressUpdated(requestId, bytes, totalBytes)
                    }
                }
            }
        }
        if (receiver != null) {
            LocalBroadcastManager.getInstance(this).registerReceiver(receiver!!, filter)
        }
    }

    private fun initToolbar() {
        setSupportActionBar(mainToolbar)
        supportActionBar?.title = "New Post"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun prepareRecyclerView() {
        Log.d(TAG, "CreatePostActivity>>>prepareRecyclerView()")
        recyclerView = findViewById(R.id.galleryCreatePost)

        if (recyclerView != null) {
            recyclerView!!.setHasFixedSize(true)
            (recyclerView!!.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            val layoutManager = getLayoutManager(applicationContext)
            recyclerView!!.layoutManager = layoutManager
            dividerSize = resources.getDimensionPixelSize(R.dimen.grid_divider_width)
            addItemDecoration(recyclerView)
        }
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

    private fun uploadTempImage(resource: Resource?) {
        Log.d(TAG, "CreatePostActivity>>>uploadTempImage(), resource name: " + resource?.name)
//        resourceTempUpdated(ResourceRepo.instance?.uploadTempResource(resource))
        resourceTempUpdated(resource)
    }

    private fun resourceTempUpdated(resource: Resource?) {
        Log.d(TAG, "CreatePostActivity>>>resourceTempUpdated()-1")
        runOnUiThread {
            Log.d(TAG, "CreatePostActivity>>>resourceTempUpdated()-2>>>run()")
            val adapter = recyclerView?.adapter as ResourcesAdapter
            if (resource != null) {
                adapter.resourceTempUpdated(resource)
            }
        }
    }

    private fun uploadImage(resource: Resource?) {
        Log.d(TAG, "CreatePostActivity>>>uploadImage(), resource name: " + resource?.name)
        resourceUpdated(ResourceRepo.instance?.uploadResource(resource))
    }

    private fun resourceUpdated(resource: Resource?) {
        Log.d(TAG, "CreatePostActivity>>>resourceUpdated()-1")
        runOnUiThread {
            Log.d(TAG, "CreatePostActivity>>>resourceUpdated()-2>>>run()")
            val adapter = recyclerView?.adapter as ResourcesAdapter
            if (resource != null) {
                adapter.resourceUpdated(resource)
            }
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
        backgroundHandler?.post({
            Log.d(TAG, "CreatePostActivity>>>handleUri()-2>>>run()")
            if (DocumentsContract.isDocumentUri(applicationContext, uri)) {
                contentResolver.takePersistableUriPermission(uri, flags)
            }

            val pair = Tools.getResourceNameAndType(applicationContext, uri)
            val resource = Resource(uri.toString(), pair.first, pair.second)
            // mark for testing
//            uploadImage(resource)
            uploadTempImage(resource)
        })
    }

    private fun getLayoutManager(context: Context?): RecyclerView.LayoutManager {
        return GridLayoutManager(context, getSpan())
    }

    private fun getSpan(): Int {
        return 1
    }

    private fun addItemDecoration(recyclerView: RecyclerView?) {
        recyclerView?.addItemDecoration(GridDividerItemDecoration(getSpan(), dividerSize))
    }

    private fun getAdapter(thumbSize: Int): ResourcesAdapter {
        return ResourcesAdapter(this, ArrayList(), thumbSize, statuses, object : ResourcesAdapter.ImageClickedListener {
            override fun onImageClicked(resource: Resource) {
                (this as ResourcesAdapter.ImageClickedListener).onImageClicked(resource)
            }

            override fun onDeleteClicked(resource: Resource, recent: Boolean?) {
                (this as ResourcesAdapter.ImageClickedListener).onDeleteClicked(resource, isRecent())
            }

            override fun onRetryClicked(resource: Resource) {
                (this as ResourcesAdapter.ImageClickedListener).onRetryClicked(resource)
            }

            override fun onCancelClicked(resource: Resource) {
                (this as ResourcesAdapter.ImageClickedListener).onCancelClicked(resource)
            }
        })
    }

//    private fun getData(): List<Resource> {
//        return ResourceRepo.instance?.list(statuses)!!
//    }

    private fun getData(): List<Resource> {
        return ResourceRepo.instance?.list(listOf(Resource.UploadStatus.QUEUED))!!
    }

    private fun isRecent(): Boolean {
        return false
    }

    private fun initThumbSizeAndLoadData() {
        val thumbSize = recyclerView!!.width / getSpan() - dividerSize / 2
        val adapter = getAdapter(thumbSize)

        recyclerView!!.adapter = adapter

        // fetch data after we know the size so we can request the exact size from Cloudinary
        adapter.replaceImages(getData())
    }
}