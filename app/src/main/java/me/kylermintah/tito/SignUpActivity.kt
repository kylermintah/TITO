package me.kylermintah.tito

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_sign_up.*
import me.kylermintah.tito.util.FirestoreUtil
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.Locale.US


class SignUpActivity : AppCompatActivity() {
    private var PRIVATE_MODE = 0
    private val PREF_NAME = "welcome-page"
    private lateinit var mAuth: FirebaseAuth
    private var textChanged = false
    private val countDownTimer: CountDownTimer = object : CountDownTimer(
        2000,
        1000
    ) {
        override fun onTick(l: Long) {}
        override fun onFinish() {
            val finalUsername: String = usernameTextBox.getText().toString().trim()
            if (!finalUsername.isEmpty()) {

                doAsync{
                    val usernameExists = userExists(finalUsername)
                        println(usernameExists)
                        uiThread {
                            if (usernameExists) {
                                usernameTextBoxLayout.error = "username taken"
                            } else {
                                usernameTextBox.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_checkmark, 0);
                            }
                        }
                }
            }
        }
    }



        fun userExists(username: String): Boolean { //        FirebaseFirestore db = FirebaseFirestore.getInstance();

            val db: FirebaseFirestore = FirebaseFirestore.getInstance()
            val answer = true
            val docRef: DocumentReference = db.collection("users").document(username)
            val documentTask: Task<DocumentSnapshot> = docRef.get()
            while (!documentTask.isComplete) { //TODO: Create one of those spinning Wheel things (It's called a progress bar)
            }
            if (documentTask.isSuccessful) {
                val document: DocumentSnapshot? = documentTask.result
                println(document )
                if (document!!.exists()) {
                    return true
                }
                if (!document.exists()) {
                    return false
                }
            }
            return answer
        }

    override fun onBackPressed() {
        val intent = Intent(this, LogInActivity::class.java)
        startActivity(intent)
        this.finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        mAuth = FirebaseAuth.getInstance()
        signUpButton.setOnClickListener{
            createUser()
        }
        addTextListener(emailTextBox, emailTextBoxLayout)
        addTextListener(passwordTextBox, passwordTextBoxLayout)
        addTextListener(confirmPasswordTextBox, confirmPasswordTextBoxLayout)
        usernameTextBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
                usernameTextBox.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                usernameTextBoxLayout.error = ""
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
            }

            override fun afterTextChanged(editable: Editable) {
                if (!textChanged) {
                    textChanged = true
                    countDownTimer.start()
                } else {
                    countDownTimer.cancel()
                    countDownTimer.start()
                }
            }
        })
    }

    private fun addTextListener(textBox : TextInputEditText, textBoxLayout: TextInputLayout) {
        textBox.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textBoxLayout.error = ""
            }
        })
    }

    private fun createUser() {
        val email: String = emailTextBox.text.toString().toLowerCase().trim()
        val password: String = passwordTextBox.text.toString()
        if (validate()) {
            mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
                val user = FirebaseAuth.getInstance().currentUser
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(usernameTextBox.text.toString().toLowerCase(US).trim()).build()
                user!!.updateProfile(profileUpdates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("YEBO YES", "User profile updated.")
                            FirestoreUtil.initCurrentUserIfFirstTime {  }
                            it.doAsync {
                                val user = hashMapOf(
                                    "email" to email,
                                    "photoURL" to "",
                                    "freinds" to null,
                                    "voiceSampleURL" to ""
                                )
                                val db = FirebaseFirestore.getInstance()

                                db.collection("users").document(usernameTextBox.text.toString().toLowerCase().trim())
                                    .set(user)
                                    .addOnSuccessListener { Log.d("CREATE USER", "DocumentSnapshot successfully written!") }
                                    .addOnFailureListener { e -> Log.w("CREATE USER", "Error writing document", e) }
                            }
                            mAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener {

                                val sharedPref: SharedPreferences = getSharedPreferences(PREF_NAME, PRIVATE_MODE)
                                if (sharedPref.getBoolean(PREF_NAME, false)) {
                                    val homeIntent = Intent(this, HomeActivity::class.java)
                                    val welcomeIntent = Intent(this, WelcomeScreen::class.java)
                                    startActivity(homeIntent)
                                    startActivity(welcomeIntent)
                                    finish()
                                } else {

                                    val editor = sharedPref.edit()
                                    editor.putBoolean(PREF_NAME, true)
                                    editor.apply()
                                    val intent  = Intent(this, HomeActivity::class.java)
                                    this.startActivity(intent)
                                    val welcomeIntent = Intent(this, WelcomeScreen::class.java)
                                    startActivity(welcomeIntent)
                                    this.finish()
                                }


                            }
                        }
                    }


            }
        }
    }

    private fun validate(): Boolean {
        val email: String = emailTextBox.text.toString().toLowerCase().trim()
        val username: String = usernameTextBox.text.toString().toLowerCase().trim()
        val password: String = passwordTextBox.text.toString()
        val confirmPassword: String = confirmPasswordTextBox.text.toString()
        var returnValue = true
        if (email.isEmpty()) {
            emailTextBoxLayout.error = "required"
            returnValue = false
        }
        if (username.isEmpty()) {
            usernameTextBoxLayout.error = "required"
            returnValue = false
        }
        if (password.isEmpty()) {
            passwordTextBoxLayout.error = "required"
            returnValue = false
        }
        if (confirmPassword.isEmpty()) {
            confirmPasswordTextBoxLayout.error = "required"
            returnValue = false
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordTextBoxLayout.error = "passwords do not match"
            returnValue = false
        }
        if (!isValidEmail(email)) {
            emailTextBoxLayout.error = "invalid email address"
            returnValue = false
        }
        return returnValue
    }
    private fun isValidEmail(target: CharSequence): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }
}
