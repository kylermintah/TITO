package me.kylermintah.tito

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_log_in.*
import kotlinx.android.synthetic.main.activity_log_in.passwordTextBox
import kotlinx.android.synthetic.main.activity_log_in.passwordTextBoxLayout
import kotlinx.android.synthetic.main.activity_log_in.signUpButton
import kotlinx.android.synthetic.main.activity_log_in.usernameTextBox
import kotlinx.android.synthetic.main.activity_log_in.usernameTextBoxLayout
import kotlinx.android.synthetic.main.activity_sign_up.*
import org.jetbrains.anko.activityUiThreadWithContext
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.doAsyncResult
import org.jetbrains.anko.uiThread
import java.util.*
import kotlin.NoSuchElementException

class LogInActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        mAuth = FirebaseAuth.getInstance()
        signUpButton.setOnClickListener {
            val signUpIntent = Intent(this, SignUpActivity::class.java)
            this.startActivity(signUpIntent)
            this.finish()
        }
        logInButton.setOnClickListener {
            logIn()
        }
    }

    private fun logIn() {
        //Get username from firestore
        val username: String = usernameTextBox.text.toString().toLowerCase(Locale.US).trim()
        val password: String = passwordTextBox.text.toString()

        doAsyncResult {
            var email = "null"
                email = validateCredentials(username, password)

            activityUiThreadWithContext {
                try {
                    executeLogIn(email)
                } catch (e1: NoSuchElementException) {
                    e1.printStackTrace()
                    usernameTextBoxLayout.error = "invalid username"
                } catch (e2: NoSuchFieldException) {
                    e2.printStackTrace()
                    passwordTextBoxLayout.error = "password incorrect"
                }
            }
        }
    }

    private fun validateCredentials(username: String, password: String): String {
        if (username.isEmpty()) {
            usernameTextBoxLayout.error = "required"
        }
        if (password.isEmpty()) {
            passwordTextBoxLayout.error = "required"
        }
        if (password.length < 6) {
            passwordTextBoxLayout.error = "incorrect password"
        }

        val email: String = if (isValidEmail(username)) {
            username
        } else {

            retrieveEmail(username)

        }
        if (email.equals("unknown")) {
            throw NoSuchElementException("No email address found")
        }
        return email
    }

    private fun isValidEmail(target: CharSequence): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    private fun retrieveEmail(username: String): String {
        val TAG = "GET EMAIL"
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(username)
        var email = "null"

        docRef.get()
            .addOnSuccessListener { document: DocumentSnapshot? ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    email = document.data!!["email"].toString()
                    Log.d(TAG, email)
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
        return email
    }

    override fun onStart() {
        val currentUser: FirebaseUser? = mAuth.currentUser
        if (currentUser != null) {
            val homeIntent = Intent(this, HomeActivity::class.java)
            this.startActivity(homeIntent)
            this.finish()
        }
        super.onStart()
    }

    private fun executeLogIn(email: String) {
        val password: String = passwordTextBox.text.toString()
        println("email: $email password: haha, sike")
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) {
                    println(it.exception)
                    passwordTextBoxLayout.error = "invalid username/password"
                    return@addOnCompleteListener
                } else {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    println("Success")
                    this.finish()
                }
            }
    }
}
