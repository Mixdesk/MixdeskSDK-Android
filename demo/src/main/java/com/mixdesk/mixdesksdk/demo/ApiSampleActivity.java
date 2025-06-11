package com.mixdesk.mixdesksdk.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mixdesk.core.MXManager;
import com.mixdesk.core.bean.MXAgent;
import com.mixdesk.core.bean.MXMessage;
import com.mixdesk.core.callback.OnEndConversationCallback;
import com.mixdesk.core.callback.OnGetMQClientIdCallBackOn;
import com.mixdesk.core.callback.OnGetMessageListCallback;
import com.mixdesk.mixdesksdk.controller.ControllerImpl;
import com.mixdesk.mixdesksdk.util.MXConfig;
import com.mixdesk.mixdesksdk.util.MXIntentBuilder;
import com.mixdesk.mixdesksdk.util.MXUtils;

import java.util.HashMap;
import java.util.List;

public class ApiSampleActivity extends Activity implements View.OnClickListener {

    private TextView currentIdTv;
    private View setCurrentIdOnlineBtn;
    private View setInputIdOnlineBtn;
    private View setCustomizedIdOnlineBtn;
    private View getNewIdBtn;
    private View setClientInfoBtn;
    private View getUnreadMessageBtn;
    private View offlineClientBtn;
    private View endConversationBtn;
    private View getCurrentAgentInfoBtn;

