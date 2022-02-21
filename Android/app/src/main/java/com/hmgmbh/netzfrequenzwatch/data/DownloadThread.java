package com.hmgmbh.netzfrequenzwatch.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.HttpsURLConnection;
import com.hmgmbh.netzfrequenzwatch.Config;

public class DownloadThread extends Thread {

    private String mTargetUrl;
    private AtomicBoolean mRunning = new AtomicBoolean();
    public static final String TAG = "DownloadThread";
    private DataRepository mRepo = DataRepository.getInstance();

    public DownloadThread(String url) {
        mTargetUrl = url;
    }

    public void stopIt() {
        mRunning.set(false);
    }

    @Override
    public void run() {
        mRunning.set(true);
        while (mRunning.get()) {
            try {
                // Does not run on UI thread (non-blocking)

                while (mRunning.get()) {

                    sleep(3000);
                    String result = downloadUrl(mTargetUrl);
                    if (result != null) {
                        Log.d(TAG, "Download finished: " + result);
                        updateFromDownload(result);
                    }
                    else {
                        mRepo.frequStatus().incrNoNet();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                mRepo.frequStatus().incrNoNet();
            }
        }
        mRunning.set(false);
    }

    private void updateFromDownload(String result) {
        if (result != null) {
            Log.d(TAG, "updateFromDownload()");
            String[] result1 = result.split(",");
            if (result1.length > 2) {
                String timestamp = result1[0].replaceAll("[^0-9.]", "");
                String[] freqs = new String[2];
                freqs[0] = result1[1].replaceAll("[^0-9.]", "");
                freqs[1] = result1[2].replaceAll("[^0-9.]", "");
                mRepo.frequStatus().setFreq(freqs, timestamp);
            }
        } else {
            mRepo.frequStatus().incrNoNet();
            Log.d(TAG, "Error in updateFromDownload()");
        }
    }

    private String downloadUrl(String sUrl) throws IOException {
        InputStream stream = null;
        HttpsURLConnection connection = null;
        String result = null;
        try {
            if (!isNetworkOnline()) {
                return null;
            }
            URL url = new URL(sUrl);
            connection = (HttpsURLConnection) url.openConnection();
            // Timeout for reading InputStream arbitrarily set to 3000ms.
            connection.setReadTimeout(3000);
            // Timeout for connection.connect() arbitrarily set to 3000ms.
            connection.setConnectTimeout(3000);
            // For this use case, set HTTP method to GET.
            connection.setRequestMethod("GET");
            // Already true by default but setting just in case; needs to be true since this request
            // is carrying an input (response) body.
            connection.setDoInput(true);
            // Open communications link (network traffic occurs here).
            connection.connect();
//            publishProgress(DownloadCallback.Progress.CONNECT_SUCCESS);
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpsURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }
            // Retrieve the response body as an InputStream.
            stream = connection.getInputStream();
//            publishProgress(DownloadCallback.Progress.GET_INPUT_STREAM_SUCCESS, 0);
            if (stream != null) {
                // Converts Stream to String with max length of 500.
                result = readStream(stream, 500);
//                publishProgress(DownloadCallback.Progress.PROCESS_INPUT_STREAM_SUCCESS, 0);
            }
        } finally {
            // Close Stream and disconnect HTTPS connection.
            if (stream != null) {
                stream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    /**
     * Converts the contents of an InputStream to a String.
     */
    private String readStream(InputStream stream, int maxLength) throws IOException {
        String result = null;
        // Read InputStream using the UTF-8 charset.
        InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
        // Create temporary buffer to hold Stream data with specified max length.
        char[] buffer = new char[maxLength];
        // Populate temporary buffer with Stream data.
        int numChars = 0;
        int readSize = 0;
        while (numChars < maxLength && readSize != -1) {
            numChars += readSize;
            int pct = (100 * numChars) / maxLength;
//            publishProgress(DownloadCallback.Progress.PROCESS_INPUT_STREAM_IN_PROGRESS, pct);
            readSize = reader.read(buffer, numChars, buffer.length - numChars);
        }
        if (numChars != -1) {
            // The stream was not empty.
            // Create String that is actual length of response body if actual length was less than
            // max length.
            numChars = Math.min(numChars, maxLength);
            result = new String(buffer, 0, numChars);
        }
        return result;
    }

    private Boolean isNetworkOnline() {
        NetworkInfo networkInfo = getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected() ||
                (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                        && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
            return false;
        }
        return true;
    }

    private NetworkInfo getActiveNetworkInfo() {
        if (Config.context != null) {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) Config.context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo;
        }
        return null;
    }
}
