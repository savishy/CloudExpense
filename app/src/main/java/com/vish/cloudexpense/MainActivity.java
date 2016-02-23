package com.vish.cloudexpense;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;



import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.services.script.model.*;
import java.util.Map;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * References:
 * http://stackoverflow.com/questions/5070830/populating-a-listview-using-arraylist
 * https://developers.google.com/apps-script/guides/rest/quickstart/android
 */
public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private TextView mOutputText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TODO this is just a simple TextView with dynamic layout.
        //this is the sample code from google.
        ViewGroup.LayoutParams tlp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mOutputText = new TextView(this);
        mOutputText.setLayoutParams(tlp);
        mOutputText.setPadding(16, 16, 16, 16);
        mOutputText.setVerticalScrollBarEnabled(true);
        mOutputText.setMovementMethod(new ScrollingMovementMethod());


        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        GoogleApiFragment googleApiFragment = new GoogleApiFragment();
        fragmentTransaction.add(googleApiFragment,"GoogleApiFragment").commit();
    }

    /**
     * This method sets {@link #mOutputText}. It is used as a way for
     * {@link GoogleApiFragment} to update TextViews etc in this activity.
     * @param text
     */
    protected void setOutputText(String text) {
        mOutputText.setText(text);
    }
}