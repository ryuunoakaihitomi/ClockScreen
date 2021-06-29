package github.ryuunoakaihitomi.clockscreen;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Browser;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.util.Date;

import github.ryuunoakaihitomi.clockscreen.databinding.ActivityMainBinding;

public class ClockScreen extends Activity {

    private static final String TAG = "ClockScreen";

    private AppCompatTextView clockView;
    private TextView batteryLabel;
    private boolean darkMode = true;

    private Bundle mBatteryInfo;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Intent.ACTION_TIME_TICK:
                    synchronizeClock();
                    break;
                case Intent.ACTION_BATTERY_CHANGED:
                    int batteryLevel = Utils.percentage(
                            intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1),
                            intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100));
                    int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                    final boolean isBatteryExist = status != BatteryManager.BATTERY_STATUS_UNKNOWN;
                    if (!isBatteryExist) {
                        Log.i(TAG, "onReceive:  Battery does not exist.");
                        batteryLabel.setVisibility(View.GONE);
                        return;
                    } else batteryLabel.setVisibility(View.VISIBLE);
                    // https://developer.android.com/training/monitoring-device-state/battery-monitoring#DetermineChargeState
                    final boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                            || status == BatteryManager.BATTERY_STATUS_FULL;
                    // BatteryManager.EXTRA_ICON_SMALL is too ugly.
                    final String symbol = isCharging ? "🔌" : "🔋";
                    mBatteryInfo = intent.getExtras();
                    batteryLabel.setText(symbol + batteryLevel + '%');
                    break;
                case Intent.ACTION_BATTERY_LOW:
                    Log.w(TAG, "onReceive: Intent.ACTION_BATTERY_LOW info = " + Utils.bundle2String4Display(mBatteryInfo));
                    System.exit(0);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        fullScreen();
        clockView = binding.clockView;
        batteryLabel = binding.batteryLabel;
        dye();
        // Center the time separator completely.
        clockView.setTypeface(Typeface.createFromAsset(getAssets(), "AndroidClock.ttf"), Typeface.BOLD);
        clockView.setOnClickListener(v -> CalendarDialog.create(ClockScreen.this, darkMode, dialog -> fullScreen()));
        binding.themeToggle.setOnClickListener(v -> {
            darkMode = !darkMode;
            dye();
        });
        binding.shareApp.setOnClickListener(v -> Browser.sendString(this, "https://github.com/ryuunoakaihitomi/ClockScreen"));
        batteryLabel.setOnLongClickListener(v -> {
            Toast.makeText(getApplicationContext(), Utils.bundle2String4Display(mBatteryInfo), Toast.LENGTH_LONG).show();
            return true;
        });
        // Too difficult to select the text before 23, copy it directly.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            clockView.setOnLongClickListener(v -> {
                v.clearFocus();
                ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE))
                        .setPrimaryClip(ClipData.newPlainText(null, clockView.getText()));
                Toast.makeText(getApplicationContext(),
                        // "Text copied to clipboard."
                        getResources().getIdentifier("text_copied", "string", "android"),
                        Toast.LENGTH_SHORT).show();
                return true;
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        synchronizeClock();
        // ClockView seems not to automatically get the focus on 9+.
        // We have to use double-click to show calendar dialog at the first time while app is running.
        clockView.requestFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_LOW);
        registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause: [  ]");
        unregisterReceiver(mBroadcastReceiver);
        super.onPause();
        // WHY DISTURB ME?
        System.exit(0);
    }

    private void dye() {
        if (darkMode) {
            clockView.setBackgroundResource(android.R.color.black);
            clockView.setTextColor(Color.WHITE);
            batteryLabel.setTextColor(Color.WHITE);
        } else {
            clockView.setBackgroundResource(android.R.color.white);
            clockView.setTextColor(Color.BLACK);
            batteryLabel.setTextColor(Color.BLACK);
        }
    }

    private void fullScreen() {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        assert controller != null;
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        // View.SYSTEM_UI_FLAG_LAYOUT_STABLE unimplemented
    }

    private void synchronizeClock() {
        clockView.setText(DateFormat.format("HH:mm", new Date()));
    }
}
