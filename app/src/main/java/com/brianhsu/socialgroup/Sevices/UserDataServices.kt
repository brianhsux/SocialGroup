package com.brianhsu.socialgroup.Sevices

import android.graphics.Color
import com.brianhsu.socialgroup.controller.App
import java.util.*

/**
 * Created by brian on 2018/3/27.
 */

object UserDataServices {

    var id = ""
    var avatarName = ""
    var avatarColor = ""
    var email = ""
    var name = ""
    var gender = ""

    fun logout() {
        id = ""
        avatarName = ""
        avatarColor = ""
        email = ""
        name = ""
        gender = ""

        App.prefs.authToken = ""
        App.prefs.userEmail = ""
        App.prefs.isLoggedIn = false

//        MessageService.clearChannels()
//        MessageService.clearMessages()
    }

    fun returnAvatarColor(components: String) : Int {
        val strippedColor = components
                .replace("[", "")
                .replace("]", "")
                .replace(",", "")

        var r = 0
        var g = 0
        var b = 0

        val scanner = Scanner(strippedColor)

        if (scanner.hasNext()) {
            r = (scanner.nextDouble() * 255).toInt()
            g = (scanner.nextDouble() * 255).toInt()
            b = (scanner.nextDouble() * 255).toInt()
        }

        return Color.rgb(r, g, b)
    }
}