package me.kylermintah.tito.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.FileInputStream
import java.util.*

object StorageUtil{
    private val storageInstanceImage:FirebaseStorage by lazy { FirebaseStorage.getInstance()}
    private val currentUserRefImage: StorageReference
        get() = storageInstanceImage.reference

    private val storageInstanceAudio:FirebaseStorage by lazy { FirebaseStorage.getInstance()}
    private val currentUserRefAudio: StorageReference
        get() = storageInstanceAudio.reference

    fun uploadProfilePhoto(imageBytes:ByteArray,
                           onSuccess:(imagePath:String) -> Unit) {
        val ref = currentUserRefImage.child("userPhotos/${UUID.nameUUIDFromBytes(imageBytes)}")
        ref.putBytes((imageBytes))
            .addOnSuccessListener { onSuccess(ref.path) }
    }

    fun uploadAudio(fileInputStream:FileInputStream,
                           onSuccess:(audioPath:String) -> Unit) {
        val ref = currentUserRefAudio.child("userVoiceRecordings/${FirebaseAuth.getInstance().currentUser!!.displayName}/${UUID.randomUUID()}")
        ref.putStream((fileInputStream))
            .addOnSuccessListener { onSuccess(ref.path) }
    }

    fun pathToImageReference(path:String) = storageInstanceImage.getReference(path)
    fun pathToAudioReference(path:String) = storageInstanceAudio.getReference(path)

}