package me.kylermintah.tito

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home.*

private const val FILE_PICKER_ID = 12
private const val PERMISSION_REQUEST = 10

class HomeActivity : AppCompatActivity() {

    var mAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        mAuth.addAuthStateListener {
            if (mAuth.currentUser == null) {
                this.finish()
            }
        }
        val adapter = MyViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(ProfileFragment(), "")
        adapter.addFragment(ControlFragment(), "")
        adapter.addFragment(FriendsFragment(), "")
        viewPager.adapter = adapter
        tabs.setupWithViewPager(viewPager)
        tabs.getTabAt(0)!!.setIcon(R.drawable.ic_profileicon)
        tabs.getTabAt(1)!!.setIcon(R.drawable.ic_bluetoothicon)
        tabs.getTabAt(2)!!.setIcon(R.drawable.ic_usersicon)
        viewPager.setCurrentItem(1, false)

        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        ActivityCompat.requestPermissions(
            this, permissions, 0
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.menuSignOut -> {
                mAuth.signOut()
                val intent = Intent(this, LogInActivity::class.java)
                this.startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    class MyViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
        private val fragmentList: MutableList<Fragment> = ArrayList()

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getCount(): Int {
            return fragmentList.size//To change body of created functions use File | Settings | File Templates.
        }

        fun addFragment(fragment: Fragment, icon: String) {
            fragmentList.add(fragment)
        }
    }


}