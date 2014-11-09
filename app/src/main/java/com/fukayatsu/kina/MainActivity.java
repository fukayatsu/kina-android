package com.fukayatsu.kina;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.GpsStatus;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.Scanner;


public class MainActivity extends Activity {

    public RequestQueue mQueue;
    private ListView mListView;
    private ArrayAdapter<String> mAdapter;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mListView.setAdapter(mAdapter);

        mQueue = VolleyHelper.getRequestQueue(this);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String userName = mSharedPreferences.getString("user_name", "");
        Log.d("userName:", userName);

        Intent intent = new Intent(this, ScannerService.class);
        startService(intent);

        reload();
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_reload) {
            reload();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String api_url_users() {
        String url = getString(R.string.api_users);
        url = url.replaceAll(":api_end_point", getString(R.string.api_endpoint))
                 .replaceAll(":spot_uuid", getString(R.string.spot_uuid));
        return url;
    }

    private void reload() {
        mAdapter.clear();
        mQueue.add(new JsonObjectRequest(Request.Method.GET, api_url_users(), null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // JSONObjectのパース、List、Viewへの追加等
                                Log.d("response", response.toString());
                                try {
                                    JSONArray users = response.getJSONArray("users");
                                    for(int i = 0; i < users.length(); i++){
                                        JSONObject user = users.getJSONObject(i);
                                        Log.d("user", user.getString("name"));
                                        Log.d("active", user.getBoolean("active") ? "✔" : "✗");
                                        mAdapter.add((user.getBoolean("active") ? "✔" : " ") + " " + user.getString("name"));
                                    }
                                    mAdapter.notifyDataSetChanged();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },

                        new Response.ErrorListener() {
                            @Override public void onErrorResponse(VolleyError error) {
                                // エラー処理 error.networkResponseで確認
                                // エラー表示など
                            }
                        }
                )
        );
    }
}
