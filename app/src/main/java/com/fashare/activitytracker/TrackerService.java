package com.fashare.activitytracker;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import de.greenrobot.event.EventBus;

public class TrackerService extends AccessibilityService {
    public static final String TAG = "TrackerService";
    public static final String COMMAND = "COMMAND";
    public static final String COMMAND_OPEN = "COMMAND_OPEN";
    public static final String COMMAND_CLOSE = "COMMAND_CLOSE";
    
    private TrackerWindowManager mTrackerWindowManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    private void initTrackerWindowManager() {
        if (mTrackerWindowManager == null) {
            mTrackerWindowManager = new TrackerWindowManager(this);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        initTrackerWindowManager();

        if (intent != null) {
            String command = intent.getStringExtra(COMMAND);
            if (command != null) {
                if (command.equals(COMMAND_OPEN)) {
                    if (checkOverlayPermission()) {
                        mTrackerWindowManager.addView();
                    } else {
                        Log.w(TAG, "悬浮窗权限未授予");
                        showPermissionGuide();
                    }
                } else if (command.equals(COMMAND_CLOSE)) {
                    mTrackerWindowManager.removeView();
                }
            }
        }
        return START_STICKY;
    }

    private boolean checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    private void showPermissionGuide() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "onAccessibilityEvent: " + event.getPackageName());
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            CharSequence packageName = event.getPackageName();
            CharSequence className = event.getClassName();
            if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(className)) {
                EventBus.getDefault().post(new ActivityChangedEvent(
                        packageName.toString(),
                        className.toString()
                ));
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.w(TAG, "onInterrupt");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (mTrackerWindowManager != null) {
            mTrackerWindowManager.removeView();
        }
    }

    public static class ActivityChangedEvent {
        private final String mPackageName;
        private final String mClassName;

        public ActivityChangedEvent(String packageName, String className) {
            mPackageName = packageName;
            mClassName = className;
        }

        public String getPackageName() {
            return mPackageName;
        }

        public String getClassName() {
            return mClassName;
        }
    }
}
