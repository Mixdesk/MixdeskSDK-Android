package com.mixdesk.mixdesksdk.demo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.mixdesk.core.MXManager;
import com.mixdesk.core.callback.OnInitCallback;
import com.mixdesk.mixdesksdk.util.MXConfig;
import com.mixdesk.mixdesksdk.util.MXIntentBuilder;
import com.mixdesk.mixdesksdk.util.MXUtils;

import java.util.HashMap;
import java.util.Random;

public class MainActivity extends Activity {
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;

    private TextView mWelcomeTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWelcomeTv = findViewById(R.id.welcome_tv);
        mWelcomeTv.setText("欢迎使用 Mixdesk SDK " + MXManager.getSDKVersion());
    }

    /**
     * 咨询客服
     *
     * @param v
     */
    public void conversation(View v) {
        // 兼容Android6.0动态权限
        conversationWrapper();
    }

    /**
     * 开发者功能
     *
     * @param v
     */
    public void developer(View v) {
        startActivity(new Intent(MainActivity.this, ApiSampleActivity.class));
    }

    /**
     * 自定义 Activity
     *
     * @param view
     */
    public void customizedConversation(View view) {
        Intent intent = new MXIntentBuilder(this, CustomizedMQConversationActivity.class).build();
        startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    conversationWrapper();
                } else {
                    MXUtils.show(this, com.mixdesk.mixdesksdk.R.string.mx_sdcard_no_permission);
                }
                break;
            }
        }

    }

    private void conversationWrapper() {
        conversation();
    }

    private void conversation() {
        MXConfig.isShowClientAvatar = true;
        HashMap<String, String> clientInfo = new HashMap<>();
        clientInfo.put("name", "rd_71041066");
        clientInfo.put("avatar", "https://img.cdeledu.com/FAQ/2021/1108/6df81c168d904c65-0_chg.jpg");
        clientInfo.put("tel", "+8615725079823");
        clientInfo.put("comment", String.valueOf((new Random().nextInt())));
        Intent intent = new MXIntentBuilder(this)
                .updateClientInfo(clientInfo)
                .setCustomizedId("88899112233")
                .build();
        startActivity(intent);
    }


    public void littleHeart(View view) {
        MXManager.init(this, App.mixdeskAppKey, new OnInitCallback() {
            @Override
            public void onSuccess(String clientId) {
                Intent intent = new MXIntentBuilder(MainActivity.this).build();
                startActivity(intent);
            }

            @Override
            public void onFailure(int code, String message) {

            }
        });
    }

    public void androidDemo(View view) {
        MXManager.init(this, App.mixdeskAppKey, new OnInitCallback() {
            @Override
            public void onSuccess(String clientId) {
                Intent intent = new MXIntentBuilder(MainActivity.this).build();
                startActivity(intent);
            }

            @Override
            public void onFailure(int code, String message) {

            }
        });
    }

    public void switchAppKey(View view) {
        final Dialog inputDialog = new Dialog(this, com.mixdesk.mixdesksdk.R.style.MQDialog);
        inputDialog.setCancelable(true);
        inputDialog.setContentView(R.layout.dialog_input);
        TextView titleTv = (TextView) inputDialog.findViewById(R.id.tv_input_title);
        titleTv.setText("输入 AppKey");
        final EditText valueEt = (EditText) inputDialog.findViewById(R.id.et_input_value);
        inputDialog.findViewById(R.id.tv_input_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MXUtils.closeKeyboard(inputDialog);
                inputDialog.dismiss();
                String appKey = valueEt.getText().toString();
                if (TextUtils.isEmpty(appKey)) {
                    return;
                }
                MXManager.init(MainActivity.this, appKey, new OnInitCallback() {
                    @Override
                    public void onSuccess(String clientId) {
                        Intent intent = new MXIntentBuilder(MainActivity.this).build();
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(int code, String message) {

                    }
                });
            }
        });
        inputDialog.show();
        MXUtils.openKeyboard(valueEt);
    }

    public void linkWebView(View view) {
        final Dialog inputDialog = new Dialog(this, com.mixdesk.mixdesksdk.R.style.MQDialog);
        inputDialog.setCancelable(true);
        inputDialog.setContentView(R.layout.dialog_input);
        TextView titleTv = (TextView) inputDialog.findViewById(R.id.tv_input_title);
        titleTv.setText("输入 聊天链接");
        final EditText valueEt = (EditText) inputDialog.findViewById(R.id.et_input_value);
        inputDialog.findViewById(R.id.tv_input_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MXUtils.closeKeyboard(inputDialog);
                inputDialog.dismiss();
                String link = valueEt.getText().toString();
                if (TextUtils.isEmpty(link)) {
                    return;
                }
                Intent intent = new Intent(MainActivity.this, ActivityWebView.class);
                intent.putExtra("link", link);
                startActivity(intent);
            }
        });
        inputDialog.show();
        MXUtils.openKeyboard(valueEt);
    }
}
