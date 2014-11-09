package com.fukayatsu.kina;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class ScannerService extends Service {
    private RequestQueue mQueue;
    private BluetoothAdapter mBluetoothAdapter;
    private Timer mTimer = null;
    private int mTimerCount = 0;
    private Beacon mLastBeacon = null;
    private int mLastFoundAt = 0;
    Handler mHandler = new Handler();


    public ScannerService() {
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        mQueue = VolleyHelper.getRequestQueue(this);
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothAdapter.startLeScan(mLeScanCallback);

        mTimer = new Timer(true);
        mTimer.schedule(new TimerTask(){
            @Override
            public void run(){
                mHandler.post( new Runnable(){
                    public void run(){
                        mTimerCount ++;
                        if (mLastBeacon == null) {
                            // 領域外であることを、10分おきに送る
                            if (mTimerCount % 600 == 0) {
                                try {
                                    sendStay(null);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if (mTimerCount - mLastFoundAt  > 30) {
                            // 30秒以上beaconを見失った場合は領域から出たことにする
                            mLastBeacon = null;
                            try {
                                sendStay(null);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        // 20秒間に1秒だけスキャン
                        if (mTimerCount % 20 == 0) {
                            mBluetoothAdapter.startLeScan(mLeScanCallback);
                        } else if (mTimerCount % 20 == 1) {
                            mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        }
                    }
                });
            }
        }, 1000, 1000);



        return START_STICKY;
    }

    private void sendStay(Beacon beacon) throws JSONException {
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String userName = mSharedPreferences.getString("user_name", "");
        if (userName.equals("")) {
            Log.d("sendStay", "userName is not set");
            return;
        }




        if (beacon == null) {
            mQueue.add(new JsonObjectRequest(Request.Method.DELETE, api_url_stays(userName), null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    // JSONObjectのパース、List、Viewへの追加等
                                    Log.d("response", response.toString());
                                }
                            },

                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                }
                            }
                    )
            );

       } else {
            JSONObject params = new JSONObject();
            params.put("uuid", beacon.getUuid());
            mQueue.add(new JsonObjectRequest(Request.Method.POST, api_url_stays(userName), params,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    // JSONObjectのパース、List、Viewへの追加等
                                    Log.d("response", response.toString());
                                }
                            },

                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                }
                            }
                    )
            );
        }
    }

    private String api_url_stays(String user_name) {
        String url = getString(R.string.api_stays);
        url = url.replaceAll(":api_end_point", getString(R.string.api_endpoint))
                .replaceAll(":user_name", user_name);
        return url;
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        // throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,byte[] scanRecord) {
            // デバイスが検出される度に呼び出されます。
            Beacon beacon = record2beacon(scanRecord);
            if (beacon == null) {
               return;
            }

            if (mLastBeacon == null) {
                try {
                    sendStay(beacon);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
//            Log.d("uuid", beacon.getUuid());
            mLastBeacon = beacon;
            mLastFoundAt = mTimerCount;
        }
    };

    private Beacon record2beacon(byte[] scanRecord) {
        if (scanRecord.length <= 30) { return null; }

        if((scanRecord[5] == (byte)0x4c) && (scanRecord[6] == (byte)0x00) &&
                (scanRecord[7] == (byte)0x02) && (scanRecord[8] == (byte)0x15)) {
            // this is beacon
        } else {
            return null;
        }
        String uuid = IntToHex2(scanRecord[9] & 0xff)
                    + IntToHex2(scanRecord[10] & 0xff)
                    + IntToHex2(scanRecord[11] & 0xff)
                    + IntToHex2(scanRecord[12] & 0xff)
                    + "-"
                    + IntToHex2(scanRecord[13] & 0xff)
                    + IntToHex2(scanRecord[14] & 0xff)
                    + "-"
                    + IntToHex2(scanRecord[15] & 0xff)
                    + IntToHex2(scanRecord[16] & 0xff)
                    + "-"
                    + IntToHex2(scanRecord[17] & 0xff)
                    + IntToHex2(scanRecord[18] & 0xff)
                    + "-"
                    + IntToHex2(scanRecord[19] & 0xff)
                    + IntToHex2(scanRecord[20] & 0xff)
                    + IntToHex2(scanRecord[21] & 0xff)
                    + IntToHex2(scanRecord[22] & 0xff)
                    + IntToHex2(scanRecord[23] & 0xff)
                    + IntToHex2(scanRecord[24] & 0xff);

        String major = IntToHex2(scanRecord[25] & 0xff) + IntToHex2(scanRecord[26] & 0xff);
        String minor = IntToHex2(scanRecord[27] & 0xff) + IntToHex2(scanRecord[28] & 0xff);
        return new Beacon(uuid, major, minor);

    }

    public String IntToHex2(int i) {
        char hex_2[] = {Character.forDigit((i>>4) & 0x0f,16),Character.forDigit(i&0x0f, 16)};
        String hex_2_str = new String(hex_2);
        return hex_2_str.toUpperCase();
    }
}
