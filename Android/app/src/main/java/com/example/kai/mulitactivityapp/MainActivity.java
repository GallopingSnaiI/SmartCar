package com.example.kai.mulitactivityapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import android.os.Handler;
import android.widget.Toast;

import static com.example.kai.mulitactivityapp.ViewUtils.replaceView;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    public static int pathViewHeight;
    ListPreference listPref;
    BluetoothAdapter adapter;
    BluetoothDevice MiDevice;
    BluetoothSocket socket;
    InputStream in;
    public static OutputStream out;
    android.widget.Button send;
    android.widget.Button connect;
    android.widget.Button swapButton;
    static PathDrawView pathView;
    Thread BlueToothThread;
    boolean stop = false;
    int position;
    byte read[];
    private List<String> stringList;
    ManualControlView manualView;
    //Netstrings nt = new Netstrings();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pathView = (PathDrawView)findViewById(R.id.canvas);

        //pathView.assignTextView((TextView)findViewById(R.id.text));

        SharedPreferences shPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        pathView.addSharedPreferences(shPref);
        PreferenceManager.setDefaultValues(this, R.xml.prefrences, true);
        String option = shPref.getString("PREF_LIST", "Medium");
        if(option == "Low"){

        }


        send = (android.widget.Button)findViewById(R.id.send);
        send.setOnClickListener(this);

        connect = (android.widget.Button)findViewById(R.id.connect);
        connect.setOnClickListener(this);

        swapButton = (android.widget.Button) findViewById(R.id.swapButton);
        pathView.setswapButton(swapButton);
        manualView = (ManualControlView) findViewById(R.id.manualView);


        //data = (TextView)findViewById(R.id.data);

    }

    public void runBT() throws IOException {

        //pairs bluetooth
        adapter = BluetoothAdapter.getDefaultAdapter();

        if(!adapter.isEnabled()) {
            Intent enable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enable, 0);
        }

        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        if(pairedDevices.size() > 0) {
            for(BluetoothDevice device : pairedDevices) {
                if(device.getName().equals("HC-06")) {
                    MiDevice = device;
                    Toast.makeText(getApplicationContext(), "Device Paired",
                            Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }


        //opens connection
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
       // UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID

        if(MiDevice.createRfcommSocketToServiceRecord(uuid) == null){
            Toast.makeText(getApplicationContext(), "Device is not paired to the car.",
                    Toast.LENGTH_SHORT).show();
        }else{
            socket = MiDevice.createRfcommSocketToServiceRecord(uuid);
        }


        socket.connect();
        out = socket.getOutputStream();
        in = socket.getInputStream();
        Toast.makeText(getApplicationContext(), "Connection Established",
                Toast.LENGTH_SHORT).show();


        //gets data
        final Handler handler = new Handler();
        final byte delimiter = 10;

        stop = false;
        position = 0;
        read = new byte[1024*4];
        BlueToothThread = new Thread(new Runnable() {

            public void run() {

                while(!Thread.currentThread().isInterrupted() && !stop) {

                    try {

                        int bytesAvailable = in.available();
                        if(bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            in.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++) {
                                byte b = packetBytes[i];
                                if(b == delimiter) {
                                    byte[] encodedBytes = new byte[position];
                                    System.arraycopy(read, 0, encodedBytes, 0, encodedBytes.length);
                                    final String result = new String(encodedBytes, "US-ASCII");
                                    position = 0;

                                    handler.post(new Runnable() {
                                        public void run() {
                                            //                                          data.setText(result);
                                            if(result.contains("Obstacle ")){
                                                System.out.println("OBSTACLE DETETECTEDSEDED!");
                                                try {
                                                    int i = Integer.parseInt(result.split("Obstacle ")[1].replaceAll("\\r|\\n", ""));
                                                    pathView.onObstacleDetected(i);
                                                }catch(Exception e){
                                                    pathView.onObstacleDetected(0);
                                                    Toast.makeText(getApplicationContext(), "Strange obstacle index",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }else{
                                                System.out.println("ELSE: "+result);
                                            }
                                        }
                                    });

                                } else {
                                    read[position++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex) {
                        stop = true;
                    }
                }
            }
        });

        BlueToothThread.start();

    }

    public void onClick(View v) {
        if (v.getId() == R.id.swapButton) {
            if (out != null) {
                pathView.resetObstacleDetected();
                /*
                This switches between the pathView and the manual drive view.
                 */
                if (pathView.getVisibility() == View.VISIBLE) {
                    replaceView(pathView, manualView);
                    swapButton.setText("Path \n Mode");

                    //Send a single stop command as it switches to manual to make sure it is in manual mode.
                    if (out != null) {
                        try {
                            out.write(("$stop").getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "No Connection Found",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //Before the pathView is made visible again, send a "end" command which should take it back into auto mode.
                    if (out != null) {
                        try {
                            out.write(("@").getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        replaceView(manualView, pathView);
                        swapButton.setText("Manual \n Mode");
                    } else {
                        Toast.makeText(getApplicationContext(), "No Connection Found",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }else{
                Toast.makeText(getApplicationContext(), "No Connection Found",
                    Toast.LENGTH_SHORT).show();
            }
        }

        if(v.getId() == R.id.connect) {
            try {
                runBT();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if(v.getId() == R.id.send) {
            if(out!= null) {
                try {
                    if (pathView.stringList == null) {
                        Toast.makeText(getApplicationContext(), "No Path Drawn",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        if (pathView.stringList.size() > 0) {
                            stringList = pathView.stringList;
                            for (String s : stringList) {
                                out.write(s.getBytes());
                            }
                            Toast.makeText(getApplicationContext(), "Message Sent",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Please draw a line.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                        pathView.resetObstacleDetected();

                    }catch(IOException e){
                        e.printStackTrace();
                    }



                }else{
                    Toast.makeText(getApplicationContext(), "No Connection Found",
                            Toast.LENGTH_SHORT).show();
                }
            }


        }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()){
            case R.id.action_settings:
                Intent settingsIntent = new Intent(MainActivity.this,
                        SettingsActivity.class);

                settingsIntent.putExtra("width",pathView.getWidth());

                startActivity(settingsIntent);
                break;

        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        pathView.validateLine();
        pathView.invalidate();
        super.onRestart();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        pathViewHeight = pathView.getHeight();
    }
}



