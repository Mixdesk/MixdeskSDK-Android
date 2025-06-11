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

    public static final String mixdeskAppKey = "mixdeskAppKeymixdeskAppKeymixdeskAppKey";


    @Override
    public void onCreate() {
        super.onCreate();

        initMixdeskSDK();

        MXManager.setDebugMode(true);
    }

    private void initMixdeskSDK() {
        // 优先使用打包时注入的 key，如果为空则使用默认值
        String mixdeskKey = !BuildConfig.MIXDESK_APP_KEY.isEmpty() ? BuildConfig.MIXDESK_APP_KEY : mixdeskAppKey;
        
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
//        customMixdeskSDK();
    }

    private void customMixdeskSDK() {
        // 配置自定义信息
//        MXConfig.ui.titleGravity = MXConfig.ui.MQTitleGravity.LEFT;
////        MXConfig.ui.backArrowIconResId = android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha;
//        MXConfig.ui.titleBackgroundResId = R.color.test_red;
//        MXConfig.ui.titleTextColorResId = R.color.test_blue;
//        MXConfig.ui.leftChatBubbleColorResId = R.color.test_green;
//        MXConfig.ui.leftChatTextColorResId = R.color.test_red;
//        MXConfig.ui.rightChatBubbleColorResId = R.color.test_red;
//        MXConfig.ui.rightChatTextColorResId = R.color.test_green;
//        MXConfig.ui.robotEvaluateTextColorResId = R.color.test_red;
//        MXConfig.ui.robotMenuItemTextColorResId = R.color.test_blue;
//        MXConfig.ui.robotMenuTipTextColorResId = R.color.test_blue;
//        MXConfig.isShowClientAvatar = true;
//
//        MXConfig.ui.leftChatBubbleColor = "#00CE7D";
//        MXConfig.ui.leftChatTextColor = "#FF5C5E";
//        MXConfig.ui.rightChatBubbleColor = "#FFB652";
//        MXConfig.ui.rightChatTextColor = "#17C7D1";
//        MXConfig.ui.backgroundColor = "#303D42";
    }

}
