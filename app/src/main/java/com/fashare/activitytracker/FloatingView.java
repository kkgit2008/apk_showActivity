package com.fashare.activitytracker;

//新增
import android.content.ClipData;
import android.content.ClipboardManager;

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


		//新增
		mTvPackageName.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				Toast.makeText(mContext, "已复制第一行到剪贴板", Toast.LENGTH_SHORT).show();
				((ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("package_name", mTvPackageName.getText().toString()));
			}
		});
		mTvClassName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "已复制第二行到剪贴板", Toast.LENGTH_SHORT).show();
                ((ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("class_name", mTvClassName.getText().toString()));
            }
        });


        mIvClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "关闭悬浮框", Toast.LENGTH_SHORT).show();
                mContext.startService(
                        new Intent(mContext, TrackerService.class)
                                .putExtra(TrackerService.COMMAND, TrackerService.COMMAND_CLOSE)
                );
            }
        });
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
