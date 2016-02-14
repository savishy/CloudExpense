package com.vish.cloudexpense;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.util.Log;

/**
 * Created by vishy on 14/2/16.
 */
public class Common {
    public static final String TAG = "Common";
    public static void showSimpleDialog(Context c, String dialogTitle, String msg) {
        new AlertDialog.Builder(c)
                .setTitle(dialogTitle)
                .setMessage(msg)
                .setCancelable(true)
                .setNeutralButton("OK",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }

    public static void printCursor(Cursor c) {
        Log.v(TAG, DatabaseUtils.dumpCursorToString(c));
    }
}
