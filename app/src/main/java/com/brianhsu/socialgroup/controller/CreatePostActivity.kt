package com.brianhsu.socialgroup.controller

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.brianhsu.socialgroup.Sevices.UserDataServices
import com.brianhsu.socialgroup.Utilities.*
import com.brianhsu.socialgroup.model.Resource
import kotlinx.android.synthetic.main.activity_create_post.*
import java.util.*

class CreatePostActivity : AppCompatActivity() {

    private var backgroundHandler: Handler? = null

    private var recyclerView: RecyclerView? = null
    private var dividerSize: Int = 0
    private val statuses = listOf(Resource.UploadStatus.QUEUED)
    private var selectData: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        initToolbar()
        initUserInfo()
        prepareRecyclerView()
        val handlerThread = HandlerThread("MainActivityWorker")
        handlerThread.start()
        backgroundHandler = Handler(handlerThread.looper)
        startService(Intent(this, CloudinaryService::class.java))
        enableSpinner(false)
    }

    public override fun onResume() {
        super.onResume()
        if (recyclerView != null) {
            if (recyclerView!!.width > 0) {
                initThumbSizeAndLoadData()
            } else {
                recyclerView!!.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
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
            if (requestCode == CHOOSE_IMAGE_REQUEST_CODE && data != null) {
                uploadImageFromIntentUri(data)
                selectData = data
            }
        }
    }

    private fun initToolbar() {
        setSupportActionBar(mainToolbar)
        supportActionBar?.title = "New Post"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initUserInfo() {
        if (App.prefs.isLoggedIn) {
            userNameCreatePost.text = UserDataServices.name

            val resourceId = resources.getIdentifier(UserDataServices.avatarName, "drawable", packageName)
            userImageCreatePost.setImageResource(resourceId)
        }
    }

    private fun prepareRecyclerView() {
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
            R.id.action_send_post -> doSendPostAction()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun doSendPostAction() {
        enableSpinner(true)
        App.prefs.postContent = contentCreatePost.text.toString()
        // 1. Upload the image to the cloudinary
        // 2. Get the url of the image in cloudinary
        // 3. Send the post request with the social group api to server
        // 4. If success, goto SocialWallFragment and refresh the adapter in it.

        val sendPostAction = Intent(BROADCAST_SEND_POST_ACTION)
        val bundle = Bundle()
        bundle.putParcelable("EXTRA_URI", selectData?.data)
        bundle.putParcelable("EXTRA_CLIP", selectData?.clipData)
        bundle.putInt("EXTRA_FLAGS", selectData!!.flags)
        sendPostAction.putExtra("EXTRA_BUNDLE", bundle)

        LocalBroadcastManager.getInstance(this).sendBroadcast(sendPostAction)
        finish()
    }

    fun uploadGalleryFabBtnClicked(view: View) {
        Tools.openMediaChooser(this, CHOOSE_IMAGE_REQUEST_CODE)
    }

    private fun uploadTempImage(resource: Resource?) {
        resourceTempUpdated(resource)
    }

    private fun resourceTempUpdated(resource: Resource?) {
        runOnUiThread {
            val adapter = recyclerView?.adapter as ResourcesAdapter
            if (resource != null) {
                adapter.resourceTempUpdated(resource)
            }
        }
    }

    private fun uploadImageFromIntentUri(data: Intent) {
        val takeFlags = data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        val uri = data.data
        if (uri != null) {
            handleUri(uri, takeFlags)
        } else if (data.clipData != null) {
            val clip = data.clipData
            for (i in 0 until clip!!.itemCount) {
                handleUri(clip.getItemAt(i).uri, takeFlags)
            }
        }
    }

    private fun handleUri(uri: Uri, flags: Int) {
        backgroundHandler?.post({
            if (DocumentsContract.isDocumentUri(applicationContext, uri)) {
                contentResolver.takePersistableUriPermission(uri, flags)
            }

            val pair = Tools.getResourceNameAndType(applicationContext, uri)
            val resource = Resource(uri.toString(), pair.first, pair.second)
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

    private fun getData(): List<Resource> {
        return ResourceRepo.instance?.list(listOf(Resource.UploadStatus.QUEUED))!!
    }

    private fun isRecent(): Boolean {
        return false
    }

    private fun initThumbSizeAndLoadData() {
        if (recyclerView != null) {
            val thumbSize = recyclerView!!.width / getSpan() - dividerSize / 2
            val adapter = getAdapter(thumbSize)

            recyclerView!!.adapter = adapter

            // fetch data after we know the size so we can request the exact size from Cloudinary
            adapter.replaceImages(getData())
        }
    }

    fun enableSpinner(enable: Boolean) {
        if (enable) {
            createSpinner.visibility = View.VISIBLE
        } else {
            createSpinner.visibility = View.INVISIBLE
        }

        contentCreatePost.isEnabled = !enable
        galleryCreatePost.isEnabled = !enable
        fabAddGallery.isEnabled = !enable
    }
}