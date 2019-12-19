package me.kylermintah.tito


import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_friends.view.*
import me.kylermintah.tito.adapter.UserAdapter
import me.kylermintah.tito.model.User


/**
 * A simple [Fragment] subclass.
 */
class FriendsFragment : Fragment() {
    private var mLastClickTime: Long = 0
    private var recyclerView: RecyclerView? = null
    private var userAdapter: UserAdapter? = null
    private lateinit var mUser : MutableList<User?>
    private var textChanged = false
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var searchBar : EditText
    private lateinit var adapter: FirestoreRecyclerAdapter<User, UserAdapter.ViewHolder>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        countDownTimer = object : CountDownTimer(
            300,
            100
        ) {
            override fun onTick(l: Long) {}
            override fun onFinish() {
                val searchText: String = searchBar.getText().toString()
                println("Searching")
                firebaseUserSearch(searchText)
            }
        }
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_friends, container, false)

        searchBar = view.findViewById(R.id.searchBar)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        mUser = ArrayList()
        userAdapter = context?.let{UserAdapter(it,mUser as ArrayList<User>, true)}
        recyclerView?.adapter = userAdapter

        view.searchBar.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!textChanged) {
                    textChanged = true
                    countDownTimer.start()
                } else {
                    countDownTimer.cancel()
                    countDownTimer.start()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

        view.invite.setOnClickListener {

            if (SystemClock.elapsedRealtime() - mLastClickTime > 2000){
                try {
                    it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "text/plain"
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "TITO")
                    val shareMessage = "Let me recommend this fresh new product to you!\n\n" +
                            "Introducing TITO (Tune In, Tune Out)\n\n" +
                            "The innovative new noise isolation snap-on-solution for my headset!" +
                            "\nProduct set to drop in May so check out their DevPost for updates here:" +
                            "\n\nhttps://devpost.com/software/tune-in-tune-out"
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                    startActivity(Intent.createChooser(shareIntent, "Share"))

                } catch (e: Exception) { //e.toString();
                }
            }
            mLastClickTime = SystemClock.elapsedRealtime();
        }
        return view
    }

   private fun firebaseUserSearch(queryText:String) {
       var searchText = queryText.toLowerCase().trim()
       var query: Query = FirebaseFirestore.getInstance()
           .collection("users").orderBy("username").startAfter(searchText).endBefore(searchText + "\uf8ff")
       var response:FirestoreRecyclerOptions<User> = FirestoreRecyclerOptions.Builder<User>()
           .setQuery(query, User::class.java)
           .build()

       adapter = object :
           FirestoreRecyclerAdapter<User, UserAdapter.ViewHolder>(
               response
           ) {
           override fun onBindViewHolder(
               holder: UserAdapter.ViewHolder,
               position: Int,
               model: User
           ) { //DONE: Update UserImageURL
               holder.setDetails(context, model.username, model.profilePicturePath)
               System.out.println(model.profilePicturePath)
           }

           override fun onCreateViewHolder(
               group: ViewGroup,
               i: Int
           ): UserAdapter.ViewHolder { // Create a new instance of the ViewHolder, in this case we are using a custom
// layout called R.layout.message for each item
               val view: View = LayoutInflater.from(group.context)
                   .inflate(R.layout.user_item_layout, group, false)
               return UserAdapter.ViewHolder(view)
           }
       }
   }
}
