package com.mcswainsoftware.snakeapp;

import android.os.AsyncTask;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public final class ServerCommunication {

    private static final String API_TEMPERATURE = "api/v1/temperature";
    private static final String API_HUMIDITY = "api/v1/humidity";
    private static final String API_SETTINGS_TEMPERATURE_THRESHOLD = "api/v1/settings/temperaturethreshold";
    private static final String API_SETTINGS_HUMIDITY_THRESHOLD = "api/v1/settings/humiditythreshold";
    private static final String API_GET_DATA = "api/v1/database/";

    public static void requestTemperature(String url, OnRequestCompletedReceiver receiver) {
        RequestTask task = new RequestTask();
        task.setOnRestCompletedReceiver(receiver);
        task.execute(url + API_TEMPERATURE);
    }

    public static void requestHumidity(String url, OnRequestCompletedReceiver receiver) {
        RequestTask task = new RequestTask();
        task.setOnRestCompletedReceiver(receiver);
        task.execute(url + API_HUMIDITY);
    }

    public static void requestTemperatureSettingsThreshold(String url, OnRequestCompletedReceiver receiver) {
        RequestTask task = new RequestTask();
        task.setOnRestCompletedReceiver(receiver);
        task.execute(url + API_SETTINGS_TEMPERATURE_THRESHOLD);
    }

    public static void requestHumiditySettingsThreshold(String url, OnRequestCompletedReceiver receiver) {
        RequestTask task = new RequestTask();
        task.setOnRestCompletedReceiver(receiver);
        task.execute(url + API_SETTINGS_HUMIDITY_THRESHOLD);
    }

    public static void setTemperatureAlertThreshold(String url, int threshold) {
        RequestTask task = new RequestTask();
        task.setMethod("POST");
        HashMap<String, String> postParams = new HashMap<>();
        postParams.put("alertTemperatureThreshold", ""+threshold);
        task.setPostData(postParams);
        task.execute(url + API_SETTINGS_TEMPERATURE_THRESHOLD);
    }

    public static void setTemperatureAlertAboveThreshold(String url, int threshold) {
        RequestTask task = new RequestTask();
        task.setMethod("POST");
        HashMap<String, String> postParams = new HashMap<>();
        postParams.put("alertTemperatureAboveThreshold", ""+threshold);
        task.setPostData(postParams);
        task.execute(url + API_SETTINGS_TEMPERATURE_THRESHOLD);
    }

    public static void setTemperatureTurnOffThreshold(String url, int threshold) {
        RequestTask task = new RequestTask();
        task.setMethod("POST");
        HashMap<String, String> postParams = new HashMap<>();
        postParams.put("turnOffTemperatureThreshold", ""+threshold);
        task.setPostData(postParams);
        task.execute(url + API_SETTINGS_TEMPERATURE_THRESHOLD);
    }

    public static void setTemperatureTurnOnThreshold(String url, int threshold) {
        RequestTask task = new RequestTask();
        task.setMethod("POST");
        HashMap<String, String> postParams = new HashMap<>();
        postParams.put("turnOnTemperatureThreshold", ""+threshold);
        task.setPostData(postParams);
        task.execute(url + API_SETTINGS_TEMPERATURE_THRESHOLD);
    }

    public static void setHumidityAlertThreshold(String url, int threshold) {
        RequestTask task = new RequestTask();
        task.setMethod("POST");
        HashMap<String, String> postParams = new HashMap<>();
        postParams.put("alertHumidityThreshold", ""+threshold);
        task.setPostData(postParams);
        task.execute(url + API_SETTINGS_HUMIDITY_THRESHOLD);
    }

    public static void setHumidityTurnOnThreshold(String url, int threshold) {
        RequestTask task = new RequestTask();
        task.setMethod("POST");
        HashMap<String, String> postParams = new HashMap<>();
        postParams.put("turnOnHumidityThreshold", ""+threshold);
        task.setPostData(postParams);
        task.execute(url + API_SETTINGS_HUMIDITY_THRESHOLD);
    }

    public static void requestPreviousData(String url, int sinceTimestamp, OnRequestCompletedReceiver receiver) {
        RequestTask task = new RequestTask();
        task.setOnRestCompletedReceiver(receiver);
        task.execute(url + API_GET_DATA + sinceTimestamp);
    }

    public static void requestPreviousData(String url, OnRequestCompletedReceiver receiver) {
        requestPreviousData(url, 0, receiver);
    }

    /**
     * The callback for a rest request
     */
    public interface OnRequestCompletedReceiver {

        /**
         * Callback method, must be overridden
         * @param response an object representing your response
         */
        void onRestCompleted(String response);
    }

    /**
     * The background task that preforms the REST transaction
     */
    private static class RequestTask extends AsyncTask<String, Void, String> {

        /**
         * The callback
         */
        private OnRequestCompletedReceiver receiver;
        private String method = "GET";
        private HashMap<String, String> postData;

        public void setPostData(HashMap<String, String> postData) {
            this.postData = postData;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        /**
         * Do the request
         * @param requests the Request to preform
         * @return the response from the server
         */
        @Override
        protected String doInBackground(String... requests) {
            HttpURLConnection conn = null;
            try {
                final String req = requests[0];

                URL url = new URL(req);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(method);

                if (method.equals("POST")) {
                    StringBuilder postString = new StringBuilder();
                    for (Map.Entry<String, String> param : postData.entrySet()) {
                        if (postString.length() != 0) postString.append('&');
                        postString.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                        postString.append('=');
                        postString.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                    }
                    byte[] postDataBytes = postString.toString().getBytes("UTF-8");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                    conn.setDoOutput(true);
                    conn.getOutputStream().write(postDataBytes);
                    conn.getOutputStream().flush();
                }

                Scanner s = new Scanner(conn.getInputStream()).useDelimiter("\\A");
                return (s.hasNext() ? s.next() : "");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(conn != null) conn.disconnect();
            }
            return "";
        }

        /**
         * Call the callback
         * @param result the response to give the callback
         */
        @Override
        protected void onPostExecute(String result) {
            if(receiver != null) receiver.onRestCompleted(result);
        }

        public void setOnRestCompletedReceiver(OnRequestCompletedReceiver receiver) {
            this.receiver = receiver;
        }
    }
}