    private MXManager MXManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer);

        MXManager = MXManager.getInstance(this);

        findViews();
        setListeners();
        updateId();
    }

    private void findViews() {
        currentIdTv = (TextView) findViewById(R.id.current_id_tv);
        setCurrentIdOnlineBtn = findViewById(R.id.set_current_client_id_online_btn);
        setInputIdOnlineBtn = findViewById(R.id.set_mixdesk_client_id_online_btn);
        setCustomizedIdOnlineBtn = findViewById(R.id.set_customised_id_online_btn);
        getNewIdBtn = findViewById(R.id.get_new_mixdesk_id_btn);
        setClientInfoBtn = findViewById(R.id.set_client_info);
        getUnreadMessageBtn = findViewById(R.id.get_unread_message_btn);
        offlineClientBtn = findViewById(R.id.set_client_offline_btn);
        endConversationBtn = findViewById(R.id.end_conversation_btn);
        getCurrentAgentInfoBtn = findViewById(R.id.get_current_agent_info_btn);
    }

    private void setListeners() {
        setCurrentIdOnlineBtn.setOnClickListener(this);
        setInputIdOnlineBtn.setOnClickListener(this);
        setCustomizedIdOnlineBtn.setOnClickListener(this);
        getNewIdBtn.setOnClickListener(this);
        setClientInfoBtn.setOnClickListener(this);
        getUnreadMessageBtn.setOnClickListener(this);
        offlineClientBtn.setOnClickListener(this);
        endConversationBtn.setOnClickListener(this);
        getCurrentAgentInfoBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        // 使用当前顾客上线
        if (id == R.id.set_current_client_id_online_btn) {
            MXConfig.registerController(new ControllerImpl(this));
            MXManager.getInstance(this).registerDeviceToken("registertoken,token;token", null);
            Intent intent = new MXIntentBuilder(this).build();
            startActivity(intent);
            // 使用指定 Mixdesk顾客id 上线
        } else if (id == R.id.set_mixdesk_client_id_online_btn) {
            showDialog("输入Mixdesk ID", new EditDialogOnClickListener() {
                @Override
                public void onInput(String clientId) {
                    if (!TextUtils.isEmpty(clientId)) {
                        Intent intent = new MXIntentBuilder(ApiSampleActivity.this)
                                .setClientId(clientId)
                                .build();
                        startActivity(intent);
                        updateId();
                    }
                }
            });
            // 使用 开发者用户id 上线
        } else if (id == R.id.set_customised_id_online_btn) {
            showDialog("输入开发者用户 ID", new EditDialogOnClickListener() {
                @Override
                public void onInput(String customizedId) {
                    if (!TextUtils.isEmpty(customizedId)) {
                        Intent intent = new MXIntentBuilder(ApiSampleActivity.this)
                                .setCustomizedId(customizedId)
                                .build();
                        startActivity(intent);
                        updateId();
                    }
                }
            });
            // 获取一个新的Mixdesk ID
        } else if (id == R.id.get_new_mixdesk_id_btn) {
            MXManager.getInstance(this).createClient(new OnGetMQClientIdCallBackOn() {
                @Override
                public void onSuccess(String mqClientId) {
                    toast("成功复制到剪贴板 :\n" + mqClientId);
                    if (!TextUtils.isEmpty(mqClientId)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            android.content.ClipboardManager mClipboard = (android.content.ClipboardManager) ApiSampleActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                            mClipboard.setPrimaryClip(ClipData.newPlainText("mq_content", mqClientId));
                        } else {
                            ClipboardManager mClipboard = (ClipboardManager) ApiSampleActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                            mClipboard.setText(mqClientId);
                        }
                    }
                }

                @Override
                public void onFailure(int code, String message) {
                    toast(message);
                }
            });
            // 上传自定义信息
        } else if (id == R.id.set_client_info) {
            MXConfig.isShowClientAvatar = true;
            final HashMap<String, String> info = new HashMap<>();
            info.put("name", "富坚义博");
            info.put("avatar", "https://s3.cn-north-1.amazonaws.com.cn/pics.mixdesk.bucket/1dee88eabfbd7bd4");
            info.put("gender", "男");
            info.put("tel", "111111");
            info.put("技能1", "休刊");
            info.put("技能2", "外出取材");
            info.put("技能3", "打麻将");
            info.put("tags", "test1,test2,test3");
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setTitle("上传自定义信息");
            alertBuilder.setMessage("avatar -> https://s3.cn-north-1.amazonaws.com.cn/pics.mixdesk.bucket/1dee88eabfbd7bd4\n" +
                    "name -> 富坚义博\n" +
                    "技能1 -> 休刊\n" +
                    "gender -> 男\n" +
                    "tel -> 111111\n" +
                    "技能2 -> 外出取材\n" +
                    "技能3 -> 打麻将");
            AlertDialog dialog = alertBuilder.create();
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new MXIntentBuilder(ApiSampleActivity.this)
                            .updateClientInfo(info)
                            .build();
                    startActivity(intent);
                }
            });
            dialog.show();
        } else if (id == R.id.get_unread_message_btn) {
            AlertDialog.Builder unreadAlertBuilder = new AlertDialog.Builder(this);
            unreadAlertBuilder.setTitle("注意");
            unreadAlertBuilder.setMessage("退出界面后收到的消息，都将算作未读消息");
            AlertDialog unreadDialog = unreadAlertBuilder.create();
            unreadDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MXManager.getInstance(ApiSampleActivity.this).getUnreadMessages(new OnGetMessageListCallback() {
                        @Override
                        public void onSuccess(List<MXMessage> messageList) {
                            toast("unread message count = " + messageList.size());
                        }

                        @Override
                        public void onFailure(int code, String message) {
                            toast("get unread message failed");
                        }
                    });
                }
            });
            unreadDialog.show();
            // 设置顾客离线
        } else if (id == R.id.set_client_offline_btn) {
            MXManager.getInstance(this).setClientOffline();
            // 结束当前对话
        } else if (id == R.id.end_conversation_btn) {
            MXManager.getInstance(this).endCurrentConversation(new OnEndConversationCallback() {
                @Override
                public void onSuccess() {
                    toast("endCurrentConversation success");
                }

                @Override
                public void onFailure(int code, String message) {
                    toast("endCurrentConversation failed:\n" + message);
                }
            });
        } else if (id == R.id.get_current_agent_info_btn) {
            MXAgent mxAgent = MXManager.getInstance(this).getCurrentAgent();
            if (mxAgent != null) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setTitle("当前客服信息");
                alertBuilder.setMessage("id:" + mxAgent.getId() + "\n" +
                        "name:" + mxAgent.getNickname() + "\n" +
                        "avatar:" + mxAgent.getAvatar() + "\n" +
                        "agentId:" + mxAgent.getAgentId() + "\n");
                AlertDialog dialog = alertBuilder.create();
                dialog.show();
            } else {
                toast("没有当前客服");
            }
        }
    }

    private void updateId() {
        currentIdTv.setText(MXManager.getCurrentClientId());
    }

    private void toast(String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), content, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showDialog(String title, final EditDialogOnClickListener editDialogOnClickListener) {
        final Dialog inputDialog = new Dialog(this, com.mixdesk.mixdesksdk.R.style.MQDialog);
        inputDialog.setCancelable(true);
        inputDialog.setContentView(R.layout.dialog_input);
        TextView titleTv = (TextView) inputDialog.findViewById(R.id.tv_input_title);
        titleTv.setText(title);
        final EditText valueEt = (EditText) inputDialog.findViewById(R.id.et_input_value);
        inputDialog.findViewById(R.id.tv_input_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MXUtils.closeKeyboard(inputDialog);
                inputDialog.dismiss();
                editDialogOnClickListener.onInput(valueEt.getText().toString());
            }
        });
        inputDialog.show();
        MXUtils.openKeyboard(valueEt);
    }

    public interface EditDialogOnClickListener {
        void onInput(String input);
    }

}
