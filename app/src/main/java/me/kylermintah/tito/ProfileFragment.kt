package me.kylermintah.tito


import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import me.kylermintah.tito.glide.GlideApp
import me.kylermintah.tito.util.FirestoreUtil
import me.kylermintah.tito.util.StorageUtil
import org.jetbrains.anko.doAsyncResult
import org.jetbrains.anko.uiThread
import java.io.ByteArrayOutputStream

/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : Fragment() {

    private var camera_image_uri: Uri? = null
    private val RC_SELECT_IMAGE = 2
    private val WRITE_EZTERNAL_STORAGE = 1000
    private val IMAGE_CAPTURE_CODE = 1001
    private lateinit var selectedImageBytes: ByteArray
    private var pictureJustChanged = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_profile, container, false)
        view.apply {
            FirestoreUtil.getCurrentUser { user ->
                if (!pictureJustChanged && user.profilePicturePath != null && user.profilePicturePath!!.isNotEmpty()) {
                    Glide.with(this)
                        .load(StorageUtil.pathToImageReference(user.profilePicturePath!!))
                        .placeholder(R.color.titoBlue)
                        .into(profile_image)
                }
            }
            changeProfilePhotoButton.setOnClickListener {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                profileImageChangeAlert(view)
            }
            recordButton.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                Toast.makeText(this.context, "Train Voice Profile", Toast.LENGTH_SHORT).show()
                val intent = Intent(this.context, RecordActivity::class.java)
                        startActivity(intent)
            }

            changeDeviceName.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                Toast.makeText(this.context, "Change Device Name", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val selectedImagePath: Uri?
        if (requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK &&
            data != null && data.data != null
        ) {
            selectedImagePath = data.data
            val outputStream = ByteArrayOutputStream()
            val source: ImageDecoder.Source =
                ImageDecoder.createSource(
                    activity!!.contentResolver,
                    Uri.parse(selectedImagePath.toString())
                )
            val selectedImageBmp: Bitmap = ImageDecoder.decodeBitmap(source)
            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            selectedImageBytes = outputStream.toByteArray()
            GlideApp.with(this).load(selectedImageBytes).into(profile_image)
            pictureJustChanged = true
            doAsyncResult {
                while (!::selectedImageBytes.isInitialized) {
                }
                uiThread {
                    val user = FirebaseAuth.getInstance().currentUser
                    StorageUtil.uploadProfilePhoto(selectedImageBytes) { imagePath ->
                        FirestoreUtil.updateCurrentUser(
                            user!!.displayName.toString(),
                            user.email.toString(),
                            imagePath,
                            null,
                            null
                        )
                        pictureJustChanged = false

                    }

                }
            }
        } else if (requestCode == IMAGE_CAPTURE_CODE) {
            println(camera_image_uri)
            val source: ImageDecoder.Source =
                ImageDecoder.createSource(
                    activity!!.contentResolver,
                    Uri.parse(camera_image_uri.toString())
                )
            val selectedImageBmp: Bitmap = ImageDecoder.decodeBitmap(source)
//            val selectedImageBmp: Bitmap =
//                BitmapFactory.decodeFile(view!!.context.getFileStreamPath(camera_image_uri.toString()).toString())

            val outputStream = ByteArrayOutputStream()
            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            selectedImageBytes = outputStream.toByteArray()
            GlideApp.with(this).load(selectedImageBytes).into(profile_image)
            pictureJustChanged = true
            doAsyncResult {
                while (!::selectedImageBytes.isInitialized) {
                }
                uiThread {
                    val user = FirebaseAuth.getInstance().currentUser
                    StorageUtil.uploadProfilePhoto(selectedImageBytes) { imagePath ->
                        FirestoreUtil.updateCurrentUser(
                            user!!.displayName.toString(),
                            user.email.toString(),
                            imagePath,
                            null,
                            null
                        )
                        pictureJustChanged = false

                    }

                }
            }

        } else {
            selectedImagePath = Uri.parse("")
        }
//            val selectedImageBmp = MediaStore.Images.Media.getBitmap(activity?.contentResolver, selectedImagePath)

    }

    override fun onStart() {
        super.onStart()
        FirestoreUtil.getCurrentUser { user ->
            if (!pictureJustChanged && user.profilePicturePath != null && user.profilePicturePath!!.isNotEmpty()) {
                GlideApp.with(this)
                    .load(StorageUtil.pathToImageReference(user.profilePicturePath!!))
                    .placeholder(R.color.titoBlue)
                    .into(profile_image)
            }
        }
    }

    fun profileImageChangeAlert(view: View) {
        // Initialize a new instance of
        val builder = AlertDialog.Builder(this.context)

        // Set the alert dialog title
        builder.setTitle("Change ProPic")

        // Display a message on alert dialog
        builder.setMessage("Updating your ProPic can help friends recognize you")

        // Set a positive button and its click listener on alert dialog
        builder.setPositiveButton("Gallery") { dialog, which ->
            Toast.makeText(this.context, "Change Propic", Toast.LENGTH_SHORT).show()
            val intent = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg,image/png"))
            }
            startActivityForResult(
                Intent.createChooser(intent, "Select Image"),
                RC_SELECT_IMAGE
            )
        }


        // Display a negative button on alert dialog
        builder.setNegativeButton("Camera") { dialog, which ->

            openCamera(view)
        }


        // Display a neutral button on alert dialog
        builder.setNeutralButton("Cancel") { _, _ ->

        }

        // Finally, make the alert dialog using builder
        val dialog: AlertDialog = builder.create()

        // Display the alert dialog on app interface
        dialog.show()
    }

    private fun openCamera(view: View) {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New ProPic")
        values.put(MediaStore.Images.Media.DESCRIPTION, "TITO Profile Photo")
        camera_image_uri = view.context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, camera_image_uri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

}
