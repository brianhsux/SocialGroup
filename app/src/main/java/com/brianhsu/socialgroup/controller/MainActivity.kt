package com.brianhsu.socialgroup.controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.brianhsu.socialgroup.R
import com.brianhsu.socialgroup.Sevices.UserDataServices
import com.brianhsu.socialgroup.Utilities.BROADCAST_USER_DATA_CHANGE
import com.brianhsu.socialgroup.Utilities.Tools
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.include_drawer_header_news.*

class MainActivity : AppCompatActivity() {

    private var actionBar: ActionBar? = null
    private var isNavigationHide = false

    private lateinit var fragment1 : SocialWallFragment
    private lateinit var fragment2 : FragmentTabsGallery
    private lateinit var fragment3 : FragmentTabsStore

    var debugTag: String = "MainActivity"

    private val userDataChangeReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (App.prefs.isLoggedIn) {
                userNameNavHeader.text = UserDataServices.name
                userEmailNavHeader.text = UserDataServices.email

                val resourceId = resources.getIdentifier(UserDataServices.avatarName, "drawable", packageName)
                userImageNavHeader.setImageResource(resourceId)

                loginBtnNavHeader.text = "Logout"

//                MessageService.getChannels { complete ->
//                    if (complete) {
//                        if (MessageService.channels.count() > 0) {
//                            selectedChannel = MessageService.channels[0]
//                            channelAdapter.notifyDataSetChanged()
//                            updateWithChannel()
//                        }
//                    }
//                }
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

        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver,
                IntentFilter(BROADCAST_USER_DATA_CHANGE))
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
//                    val titleRelease = item.title.toString() + " New Release"
//                    newReleaseTitleTabStore.text = titleRelease
                    transaction.replace(R.id.fragment_container, fragment1)
                    transaction.addToBackStack(null)
                    transaction.commit()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_favorites -> {
                    actionBar?.title = item.title
//                    val titleRelease = item.title.toString() + " New Release"
//                    newReleaseTitleTabStore.text = titleRelease
                    transaction.replace(R.id.fragment_container, fragment2)
                    transaction.addToBackStack(null)
                    transaction.commit()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_nearby -> {
                    actionBar?.title = item.title
//                    val titleRelease = item.title.toString() + " New Release"
//                    newReleaseTitleTabStore.text = titleRelease
                    transaction.replace(R.id.fragment_container, fragment3)
                    transaction.addToBackStack(null)
                    transaction.commit()
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
//            mainChannelName.text = "Please log in!!"

        } else {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }
    }

    fun newPostFabBtnClicked(view: View) {
        val createPostIntent = Intent(this, CreatePostActivity::class.java)
        startActivity(createPostIntent)
    }
}