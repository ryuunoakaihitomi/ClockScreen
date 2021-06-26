package github.ryuunoakaihitomi.hmclockscreen;

import android.os.Bundle;
import android.util.Log;

import java.math.BigDecimal;

public class Utils {

    private static final String TAG = "Utils";

    private Utils() {
    }

    public static int percentage(int part, int total) {
        BigDecimal ratio = BigDecimal.valueOf(part * 100 / total);
        return ratio.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
    }

    public static String bundle2String4Display(Bundle bundle) {
        Log.d(TAG, "bundle2String4Display: " + bundle);
        StringBuilder extras = new StringBuilder();
        for (String key : bundle.keySet()) {
            extras.append(key).append(": ").append(bundle.get(key)).append(System.lineSeparator());
        }
        return extras.toString();
    }
}
