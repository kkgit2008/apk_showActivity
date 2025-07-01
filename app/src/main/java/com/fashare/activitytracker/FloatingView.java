package com.fashare.activitytracker;

//新增
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Looper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import de.greenrobot.event.EventBus;

/**
 * Created by jinliangshan on 16/12/26.
 */
public class FloatingView extends LinearLayout {
    public static final String TAG = "FloatingView";

    private final Context mContext;
    private final WindowManager mWindowManager;
    private TextView mTvPackageName;
    private TextView mTvClassName;
    private ImageView mIvClose;

    public FloatingView(Context context) {
        super(context);
        mContext = context;
        mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        initView();
    }

    private void initView() {
        inflate(mContext, R.layout.layout_floating, this);
        mTvPackageName = (TextView) findViewById(R.id.tv_package_name);
        mTvClassName = (TextView) findViewById(R.id.tv_class_name);
        mIvClose = (ImageView) findViewById(R.id.iv_close);

        mIvClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "关闭悬浮窗", Toast.LENGTH_SHORT).show();
                mContext.startService(
                        new Intent(mContext, TrackerService.class)
                                .putExtra(TrackerService.COMMAND, TrackerService.COMMAND_CLOSE)
                );
            }
        });


        // 新增剪贴板功能（优化版）
        try {
            final ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            
            mTvPackageName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clipboard != null) {
                        String text = mTvPackageName.getText().toString();
                        ClipData clip = ClipData.newPlainText("package_name", text);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(mContext, "已复制: " + text, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext, "剪贴板不可用", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        
            mTvClassName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clipboard != null) {
                        String text = mTvClassName.getText().toString();
                        ClipData clip = ClipData.newPlainText("class_name", text);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(mContext, "已复制: " + text, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext, "剪贴板不可用", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "剪贴板操作失败: " + e.getMessage());
            if (Looper.myLooper() == Looper.getMainLooper()) {
                Toast.makeText(mContext, "复制失败，请重试", Toast.LENGTH_SHORT).show();
            }
        }


    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(TrackerService.ActivityChangedEvent event){
        Log.d(TAG, "event:" + event.getPackageName() + ": " + event.getClassName());
        String packageName = event.getPackageName(),
                className = event.getClassName();

        mTvPackageName.setText(packageName);
        mTvClassName.setText(
                className.startsWith(packageName)?
                className.substring(packageName.length()):
                className
        );
    }

    Point preP, curP;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                preP = new Point((int)event.getRawX(), (int)event.getRawY());
                break;

            case MotionEvent.ACTION_MOVE:
                curP = new Point((int)event.getRawX(), (int)event.getRawY());
                int dx = curP.x - preP.x,
                        dy = curP.y - preP.y;

                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) this.getLayoutParams();
                layoutParams.x += dx;
                layoutParams.y += dy;
                mWindowManager.updateViewLayout(this, layoutParams);

                preP = curP;
                break;
        }

        return false;
    }
}
