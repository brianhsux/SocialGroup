package com.brianhsu.socialgroup.login

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import android.arch.lifecycle.ViewModelProviders
import com.brianhsu.socialgroup.R
import com.brianhsu.socialgroup.Sevices.AuthService
import com.brianhsu.socialgroup.controller.CreateUserActivity
import com.brianhsu.socialgroup.databinding.ActivityLoginBinding
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private var actionBar: ActionBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_login)

        // Obtain ViewModel from ViewModelProviders
        val loginViewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        loginViewModel.init(this)

        // Obtain binding
        val activityLoginBinding : ActivityLoginBinding =
                DataBindingUtil.setContentView(this, R.layout.activity_login)

        // Bind layout with ViewModel
        activityLoginBinding.loginviewmodel = loginViewModel

        enableSpinner(false)
    }

//    fun loginLoginBtnClicked(view: View) {
//        enableSpinner(true)
//
//        val email = loginEmailText.text.toString()
//        val password = loginPasswordText.text.toString()
//
//        hideKeyboard()
//        if (email.isNotEmpty() && password.isNotEmpty()) {
//            AuthService.loginUser(email, password) { loginSuccess ->
//                if (loginSuccess) {
//                    AuthService.findUserByEmail(this) { findSuccess ->
//                        if (findSuccess) {
//                            enableSpinner(false)
//                            finish()
//                        } else {
//                            errorToast()
//                        }
//                    }
//                } else {
//                    errorToast()
//                }
//            }
//        } else {
//            Toast.makeText(this, "Make sure email, and password are filled in.",
//                    Toast.LENGTH_LONG).show()
//            enableSpinner(false)
//        }
//    }

    fun errorToast() {
        Toast.makeText(this, "Something went wrong, please try again.",
                Toast.LENGTH_LONG).show()
        enableSpinner(false)
    }

    fun loginCreateUserBtnClicked(view: View) {
        val createUserIntent = Intent(this, CreateUserActivity::class.java)
        startActivity(createUserIntent)
        finish()
    }

    fun enableSpinner(enable: Boolean) {
        if (enable) {
            loginSpinner.visibility = View.VISIBLE
        } else {
            loginSpinner.visibility = View.INVISIBLE
        }

        loginEmailText.isEnabled = !enable
        loginPasswordText.isEnabled = !enable
        loginLoginBtn.isEnabled = !enable
        loginCreateUserBtn.isEnabled = !enable
    }
}
