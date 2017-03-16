package com.example.kit.personalalarm;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static android.R.attr.button;
import static android.R.attr.color;
import static android.R.attr.configChanges;
import static android.R.attr.data;
import static android.R.attr.icon;
import static android.R.attr.inputType;
import static java.sql.Types.NULL;

public class MainActivity extends AppCompatActivity  { //Author: YAN Tsz Kit (Student ID:54106008)
    /*protected LocationManager locationManager;
    protected LocationListener locationListener;*/
    private GPS gps;
    private double lat;
    private double lon;
    public boolean EditFlag = false;
    public String Em_text = "Help, I'm at ";
    //

    String notification_text = "Not Connected. Click this to Reconnect.";

    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothDevice bluetoothDevice = null;
    private BluetoothSocket bluetoothSocket = null;
    private Set<BluetoothDevice> pairedDevices;

    private StringBuilder recDataString = new StringBuilder();

    ImageView icon_imageview;
    EditText call_edittext,callno_edittext,smsno_textview,sms_edittext;
    Button call_button, sms_button,gps_button, stop_button,info_imageview;
    Switch led_switch,sound_switch,call_switch,sms_switch;
    TextView info_textview;

    private ProgressDialog connecting;
    private AlertDialog.Builder builderSingle;

    private ConnectedThread mConnectedThread;
    private DataCommThread mDataCommThread;

    private Handler bluetoothIn;
    final int handlerState = 0;

    private NotificationManager notificationManager;
    private Notification notification;
    private Notification.Builder builder;

    // SPP UUID service - this should work for most devices

    public static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Arthor: YAN Tsz Kit Student ID:54106008
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionCheck();


        connecting = new ProgressDialog(this);
        builderSingle = new AlertDialog.Builder(this);

