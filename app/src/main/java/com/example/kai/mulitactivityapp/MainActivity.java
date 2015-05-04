package com.example.kai.mulitactivityapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import android.os.Handler;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    BluetoothAdapter adapter;
    BluetoothDevice MiDevice;
    BluetoothSocket socket;
    InputStream in;
    OutputStream out;
    TextView data;
    android.widget.Button send;
    android.widget.Button connect;
    PathDrawView pathView;
    Thread BlueToothThread;
    boolean stop = false;
    int position;
    byte read[];
    private List<String> stringList;
    //Netstrings nt = new Netstrings();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pathView = (PathDrawView)findViewById(R.id.canvas);
        pathView.assignTextView((TextView)findViewById(R.id.text));




        send = (android.widget.Button)findViewById(R.id.send);
        send.setOnClickListener(this);

        connect = (android.widget.Button)findViewById(R.id.connect);
        connect.setOnClickListener(this);




        data = (TextView)findViewById(R.id.data);

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
                    data.setText("device paired");
                    break;
                }
            }
        }

        //opens connection
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
//        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID

        socket = MiDevice.createRfcommSocketToServiceRecord(uuid);

        socket.connect();
        out = socket.getOutputStream();
        in = socket.getInputStream();

        data.setText("connection established");


        //gets data
        final Handler handler = new Handler();
        final byte delimiter = 10;

        stop = false;
        position = 0;
        read = new byte[1024];
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
                                            data.setText(result);
                                            //                                          data.setText(result);
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


        if(v.getId() == R.id.connect) {
            try {
                runBT();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if(v.getId() == R.id.send) {

            try {
                stringList = pathView.stringList;
                for(String s:stringList){
                    out.write(s.getBytes());
                }
                data.setText("message sent");

            } catch (IOException e) {
                e.printStackTrace();
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class Button {
    }
}



