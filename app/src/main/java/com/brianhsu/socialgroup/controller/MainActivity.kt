package com.brianhsu.socialgroup.controller

import android.content.*
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.DocumentsContract
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.recyclerview.R.attr.layoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.brianhsu.socialgroup.R
import com.brianhsu.socialgroup.Sevices.AuthService
import com.brianhsu.socialgroup.Sevices.CloudinaryService
import com.brianhsu.socialgroup.Sevices.PostService
import com.brianhsu.socialgroup.Sevices.UserDataServices
import com.brianhsu.socialgroup.Utilities.*
import com.brianhsu.socialgroup.model.Resource
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_social_wall.*
import kotlinx.android.synthetic.main.include_drawer_header_news.*

class MainActivity : AppCompatActivity() {

    private var actionBar: ActionBar? = null
    private var isNavigationHide = false

    private lateinit var fragment1 : SocialWallFragment
    private lateinit var fragment2 : FragmentTabsGallery
    private lateinit var fragment3 : FragmentTabsStore

    var debugTag: String = "MainActivity"

    private var parent_view: View? = null
    private var backgroundHandler: Handler? = null

    private var postsDataChangedReceiver: BroadcastReceiver? = null
    private var postsSendActionReceiver: BroadcastReceiver? = null
    private var postMoreInfoDialogReceiver: BroadcastReceiver? = null
    private var userDataChangeReceiver: BroadcastReceiver? = null

    private var mBehavior: BottomSheetBehavior<View>? = null
    private var mBottomSheetDialog: BottomSheetDialog? = null

    private var mIsRunning: Boolean = false


