package co.izuchukwu.usbkit;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.mangusja.libaums.javafs.JavaFsFileSystemCreator;
import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.FileSystemFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import de.waldheinz.fs.util.FileDisk;

public class Home extends AppCompatActivity {

    private static Context context;

    public static Context getContext() {
        return Home.context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Home.context = getApplicationContext();

        FloatingActionButton clearFab = findViewById(R.id.clearFab);
        clearFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clear();
            }
        });

        // test1
        Button testButton = findViewById(R.id.test1Button);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                test1();
            }
        });

        // test 2
        testButton = findViewById(R.id.test2Button);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                test2();
            }
        });

        // register usb permission broadcast receiver
        final PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(usbPermissionReceiver, filter);

        // test 3
        testButton = findViewById(R.id.test3Button);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                test3(permissionIntent);
            }
        });

        // test 4
        testButton = findViewById(R.id.test4Button);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                test4(permissionIntent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
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

    //
    // Tests
    //

    public void test1() {
        // Test 1: find usb path via getExternalFilesDir, pass to fat32-lib
        // result: op-1 does not appear in getExternalFilesDir results
        log("Test 1", "## Running test 1");
        log("Test 1", "Listing results of getExternalFilesDirs()");

        Context context = getContext();
        File[] files = context.getExternalFilesDirs(null);
        for (File file : files) {
            log("Test 1", "External found");
            log("Test 1", "Path: " + file.getAbsolutePath());
            for (File root : file.listRoots()) {
                log("Test 1", "FS Root: " + root.getAbsolutePath());
            }
        }
        File file = new File("/storage/");
        for (File root : file.listFiles()) {
            log("Test 1", "Storage child: " + root.getAbsolutePath());
        }

        log("Test 1", "## test complete");
    }

    public void test2() {
        // Test 2: find op-1 via UsbManager
        // result: found it, nice
        // op-1 product id: 2
        // op-1 company id: 9063
        log("Test 2", "## Running test 2");
        log("Test 2", "Listing devices in UsbManager");

        Context context = getContext();
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> devicesMap = usbManager.getDeviceList();
        int deviceIterator = 0;
        for (String key : devicesMap.keySet()) {
            String deviceNum = "[" + deviceIterator + "] ";
            UsbDevice device = devicesMap.get(key);
            log("Test 2", deviceNum + "Found: " + key);
            log("Test 2", deviceNum + "Device Class: " + device.getDeviceClass());
            log("Test 2", deviceNum + "Device Name: " + device.getDeviceName());
            log("Test 2", deviceNum + "Product Name: " + device.getProductName());
            log("Test 2", deviceNum + "Product ID: " + device.getProductId());
            log("Test 2", deviceNum + "Company Name: " + device.getManufacturerName());
            log("Test 2", deviceNum + "Company ID: " + device.getVendorId());
            log("Test 2", deviceNum + "String Representation: " + device.toString());
        }

        log("Test 2", "## test complete");
    }

    public void test3(PendingIntent permissionIntent) {
        // Test 2: use path from UsbManager to init fat32-lib
        // note: though this looks for an op-1, in the future this could look for any mass usb device
        // result: path not accessible per android security model
        log("Test 3", "## Running test 3");
        log("Test 3", "Fetching path from UsbManager");

        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> devicesMap = usbManager.getDeviceList();
        UsbDevice op1 = null;
        for (String key : devicesMap.keySet()) {
            UsbDevice device = devicesMap.get(key);
            if (device.getProductId() == 2 && device.getVendorId() == 9063) {
                // op-1 found
                op1 = device;
                break;
            }
        }
        String serial , path;
        if (op1 == null) {
            log("Test 3", "OP-1 not found :(");
            test3complete();
            return;
        } else {
            serial = op1.getSerialNumber().substring(op1.getSerialNumber().length() - 6);
            path = op1.getDeviceName();
            log("Test 3", "OP-1 found");
            log("Test 3", "OP-1 Serial: " + serial);
            log("Test 3", "OP-1 Path: " + path);
        }
        log("Test 3", "Requesting USB permissions for OP-1");
        usbManager.requestPermission(op1, permissionIntent);
        log("Test 3", "Creating FileDisk from OP-1 path");
        File op1File = new File(path);
        try {
            FileDisk op1Disk = new FileDisk(op1File, false);
            log("Test 3", "FileDisk size: " + op1Disk.getSize());
        } catch (Exception e) {
            log("Test 3", "ahh :( fat32-lib exception: " + e.toString());
        }


        test3complete();
    }

    public void test3complete() {
        log("Test 3", "## test complete");
    }

    public void test4(PendingIntent permissionIntent) {
        // Test 4: use libaums with jnode fs implementation
        log("Test 4", "## Running test 4");
        log("Test 4", "Finding devices with libaums");
        FileSystemFactory.registerFileSystem(new JavaFsFileSystemCreator());
        UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(context);

        for(UsbMassStorageDevice device: devices) {
            UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
            usbManager.requestPermission(device.getUsbDevice(), permissionIntent);
            try {
                // before interacting with a device you need to call init()!
                device.init();
                // Only uses the first partition on the device
                FileSystem currentFs = device.getPartitions().get(0).getFileSystem();
                log("Test 4", "Capacity: " + currentFs.getCapacity());
                log("Test 4", "Occupied Space: " + currentFs.getOccupiedSpace());
                log("Test 4", "Free Space: " + currentFs.getFreeSpace());
                log("Test 4", "Chunk size: " + currentFs.getChunkSize());
            } catch (IOException e) {
                e.printStackTrace();
                String stackTrace = Log.getStackTraceString(e);
                log("Test 4", "aww :( hit an IOException, stack trace:\n" + stackTrace);
            }
        }
    }

    //
    // Log
    //

    public void log(String test, String message) {
        Log.v(test, message);
        TextView logTextView = (TextView) findViewById(R.id.logTextView);
        logTextView.append("\n[" + test + "] " + message);
        scrollLogToBottom();
    }

    public void scrollLogToBottom() {
        final ScrollView scrollView = findViewById(R.id.logScrollView);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    public void clear() {
        TextView logTextView = (TextView) findViewById(R.id.logTextView);
        logTextView.setText("readyyy");
    }

    //
    // USB Permissions Listener
    //

    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";
    private final BroadcastReceiver usbPermissionReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            //call method to set up device communication
                            log("USBKit", "Permission received");
                        }
                    }
                    else {
                        log("USBKit", "Permission denied");
                    }
                }
            }
        }
    };
}
