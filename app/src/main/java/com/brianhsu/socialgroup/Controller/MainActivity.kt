package com.brianhsu.socialgroup.Controller

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.ActionBar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.brianhsu.socialgroup.R
import com.brianhsu.socialgroup.Utilities.Tools
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_tabs_store_dark.*

class MainActivity : AppCompatActivity() {

    private var actionBar: ActionBar? = null
    private var isNavigationHide = false

    private lateinit var fragment1 : FragmentTabsStore
    private lateinit var fragment2 : FragmentTabsGallery
    private lateinit var fragment3 : FragmentTabsStore

    var debugTag: String = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.v(debugTag, "onCreate")

        initToolbar()
        initComponent()
        initFragment()
    }

    private fun initToolbar() {
        mainToolbar.setNavigationIcon(R.drawable.ic_menu)
        setSupportActionBar(mainToolbar)
        actionBar = supportActionBar
        actionBar?.title = "Recents"
        actionBar?.setDisplayHomeAsUpEnabled(true)
        Tools.setSystemBarColor(this, R.color.grey_1000)
    }

    private fun initComponent() {
        mainNavigation?.setOnNavigationItemSelectedListener { item ->
            val transaction = supportFragmentManager.beginTransaction()
            when (item.itemId) {
                R.id.navigation_recent -> {
                    actionBar?.title = item.title
                    val titleRelease = item.title.toString() + " New Release"
                    newReleaseTitleTabStore.text = titleRelease
                    transaction.replace(R.id.fragment_container, fragment1)
                    transaction.addToBackStack(null)
                    transaction.commit()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_favorites -> {
                    actionBar?.title = item.title
                    val titleRelease = item.title.toString() + " New Release"
                    newReleaseTitleTabStore.text = titleRelease
                    transaction.replace(R.id.fragment_container, fragment2)
                    transaction.addToBackStack(null)
                    transaction.commit()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_nearby -> {
                    actionBar?.title = item.title
                    val titleRelease = item.title.toString() + " New Release"
                    newReleaseTitleTabStore.text = titleRelease
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
        fragment1 = FragmentTabsStore.newInstance()
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

}