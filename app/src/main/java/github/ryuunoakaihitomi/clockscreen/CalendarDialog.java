package github.ryuunoakaihitomi.clockscreen;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Message;
import android.util.Log;
import android.widget.DatePicker;

import java.util.Arrays;
import java.util.Calendar;

/**
 * Show a calendar dialog with the current date.
 */

public class CalendarDialog {

    private static final String TAG = "CalendarDialog";

    private CalendarDialog() {
    }

    static void create(Context context, boolean darkMode, DialogInterface.OnCancelListener cancelCallback) {
        int themeResId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                darkMode ? android.R.style.Theme_DeviceDefault_Dialog_Alert : android.R.style.Theme_DeviceDefault_Light_Dialog_Alert :
                // On 5.0,AlertDialog.THEME_DEVICE_DEFAULT_DARK will let the the calendar(right side) be truncated.
                // (Sun -> Thu & Fri)
                Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP ?
                        darkMode ? android.R.style.Theme_Material_Dialog : android.R.style.Theme_Material_Light_Dialog :
                        darkMode ? AlertDialog.THEME_DEVICE_DEFAULT_DARK : AlertDialog.THEME_DEVICE_DEFAULT_LIGHT;
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR), month = calendar.get(Calendar.MONTH), date = calendar.get(Calendar.DATE);
        Log.i(TAG, "create: Today is " + Arrays.asList(year, month + 1, date));
        DatePickerDialog dialog = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ?
                new DatePickerDialog(context, themeResId) :
                new DatePickerDialog(context, themeResId, null, year, month, date);
        dialog.setOnCancelListener(cancelCallback);
        DatePicker datePicker = dialog.getDatePicker();
        datePicker.setEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // datePicker.setEnabled() doesn't work properly on 5.0+(Can only let year label be unavailable),
            // but datePicker.set(Max/Min)Date can't limit the scope on 5.0.
            long now = System.currentTimeMillis();
            datePicker.setMaxDate(now);
            datePicker.setMinDate(now - 1000);   // Fix java.lang.IllegalArgumentException: fromDate: X does not precede toDate: X
        }

        // Style List:
        //  19        Title + Done (Tablet)
        //  21-23     Title + Cancel + OK
        //  24-29(+)  Cancel + OK

        // Hide "Set Date".
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            dialog.setTitle(null);
        // Hide "Done" and "Cancel".
        Message nullMsg = Message.obtain();
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, null, nullMsg);
        // For compatibility in 4.4 Samsung mobile env (Showing "Cancel" btn)
        // and 5.0+
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, null, nullMsg);
        Log.i(TAG, "create: " + dialog);
        dialog.show();
    }
}
