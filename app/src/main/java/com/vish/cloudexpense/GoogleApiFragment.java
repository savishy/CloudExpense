package com.vish.cloudexpense;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.auth.GoogleAuthException;
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
import com.google.api.services.script.model.ExecutionRequest;
import com.google.api.services.script.model.Operation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 *
 * References:
 * http://stackoverflow.com/a/28849764/682912
 */
public class GoogleApiFragment extends Fragment {

    /**
     * This allows the fragment to communicate with the activity.
     *
     * The container activity must implement this interface.
     * Once the Google API Call is finished, this method is called.
     */
    public interface ApiCallFinishListener {
        /**
         * The container activity should implement this method.
         * This method contains the logic for what to do with the output of
         * The API Call.
         * @param output a {@code List<String>} containing output of API call.
         */
        public void onApiCallFinishedGetResultArraylist(List<String> output);
    }

    ApiCallFinishListener mCallback;
    /**
     * List of supported Google API Methods. Each method
     * is identified by a method name and method index.
     */
    public static enum apiMethods {
        ADD_EXPENSE("addExpense",0),
        GET_EXPENSE_CATEGORIES("getAllExpenseCategories",1),
        GET_CUMULATIVE_EXPENSES("getCumulativeExpenses",2),
        GET_EXPENSES_BY_CATEGORY("getExpensesByCategory",3),
        ;

        private String methodName;
        private int methodIndex;

        apiMethods(String name, int index) {
            this.methodName = name;
            this.methodIndex = index;
        }
        public String getMethodName() {
            return methodName;
        }
        public int getMethodIndex() {
            return methodIndex;
        }
        public apiMethods getMethodNameByIndex(int index) {
            for (apiMethods a : apiMethods.values()) {
                if (a.getMethodIndex() == index)
                    return a;
            }
            return null;
        }
    }
    public apiMethods apiMethod;

    public static GoogleApiFragment newInstance(apiMethods method) {
        GoogleApiFragment f = new GoogleApiFragment();
        f.apiMethod = method;
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("methodIndex", method.getMethodIndex());
        f.setArguments(args);
        return f;
    }

    protected static final String TAG = "GoogleApiFragment";
    GoogleAccountCredential mCredential;

    ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String PREF_ACCOUNT_NAME = "accountName";

