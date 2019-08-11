package com.droid.symbipool

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_authentication.*


class AuthenticationActivity : AppCompatActivity() {

    private var mBottomSheetDialog: BottomSheetDialog? = null
    private var emailList: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        initEmailList()
        lottieAnimation.playAnimation()
        clickListeners()
    }

    private fun initEmailList() {
        FirebaseFirestore.getInstance().collection(DatabaseUtils.EMAILS_COLLECTION)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e(javaClass.simpleName, "Query error: ${firebaseFirestoreException.message}")
                    return@addSnapshotListener
                }

                querySnapshot?.documents?.run {
                    if (this.isNullOrEmpty()) {
                        return@addSnapshotListener
                    } else {
                        emailList = this[0]["email"] as ArrayList<String>
                        Log.d(javaClass.simpleName, "Emails: $emailList")
                    }
                }
            }
    }

    private fun clickListeners() {
        btLogin.setOnClickListener {
            openBS(AuthType.LOGIN)
        }

        btSignup.setOnClickListener {
            openBS(AuthType.SIGN_UP)
        }
    }

    override fun onStop() {
        mBottomSheetDialog?.dismiss()
        super.onStop()
    }

    private fun openBS(authType: AuthType) {
        if (!isConnectedToNetwork(this)) {
            Snackbar.make(rootLayout, "Please verify your network connection to proceed", Snackbar.LENGTH_LONG).show()
            return
        }

        mBottomSheetDialog = BottomSheetDialog(this)
        mBottomSheetDialog?.window?.setDimAmount(0.9F)
        val sheetView = layoutInflater.inflate(
            R.layout.auth_bottom_sheet, null
        )

        val btnNext = sheetView.findViewById(R.id.btnNext) as Button
        val etPasswordLayout = sheetView.findViewById(R.id.inputPassword) as TextInputLayout
        val etEmailLayout = sheetView.findViewById(R.id.inputEmail) as TextInputLayout
        val etPassword = sheetView.findViewById(R.id.etPassword) as TextInputEditText
        val etEmail = sheetView.findViewById(R.id.etEmail) as TextInputEditText
        val progressBar = sheetView.findViewById(R.id.progress) as ProgressBar

        etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                etPasswordLayout.error = null
            }
        })

        etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                etEmailLayout.error = null
            }
        })

        if (authType == AuthType.LOGIN)
            btnNext.text = "Login"
        else {
            btnNext.text = "Sign up"
        }

        mBottomSheetDialog?.setContentView(sheetView)
        mBottomSheetDialog?.show()

        btnNext.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (!isEmailValid(email)) {
                etEmailLayout.error = "Invalid Email Address"
                return@setOnClickListener
            }

            if (password.length < 6) {
                etPasswordLayout.error = "Password must be at-least 7 characters"
                return@setOnClickListener
            }

            progressBar.visible()
            btnNext.gone()

            if (authType == AuthType.SIGN_UP) {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        mBottomSheetDialog?.dismiss()
                        if (it.isSuccessful) {
                            val currentUser = FirebaseAuth.getInstance().currentUser
                            currentUser?.sendEmailVerification()?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    showSignupSuccess()
                                } else {
                                    showAuthFailure()
                                }
                            }
                        } else {
                            Snackbar.make(rootLayout, "${it.exception?.message}", Snackbar.LENGTH_LONG).show()
                        }
                    }
            } else {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        mBottomSheetDialog?.dismiss()
                        if (task.isSuccessful) {
                            val currentUser = FirebaseAuth.getInstance().currentUser
                            if (currentUser != null && currentUser.isEmailVerified) {
                                processToDashboard()
                            } else {
                                Snackbar.make(rootLayout, "Please verify your email to proceed", Snackbar.LENGTH_LONG)
                                    .show()
                            }
                        } else {
                            Snackbar.make(rootLayout, "${task.exception?.message}", Snackbar.LENGTH_LONG).show()
                        }
                    }
            }
        }
    }

    private fun showAuthFailure() {
        MaterialDialog(this@AuthenticationActivity).show {
            title(R.string.auth_fail_title)
            cancelable(false)
            message(R.string.auth_fail_message)
            positiveButton(R.string.try_again) {
                dismiss()
            }
        }
    }

    private fun showSignupSuccess() {
        MaterialDialog(this@AuthenticationActivity).show {
            title(R.string.sign_up_success_title)
            cancelable(false)
            message(text = "Please login after verifying your email. \n \nVerification link has been sent to ${FirebaseAuth.getInstance().currentUser?.email}")
            positiveButton(R.string.try_again) {
                dismiss()
            }
        }
    }

    private fun isEmailValid(email: CharSequence): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()


        //TODO Uncheck email
        //&& isSymbosisEmail(email)
    }

    private fun isSymbosisEmail(userEmail: CharSequence): Boolean {
        if (emailList.isNullOrEmpty())
            return true

        val modifiedEmail = userEmail.substring(userEmail.lastIndexOf("@") + 1)
        emailList.forEach { email ->
            if (modifiedEmail == (email))
                return true
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null && user.isEmailVerified) {
            processToDashboard()
        }
    }

    private fun processToDashboard() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    enum class AuthType {
        LOGIN, SIGN_UP
    }
}
