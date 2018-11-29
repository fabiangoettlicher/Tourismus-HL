package com.htlhl.tourismus_hl_old;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

class GetBitmapOnline extends AsyncTask<String, Integer, Bitmap> {
    private String stringURL = null;
    private AsyncResponse delegate = null;

    GetBitmapOnline(String url, AsyncResponse delegate) {
        this.stringURL = url;
        this.delegate = delegate;
    }

    interface AsyncResponse {
        void processFinish(Bitmap bitmap);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        delegate.processFinish(bitmap);

    }

    @Override
    protected Bitmap doInBackground(String... params) {
        Bitmap bitmap = null;
        if (stringURL != null) {
            try {
                URL url = new URL(stringURL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                bitmap = BitmapFactory.decodeStream(conn.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }
}
