package com.brianhsu.socialgroup.login

import android.databinding.BindingAdapter
import android.support.design.widget.TextInputEditText
import android.util.Log
import android.view.View
import android.widget.ProgressBar

object BindingAdapters {
    val TAG: String = "BBB>>>BindingAdapters"

    @BindingAdapter("error")
    @JvmStatic fun setError(editText: TextInputEditText, strOrResId: Any?) {
        Log.d(TAG, "setError(), strOrResId: $strOrResId")
        if (strOrResId is Int) {
            editText.error = editText.context.getString(strOrResId)
        } else {
            // Do nothing when editText is empty
//            editText.error = strOrResId as String
//            editText.error = "Something error!"
        }
    }

    @BindingAdapter("onFocus")
    @JvmStatic fun bindFocusChange(editText: TextInputEditText, onFocusChangeListener: View.OnFocusChangeListener?) {
        Log.d(TAG, "bindFocusChange()")
        if (editText.onFocusChangeListener == null) {
            editText.onFocusChangeListener = onFocusChangeListener
        }
    }

    @BindingAdapter("visibility")
    @JvmStatic fun progressBarVisibility(progressBar: ProgressBar, visible: Boolean) {
        when(visible) {
            true -> progressBar.visibility = View.VISIBLE
            false -> progressBar.visibility = View.GONE
        }
    }
}