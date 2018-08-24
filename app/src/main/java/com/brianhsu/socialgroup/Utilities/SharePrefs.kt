package com.brianhsu.socialgroup.Utilities

import android.content.Context
import android.content.SharedPreferences
import com.android.volley.toolbox.Volley

/**
 * Created by brian on 2018/4/12.
 */
class SharePrefs(context: Context) {

    val PREFS_FILENAME = "prefs"
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0)

    val USER_EMAIL = "userEmail"
    val AUTH_TOKEN = "authToken"
    val IS_LOGGED_IN = "isLoggedIn"
    val BASE_URL = "baseUrl"
    val IS_DEBUG_MODE = "isDebugMode"
    var POST_CONTENT = "postContent"

    val BASE_C9_URL = "https://webdevbootcamp-brianhsux.c9users.io/v1/"
    val BASE_HEROKU_URL = "https://socialgroupapi.herokuapp.com/v1/"

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(IS_LOGGED_IN, value).apply()

    var authToken: String
        get() = prefs.getString(AUTH_TOKEN, "")
        set(value) = prefs.edit().putString(AUTH_TOKEN, value).apply()

    var userEmail: String
        get() = prefs.getString(USER_EMAIL, "")
        set(value) = prefs.edit().putString(USER_EMAIL, value).apply()

    var baseUrl: String
        get() = prefs.getString(BASE_URL, BASE_HEROKU_URL)
        set(value) = prefs.edit().putString(BASE_URL, value).apply()

    var isDebugMode: Boolean
        get() = prefs.getBoolean(IS_DEBUG_MODE, false)
        set(value) = prefs.edit().putBoolean(IS_DEBUG_MODE, value).apply()

    var postContent: String
        get() = prefs.getString(POST_CONTENT, "")
        set(value) = prefs.edit().putString(POST_CONTENT, value).apply()

    val requestQueue = Volley.newRequestQueue(context)

    val URL_REGISTER = "${baseUrl}account/register"
    val URL_LOGIN = "${baseUrl}account/login"
    val URL_CREATE_USER = "${baseUrl}user/add"
    val URL_GET_USER = "${baseUrl}user/byEmail/"

    val URL_CREATE_POST = "${baseUrl}post/add"
    val URL_READ_POST = "${baseUrl}post"
    val URL_DELETE_POST = "${baseUrl}post"
}