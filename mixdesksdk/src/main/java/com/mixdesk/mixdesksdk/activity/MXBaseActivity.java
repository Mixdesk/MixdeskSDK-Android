package com.mixdesk.mixdesksdk.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mixdesk.mixdesksdk.R;
import com.mixdesk.mixdesksdk.util.MXConfig;
import com.mixdesk.mixdesksdk.util.MXUtils;

/**
 * OnePiece
 * Created by xukq on 6/27/16.
 */
public abstract class MXBaseActivity extends Activity {

    private RelativeLayout mTitleRl;
    private RelativeLayout mBackRl;
    private TextView mBackTv;
    private ImageView mBackIv;
    private TextView mTitleTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MXUtils.updateLanguage(this);
        setContentView(getLayoutRes());

        mTitleRl = findViewById(R.id.title_rl);
        mBackRl = findViewById(R.id.back_rl);
        mBackTv = findViewById(R.id.back_tv);
        mBackIv = findViewById(R.id.back_iv);
        mTitleTv = findViewById(R.id.title_tv);
        applyCustomUIConfig();
        mBackRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        initView(savedInstanceState);
        setListener();
        processLogic(savedInstanceState);
    }

    private void applyCustomUIConfig() {
        if (MXConfig.DEFAULT != MXConfig.ui.backArrowIconResId) {
            mBackIv.setImageResource(MXConfig.ui.backArrowIconResId);
        }

        // 处理标题栏背景色
        MXUtils.applyCustomUITintDrawable(mTitleRl, android.R.color.white, R.color.mx_activity_title_bg, MXConfig.ui.titleBackgroundResId);

        // 处理标题、返回、返回箭头颜色
        MXUtils.applyCustomUITextAndImageColor(R.color.mx_activity_title_textColor, MXConfig.ui.titleTextColorResId, mBackIv, mBackTv, mTitleTv);

        // 通过 #FFFFFF 方式设置颜色：处理标题栏背景色、处理标题、返回、返回箭头颜色
        if (!TextUtils.isEmpty(MXConfig.ui.titleBackgroundColor)) {
            mTitleRl.setBackgroundColor(Color.parseColor(MXConfig.ui.titleBackgroundColor));
        }
        if (!TextUtils.isEmpty(MXConfig.ui.titleTextColor)) {
            int color = Color.parseColor(MXConfig.ui.titleTextColor);
            mBackIv.clearColorFilter();
            mBackIv.setColorFilter(color);
            mBackTv.setTextColor(color);
            mTitleTv.setTextColor(color);
        }

        // 处理标题文本的对其方式
        MXUtils.applyCustomUITitleGravity(mBackTv, mTitleTv);
    }

    protected void setTitle(String title) {
        mTitleTv.setText(title);
    }

    protected abstract int getLayoutRes();

    /**
     * 初始化布局以及View控件
     */
    protected abstract void initView(Bundle savedInstanceState);

    /**
     * 给View控件添加事件监听器
     */
    protected abstract void setListener();

    /**
     * 处理业务逻辑，状态恢复等操作
     *
     * @param savedInstanceState
     */
    protected abstract void processLogic(Bundle savedInstanceState);

}

