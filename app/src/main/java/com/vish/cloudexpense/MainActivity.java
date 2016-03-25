package com.vish.cloudexpense;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * References:
 * http://stackoverflow.com/questions/5070830/populating-a-listview-using-arraylist
 * https://developers.google.com/apps-script/guides/rest/quickstart/android
 */
public class MainActivity extends Activity implements GoogleApiFragment.ApiCallFinishListener {
    private static final String TAG = "MainActivity";
//    private TextView mOutputText;
    private ListView expenseCategories;
    protected ArrayAdapter<String> arrayAdapter;
    protected List<String> arrayList = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        //TODO this is just a simple TextView with dynamic layout.
//        //this is the sample code from google.
//        LinearLayout activityLayout = new LinearLayout(this);
//        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.MATCH_PARENT);
//        activityLayout.setLayoutParams(lp);
//        activityLayout.setOrientation(LinearLayout.VERTICAL);
//        activityLayout.setPadding(16, 16, 16, 16);
//        ViewGroup.LayoutParams tlp = new ViewGroup.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT);
//        mOutputText = new TextView(this);
//        mOutputText.setLayoutParams(tlp);
//        mOutputText.setPadding(16, 16, 16, 16);
//        mOutputText.setVerticalScrollBarEnabled(true);
//        mOutputText.setMovementMethod(new ScrollingMovementMethod());
//        activityLayout.addView(mOutputText);

        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        arrayList.add("Hello World");
        expenseCategories = (ListView) findViewById(R.id.expenseCategoriesListView);

        arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                arrayList);
        expenseCategories.setAdapter(arrayAdapter);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        GoogleApiFragment googleApiFragment = GoogleApiFragment.newInstance
                (GoogleApiFragment.apiMethods.GET_EXPENSES_BY_CATEGORY);
        fragmentTransaction.add(googleApiFragment,"GoogleApiFragment").commit();
    }



    public void startAddExpense(View view) {
        Intent intent = new Intent(this,AddExpenseActivity.class);
        startActivity(intent);

    }

    /**
     * This method takes output of Api call and updates array of results.
     * @see
     * {@link com.vish.cloudexpense.GoogleApiFragment.ApiCallFinishListener#onApiCallFinishedGetResultArraylist(List)}
     * @param output
     */
    @Override
    public void onApiCallFinishedGetResultArraylist(List<String> output) {
        //update arrayList in main activity
        arrayList.clear();
        arrayList.addAll(output);
        //notify the ListView Adapter that data has changed.
        Log.d(TAG, Arrays.toString((arrayList.toArray(new String[]{""}))));
        arrayAdapter.notifyDataSetChanged();

    }
}