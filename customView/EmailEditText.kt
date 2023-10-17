package com.app.dicodingstoryapp.customView

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.app.dicodingstoryapp.ApiService
import com.app.dicodingstoryapp.R


class EmailEditText : AppCompatEditText {


    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_baseline_email_24)
        setIconDrawables(startOfTheText = drawable)
        hint = ""
        compoundDrawablePadding = 20
        inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email = s.toString().trim()
                if (email.isEmpty()) {
                    error = "Masukkan email"
                } else if (!ApiService.isEmailValid(email)) {
                    error = "Masukkan alamat email yang valid"
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

}