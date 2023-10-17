package com.app.dicodingstoryapp.customView

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.app.dicodingstoryapp.ApiService
import com.app.dicodingstoryapp.R

class PasswordEditText : AppCompatEditText {
//    private val errorIcon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_error_24)
//    private val passwordIcon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_lock_24)
//    private val passwordVisibilityIcon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_password_toggle_24)
//    private var isPasswordVisible: Boolean = false

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private var isPasswordVisible = false

    private fun init() {
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_baseline_lock_24)
        setIconDrawables(startOfTheText = drawable)
        hint = ""
        compoundDrawablePadding = 20
        inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
        setPasswordVisibility(isPasswordVisible)

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val password = s.toString().trim()
                if (password.isEmpty()) {
                    error = "Masukkan password"
                } else if (!ApiService.isPasswordValid(password)) {
                    error = "Password minimal 8 karakter"
                } else {
                    error = null
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }

    private fun setIconDrawables(
        startOfTheText: Drawable? = null,
        topOfTheText:Drawable? = null,
        endOfTheText:Drawable? = null,
        bottomOfTheText: Drawable? = null
    ){
        setCompoundDrawablesWithIntrinsicBounds(
            startOfTheText,
            topOfTheText,
            endOfTheText,
            bottomOfTheText
        )
    }

    private fun setPasswordVisibility(isVisible: Boolean) {
        if (isVisible) {
            transformationMethod = null
        } else {
            transformationMethod = PasswordTransformationMethod.getInstance()
        }
        requestFocus()
    }

    private fun togglePassword() {
        isPasswordVisible = !isPasswordVisible
        setPasswordVisibility(isPasswordVisible)
    }

}