        info_imageview = (Button) findViewById(R.id.info_imageview);
        //info_imageview.setImageResource(R.drawable.errorinfo);
        info_imageview.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(info_textview.getVisibility() == View.VISIBLE)
                    info_textview.setVisibility(View.INVISIBLE);
                else
                    info_textview.setVisibility(View.VISIBLE);
            }
        });
        sms_switch = (Switch) findViewById(R.id.sms_switch);
        sms_switch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if(!sms_switch.isChecked())
                {
                    sms_edittext.setVisibility(View.INVISIBLE);
                    smsno_textview.setVisibility(View.INVISIBLE);
                    sms_button.setVisibility(View.INVISIBLE);
                }else
                {
                    sms_edittext.setVisibility(View.VISIBLE);
                    smsno_textview.setVisibility(View.VISIBLE);
                    sms_button.setVisibility(View.VISIBLE);
                }
                writeSetting();
            }
        });

        stop_button = (Button) findViewById(R.id.STOP);
        stop_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                try {
                    mDataCommThread.write("4");
                }catch(Exception e)
                {};
            }
        });

        /*
        gps_button = (Button) findViewById(R.id.gps_button);
        gps_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                gps = new GPS(MainActivity.this);

                if(gps.isgetLocation()){

                    lon = gps.getLongitude();
                    lat = gps.getLatitude();

                    Toast.makeText(getBaseContext(),"longitude:"+lon+" latitude:"+lat,Toast.LENGTH_SHORT).show();

                    sendSMS(sms_edittext.getText().toString(),"HELP! I'm at "+"https://www.google.com.hk/maps/@"+Double.toString(lat)+","+Double.toString(lon)+",21z?hl=zh-TW&authuser=0+");

                }
                else
                {
                    gps.showSettingsAlert();
                }
            }
                                      }
                );
            */
        info_textview = (TextView) findViewById(R.id.info_textview);
        info_textview.setVisibility(View.VISIBLE);
        info_textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (EditFlag) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle("Edit the emergency message");
                    final EditText editText = new EditText(MainActivity.this);
                    editText.setText(Em_text);
                    dialog.setView(editText);

                    dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        // do something when the button is clicked
                        public void onClick(DialogInterface arg0, int arg1) {
                            Em_text = editText.getText().toString();
                            info_textview.setText(Html.fromHtml("<u><FONT COLOR=\"#355EE6\" >" + "Edit Message: "+"</Font></u>"+"<FONT COLOR=\"#000000\" >"+Em_text+" [Your Location]"));
                        }
                    });
                    dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        // do something when the button is clicked
                        public void onClick(DialogInterface arg0, int arg1) {
                            //...
                        }
                    });
                    dialog.show();
                }
            }
        });
        sms_switch.setChecked(true);
        led_switch = (Switch) findViewById(R.id.led_switch);
        led_switch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                writeSetting();
            }
        });
        sound_switch = (Switch) findViewById(R.id.sound_switch);
        sound_switch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                writeSetting();
            }
        });
        call_switch = (Switch) findViewById(R.id.call_switch);
        callno_edittext = (EditText) findViewById(R.id.callno_edittext);
        callno_edittext.setEnabled(false);
        smsno_textview = (EditText) findViewById(R.id.smsno_textview);
        smsno_textview.setEnabled(false);
        icon_imageview = (ImageView) findViewById(R.id.icon_imageview);
        icon_imageview.setImageResource(R.drawable.disconnected);
        icon_imageview.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                if(!call_switch.isEnabled())
                    builderSingle.show();
                else
                    Toast.makeText(getBaseContext(), "Already Connected.", Toast.LENGTH_SHORT).show();
                return true;
            };
        });
        call_switch.setChecked(true);
        sms_edittext = (EditText) findViewById(R.id.sms_edittext);
        sms_edittext.setEnabled(false);
        call_edittext = (EditText) findViewById(R.id.call_edittext);
        call_edittext.setEnabled(false);
        call_switch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if(!call_switch.isChecked())
                {
                    call_edittext.setVisibility(View.INVISIBLE);
                    callno_edittext.setVisibility(View.INVISIBLE);
                    call_button.setVisibility(View.INVISIBLE);
                }else
                {
                    call_edittext.setVisibility(View.VISIBLE);
                    callno_edittext.setVisibility(View.VISIBLE);
                    call_button.setVisibility(View.VISIBLE);
                }
                writeSetting();
            }
        });

        call_button = (Button) findViewById(R.id.call_button);
        call_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if(!call_edittext.isEnabled())
                {
                    call_button.setText("DONE");
                    call_edittext.setEnabled(true);
                    call_edittext.requestFocus();
                    call_edittext.selectAll();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(call_edittext, InputMethodManager.SHOW_IMPLICIT);

                }else
                {
                    call_edittext.setSelectAllOnFocus(false);
                    call_button.setText("EDIT");
                    call_edittext.setEnabled(false);
                    if(call_edittext.getText().toString().isEmpty())
                        call_edittext.setText("999");
                    try {
                        writeSetting();
                        Toast.makeText(getBaseContext(),"Emergency Call Number Edited.",Toast.LENGTH_SHORT).show();
                    }catch(Exception e){};
                }
            }
        });
        sms_button = (Button) findViewById(R.id.sms_button);
        sms_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if(!sms_edittext.isEnabled())
                {
                    sms_button.setText("DONE");
                    sms_edittext.setEnabled(true);
                    sms_edittext.requestFocus();
                    sms_edittext.selectAll();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(sms_edittext, InputMethodManager.SHOW_IMPLICIT);

                }else
                {
                    sms_edittext.setSelectAllOnFocus(false);
                    sms_button.setText("EDIT");
                    sms_edittext.setEnabled(false);
                    if(sms_edittext.getText().toString().isEmpty())
                        sms_edittext.setText("992");
                    try {
                        writeSetting();
                        Toast.makeText(getBaseContext(),"Emergency SMS Number Edited.",Toast.LENGTH_SHORT).show();
                    }catch(Exception e){};
                }
            }
        });

        readSetting();
        led_switch.setEnabled(false);
        sound_switch.setEnabled(false);
        call_switch.setEnabled(false);
        sms_switch.setEnabled(false);
        call_button.setEnabled(false);
        sms_button.setEnabled(false);



        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null)
        {
            new AlertDialog.Builder(this)
                    .setTitle("Not compatible")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnOn, 0);
            }
            pairedDevices = bluetoothAdapter.getBondedDevices();
            final ArrayList<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1);


            builderSingle.setNegativeButton(
                    "Refresh",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            arrayAdapter.clear();
                            pairedDevices = bluetoothAdapter.getBondedDevices();
                            deviceList.clear();
                            for (BluetoothDevice bt : pairedDevices) {
                                arrayAdapter.add(bt.getName());
                                deviceList.add(bt);
                            }
                            arrayAdapter.notifyDataSetChanged();
                            builderSingle.show();
                        }
                    });
            builderSingle.setTitle("Select HC-06");


            for (BluetoothDevice bt : pairedDevices) {
                arrayAdapter.add(bt.getName());
                deviceList.add(bt);
            }


            connecting.setMessage("Connecting...");
            connecting.setCancelable(true);

            if (bluetoothSocket == null || !bluetoothSocket.isConnected()) {
                builderSingle.setAdapter(
                        arrayAdapter,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {


                                bluetoothDevice = deviceList.get(which);
                                try {

                                    connecting.show();
                                    bluetoothSocket = createBluetoothSocket(bluetoothDevice);

                                } catch (IOException e) {

                                    Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
                                    connecting.cancel();
                                }
                                mConnectedThread = new ConnectedThread(bluetoothDevice);
                                mConnectedThread.start();
                            }
                        });
                builderSingle.show();
            }

        }

        final int notifyID = 1; // 通知的識別號碼
        final int requestCode = notifyID; // PendingIntent的Request Code
        final Intent intent = new Intent(getApplicationContext(), MainActivity.class); // 開啟另一個Activity的Intent
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setAction(Intent.ACTION_MAIN);
        final int flags = PendingIntent.FLAG_UPDATE_CURRENT; // ONE_SHOT：PendingIntent只使用一次；CANCEL_CURRENT：PendingIntent執行前會先結束掉之前的；NO_CREATE：沿用先前的PendingIntent，不建立新的PendingIntent；UPDATE_CURRENT：更新先前PendingIntent所帶的額外資料，並繼續沿用
        final TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext()); // 建立TaskStackBuilder
        stackBuilder.addParentStack(MainActivity.class); // 加入目前要啟動的Activity，這個方法會將這個Activity的所有上層的Activity(Parents)都加到堆疊中
        stackBuilder.addNextIntent(intent); // 加入啟動Activity的Intent
        final PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE); // 取得系統的通知服務

        builder = new Notification.Builder(getApplicationContext()).setSmallIcon(R.drawable.disconnected).setContentTitle("MVP-A").setContentText(notification_text).setContentIntent(pendingIntent);


        notification =  builder.build();// 建立通知
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notificationManager.notify(notifyID, notification); // 發送通知



    }

    @Override
    public void onResume()
    {
        super.onResume();
    };


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
        //Author: YAN Tsz Kit (Student ID:54106008)
    }

    private void writeSetting()
    {//Author: YAN Tsz Kit (Student ID:54106008)
        try {
            int settingCode = 0;
            FileOutputStream fOut = openFileOutput("config",MODE_WORLD_READABLE);
            if(led_switch.isChecked())
            {
                fOut.write("T".getBytes());
                settingCode += 1;
            }
            else
            {
                fOut.write("F".getBytes());
            }
            if(sound_switch.isChecked())
            {
                fOut.write("T".getBytes());
                settingCode += 2;
            }
            else
            {
                fOut.write("F".getBytes());
            }
            if(call_switch.isChecked())
                fOut.write("T".getBytes());
            else
                fOut.write("F".getBytes());
            if(sms_switch.isChecked())
                fOut.write("T".getBytes());
            else
                fOut.write("F".getBytes());
            fOut.write(call_edittext.getText().toString().getBytes());
            fOut.write("C".getBytes());
            fOut.write(sms_edittext.getText().toString().getBytes());
            fOut.write("M".getBytes());
            fOut.close();
            mDataCommThread.write(String.valueOf(settingCode));
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private void readSetting()
    {//Author: YAN Tsz Kit (Student ID:54106008)
        try{
            FileInputStream fin = openFileInput("config");
            int c, inputcount = 0;
            String temp="";

            while( (c = fin.read()) != -1) {
                if (inputcount < 4)
                {
                    switch (inputcount)
                    {
                        case 0:
                            if ((char) c == 'T')
                                led_switch.setChecked(true);
                            else
                                led_switch.setChecked(false);
                            break;
                        case 1:
                            if ((char) c == 'T')
                                sound_switch.setChecked(true);
                            else
                                sound_switch.setChecked(false);
                            break;
                        case 2:
                            if ((char) c == 'T')
                                call_switch.setChecked(true);
                            else
                            {
                                call_switch.setChecked(false);
                                call_edittext.setVisibility(View.INVISIBLE);
                                callno_edittext.setVisibility(View.INVISIBLE);
                                call_button.setVisibility(View.INVISIBLE);
                            }
                            break;
                        case 3:
                            if ((char) c == 'T')
                                sms_switch.setChecked(true);
                            else
                            {
                                sms_switch.setChecked(false);
                                sms_edittext.setVisibility(View.INVISIBLE);
                                smsno_textview.setVisibility(View.INVISIBLE);
                                sms_button.setVisibility(View.INVISIBLE);
                            }
                            break;
                    }
                }
                else
                {
                    if((char)c == 'C')
                    {
                        call_edittext.setText(temp);
                        temp = "";
                    }else if((char)c == 'M')
                    {
                        sms_edittext.setText(temp);
                        temp = "";
                    }
                    else
                        temp = temp + Character.toString((char)c);
                }

                inputcount++;
            }
        }
        catch(Exception e){
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private void call(String phoneNumber)
    {//Author: YAN Tsz Kit (Student ID:54106008)
        {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:"+phoneNumber));
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED)
            {
                try {
                if (callIntent.resolveActivity(getPackageManager()) != null)
                    startActivity(callIntent);

                } catch (Exception ex) {
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                ex.printStackTrace();
                }
            }

        }
    }

    public void sendSMS(String phoneNumber, String msg)
    {
        //Author: YAN Tsz Kit (Student ID:54106008)
        //The number on which you want to send SMS
        try {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, msg, null, null);
            Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT).show();
        }catch(Exception ex)
        {
            Toast.makeText(getApplicationContext(), "Message Cannot Send", Toast.LENGTH_SHORT).show();
        }
    }

    private void startDataCommThread(BluetoothSocket socket)
    {
        mDataCommThread = new DataCommThread(socket);
        mDataCommThread.start();
    }

    private class ConnectedThread extends Thread { //Thread for connecting the device
        //Author: YAN Tsz Kit (Student ID:54106008)
        private BluetoothSocket bluetoothSocket = null;
        private final BluetoothDevice bluetoothDevice;

        public ConnectedThread(BluetoothDevice device)
        {
            bluetoothDevice = device;

            try
            {
                bluetoothSocket = createBluetoothSocket(device);
            }catch(IOException e){};


        }

        public void run()
        {
            boolean success = false;

            try{
                bluetoothSocket.connect();
                success = true;
            }catch(IOException e){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connecting.cancel();
                        Toast.makeText(getBaseContext(),"Connection Failure.",Toast.LENGTH_SHORT).show();
                        EditFlag = false;
                    }
                });

                try{
                    bluetoothSocket.close();
                }catch(IOException ioe){};
            }
            if(success)
            {
                startDataCommThread(bluetoothSocket);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        writeSetting();
                        EditFlag = true;
                        icon_imageview.setImageResource(R.drawable.connected);
                        led_switch.setEnabled(true);
                        sound_switch.setEnabled(true);
                        call_switch.setEnabled(true);
                        sms_switch.setEnabled(true);
                        call_button.setEnabled(true);
                        sms_button.setEnabled(true);
                        info_textview.setText(Html.fromHtml("<u><FONT COLOR=\"#355EE6\" >" + "Edit Message: "+"</Font></u>"+"<FONT COLOR=\"#000000\" >"+Em_text+" [Your Location]."));
                        info_imageview.setBackgroundResource(R.drawable.normalinfo);
                        notification_text = "Defending Harm from you.";
                        builder.setContentText(notification_text);
                        builder.setSmallIcon(R.drawable.connected);
                        notificationManager.notify(1, builder.build());
                        //info_imageview.setImageResource(R.drawable.normalinfo);
                        info_textview.setBackgroundColor(getBaseContext().getResources().getColor(R.color.grey));
                        Toast.makeText(getBaseContext(), "Device Connected", Toast.LENGTH_SHORT).show();
                        connecting.cancel();
                    }
                });

            }
        }


        public void cancel()
        {
            try{
                bluetoothSocket.close();
            }catch(IOException e){};
        }
    }

    private class DataCommThread extends Thread{ //Thread for data communication
        //Author: YAN Tsz Kit (Student ID:54106008)
        private final BluetoothSocket connectedBluetoothSocket;
        private final InputStream connectedInputStream;
        private final OutputStream connectedOutputStream;

        public DataCommThread(BluetoothSocket socket)
        {
            connectedBluetoothSocket = socket;
            InputStream in = null;
            OutputStream out = null;

            try{
                in = socket.getInputStream();
                out = socket.getOutputStream();
            }catch(IOException e){};

            connectedInputStream = in;
            connectedOutputStream = out;
        }

        @Override
        public void run()
        {
            byte[] buffer = new byte[1024];
            int bytes;
            String strRx = "";
            while(true)
            {


                try{
                    bytes = connectedInputStream.read(buffer);
                    final String strReceived = new String(buffer, 0, bytes);
                    final String strByte = String.valueOf(bytes) + " bytes received.";

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(strReceived.contains("H"))
                            {
                            //GPS
                            gps = new GPS(MainActivity.this);

                            if(gps.isgetLocation()){

                                lon = gps.getLongitude();
                                lat = gps.getLatitude();
                            }
                            else
                            {
                                gps.showSettingsAlert();
                            }
                            //GPS

                                if(sms_switch.isChecked())
                                    sendSMS(sms_edittext.getText().toString(),Em_text+
                                            "https://www.google.com.hk/maps?q="+Double.toString(lat)+
                                            ","+Double.toString(lon)+"&z=21");//?hl=zh-TW&authuser=0+
                                if(call_switch.isChecked())
                                    call(call_edittext.getText().toString());
                            }
                        }
                    });
                }catch(IOException e){};

            }
        }

        public void write(String data)
        {
            try{
                connectedOutputStream.write(data.getBytes());
            }catch(IOException e){};
        }

        public void cancel(){
            try{
                connectedBluetoothSocket.close();
            }catch(IOException e){};
        }
    }
    private void permissionCheck()
    {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
        }
        permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }
        permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 3);
        }
        permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 4);
        }
        permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS} , 5);
        }
        permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN} , 6);
        }

    }


/*
    private void getLocation()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                &&ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            System.out.print(location.getLatitude() + "   " + location.getLongitude());
        }catch(Exception e)
        {
            //e.printStackTrace();
        };
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

*/

}
