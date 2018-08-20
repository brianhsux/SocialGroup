package com.brianhsu.socialgroup.controller

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.brianhsu.socialgroup.R
import kotlinx.android.synthetic.main.activity_setting_sectioned.*
import kotlinx.android.synthetic.main.toolbar.*

class SettingSectioned : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_sectioned)
        initToolbar()
        initComponent()
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Device Setting"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun initComponent() {
        debugModeSwitch.isChecked = App.prefs.isDebugMode
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.menu_search, menu)
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

    fun debugModeSwitchClicked(view: View) {
        if (debugModeSwitch.isChecked) {
            App.prefs.baseUrl = App.prefs.BASE_C9_URL
            App.prefs.isDebugMode = true
            Toast.makeText(applicationContext, "Open Debug Mode, Use C9 Server", Toast.LENGTH_SHORT).show()
        } else {
            App.prefs.isDebugMode = false
            App.prefs.baseUrl = App.prefs.BASE_HEROKU_URL
            Toast.makeText(applicationContext, "Close Debug Mode, Use Heroku Server", Toast.LENGTH_SHORT).show()
        }
    }
}