    //This is the list of OAuth scopes required by the
    //Google Apps script.
    private static final String[] SCOPES = {
            "https://www.googleapis.com/auth/drive",
            "https://www.googleapis.com/auth/spreadsheets"
    };
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);

        mProgress = new ProgressDialog(getActivity());
        mProgress.setMessage("Calling Google Apps Script Execution API ...");


        // Initialize credentials and service object.
        SharedPreferences settings = getActivity().getPreferences(Context.MODE_PRIVATE);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getActivity().getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
    }


    /**
     * No UI for this Fragment. So we return null.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
//        Common.showSimpleDialog(getActivity(), "onActivityCreated", "Fragment has been created");
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    public void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        Log.d(TAG,"onActivityResult: " + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != getActivity().RESULT_OK) {
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == getActivity().RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        Log.d(TAG, "selected account:" + accountName);
                        mCredential.setSelectedAccountName(accountName);
                        SharedPreferences settings =
                                getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                    }
                } else if (resultCode == getActivity().RESULT_CANCELED) {
                    Common.showSimpleDialog(getActivity(), "Response", "Account unspecified");
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode != getActivity().RESULT_OK) {
                    chooseAccount();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach");

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (ApiCallFinishListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ApiCallFinishListener");
        }

        if (mCredential == null) {
            Log.e (TAG,"Google Account credentials are null!");
        }
    }

    /**
     * Called when Fragment is detached.
     * TODO what's needed when Google Api Fragment is detached?
     */
    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG,"onDetach");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            refreshResults();
        } else {

            Common.showSimpleDialog(getActivity(), "Response", "Google Play Services required: " +
                    "after installing, close and relaunch this app.");
        }
    }

    /**
     * Extend the given HttpRequestInitializer (usually a credentials object)
     * with additional initialize() instructions.
     *
     * @param requestInitializer the initializer to copy and adjust; typically
     *         a credential object.
     * @return an initializer with an extended read timeout.
     */
    private static HttpRequestInitializer setHttpTimeout(
            final HttpRequestInitializer requestInitializer) {
        return new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest httpRequest)
                    throws IOException {
                requestInitializer.initialize(httpRequest);
                // This allows the API to call (and avoid timing out on)
                // functions that take up to 6 minutes to complete (the maximum
                // allowed script run time), plus a little overhead.
                httpRequest.setReadTimeout(380000);
            }
        };
    }

    /**
     * Check that Google Play services APK is installed and up to date. Will
     * launch an error dialog for the user to update Google Play Services if
     * possible.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS ) {
            return false;
        }
        return true;
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                connectionStatusCode,
                getActivity(),
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * Starts an activity in Google Play Services so the user can pick an
     * account.
     * <p>
     *     This method calls {@link #startActivityForResult(Intent, int)} to launch the
     *     Google account selection activity.<br>
     *     The selected account is processed in the
     *     {@link #onActivityResult(int, int, Intent)}
     *     method. See http://developer.android.com/training/basics/intents/result.html
     * </p>
     */
    private void chooseAccount() {
        startActivityForResult(
                mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    /**
     * Attempt to get a set of data from the Google Apps Script Execution API to display. If the
     * email address isn't known yet, then call chooseAccount() method so the
     * user can pick an account.
     */
    private void refreshResults() {
        Log.d(TAG,"refreshResults");
        if (mCredential.getSelectedAccountName() == null) {
            Log.d(TAG,"asking for Google credentials");
            chooseAccount();
        } else {
            if (isDeviceOnline()) {
                new MakeRequestTask(mCredential).execute();
            } else {
                Common.showSimpleDialog(getActivity(), "Response", "No network connection available");
            }
        }
    }



    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * An asynchronous task that handles the Google Apps Script Execution API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.script.Script mService = null;
        private Exception mLastError = null;

        public MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.script.Script.Builder(
                    transport, jsonFactory, setHttpTimeout(credential))
                    .setApplicationName("CloudExpenseTracker")
                    .build();
        }

        /**
         * Background task to call Google Apps Script Execution API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Call the API to run an Apps Script function that returns a list
         * of categorized expenses.
         *
         * @return list of String folder names and their IDs
         * @throws IOException
         */
        private List<String> getDataFromApi()
                throws IOException, GoogleAuthException {
            // ID of the script to call. Acquire this from the Apps Script editor,
            // under Publish > Deploy as API executable.
            String scriptId = "MM4uLUw9bV6-vUZuxqHcH-XvYgb_i6vya";

            List<String> responseList = new ArrayList<String>();

            //get the method to execute.
            if (apiMethod == null) {
                throw new IOException ("API Method object is null!");
            }
            String method = apiMethod.getMethodName();
            Log.d(TAG,"Execute remote: " + method);

            // Create an execution request object.
            //Here we will set the Google Apps Script function to execute.
            ExecutionRequest request = new ExecutionRequest()
                    .setFunction(method);

            // Make the request.
            Operation op =
                    mService.scripts().run(scriptId, request).execute();

            // Print results of request.
            if (op.getError() != null) {
                throw new IOException(getScriptError(op));
            }
            if (op.getResponse() != null &&
                    op.getResponse().get("result") != null) {
                // The result provided by the API needs to be cast into
                // the correct type, based upon what types the Apps Script
                // function returns. Here, the function returns an Apps
                // Script Object with String keys and values, so must be
                // cast into a Java Map (folderSet).
                Map<String, String> responseMap =
                        (Map<String, String>)(op.getResponse().get("result"));

                for (String id: responseMap.keySet()) {
                    responseList.add(
                            String.format("%s (%s)", responseMap.get(id), id));
                }
            }
            Log.d(TAG,"getDataFromApi: " + responseList.toString());
            return responseList;
        }

        /**
         * Interpret an error response returned by the API and return a String
         * summary.
         *
         * @param op the Operation returning an error response
         * @return summary of error response, or null if Operation returned no
         *     error
         */
        private String getScriptError(Operation op) {
            if (op.getError() == null) {
                return null;
            }

            // Extract the first (and only) set of error details and cast as a Map.
            // The values of this map are the script's 'errorMessage' and
            // 'errorType', and an array of stack trace elements (which also need to
            // be cast as Maps).
            Map<String, Object> detail = op.getError().getDetails().get(0);
            List<Map<String, Object>> stacktrace =
                    (List<Map<String, Object>>)detail.get("scriptStackTraceElements");

            StringBuilder sb =
                    new StringBuilder("\nScript error message: ");
            sb.append(detail.get("errorMessage"));

            if (stacktrace != null) {
                // There may not be a stacktrace if the script didn't start
                // executing.
                sb.append("\nScript error stacktrace:");
                for (Map<String, Object> elem : stacktrace) {
                    sb.append("\n  ");
                    sb.append(elem.get("function"));
                    sb.append(":");
                    sb.append(elem.get("lineNumber"));
                }
            }
            sb.append("\n");
            Log.w(TAG,"getScriptError: " + sb.toString());
            return sb.toString();
        }


        @Override
        protected void onPreExecute() {
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
                Common.showSimpleDialog(getActivity(), "Response", "No results returned.");
                mCallback.onApiCallFinishedGetResultArraylist(new ArrayList<String>());
            } else {
                output.add(0, "Data retrieved using the Google Apps Script Execution API:");
                Common.showSimpleDialog(getActivity(), "Response", TextUtils.join("\n", output));

                //call the callback function which has been
                //implemented in main activity.
                mCallback.onApiCallFinishedGetResultArraylist(output);

            }


            //return to activity that called this.
            startActivity(getActivity().getIntent());
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            REQUEST_AUTHORIZATION);
                } else {
                    Common.showSimpleDialog(getActivity(), "Response", "The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                Common.showSimpleDialog(getActivity(), "Response", "Request cancelled.");
            }
        }
    }
}
