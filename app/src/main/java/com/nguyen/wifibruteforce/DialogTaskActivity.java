package com.nguyen.wifibruteforce;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DialogTaskActivity extends Activity  {

    @BindView(R.id.btnCancel)
    Button btnStop;
    DictionaryTask dictionaryTask;
    BruteForceTask bruteForceTask;
    int flag;

    WifiScanReceiver2 wifiScanReceiver;
    ConnectivityReceiver connectivityReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog);
        ButterKnife.bind(this);
        this.setFinishOnTouchOutside(false);

        Intent intent = getIntent();
        String ssid = intent.getStringExtra("SSID");

        connectivityReceiver = new ConnectivityReceiver();

        if (getIntent().hasExtra("PATH")) {             //dictionary method
            Toast.makeText(getApplicationContext(), "dic", Toast.LENGTH_LONG).show();
            Log.d("running", intent.getStringExtra("PATH"));

            ArrayList<String> listPass ;
            String path = intent.getStringExtra("PATH");
            if (path.equals("default_path")) {
                Log.d("running", "file path default");
                listPass = readDefault();
            }else {
                listPass = read(path);
            }
            flag = 0;
            doStart(ssid, listPass, flag);

        } else {                //brute force method
            flag = 1;
            doStart(ssid, null, flag);
            Toast.makeText(getApplicationContext(), "bf", Toast.LENGTH_LONG).show();
        }
    }

    private ArrayList readDefault() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("pass_dictionary_test.txt")))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ArrayList arrPass = new ArrayList(Arrays.asList(br.lines().toArray()));
                br.close();
                return arrPass;
            } else {
                String line;
                ArrayList arrPass = new ArrayList();
                while ((line = br.readLine()) != null) {
                    arrPass.add(line);
                }
                br.close();
                return arrPass;
            }
        } catch (FileNotFoundException e) {
            Log.e("readFile", "not found");
            Toast.makeText(getApplicationContext(), getString(R.string.file_not_found), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("readFile", "IOEx");
            Toast.makeText(getApplicationContext(), getString(R.string.IOException), Toast.LENGTH_LONG).show();
        }
        return null;
    }

    //2 types of reading file to arraylist (FileReader)
    private ArrayList read(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ArrayList arrPass = new ArrayList(Arrays.asList(br.lines().toArray()));
                br.close();
                return arrPass;
            } else {
                String line;
                ArrayList arrPass = new ArrayList();
                while ((line = br.readLine()) != null) {
                    arrPass.add(line);
                }
                br.close();
                return arrPass;
            }
        } catch (FileNotFoundException e) {
            Log.e("readFile", "not found");
            Toast.makeText(getApplicationContext(), getString(R.string.file_not_found), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("readFile", "IOEx");
            Toast.makeText(getApplicationContext(), getString(R.string.IOException), Toast.LENGTH_LONG).show();
        }
        return null;
    }

    private void doStart(String ssid, ArrayList<String> listPass, int flag) {
        /*
        flag == 0: dictionary
        flag == 1: brute force
        */
        if (flag == 0) {
            dictionaryTask = new DictionaryTask(DialogTaskActivity.this);
            dictionaryTask.setArrPass(listPass);
            dictionaryTask.execute(ssid);
        } else if (flag == 1) {
            Log.d("running", "method bf " + ssid);
            bruteForceTask = new BruteForceTask(DialogTaskActivity.this);
            bruteForceTask.execute(ssid);

        } else {
            Log.e("running", "some thing wrong");

        }
    }

    private void doStop(int flag) {
        if (flag == 0) {
//            if (dictionaryTask != null && dictionaryTask.getStatus() != AsyncTask.Status.FINISHED)
            dictionaryTask.cancel(true);
            if (dictionaryTask.isCancelled()) {
//            Log.d("running", "task cancel");
                finish();
            }
        } else {
            Log.d("running", "doStop flag !=0");
            bruteForceTask.cancel(true);
            if (bruteForceTask.isCancelled()) {
//            Log.d("running", "task cancel");
                finish();
            }
            finish();
        }
    }


    @OnClick(R.id.btnCancel)
    public void onViewClicked() {
        doStop(flag);
    }

//    @Override
//    public void onNetworkConnectionChanged(boolean isConnected) {
//        Log.d("running","supplicant task: "+)
//
//    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public class WifiScanReceiver2 extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
//            List<ScanResult> results = wifi.getScanResults();
//            updateCurrentWifi(results);
//            scanWifi(results);
            Log.d("running", "bcr: " + intent.toString());
            if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo networkInfo =
                        intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if(networkInfo.isConnected()) {
                    // Wifi is connected
                    Log.d("running Inetify", "Wifi is connected: " + String.valueOf(networkInfo));
                }
            } else if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                NetworkInfo networkInfo =
                        intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if(networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
                    // Wifi is disconnected
                    Log.d("running Inetify", "Wifi is disconnected: " + String.valueOf(networkInfo));
                }
            }
        }
    }

//    @Override
//    protected void onPause() {
//        unregisterReceiver(connectivityReceiver);
//        super.onPause();
//    }
//
//    @Override
//    protected void onResume() {
//        // register connection status listener
//        super.onResume();
//        registerReceiver(connectivityReceiver, new IntentFilter(WifiManager.EXTRA_WIFI_STATE));
//
//    }
}
