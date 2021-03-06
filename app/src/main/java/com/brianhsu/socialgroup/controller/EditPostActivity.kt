package com.brianhsu.socialgroup.controller

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.DocumentsContract
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import com.brianhsu.socialgroup.Adapters.ResourcesAdapter
import com.brianhsu.socialgroup.R
import com.brianhsu.socialgroup.Sevices.CloudinaryService
import com.brianhsu.socialgroup.Sevices.PostService
import com.brianhsu.socialgroup.Sevices.UserDataServices
import com.brianhsu.socialgroup.Utilities.*
import com.brianhsu.socialgroup.model.Resource
import kotlinx.android.synthetic.main.activity_edit_post.*
import java.util.ArrayList

class EditPostActivity : AppCompatActivity(), ResourcesAdapter.ImageClickedListener {

    private var backgroundHandler: Handler? = null

    private var recyclerView: RecyclerView? = null
    private var dividerSize: Int = 0
    private val statuses = listOf(Resource.UploadStatus.QUEUED)
    private var selectData: Intent? = null
    private var shouldHandleEditPostImage: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_post)

        initToolbar()
        initUserInfo()
        prepareRecyclerView()
        val handlerThread = HandlerThread("MainActivityWorker")
        handlerThread.start()
        backgroundHandler = Handler(handlerThread.looper)
        startService(Intent(this, CloudinaryService::class.java))
    }

    public override fun onResume() {
        super.onResume()

        val editable: Editable = SpannableStringBuilder(PostService.editPost.postContent)
        contentEditPost.text = editable
        if (shouldHandleEditPostImage) {
            handleEditPostImage()
        }

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
            shouldHandleEditPostImage = false
            if (requestCode == CHOOSE_IMAGE_REQUEST_CODE && data != null) {
                uploadImageFromIntentUri(data)
                selectData = data
            }
        }
    }

    private fun initToolbar() {
        setSupportActionBar(mainToolbar)
        supportActionBar?.title = "Edit Post"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initUserInfo() {
        if (App.prefs.isLoggedIn) {
            userNameEditPost.text = UserDataServices.name

            val resourceId = resources.getIdentifier(UserDataServices.avatarName, "drawable", packageName)
            userImageEditPost.setImageResource(resourceId)
        }
    }

    private fun prepareRecyclerView() {
        recyclerView = findViewById(R.id.galleryEditPost)

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
        menuInflater.inflate(R.menu.menu_edit_post, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
            R.id.action_edit_post -> doEditPostAction()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun doEditPostAction() {
        App.prefs.postContent = contentEditPost.text.toString()
        // 1. Upload the image to the cloudinary
        // 2. Get the url of the image in cloudinary
        // 3. Send the post request with the social group api to server
        // 4. If success, goto SocialWallFragment and refresh the adapter in it.

        if (selectData != null) {
            val editPostAction = Intent(BROADCAST_EDIT_POST_ACTION)
            editPostAction.putExtra("CONTENT", BROADCAST_EDIT_POST_LOCAL_IMAGE_CONTENT)
            val bundle = Bundle()
            bundle.putParcelable("EXTRA_URI", selectData?.data)
            bundle.putParcelable("EXTRA_CLIP", selectData?.clipData)
            bundle.putInt("EXTRA_FLAGS", selectData!!.flags)
            editPostAction.putExtra("EXTRA_BUNDLE", bundle)

            LocalBroadcastManager.getInstance(this).sendBroadcast(editPostAction)
            finish()
        } else if (!TextUtils.isEmpty(PostService.editPost.postImageId)) {
            val editPostAction = Intent(BROADCAST_EDIT_POST_ACTION)
            editPostAction.putExtra("CONTENT", BROADCAST_EDIT_POST_REMOTE_IMAGE_CONTENT)
            val bundle = Bundle()
            bundle.putString("EXTRA_POST_IMAGE_ID", PostService.editPost.postImageId)
            editPostAction.putExtra("EXTRA_BUNDLE", bundle)

            LocalBroadcastManager.getInstance(this).sendBroadcast(editPostAction)
            finish()
        } else {
            Toast.makeText(applicationContext, "Choose a image to share.", Toast.LENGTH_SHORT).show()
        }

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
                adapter.replaceImage(resource)
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

    private fun handleEditPostImage() {
        backgroundHandler?.post({
            val resource = Resource()
            resource.cloudinaryPublicId = PostService.editPost.postImageId
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

    override fun onImageClicked(resource: Resource) {
        Toast.makeText(applicationContext, "onImageClicked", Toast.LENGTH_SHORT).show()
    }

    override fun onCancelClicked(resource: Resource) {
        Toast.makeText(applicationContext, "onCancelClicked", Toast.LENGTH_SHORT).show()
        runOnUiThread {
            val adapter = recyclerView?.adapter as ResourcesAdapter
            adapter.removeImage(resource)
        }
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
            val adapter = ResourcesAdapter(this, ArrayList(), thumbSize, statuses, this)

            recyclerView!!.adapter = adapter

            // fetch data after we know the size so we can request the exact size from Cloudinary
            adapter.replaceImages(getData())
        }
    }
}