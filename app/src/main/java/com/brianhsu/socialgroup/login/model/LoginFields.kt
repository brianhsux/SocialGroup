package com.brianhsu.socialgroup.login.model

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.databinding.ObservableField
import com.brianhsu.socialgroup.R
import com.brianhsu.socialgroup.BR

class LoginFields : BaseObservable() {

    private var TAG: String = "BBB>>>LoginFields"

    // Notify that the valid property could have changed.
    var email: String = ""
        set(email) {
            field = email
            notifyPropertyChanged(BR.valid)
        }
    // Notify that the valid property could have changed.
    var password: String = ""
        set(password) {
            field = password
            notifyPropertyChanged(BR.valid)
        }
    var emailError: ObservableField<Int> = ObservableField()
    var passwordError: ObservableField<Int> = ObservableField()

    val isValid: Boolean
        @Bindable
        get() {
            var valid = isEmailValid(false)
            valid = isPasswordValid(false) && valid
            return valid
        }

    fun isEmailValid(setMessage: Boolean): Boolean {
        // Minimum a@b.c
        if (email.length > 5) {
            val indexOfAt = email.indexOf("@")
            val indexOfDot = email.lastIndexOf(".")
            return when {
                indexOfAt in 1..(indexOfDot - 1) && indexOfDot < email.length - 1 -> {
                    emailError.set(null)
                    true
                }
                else -> {
                    if (setMessage)
                        emailError.set(R.string.error_format_invalid)
                    false
                }
            }
        }
        if (setMessage) {
            emailError.set(R.string.error_too_short)
        }

        return false
    }

    fun isPasswordValid(setMessage: Boolean): Boolean {
        return when {
            password.length > 5 -> {
                passwordError.set(null)
                true
            }
            else -> {
                if (setMessage)
                    passwordError.set(R.string.error_too_short)
                false
            }
        }
    }
}
