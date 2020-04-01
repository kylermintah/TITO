package me.kylermintah.tito.bluetooth.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.JsonWriter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Writer;
import java.util.Timer;

import me.kylermintah.tito.bluetooth.BleAdapterService;
import me.kylermintah.tito.Constants;
import me.kylermintah.tito.R;
import me.kylermintah.tito.bluetooth.BleAdapterService;


public class PeripheralControlActivity extends Activity {
    private TextView hiValue;
    private TextView loValue;
    private SeekBar seekBarHi;
    private SeekBar seekBarLo;
    private Button updateButton;
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_ID = "id";
    private BleAdapterService bluetooth_le_adapter;

    private TextView titoDeviceFound;
    private String device_name;
    private String device_address;
    private Timer mTimer;
    private boolean sound_alarm_on_disconnect = false;
    private int alert_level;
    private boolean back_requested = false;
    private boolean share_with_server = false;
    private Switch share_switch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_peripheral_control);
        titoDeviceFound = ((TextView) this.findViewById(R.id.titoDeviceFound));
        hiValue = this.findViewById(R.id.hiValue);
        loValue = this.findViewById(R.id.loValue);
        updateButton = this.findViewById(R.id.updateButton);
        BluetoothDevice bd;
        // read intent data
        final Intent intent = getIntent();
        device_name = intent.getStringExtra(EXTRA_NAME);
        device_address = intent.getStringExtra(EXTRA_ID);
        // show the device name
        ((TextView) this.findViewById(R.id.nameTextView)).setText("Device : "+device_name+" ["+device_address+"]");

        seekBarHi = findViewById(R.id.seekBarHi);
        seekBarLo = findViewById(R.id.seekBarLo);

        seekBarHi.setOnSeekBarChangeListener(seekBarChangeListenerHi);
        seekBarLo.setOnSeekBarChangeListener(seekBarChangeListenerLo);

        seekBarHi.setEnabled(false);
        seekBarLo.setEnabled(false);



        int progress = seekBarHi.getProgress();
        hiValue.setText(progress+" hz");
        progress = seekBarLo.getProgress();
        loValue.setText(progress+" hz");

        // disable the noise button
        // disable the LOW/MID/HIGH alert level selection buttons
//        share_switch.setEnabled(false);
//        share_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            public void onCheckedChanged(CompoundButton buttonView,
//                                         boolean isChecked) {
//// we'll complete this later
//            }
//        });
// connect to the Bluetooth adapter service
        Intent gattServiceIntent = new Intent(this, BleAdapterService.class);
        bindService(gattServiceIntent, service_connection, BIND_AUTO_CREATE);
        showMsg("READY");
    }
    private void showMsg(final String msg) {
        Log.d(Constants.TAG, msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            }
        });
    }
    private final ServiceConnection service_connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bluetooth_le_adapter = ((BleAdapterService.LocalBinder) service).getService();
            bluetooth_le_adapter.setActivityHandler(message_handler);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetooth_le_adapter = null;
        }
    };


    @SuppressLint("HandlerLeak")
    private Handler message_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle;
            String service_uuid = "";
            String characteristic_uuid = "";
            byte[] b = null;
// message handling logic
            switch (msg.what) {
                case BleAdapterService.MESSAGE:
                    bundle = msg.getData();
                    String text = bundle.getString(BleAdapterService.PARCEL_TEXT);
                    showMsg(text);
                    break;
                case BleAdapterService.GATT_CONNECTED:
                    ((Button) PeripheralControlActivity.this
                            .findViewById(R.id.connectButton)).setEnabled(false);
// we're connected
                    showMsg("CONNECTED");
                    break;
                case BleAdapterService.GATT_DISCONNECT:
                    ((Button) PeripheralControlActivity.this
                            .findViewById(R.id.connectButton)).setEnabled(true);
// we're disconnected
                    showMsg("DISCONNECTED");
                    if (back_requested) {
                        PeripheralControlActivity.this.finish();
                    }
                    break;
            }
        }
    };

    public void onLow(View view) {
    }
    public void onMid(View view) {
    }
    public void onHigh(View view) {
    }
    public void onNoise(View view) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(service_connection);
        bluetooth_le_adapter = null;
    }

    public void onConnect(View view) {
        showMsg("onConnect");
        AsyncTask<Void,Void,Void> asyncTak = new AsyncTask<Void, Void, Void>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    Thread.sleep(1340);


                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Handler handle = new Handler();
                        handle.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(bluetooth_le_adapter, "connected to device", Toast.LENGTH_SHORT).show();
                                titoDeviceFound.setText("Connected to TITO Device");
                                titoDeviceFound.setTextColor(getResources().getColor(R.color.titoBlue));
                                seekBarHi.setEnabled(true);
                                seekBarLo.setEnabled(true);
                            }
                        },1304);
                    }
                });

                return null;
            }
        };
        asyncTak.execute();

        if (bluetooth_le_adapter != null) {
            if (bluetooth_le_adapter.connect(device_address)) {
                ((Button) PeripheralControlActivity.this
                        .findViewById(R.id.connectButton)).setEnabled(false);
            } else {
                showMsg("onConnect: failed to connect");
            }
        } else {
            showMsg("onConnect: bluetooth_le_adapter=null");
        }
    }

    public void onBackPressed() {
        Log.d(Constants.TAG, "onBackPressed");
        back_requested = true;
        if (bluetooth_le_adapter.isConnected()) {
            try {
                bluetooth_le_adapter.disconnect();
            } catch (Exception e) {
            }
        } else {
            finish();
        }
    }

    SeekBar.OnSeekBarChangeListener seekBarChangeListenerHi = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // updated continuously as the user slides the thumb
            hiValue.setText(progress+" Hz");
            updateButton.setEnabled(true);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // called when the user first touches the SeekBar
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // called after the user finishes moving the SeekBar
        }
    };

    SeekBar.OnSeekBarChangeListener seekBarChangeListenerLo = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // updated continuously as the user slides the thumb
            loValue.setText(progress + " Hz");
            updateButton.setEnabled(true);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // called when the user first touches the SeekBar
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // called after the user finishes moving the SeekBar
        }
    };

    public void onUpdate(View view) {

        int hi = Integer.parseInt(hiValue.getText().toString().replaceAll("[\\D]", "").trim());
        int lo = Integer.parseInt(loValue.getText().toString().replaceAll("[\\D]", "").trim());

        if (hi < lo) {
            Toast.makeText(bluetooth_le_adapter, "Hi must be greater than Low", Toast.LENGTH_SHORT).show();
        } else {
            JSONObject json = writeJSON(hi,lo);
            Log.d("TITO_BLUETOOTH", json.toString());
            updateButton.setEnabled(false);
        }
    }

    public JSONObject writeJSON(int hi, int lo) {
        JSONObject object = new JSONObject();
        try {
            object.put("hi", new Integer(hi));
            object.put("lo", new Integer(lo));
            object.put("reset", new Boolean(true));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(object);

        return object;
    }
}