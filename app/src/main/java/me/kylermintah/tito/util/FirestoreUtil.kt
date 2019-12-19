package me.kylermintah.tito.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import me.kylermintah.tito.model.User

object FirestoreUtil {
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val mAuth = FirebaseAuth.getInstance()
    private val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document("users/${mAuth.currentUser?.displayName}")


    fun initCurrentUserIfFirstTime(onComplete: () -> Unit) {
        currentUserDocRef.get().addOnCompleteListener { documentSnapshot ->
            if (documentSnapshot.result!!.exists()) {
                val newUser =
                    User(
                        mAuth.currentUser?.displayName ?: "",
                        "",
                        "",
                        ArrayList(),
                        "")
                currentUserDocRef.set(newUser).addOnSuccessListener {
                    onComplete()
                }
            } else
                onComplete()
        }
    }

    fun updateCurrentUser(
        username: String = "",
        email: String = "",
        profilePicturePath: String? = null,
        friends: ArrayList<String>? = null,
        voiceSamplePath: String?
    ) {
        val userFieldMap = mutableMapOf<String, Any>()
        if (username.isNotBlank()) userFieldMap["username"] = username
        if (email.isNotBlank()) userFieldMap["email"] = email
        if (profilePicturePath != null)
            userFieldMap["profilePicturePath"] = profilePicturePath
        if (friends != null)
            userFieldMap["friends"] = friends
        if (voiceSamplePath != null)
            userFieldMap["voiceSamplePath"] = voiceSamplePath
        currentUserDocRef.update(userFieldMap)
    }

    fun getCurrentUser(onComplete: (User) -> Unit) {

        currentUserDocRef.get().addOnSuccessListener {
            onComplete(
                it.toObject(
                    User::class.java
                )!!
            )
        }
    }
}