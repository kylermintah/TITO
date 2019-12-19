package me.kylermintah.tito.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import de.hdodenhof.circleimageview.CircleImageView
import me.kylermintah.tito.R
import me.kylermintah.tito.glide.GlideApp
import me.kylermintah.tito.model.User

class UserAdapter(
    private var mContext: Context,
    private var mUser: List<User>,
    private var isFragment: Boolean = false
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var usernameTextView: TextView = itemView.findViewById(R.id.itemUserName)
        var userProfileImageView: CircleImageView = itemView.findViewById(R.id.profilePhotoSearch)
        var followButton: MaterialButton = itemView.findViewById(R.id.followButton)

        fun setDetails(
            con: Context?,
            userName: String?,
            userImage: String?
        ) {
            usernameTextView.text = userName
            if (userImage == null) {
                userProfileImageView.setImageResource(R.color.titoBlue)
            } else {
                if (con != null) {
                    GlideApp.with(con)
                        .load(userImage).into(userProfileImageView)
                }
            }
//            user_page.setOnClickListener {
//                val intent =
//                    Intent(con, User::class.java).putExtra("username", userName)
//                        .addFlags(
//                            Intent.FLAG_ACTIVITY_NEW_TASK
//                        )
//                con.startActivity(intent)
//            }
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.ViewHolder {
        val view = LayoutInflater.from((mContext)).inflate(R.layout.user_item_layout, parent, false)
        return UserAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUser.size
    }

    override fun onBindViewHolder(holder: UserAdapter.ViewHolder, position: Int) {
        var user = mUser[position]
        holder.usernameTextView.text = user.username
        GlideApp.with(mContext).load(user.profilePicturePath).into(holder.userProfileImageView)
    }



}