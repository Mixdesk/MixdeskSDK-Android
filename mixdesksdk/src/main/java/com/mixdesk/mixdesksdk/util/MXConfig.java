package com.mixdesk.mixdesksdk.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

import android.view.View;

import com.mixdesk.core.MXManager;
import com.mixdesk.core.MXNotificationMessageConfig;
import com.mixdesk.core.bean.MXNotificationMessage;
import com.mixdesk.core.callback.OnInitCallback;
import com.mixdesk.core.callback.OnNotificationMessageOnClickListener;
import com.mixdesk.mixdesksdk.R;
import com.mixdesk.mixdesksdk.callback.MXActivityLifecycleCallback;
import com.mixdesk.mixdesksdk.callback.MXSimpleActivityLifecyleCallback;
import com.mixdesk.mixdesksdk.callback.OnLinkClickCallback;
import com.mixdesk.mixdesksdk.controller.ControllerImpl;
import com.mixdesk.mixdesksdk.controller.MXController;
import com.mixdesk.mixdesksdk.imageloader.MXImageLoader;

import java.util.HashMap;
import java.util.Map;


public final class MXConfig {
    public static final int DEFAULT = -1;

    public static final class ui {
        public static MQTitleGravity titleGravity = MQTitleGravity.CENTER; // 标题文字对其方式
        @ColorRes
        public static int titleBackgroundResId = DEFAULT; // 标题栏背景颜色
        @ColorRes
        public static int titleTextColorResId = DEFAULT; // 标题栏文字颜色
        @ColorRes
        public static int leftChatBubbleColorResId = DEFAULT; // 左边气泡背景颜色
        @ColorRes
        public static int rightChatBubbleColorResId = DEFAULT; // 右边气泡背景颜色
        @ColorRes
        public static int leftChatTextColorResId = DEFAULT; // 左边气泡文字颜色
        @ColorRes
        public static int rightChatTextColorResId = DEFAULT; // 右边气泡文字颜色
        @DrawableRes
        public static int backArrowIconResId = DEFAULT; // 返回箭头图标资源id
        @ColorRes
        public static int robotMenuItemTextColorResId = DEFAULT; // 机器人菜单消息列表文字颜色
        @ColorRes
        public static int robotMenuTipTextColorResId = DEFAULT; // 机器人菜单消息提示文本颜色
        @ColorRes
        public static int robotEvaluateTextColorResId = DEFAULT; // 机器人消息评价按钮的文字颜色

        public static String titleBackgroundColor = "";
        public static String titleTextColor = "";
        public static String backgroundColor = "";
        public static String leftChatBubbleColor = "";
        public static String rightChatBubbleColor = "";
        public static String leftChatTextColor = "";
        public static String rightChatTextColor = "";
        public static boolean isShowTitle = true; // 是否显示 Title

        public static Bitmap backNavIcon = null; // 返回按钮图标
        public static int backNavWidth = 0; // 返回按钮图标宽度
        public static int backNavHeight = 0; // 返回按钮图标高度
        public static int backNavMarginLeft = 0; // 返回按钮图左侧 margin
        public static String navRightButtonTxt = ""; // 导航栏右侧按钮文字
        public static String navRightButtonImageUrl = null; // 导航栏右侧按钮图标 url
        public static int navRightButtonImageWidth = 0; // 导航栏右侧按钮宽度
        public static int navRightButtonImageHeight = 0; // 导航栏右侧按钮高度
        public static View.OnClickListener navRightButtonOnClickListener; // 导航栏右侧按钮的点击回调

        public static View.OnClickListener navBackButtonOnClickListener; // 返回按钮点击回调

        public enum MQTitleGravity {
            LEFT, CENTER
        }
    }

    public static boolean isVoiceSwitchOpen = true; // 语音开关
    public static boolean isSoundSwitchOpen = true; // 声音开关
    public static boolean isLoadMessagesFromNativeOpen = false; // 加载本地数据开关
    @Deprecated
    public static boolean isEvaluateSwitchOpen = true; // 是否开启评价
    public static boolean isShowClientAvatar = false; // 是否显示客户头像
    public static boolean isShowAgentAvatar = false; // 是否显示客服头像
    public static boolean isPhotoSendOpen = true; // 是否显示发送图片消息按钮
    public static boolean isCameraImageSendOpen = true; // 是否显示发送相机图片消息按钮
    public static boolean isEmojiSendOpen = true; // 是否显示发送 Emoji 表情消息按钮

    public static boolean isCloseSocketAfterDestroy = false; // 是否在退出客服界面时关闭 socket 长连接
    public static String language;

    private static MXActivityLifecycleCallback sActivityLifecycleCallback;
    private static OnLinkClickCallback sOnLinkClickCallback;

    private static MXController sController;

    public static MXController getController(Context context) {
        if (sController == null) {
            synchronized (MXConfig.class) {
                if (sController == null) {
                    sController = new ControllerImpl(context.getApplicationContext());
                }
            }
        }
        return sController;
    }

    public static void registerController(MXController controller) {
        sController = controller;
    }

    public static void setActivityLifecycleCallback(MXActivityLifecycleCallback lifecycleCallback) {
        sActivityLifecycleCallback = lifecycleCallback;
    }

    public static MXActivityLifecycleCallback getActivityLifecycleCallback() {
        if (sActivityLifecycleCallback == null) {
            sActivityLifecycleCallback = new MXSimpleActivityLifecyleCallback();
        }
        return sActivityLifecycleCallback;
    }


    /**
     * 设置链接点击的回调
     * 注意:设置监听回调后,将不再跳转网页.如果需要跳转,开发者需要自行处理,例如: ac
     *
     * @param onLinkClickCallback 回调
     */
    public static void setOnLinkClickCallback(OnLinkClickCallback onLinkClickCallback) {
        MXConfig.sOnLinkClickCallback = onLinkClickCallback;
    }

    public static OnLinkClickCallback getOnLinkClickCallback() {
        return MXConfig.sOnLinkClickCallback;
    }

    public static void setLanguage(String language) {
        MXConfig.language = language;
    }

    @Deprecated
    public static void init(Context context, String appKey, MXImageLoader imageLoader, final OnInitCallback onInitCallBack) {
        MXManager.init(context, appKey, onInitCallBack);
        initDefaultNotificationCardResource(context);
    }

    public static void init(Context context, String appKey, OnInitCallback onInitCallBack) {
        MXManager.init(context, appKey, onInitCallBack);
        initDefaultNotificationCardResource(context);
    }

    private static void initDefaultNotificationCardResource(final Context context) {
        Map<String, Object> resource = new HashMap<>();
        resource.put("notificationCardLayoutId", R.layout.mx_notification_card);
        resource.put("titleTvId", R.id.mx_title_tv);
        resource.put("firstContentTvId", R.id.mx_first_content_tv);
        resource.put("avatarIvId", R.id.mx_title_iv);
        MXNotificationMessageConfig.getInstance().setNotificationCardResource(resource);
        MXNotificationMessageConfig.getInstance().setOnNotificationMessageOnClickListener(new OnNotificationMessageOnClickListener() {
            @Override
            public void onClick(View view, MXNotificationMessage notificationMessage) {
                Intent intent = new MXIntentBuilder(context).build();
                context.startActivity(intent);
            }
        });
    }
}

