package com.vish.cloudexpense;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import com.vish.cloudexpense.GoogleApiFragment.ApiCallFinishListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AddExpenseActivity extends AppCompatActivity implements
        DatePickerFragment.OnDateSelectedListener, ApiCallFinishListener {
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static final String TAG = "AddExpenseActivity";
    private FragmentTransaction fragmentTransaction;
    private GoogleApiFragment googleApiFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        //set datepicker button text to current date.
        Button datePickerButton = (Button) findViewById(R.id.datePickerButton);
        datePickerButton.setText(sdf.format(new Date()));

        FragmentManager fragmentManager = getFragmentManager();
        googleApiFragment = fragmentManager.findFragmentByTag(GoogleApiFragment.TAG);
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    /**
     * Do something with the date selected.
     * @param i
     * @param i1
     * @param i2
     */
    @Override
    public void onDateSelected(int i, int i1, int i2) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, i);
        c.set(Calendar.MONTH, i1);
        c.set(Calendar.DAY_OF_MONTH, i2);
        String chosenDate = sdf.format(c.getTime());
        Log.d(TAG, chosenDate);

        //set datepicker button text to chosen date.
        Button datePickerButton = (Button) findViewById(R.id.datePickerButton);
        datePickerButton.setText(chosenDate);

    }

    @Override
    public void onApiCallFinishedGetResultArraylist(List<String> output) {

    }
}
