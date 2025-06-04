package com.mixdesk.mixdesksdk.demo;

import android.app.Application;
import android.widget.Toast;

import com.mixdesk.core.MXManager;
import com.mixdesk.core.callback.OnInitCallback;
import com.mixdesk.mixdesksdk.util.MXConfig;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/5/25 下午4:28
 * 描述:
 */
public class App extends Application {

    public static final String mixdeskAppKey = "";


    @Override
    public void onCreate() {
        super.onCreate();

        MXManager.setDebugMode(true);
        initMixdeskSDK();
    }

    private void initMixdeskSDK() {
        String mixdeskKey = mixdeskAppKey;
        MXConfig.init(this, mixdeskKey, new OnInitCallback() {
            @Override
            public void onSuccess(String clientId) {
                Toast.makeText(App.this, "init success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int code, String message) {
                Toast.makeText(App.this, "int failure message = " + message, Toast.LENGTH_SHORT).show();
            }
        });
        MXManager.setDebugMode(true);

        // 可选
        customMixdeskSDK();
    }

    private void customMixdeskSDK() {
        // 配置自定义信息
//        MQConfig.ui.titleGravity = MQConfig.ui.MQTitleGravity.LEFT;
//        MQConfig.ui.backArrowIconResId = android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha;
//        MQConfig.ui.titleBackgroundResId = R.color.test_red;
//        MQConfig.ui.titleTextColorResId = R.color.test_blue;
//        MQConfig.ui.leftChatBubbleColorResId = R.color.test_green;
//        MQConfig.ui.leftChatTextColorResId = R.color.test_red;
//        MQConfig.ui.rightChatBubbleColorResId = R.color.test_red;
//        MQConfig.ui.rightChatTextColorResId = R.color.test_green;
//        MQConfig.ui.robotEvaluateTextColorResId = R.color.test_red;
//        MQConfig.ui.robotMenuItemTextColorResId = R.color.test_blue;
//        MQConfig.ui.robotMenuTipTextColorResId = R.color.test_blue;
//        MQConfig.isShowClientAvatar = true;

//        MQConfig.ui.leftChatBubbleColor = "#00CE7D";
//        MQConfig.ui.leftChatTextColor = "#FF5C5E";
//        MQConfig.ui.rightChatBubbleColor = "#FFB652";
//        MQConfig.ui.rightChatTextColor = "#17C7D1";
//        MQConfig.ui.backgroundColor = "#303D42";
    }

}
