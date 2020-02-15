package me.kylermintah.tito


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_control.view.*
import me.kylermintah.tito.bluetooth.ConnectActivity

/**
 * A simple [Fragment] subclass.
 */
class ControlFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_control, container, false)

        view.connectButton.setOnClickListener(View.OnClickListener {
            val intent = Intent(view.context, ConnectActivity::class.java)
            startActivity(intent)
        })

        return view
    }



}
