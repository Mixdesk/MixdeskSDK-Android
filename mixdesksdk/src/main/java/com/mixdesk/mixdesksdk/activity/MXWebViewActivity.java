package com.mixdesk.mixdesksdk.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mixdesk.mixdesksdk.R;
import com.mixdesk.mixdesksdk.util.MXConfig;
import com.mixdesk.mixdesksdk.util.MXUtils;

/**
 * OnePiece
 * Created by xukq on 6/24/16.
 */
public class MXWebViewActivity extends Activity implements View.OnClickListener {

    public static final String CONTENT = "content";

    private RelativeLayout mTitleRl;
    private RelativeLayout mBackRl;
    private TextView mBackTv;
    private ImageView mBackIv;
    private TextView mTitleTv;
    private WebView mWebView;

    private RelativeLayout mEvaluateRl;
    private TextView mUsefulTv;
    private TextView mUselessTv;
    private TextView mAlreadyFeedbackTv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MXUtils.updateLanguage(this);
        setContentView(R.layout.mx_activity_webview);

        findViews();
        setListeners();
        applyCustomUIConfig();
        logic();
    }

    private void findViews() {
        mTitleRl = findViewById(R.id.title_rl);
        mBackRl = findViewById(R.id.back_rl);
        mBackTv = findViewById(R.id.back_tv);
        mBackIv = findViewById(R.id.back_iv);
        mTitleTv = findViewById(R.id.title_tv);
        mWebView = findViewById(R.id.webview);

        mEvaluateRl = findViewById(R.id.ll_robot_evaluate);
        mUsefulTv = findViewById(R.id.tv_robot_useful);
        mUselessTv = findViewById(R.id.tv_robot_useless);
        mAlreadyFeedbackTv = findViewById(R.id.tv_robot_already_feedback);
    }

    private void setListeners() {
        mBackRl.setOnClickListener(this);
        mUsefulTv.setOnClickListener(this);
        mUselessTv.setOnClickListener(this);
        mAlreadyFeedbackTv.setOnClickListener(this);
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

    private void logic() {
        if (getIntent() != null) {
            handleRobotRichTextMessage();
            String data = getIntent().getStringExtra(CONTENT);
            mWebView.loadDataWithBaseURL(null, data, "text/html", "utf-8", null);

        }
    }

    private void handleRobotRichTextMessage() {
//        if (sRobotMessage != null) {
//            if (TextUtils.equals(RobotMessage.SUB_TYPE_EVALUATE, sRobotMessage.getSubType())
//                    || BaseMessage.TYPE_CONTENT_RICH_TEXT.equals(sRobotMessage.getContentType())) {
//                mEvaluateRl.setVisibility(View.VISIBLE);
//                if (sRobotMessage.isAlreadyFeedback()) {
//                    mUselessTv.setVisibility(View.GONE);
//                    mUsefulTv.setVisibility(View.GONE);
//                    mAlreadyFeedbackTv.setVisibility(View.VISIBLE);
//                } else {
//                    mUselessTv.setVisibility(View.VISIBLE);
//                    mUsefulTv.setVisibility(View.VISIBLE);
//                    mAlreadyFeedbackTv.setVisibility(View.GONE);
//                }
//            }
//        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back_rl) {
            onBackPressed();
//        } else if (id == R.id.tv_robot_useful) {
//            evaluate(RobotMessage.EVALUATE_USEFUL);
//        } else if (id == R.id.tv_robot_useless) {
//            evaluate(RobotMessage.EVALUATE_USELESS);
        } else if (id == R.id.tv_robot_already_feedback) {
            mEvaluateRl.setVisibility(View.GONE);
        }
    }

//    private void evaluate(final int useful) {
//        String clientMsg = "";
//        try {
//            JSONObject extraObj = new JSONObject(sRobotMessage.getExtra());
//            clientMsg = extraObj.optString("client_msg");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        MQConfig.getController(this).evaluateRobotAnswer(sRobotMessage.getId(), clientMsg, sRobotMessage.getQuestionId(), useful, new OnEvaluateRobotAnswerCallback() {
//            @Override
//            public void onFailure(int code, String message) {
//                MQUtils.show(MQWebViewActivity.this, R.string.mx_evaluate_failure);
//            }
//
//            @Override
//            public void onSuccess(String message) {
//                sRobotMessage.setFeedbackUseful(useful);
//                handleRobotRichTextMessage();
//            }
//        });
//    }
}
