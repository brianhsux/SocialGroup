package com.brianhsu.socialgroup.login

import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.databinding.Bindable
import android.databinding.ObservableField
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.android.databinding.library.baseAdapters.BR
import com.brianhsu.socialgroup.Sevices.AuthService
import com.brianhsu.socialgroup.Utilities.ObservableViewModel
import com.brianhsu.socialgroup.login.model.LoginFields

class LoginViewModel : ObservableViewModel() {

    private val TAG: String = "BBB>>>LoginViewModel"

    private var email: String = ""
    private var password: String = ""
    @SuppressLint("StaticFieldLeak")
    private lateinit var context: Context
    private lateinit var login: LoginFields
    private var onFocusEmail: View.OnFocusChangeListener? = null
    private var onFocusPassword: View.OnFocusChangeListener? = null
    private val buttonClick = MutableLiveData<LoginFields>()
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
    fun getSpinnerVisibility(): Int {
        return if (isShowSpinner) View.VISIBLE else View.GONE
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

    fun onLoginBtnClicked() {
        if (login.isValid) {
            isShowSpinner = true
            hideKeyboard()
            notifyPropertyChanged(BR.spinnerVisibility)
            notifyPropertyChanged(BR.isShowSpinner)
            buttonClick.value = login

            AuthService.loginUser(login.email, login.password) { loginSuccess ->
                if (loginSuccess) {
                    AuthService.findUserByEmail(context) { findSuccess ->
                        if (findSuccess) {
                            isShowSpinner = false
                            (context as Activity).finish()
                        } else {
                            errorToast()
                        }
                    }
                } else {
                    errorToast()
                }
            }
        }
    }

    private fun errorToast() {
        Toast.makeText(context, "Something went wrong, please try again.",
                Toast.LENGTH_LONG).show()
        isShowSpinner = false
    }

    private fun hideKeyboard() {
        val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow((context as Activity).currentFocus.windowToken, 0)
        }
    }
}