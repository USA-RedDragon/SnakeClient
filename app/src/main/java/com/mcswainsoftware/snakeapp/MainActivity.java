package com.mcswainsoftware.snakeapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    final Handler refreshHandler = new Handler();
    private Runnable autoRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switch (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)) {
            case ConnectionResult.SUCCESS:
            case ConnectionResult.SERVICE_UPDATING:
                break;
            case ConnectionResult.SERVICE_MISSING:
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
            case ConnectionResult.SERVICE_INVALID:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=" + GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE));
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
                Utils.sendNotification(this, "Google Play Services Required", "Tap to install or update Google Play Services", pendingIntent);
                break;
            case ConnectionResult.SERVICE_DISABLED:
                intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE));
                pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
                Utils.sendNotification(this, "Google Play Services Disabled", "Tap to enable Google Play Services", pendingIntent);
                break;
        }
        FirebaseMessaging.getInstance().subscribeToTopic("alerts");

        setContentView(R.layout.activity_main);

        autoRefresh = new Runnable() {
            @Override
            public void run() {
                ServerCommunication.requestHumidity("http://192.168.0.32/", new ServerCommunication.OnRequestCompletedReceiver() {
                    @Override
                    public void onRestCompleted(String response) {
                        if(!response.equals(""))
                            updateHumidityUI(Integer.parseInt(response));
                    }
                });

                ServerCommunication.requestTemperature("http://192.168.0.32/", new ServerCommunication.OnRequestCompletedReceiver() {
                    @Override
                    public void onRestCompleted(String response) {
                        if(!response.equals(""))
                            updateTemperatureUI(Integer.parseInt(response));
                    }
                });

                ServerCommunication.requestPreviousData("http://192.168.0.32/", (int) (System.currentTimeMillis() / 1000L) - 60 * 60, new ServerCommunication.OnRequestCompletedReceiver() {
                    @Override
                    public void onRestCompleted(String response) {
                        if(!response.equals(""))
                            loadGraphData(response);
                    }
                });
                refreshHandler.postDelayed(this, 5000);
            }
        };
        refreshHandler.post(autoRefresh);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        refreshHandler.removeCallbacks(autoRefresh);
    }

    private void loadGraphData(String data) {
        try {
            final JSONArray humidityArray = new JSONObject(data).getJSONArray("humidity");
            final JSONArray temperatureArray = new JSONObject(data).getJSONArray("temperature");
            final JSONArray timeArray = new JSONObject(data).getJSONArray("time");


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private MenuItem.OnMenuItemClickListener menuClickListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (item.getItemId() == R.id.menu_refresh) {
                ServerCommunication.requestHumidity("http://192.168.0.32/", new ServerCommunication.OnRequestCompletedReceiver() {
                    @Override
                    public void onRestCompleted(String response) {
                        if(!response.equals(""))
                            updateHumidityUI(Integer.parseInt(response));
                    }
                });

                ServerCommunication.requestTemperature("http://192.168.0.32/", new ServerCommunication.OnRequestCompletedReceiver() {
                    @Override
                    public void onRestCompleted(String response) {
                        if(!response.equals(""))
                            updateTemperatureUI(Integer.parseInt(response));
                    }
                });
                ServerCommunication.requestPreviousData("http://192.168.0.32/", (int) (System.currentTimeMillis() / 1000L) - 60 * 60, new ServerCommunication.OnRequestCompletedReceiver() {
                    @Override
                    public void onRestCompleted(String response) {
                        if(!response.equals(""))
                            loadGraphData(response);
                    }
                });
                return true;
            } else if(item.getItemId() == R.id.menu_settings) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                MainActivity.this.startActivity(intent);
                return true;
            }
            return false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_actionbar, menu);
        menu.findItem(R.id.menu_refresh).setOnMenuItemClickListener(menuClickListener);
        menu.findItem(R.id.menu_settings).setOnMenuItemClickListener(menuClickListener);
        return true;
    }

    private void updateHumidityUI(int humidity) {
        TextView humidityText = findViewById(R.id.humidity_text);
        humidityText.setText(getString(R.string.humidity_text, humidity));
    }

    private void updateTemperatureUI(int temperature) {
        TextView temperatureText = findViewById(R.id.temperature_text);
        temperatureText.setText(getString(R.string.temperature_text, temperature, "F"));
    }
}
