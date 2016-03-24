package com.vish.cloudexpense;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * a fragment that shows a dialog to choose a date.
 *
 * References:
 * http://developer.android.com/guide/topics/ui/controls/pickers.html#DatePickerFragment
 *
 * Created by vishy on 3/24/2016.
 */
public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    OnDateSelectedListener mCallback;
    /**
     * For the fragment to communicate properly with the
     * calling activity, the container activity must implement
     * this interface.
     *
     * http://developer.android.com/training/basics/fragments/communicating.html
     */
    public interface OnDateSelectedListener {
        public void onDateSelected(int i, int i1, int i2);
    }

    private static final String TAG = "DatePickerFragment";
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        mCallback.onDateSelected(i,i1,i2);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnDateSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnDateSelectedListener");
        }
    }
}