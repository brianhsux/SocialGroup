package com.brianhsu.socialgroup.login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.Bindable
import android.databinding.ObservableField
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.android.databinding.library.baseAdapters.BR
import com.brianhsu.socialgroup.Sevices.AuthService
import com.brianhsu.socialgroup.Utilities.ObservableViewModel
import com.brianhsu.socialgroup.controller.CreateUserActivity
import com.brianhsu.socialgroup.login.model.LoginFields

class LoginViewModel : ObservableViewModel() {

    private val TAG: String = "BBB>>>LoginViewModel"

    @SuppressLint("StaticFieldLeak")
    private lateinit var context: Context
    private lateinit var login: LoginFields
    private var onFocusEmail: View.OnFocusChangeListener? = null
    private var onFocusPassword: View.OnFocusChangeListener? = null
    private var isShowSpinner: Boolean = false

    val emailText = ObservableField("Email")
    val passwordText = ObservableField("Password")

    internal fun init(context: Context) {
        this.context = context
        login = LoginFields()
        onFocusEmail = View.OnFocusChangeListener { view, focused ->
            val et = view as EditText
            if (et.text.isNotEmpty() && !focused) {
                login.isEmailValid(true)
            }
        }

        onFocusPassword = View.OnFocusChangeListener { view, focused ->
            val et = view as EditText
            if (et.text.isNotEmpty() && !focused) {
                login.isPasswordValid(true)
            }
        }
    }

    @Bindable
    fun getIsShowSpinner(): Boolean {
        return isShowSpinner
    }

    fun getLogin(): LoginFields {
        return login
    }

    fun getEmailOnFocusChangeListener(): View.OnFocusChangeListener? {
        return onFocusEmail
    }

    fun getPasswordOnFocusChangeListener(): View.OnFocusChangeListener? {
        return onFocusPassword
    }

    fun loginLoginBtnClicked() {
        if (login.isValid) {
            isShowSpinner = true
            hideKeyboard()
            notifyPropertyChanged(BR.isShowSpinner)

            AuthService.loginUser(login.email, login.password) { loginSuccess ->
                if (loginSuccess) {
                    AuthService.findUserByEmail(context) { findSuccess ->
                        if (findSuccess) {
                            isShowSpinner = false
                            notifyPropertyChanged(BR.isShowSpinner)
                            (context as Activity).finish()
                        } else {
                            showErrorToast()
                        }
                    }
                } else {
                    showErrorToast()
                }
            }
        }
    }

    fun loginCreateUserBtnClicked() {
        val createUserIntent = Intent(context, CreateUserActivity::class.java)
        context.startActivity(createUserIntent)
        (context as Activity).finish()
    }

    private fun showErrorToast() {
        Toast.makeText(context, "Something went wrong, please try again.",
                Toast.LENGTH_LONG).show()
        isShowSpinner = false
        notifyPropertyChanged(BR.isShowSpinner)
    }

    private fun hideKeyboard() {
        val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow((context as Activity).currentFocus.windowToken, 0)
        }
    }
}