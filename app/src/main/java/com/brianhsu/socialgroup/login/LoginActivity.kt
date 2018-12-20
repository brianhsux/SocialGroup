package com.brianhsu.socialgroup.login

import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.arch.lifecycle.ViewModelProviders
import com.brianhsu.socialgroup.R
import com.brianhsu.socialgroup.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtain ViewModel from ViewModelProviders
        val loginViewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        loginViewModel.init(this)

        // Obtain binding
        val activityLoginBinding : ActivityLoginBinding =
                DataBindingUtil.setContentView(this, R.layout.activity_login)

        // Bind layout with ViewModel
        activityLoginBinding.loginviewmodel = loginViewModel
    }
}