    private fun unregisterSocialGroupReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(socialGroupReceiver!!)
    }

    private fun registerSocialGroupReceiver() {
        Log.d(TAG, "registerSocialGroupReceiver()")
        val filter = IntentFilter()
        filter.addAction(BROADCAST_USER_DATA_CHANGE)
        filter.addAction(CloudinaryService.ACTION_RESOURCE_MODIFIED)
        filter.addAction(BROADCAST_SEND_POST_ACTION)
        filter.addAction(BROADCAST_EDIT_POST_ACTION)
        filter.addAction(BROADCAST_POST_MORE_INFO_DIALOG)
        LocalBroadcastManager.getInstance(this).registerReceiver(socialGroupReceiver, filter)
    }

    private val socialGroupReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            val action = intent?.action
            Log.d(TAG, "ACTION = ${intent?.action} ")

            if (!action.isNullOrEmpty()) {
                when (action) {
                    BROADCAST_USER_DATA_CHANGE -> {
                        Log.d(TAG, "BROADCAST_USER_DATA_CHANGE")
                        if (App.prefs.isLoggedIn) {
                            userNameNavHeader.text = UserDataServices.name
                            userEmailNavHeader.text = UserDataServices.email

                            val resourceId = resources.getIdentifier(UserDataServices.avatarName, "drawable", packageName)
                            userImageNavHeader.setImageResource(resourceId)

                            loginBtnNavHeader.text = "Logout"

                            refreshSocialWallUi()
                        }
                    }

                    CloudinaryService.ACTION_RESOURCE_MODIFIED -> {
                        Log.d(TAG, "CloudinaryService.ACTION_RESOURCE_MODIFIED")
                        if (!isFinishing) {
                            if (CloudinaryService.ACTION_RESOURCE_MODIFIED == intent.action) {
                                val resource = intent.getSerializableExtra("resource") as Resource

                                // TODO: How can we know this time should new post or edit post?
                                if (!resource.cloudinaryPublicId.isNullOrEmpty()) {
                                    if (PostService.editPost.postId == "") {
                                        PostService.createPost(UserDataServices.email, UserDataServices.name,
                                                UserDataServices.avatarName, resource.cloudinaryPublicId!!,
                                                App.prefs.postContent) { createSuccess ->
                                            if (createSuccess) {
                                                refreshSocialWallUi()
                                            } else {
                                                errorToast()
                                            }
                                        }
                                    } else {
                                        PostService.editPost(PostService.editPost.postId, UserDataServices.email,
                                                UserDataServices.name, UserDataServices.avatarName,
                                                resource.cloudinaryPublicId!!, App.prefs.postContent) { createSuccess ->
                                            if (createSuccess) {
                                                refreshSocialWallUi()
                                            } else {
                                                errorToast()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    BROADCAST_SEND_POST_ACTION -> {
                        Log.d(TAG, "BROADCAST_SEND_POST_ACTION")
                        enableSpinner(true)

                        val bundle: Bundle?
                        var uri: Uri? = null
                        var clip: ClipData? = null
                        var flags = 0

                        try {
                            bundle = intent.getBundleExtra("EXTRA_BUNDLE")
                            uri = bundle.getParcelable<Uri>("EXTRA_URI")
                            clip = bundle.getParcelable<ClipData>("EXTRA_CLIP")
                            flags = bundle.getInt("EXTRA_FLAGS")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        uploadDataToCloudinary(uri, clip, flags)
                    }

                    BROADCAST_EDIT_POST_ACTION -> {
                        Log.d(TAG, "BROADCAST_EDIT_POST_ACTION")
                        enableSpinner(true)

                        val content = intent.getStringExtra("CONTENT")
                        val bundle = intent.getBundleExtra("EXTRA_BUNDLE")
                        val postImageId = bundle.getString("EXTRA_POST_IMAGE_ID")
                        var uri: Uri? = null
                        var clip: ClipData? = null
                        var flags = 0

                        try {
                            uri = bundle.getParcelable<Uri>("EXTRA_URI")
                            clip = bundle.getParcelable<ClipData>("EXTRA_CLIP")
                            flags = bundle.getInt("EXTRA_FLAGS")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        Log.d(TAG, "BROADCAST_EDIT_POST_ACTION, content: $content, postImageId: $postImageId")

                        when (content) {
                            BROADCAST_EDIT_POST_LOCAL_IMAGE_CONTENT -> {
                                Log.d(TAG, "BROADCAST_EDIT_POST_LOCAL_IMAGE_CONTENT")

                                // TODO: Need to do with another method
                                uploadDataToCloudinary(uri, clip, flags)
                            }

                            BROADCAST_EDIT_POST_REMOTE_IMAGE_CONTENT -> {
                                Log.d(TAG, "BROADCAST_EDIT_POST_REMOTE_IMAGE_CONTENT, postId: ${PostService.editPost.postId}")

                                PostService.editPost(PostService.editPost.postId, UserDataServices.email, UserDataServices.name,
                                        UserDataServices.avatarName, PostService.editPost.postImageId,
                                        App.prefs.postContent) { createSuccess ->
                                    if (createSuccess) {
                                        refreshSocialWallUi()
                                    } else {
                                        errorToast()
                                    }
                                }
                            }
                        }

                    }

                    BROADCAST_POST_MORE_INFO_DIALOG -> {
                        Log.d(TAG, "BROADCAST_POST_MORE_INFO_DIALOG")
                        val bundle: Bundle?
                        var postId: String? = ""
                        try {
                            bundle = intent.getBundleExtra("EXTRA_BUNDLE")
                            postId = bundle?.getString("EXTRA_POST_ID")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        if (!postId.isNullOrEmpty()) {
                            showBottomSheetDialog(postId!!)
                        }
                    }
                }
            }
        }
    }

    fun refreshSocialWallUi() {
        PostService.readAllPosts(this) { readSuccess ->
            if (readSuccess) {
                if (mIsRunning) {
                    fragment1.refresh()
                    App.prefs.needRefreshOnResume = false
                } else {
                    App.prefs.needRefreshOnResume = true
                }
                enableSpinner(false)
            } else {
                errorToast()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initToolbar()
        initNavigationMenu()
        initComponent()
        initFragment()

        mIsRunning = true

        parent_view = findViewById<View>(android.R.id.content)

        registerSocialGroupReceiver()

        val handlerThread = HandlerThread("MainActivityWorker")
        handlerThread.start()
        backgroundHandler = Handler(handlerThread.looper)

        enableSpinner(true)
        refreshSocialWallUi()

        if (App.prefs.isLoggedIn) {
            AuthService.findUserByEmail(this) { findSuccess ->
                if (findSuccess) {
                    enableSpinner(false)
                } else {
                    App.prefs.isLoggedIn = false
                    errorToast()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterSocialGroupReceiver()
    }

    private fun initToolbar() {
        setSupportActionBar(mainToolbar)
        actionBar = supportActionBar
        actionBar?.title = "Recents"
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeButtonEnabled(true)
        Tools.setSystemBarColor(this, R.color.grey_1000)
    }

    private fun initNavigationMenu() {
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(this, drawer, mainToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.setDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener { item ->
            Toast.makeText(applicationContext, item.title.toString() + " Selected", Toast.LENGTH_SHORT).show()
            actionBar?.setTitle(item.title)
            drawer.closeDrawers()
            true
        }

        // open drawer at start
        drawer.openDrawer(GravityCompat.START)
    }

    private fun initComponent() {
        mainNavigation?.setOnNavigationItemSelectedListener { item ->
            val transaction = supportFragmentManager.beginTransaction()
            when (item.itemId) {
                R.id.navigation_recent -> {
                    actionBar?.title = item.title
                    transaction.replace(R.id.fragment_container, fragment1)
                    transaction.addToBackStack(null)
                    transaction.commitAllowingStateLoss()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_favorites -> {
                    actionBar?.title = item.title
                    transaction.replace(R.id.fragment_container, fragment2)
                    transaction.addToBackStack(null)
                    transaction.commitAllowingStateLoss()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_nearby -> {
                    actionBar?.title = item.title
                    transaction.replace(R.id.fragment_container, fragment3)
                    transaction.addToBackStack(null)
                    transaction.commitAllowingStateLoss()
                    return@setOnNavigationItemSelectedListener true
                }
                else -> {
                    return@setOnNavigationItemSelectedListener false
                }
            }
        }

//        mainNestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener {
//            _, _, scrollY, _, oldScrollY ->
//            if (scrollY < oldScrollY) { // up
//                animateNavigation(false)
//                animateSearchBar(false)
//            }
//            if (scrollY > oldScrollY) { // down
//                animateNavigation(true)
//                animateSearchBar(true)
//            }
//        })


        mBehavior = BottomSheetBehavior.from<View>(bottom_sheet)
    }

    private fun initFragment() {
        fragment1 = SocialWallFragment.newInstance()
        fragment2 = FragmentTabsGallery.newInstance()
        fragment3 = FragmentTabsStore.newInstance()

        supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragment1)
                .show(fragment1)
                .commit()
    }

    override fun onResume() {
        super.onResume()
        mIsRunning = true

        if (App.prefs.needRefreshOnResume) {
            fragment1.refresh()
        }

        Log.d(TAG, "userEmail: ${App.prefs.userEmail}")
    }

    override fun onPause() {
        super.onPause()
        mIsRunning = false
    }

    fun enableSpinner(enable: Boolean) {
        if (enable) {
            loaddingSpinner.visibility = View.VISIBLE
        } else {
            loaddingSpinner.visibility = View.INVISIBLE
        }

    }

    fun errorToast() {
        Toast.makeText(this, "Something went wrong, please try again.",
                Toast.LENGTH_LONG).show()
        enableSpinner(false)
    }

    private fun animateNavigation(hide: Boolean) {
        if (isNavigationHide && hide || !isNavigationHide && !hide) return
        isNavigationHide = hide
        val moveY = if (hide) 2 * mainNavigation.height else 0
        mainNavigation.animate().translationY(moveY.toFloat()).setStartDelay(100).setDuration(300).start()
    }

    private var isSearchBarHide = false

    private fun animateSearchBar(hide: Boolean) {
        if (isSearchBarHide && hide || !isSearchBarHide && !hide) return
        isSearchBarHide = hide
        val moveY = if (hide) -(2 * mainSearchBar.height) else 0
        mainSearchBar.animate().translationY(moveY.toFloat()).setStartDelay(100).setDuration(300).start()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search_setting, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        } else if (item.itemId == R.id.action_settings) {
            val openSettingsIntent = Intent(this, SettingSectioned::class.java)
            startActivity(openSettingsIntent)
        } else {
            Toast.makeText(applicationContext, item.title, Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
    }


    fun loginBtnNavClicked(view: View) {
        if (App.prefs.isLoggedIn) {
            UserDataServices.logout()
//            channelAdapter.notifyDataSetChanged()
//            messageAdapter.notifyDataSetChanged()

            userNameNavHeader.text = ""
            userEmailNavHeader.text = ""
            userImageNavHeader.setImageResource(R.drawable.profiledefault)
            userImageNavHeader.setBackgroundColor(Color.TRANSPARENT)
            loginBtnNavHeader.text = "Login"

            refreshSocialWallUi()

        } else {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }
    }

    fun newPostFabBtnClicked(view: View) {
        val createPostIntent = Intent(this, CreatePostActivity::class.java)

        if (App.prefs.isLoggedIn && App.prefs.userEmail.isNotEmpty()) {
            startActivity(createPostIntent)
        } else {
            if (parent_view != null) {
                Snackbar.make(parent_view!!, "Please Login", Snackbar.LENGTH_SHORT).show()
            }
        }

    }

    private fun uploadDataToCloudinary(uri: Uri?, clipData: ClipData?, flags: Int) {
            val takeFlags = flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            if (uri != null) {
                handleUriNew(uri, takeFlags)
            } else if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    handleUriNew(clipData.getItemAt(i).uri, takeFlags)
                }
            }
    }

    private fun handleUriNew(uri: Uri, flags: Int) {
        backgroundHandler?.post({
            if (DocumentsContract.isDocumentUri(applicationContext, uri)) {
                contentResolver.takePersistableUriPermission(uri, flags)
            }
            val pair = Tools.getResourceNameAndType(applicationContext, uri)
            val resource = Resource(uri.toString(), pair.first, pair.second)
            uploadImageNew(resource)
        })
    }

    private fun uploadImageNew(resource: Resource?) {
        ResourceRepo.instance?.uploadResource(resource)
    }

    private fun showBottomSheetDialog(postId: String) {
        if (mBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) {
            mBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        val view = layoutInflater.inflate(R.layout.sheet_list, null)

        (view.findViewById(R.id.lyt_edit_post) as View).setOnClickListener {
//            Toast.makeText(applicationContext, "Edit Post $postId", Toast.LENGTH_SHORT).show()
            mBottomSheetDialog?.dismiss()
            PostService.getPostById(postId) { getSuccess ->
                if (getSuccess) {
                    Toast.makeText(applicationContext, "Get Post $postId Success.", Toast.LENGTH_SHORT).show()
                    val editPostIntent = Intent(this, EditPostActivity::class.java)
                    startActivity(editPostIntent)
                } else {
                    errorToast()
                }
            }
        }

        (view.findViewById(R.id.lyt_delete_post) as View).setOnClickListener {
            Toast.makeText(applicationContext, "Delete Post $postId", Toast.LENGTH_SHORT).show()
            mBottomSheetDialog?.dismiss()
            PostService.deletePostById(postId) { deleteSuccess ->
                if (deleteSuccess) {
                    refreshSocialWallUi()
                } else {
                    errorToast()
                }
            }
        }

        mBottomSheetDialog = BottomSheetDialog(this)
        mBottomSheetDialog?.setContentView(view)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBottomSheetDialog?.window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }

        mBottomSheetDialog?.show()
        mBottomSheetDialog?.setOnDismissListener(DialogInterface.OnDismissListener { mBottomSheetDialog = null })
    }
}