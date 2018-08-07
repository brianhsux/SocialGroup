package com.brianhsu.socialgroup.Controller

import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Toast
import com.brianhsu.socialgroup.R
import com.brianhsu.socialgroup.Sevices.AuthService
import com.brianhsu.socialgroup.Utilities.BROADCAST_USER_DATA_CHANGE
//import com.example.brianhsu.smack.R
//import com.example.brianhsu.smack.Services.AuthService
//import com.example.brianhsu.smack.Utilities.BROADCAST_USER_DATA_CHANGE
import kotlinx.android.synthetic.main.activity_create_user.*
import java.util.*

class CreateUserActivity : AppCompatActivity() {


    var userAvatar = "profiledefault"
    var avatarColor = "[0.5, 0.5, 0.5, 1]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)

        initToolbar()
        createSpinner.visibility = View.INVISIBLE
    }

    private fun initToolbar() {
        setSupportActionBar(mainToolbar)
        supportActionBar?.setTitle("Sign Up")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    fun ClosedRange<Int>.random() =
            Random().nextInt((endInclusive + 1) - start) +  start

    fun generateUserAvatar(view: View) {
        val randomNum = (1..8).random()

        userAvatar = when (radioGroupGender.checkedRadioButtonId) {
            radio_male.id -> "photo_male_$randomNum"
            else -> "photo_female_$randomNum"
        }

        val imageResId = resources.getIdentifier(userAvatar, "drawable", packageName)
        createAvatarImageView.setImageResource(imageResId)
    }

    fun createUserClicked(view: View) {
        enableSpinner(true)

        val userName = createUserNameText.text.toString()
        val email = createUserEmailText.text.toString()
        val password = createUserPasswordText.text.toString()
        val gender = when (radioGroupGender.checkedRadioButtonId) {
            radio_male.id -> "male"
            else -> "female"
        }

        if (userName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
            AuthService.registerUser(email, password) { registerSuccess ->
                if (registerSuccess) {
                    AuthService.loginUser(email, password) { loginSuccess ->
                        if (loginSuccess) {
                            AuthService.createUser(userName, email, userAvatar, avatarColor, gender) { createSuccess ->
                                if (createSuccess) {
                                    val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
                                    LocalBroadcastManager.getInstance(this).sendBroadcast(userDataChange)
                                    enableSpinner(false)
                                    finish()
                                } else {
                                    errorToast()
                                }
                            }
                        } else {
                            errorToast()
                        }
                    }
                } else {
                    errorToast()
                }
            }
        } else {
            Toast.makeText(this, "Make sure user name, email, and password are filled in.",
                    Toast.LENGTH_LONG).show()
            enableSpinner(false)
        }


    }

    fun errorToast() {
        Toast.makeText(this, "Something went wrong, please try again.",
                Toast.LENGTH_LONG).show()
        enableSpinner(false)
    }

    fun enableSpinner(enable: Boolean) {
        if (enable) {
            createSpinner.visibility = View.VISIBLE
        } else {
            createSpinner.visibility = View.INVISIBLE
        }

        createAvatarImageView.isEnabled = !enable
        createUserBtn.isEnabled = !enable
    }
}
