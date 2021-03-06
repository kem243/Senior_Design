package com.droiduino.simplebluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.SystemClock;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    public char selectedString = 'X';

    public double target_pitch_high_e_default = 329.63;
    public double target_pitch_b_default = 246.94;
    public double target_pitch_g_default = 196.00;
    public double target_pitch_d_default = 146.83;
    public double target_pitch_a_default = 110.00;
    public double target_pitch_low_e_default = 82.41;

    public boolean custom_tuning = false;

    public double target_pitch_high_e_custom = 0.0;
    public double target_pitch_b_custom = 0.0;
    public double target_pitch_g_custom = 0.0;
    public double target_pitch_d_custom = 0.0;
    public double target_pitch_a_custom = 0.0;
    public double target_pitch_low_e_custom = 0.0;

    // GUI Components
    private TextView mBluetoothStatus;
    private TextView mReadBuffer;
    private Button mScanBtn;
    private Button mOffBtn;
    private Button mListPairedDevicesBtn;
    private Button mDiscoverBtn;
    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;
    private ListView mDevicesListView;
    private CheckBox mLED1;
    private CheckBox mLED2;

    private Handler mHandler; // Our main handler that will receive callback notifications
    private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier


    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothStatus = (TextView)findViewById(R.id.bluetoothStatus);
        mReadBuffer = (TextView) findViewById(R.id.readBuffer);
        mScanBtn = (Button)findViewById(R.id.scan);
        mOffBtn = (Button)findViewById(R.id.off);
        mDiscoverBtn = (Button)findViewById(R.id.discover);
        mListPairedDevicesBtn = (Button)findViewById(R.id.PairedBtn);
        mLED1 = (CheckBox)findViewById(R.id.checkboxLED1);
        mLED2 = (CheckBox)findViewById(R.id.checkboxLED2);

        mBTArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio

        mDevicesListView = (ListView)findViewById(R.id.devicesListView);
        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);




        mHandler = new Handler(){
            public void handleMessage(android.os.Message msg){
                if(msg.what == MESSAGE_READ){
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    mReadBuffer.setText(readMessage);
                }

                if(msg.what == CONNECTING_STATUS){
                    if(msg.arg1 == 1)
                        mBluetoothStatus.setText("Connected to Device: " + (String)(msg.obj));
                    else
                        mBluetoothStatus.setText("Connection Failed");
                }
            }
        };

        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            mBluetoothStatus.setText("Status: Bluetooth not found");
            Toast.makeText(getApplicationContext(),"Bluetooth device not found!",Toast.LENGTH_SHORT).show();
        }
        else {

            mLED1.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    System.out.print("left was listened to");
                    if(mConnectedThread != null) {
                        //First check to make sure thread created
                        // Binary Time value
                        mConnectedThread.write("1");
//                        mConnectedThread.write("0");
//                        mConnectedThread.write("1");
//                        mConnectedThread.write("0");
//                        mConnectedThread.write("1");
//                        mConnectedThread.write("0");
//                        mConnectedThread.write("1");
//                        mConnectedThread.write("0");
                        System.out.print("End of if statement for left");
                    }
                }
            });
            mLED2.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    System.out.print("right was listened to");
                    if(mConnectedThread != null) {
                        //First check to make sure thread created
                        // Binary Time value
                        mConnectedThread.write("2");
//                        mConnectedThread.write("0");
//                        mConnectedThread.write("1");
//                        mConnectedThread.write("0");
//                        mConnectedThread.write("1");
//                        mConnectedThread.write("0");
//                        mConnectedThread.write("1");
//                        mConnectedThread.write("0");
                        System.out.print("End of if statement for right");
                    }
                }
            });


            mScanBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //     art.onRequestPermissionsResult();
                    getpitch();
                }
            });

            mOffBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    //FloatingActionButton highEStringButton = view.findViewById(R.id.high_e_string_button);
                    //highEStringButton.setBackgroundTintList(contextInstance.getResources().getColorStateList(R.color.your_xml_name));
                    selectedString = 'E';
                    System.out.println("selected E");
                }
            });

            mListPairedDevicesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    listPairedDevices(v);
                }
            });

            mDiscoverBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    discover(v);
                }
            });
        }
    }

    private void bluetoothOn(View view){
        if (!mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            mBluetoothStatus.setText("Bluetooth enabled");
            Toast.makeText(getApplicationContext(),"Bluetooth turned on",Toast.LENGTH_SHORT).show();

        }
        else{
            Toast.makeText(getApplicationContext(),"Bluetooth is already on", Toast.LENGTH_SHORT).show();
        }
    }

    // Enter here after user selects "yes" or "no" to enabling radio
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data) {
        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, Data);
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                mBluetoothStatus.setText("Enabled");
            } else
                mBluetoothStatus.setText("Disabled");
        }
    }

    private void bluetoothOff(View view){
        mBTAdapter.disable(); // turn off
        mBluetoothStatus.setText("Bluetooth disabled");
        Toast.makeText(getApplicationContext(),"Bluetooth turned Off", Toast.LENGTH_SHORT).show();
    }

    private void discover(View view){
        // Check if the device is already discovering
        if(mBTAdapter.isDiscovering()){
            mBTAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(),"Discovery stopped",Toast.LENGTH_SHORT).show();
        }
        else{
            if(mBTAdapter.isEnabled()) {
                mBTArrayAdapter.clear(); // clear items
                mBTAdapter.startDiscovery();
                Toast.makeText(getApplicationContext(), "Discovery started", Toast.LENGTH_SHORT).show();
                registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
            else{
                Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
    }

    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name to the list
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mBTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    private void listPairedDevices(View view){
        mPairedDevices = mBTAdapter.getBondedDevices();
        if(mBTAdapter.isEnabled()) {
            // put it's one to the adapter
            for (BluetoothDevice device : mPairedDevices)
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

            Toast.makeText(getApplicationContext(), "Show Paired Devices", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            if(!mBTAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                return;
            }

            mBluetoothStatus.setText("Connecting...");
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            final String address = info.substring(info.length() - 17);
            final String name = info.substring(0,info.length() - 17);

            // Spawn a new thread to avoid blocking the GUI one
            new Thread()
            {
                public void run() {
                    boolean fail = false;

                    BluetoothDevice device = mBTAdapter.getRemoteDevice(address);

                    try {
                        mBTSocket = createBluetoothSocket(device);
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        mBTSocket.connect();
                    } catch (IOException e) {
                        try {
                            fail = true;
                            mBTSocket.close();
                            mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                    .sendToTarget();
                        } catch (IOException e2) {
                            //insert code to deal with this
                            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(fail == false) {
                        mConnectedThread = new ConnectedThread(mBTSocket);
                        mConnectedThread.start();

                        mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                                .sendToTarget();
                    }
                }
            }.start();
        }
    };

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if(bytes != 0) {
                        SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        bytes = mmInStream.available(); // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read
                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget(); // Send the obtained bytes to the UI activity
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
    public int calculate(int sampleRate, short [] audioData){
        int sampleSize = 2048;
        FFT fourier = new FFT();
        Complex[] x = new Complex[sampleSize];
        int max = 600;
        int temp = audioData.length - sampleSize;
        for (int i = temp; i < audioData.length; i++){
            x[i - temp] = new Complex((double)audioData[i], 0);
        }
        Complex[] y = fourier.fft(x);
        double[] amp = new double[max];
        double total = 0;
        double ave = 0;
        for (int i = 0; i < max; i++){
            double temp2 = y[i].re();
            double nextTemp = temp2 * temp2 / sampleSize;
            total += nextTemp;
            amp[i] = nextTemp;
        }
        ave = total / max;
        double maximum = 0;
        int ind = 0;
        double mult = 2;
        for (int j = 0; j < max - 10; j++){
            if (amp[j] > 2 * ave){
                for (int i = j; i < j + 10; i++){
                    if (amp[i] > maximum){
                        maximum = amp[i];
                        ind = i;
                    }
                }
                break;
            }
            if (j == max - 11){
                mult -= .5;
                j = 0;
            }
        }
        x = null;
        y = null;
        int diffval = calculateDiff(ind * sampleRate / sampleSize);
        if(diffval < 0){
            if(mConnectedThread != null) {
                //First check to make sure thread created
                // Binary Time value
                mConnectedThread.write("1");
//                        mConnectedThread.write("0");
//                        mConnectedThread.write("1");
//                        mConnectedThread.write("0");
//                        mConnectedThread.write("1");
//                        mConnectedThread.write("0");
//                        mConnectedThread.write("1");
//                        mConnectedThread.write("0");
                System.out.print("End of if statement for left");
            }
        } else if (diffval > 0){
            if(mConnectedThread != null) {
                //First check to make sure thread created
                // Binary Time value
                mConnectedThread.write("2");
//                        mConnectedThread.write("0");
//                        mConnectedThread.write("1");
//                        mConnectedThread.write("0");
//                        mConnectedThread.write("1");
//                        mConnectedThread.write("0");
//                        mConnectedThread.write("1");
//                        mConnectedThread.write("0");
                System.out.print("End of if statement for left");
            }
        }
        return Math.round(ind * sampleRate / sampleSize);
    }

    public int calculateDiff(double currentFrequency){
        //TextView text = getView().findViewById(R.id.textview_first2);
        double targetFrequency = 0.0;
        switch (selectedString) {
            case 'E':
                if (custom_tuning){
                    targetFrequency = target_pitch_high_e_custom;
                } else{
                    targetFrequency = target_pitch_high_e_default;
                }
                break;
            case 'b':
                if (custom_tuning){
                    targetFrequency = target_pitch_b_custom;
                } else{
                    targetFrequency = target_pitch_b_default;
                }
                break;
            case 'g':
                if (custom_tuning){
                    targetFrequency = target_pitch_g_custom;
                } else{
                    targetFrequency = target_pitch_g_default;
                }
                break;
            case 'd':
                if (custom_tuning){
                    targetFrequency = target_pitch_d_custom;
                } else{
                    targetFrequency = target_pitch_d_default;
                }
                break;
            case 'a':
                if (custom_tuning){
                    targetFrequency = target_pitch_a_custom;
                } else{
                    targetFrequency = target_pitch_a_default;
                }
                break;
            case 'e':
                if (custom_tuning){
                    targetFrequency = target_pitch_low_e_custom;
                } else{
                    targetFrequency = target_pitch_low_e_default;
                }
                break;
            default:
                System.out.println("default");
                return 0;
        }
        double diff = 0.0;
        double stepAmount = 0.0;
        diff = targetFrequency - currentFrequency;  
        //text.setText("Difference = " + (int)diff);

        return (int)diff;
    }

    public void getpitch(){
        int channel_config = AudioFormat.CHANNEL_IN_MONO;
        int format = AudioFormat.ENCODING_PCM_16BIT;
        int sampleSize = 5512;
        int bufferSize = 2756;
        //TextView text = getView().findViewById(R.id.textview_first);
        AudioRecord audioInput = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleSize, channel_config, format, bufferSize);
        //TextView txtview = (TextView)findViewById(R.id.text);

        short[] audioBuffer = new short[bufferSize];
        audioInput.startRecording();
        audioInput.read(audioBuffer, 0, bufferSize);
        //recorder.startRecording();
        //recorder.read(audioBuffer, 0, bufferSize);
        //txtview.setText(""+calculate(8000,audioBuffer));
        audioInput.stop();
        calculate(sampleSize, audioBuffer);
        //text.setText("Frequency = " + calculate(sampleSize, audioBuffer));
        audioBuffer = null;
        audioInput.release();
    }
}