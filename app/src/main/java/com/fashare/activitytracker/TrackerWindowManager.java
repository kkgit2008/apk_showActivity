package com.fashare.activitytracker;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

public class TrackerWindowManager {
    private static final String TAG = "TrackerWindowManager";
    private final Context mContext;
    private final WindowManager mWindowManager;
    private View mFloatingView;
    private final WindowManager.LayoutParams mLayoutParams;

    public TrackerWindowManager(Context context) {
        mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = createLayoutParams();
    }

    private WindowManager.LayoutParams createLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.START | Gravity.TOP;
        
        // 动态窗口类型选择
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL 
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        
        return params;
    }

    public void addView() {
        try {
            if (mFloatingView == null) {
                mFloatingView = new FloatingView(mContext);
                mFloatingView.setLayoutParams(mLayoutParams);
                mWindowManager.addView(mFloatingView, mLayoutParams);
                Log.d(TAG, "悬浮窗添加成功");
            }
        } catch (Exception e) {
            Log.e(TAG, "添加悬浮窗失败: " + e.getMessage());
        }
    }

    public void removeView() {
        try {
            if (mFloatingView != null) {
                mWindowManager.removeView(mFloatingView);
                mFloatingView = null;
                Log.d(TAG, "悬浮窗移除成功");
            }
        } catch (Exception e) {
            Log.e(TAG, "移除悬浮窗失败: " + e.getMessage());
        }
    }
}