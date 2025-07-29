package com.mixdesk.mixdesksdk.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorListenerAdapter;

import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mixdesk.core.MXManager;
import com.mixdesk.core.MXMessageManager;
import com.mixdesk.core.bean.MXAgent;
import com.mixdesk.core.bean.MXMessage;
import com.mixdesk.core.bean.MXEvaluateConfig;
import com.mixdesk.core.callback.OnGetActiveConvCallback;
import com.mixdesk.core.callback.OnGetMessageListCallback;
import com.mixdesk.core.callback.SuccessCallback;
import com.mixdesk.mixdesksdk.R;
import com.mixdesk.mixdesksdk.callback.OnClientOnlineCallback;
import com.mixdesk.mixdesksdk.callback.OnEvaluateConfigCallback;
import com.mixdesk.mixdesksdk.callback.OnFinishCallback;
import com.mixdesk.mixdesksdk.callback.OnGetMessageListCallBack;
import com.mixdesk.mixdesksdk.callback.OnMessageSendCallback;
import com.mixdesk.mixdesksdk.callback.SimpleCallback;
import com.mixdesk.mixdesksdk.chatitem.MXHybridItem;
import com.mixdesk.mixdesksdk.controller.ControllerImpl;
import com.mixdesk.mixdesksdk.controller.MXController;
import com.mixdesk.mixdesksdk.dialog.MXConfirmDialog;
import com.mixdesk.mixdesksdk.dialog.MXEvaluateLevelDialog;
import com.mixdesk.mixdesksdk.dialog.MXListDialog;
import com.mixdesk.mixdesksdk.imageloader.MXImage;
import com.mixdesk.mixdesksdk.imageloader.MXImageLoader;
import com.mixdesk.mixdesksdk.model.Agent;
import com.mixdesk.mixdesksdk.model.AgentChangeMessage;
import com.mixdesk.mixdesksdk.model.BaseMessage;
import com.mixdesk.mixdesksdk.model.EvaluateMessage;
import com.mixdesk.mixdesksdk.model.FileMessage;
import com.mixdesk.mixdesksdk.model.PhotoMessage;
import com.mixdesk.mixdesksdk.model.TipMessage;
import com.mixdesk.mixdesksdk.model.TextMessage;
import com.mixdesk.mixdesksdk.model.VideoMessage;
import com.mixdesk.mixdesksdk.model.VoiceMessage;
import com.mixdesk.mixdesksdk.third.swiperefresh.SwipeRefreshLayout;
import com.mixdesk.mixdesksdk.util.ErrorCode;
import com.mixdesk.mixdesksdk.util.MXAudioPlayerManager;
import com.mixdesk.mixdesksdk.util.MXAudioRecorderManager;
import com.mixdesk.mixdesksdk.util.MXChatAdapter;
import com.mixdesk.mixdesksdk.util.MXConfig;
import com.mixdesk.mixdesksdk.util.MXSimpleTextWatcher;
import com.mixdesk.mixdesksdk.util.MXSoundPoolManager;
import com.mixdesk.mixdesksdk.util.MXTimeUtils;
import com.mixdesk.mixdesksdk.util.MXUtils;
import com.mixdesk.mixdesksdk.widget.MXCustomKeyboardLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MXConversationActivity
        extends Activity
        implements View.OnClickListener,
        MXEvaluateLevelDialog.Callback,
        MXCustomKeyboardLayout.Callback,
        View.OnTouchListener,
        MXHybridItem.OnCallbackListener {
    private static final String TAG = MXConversationActivity.class.getSimpleName();

    // 权限
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    private static final int RECORD_AUDIO_REQUEST_CODE = 2;
    private static final int WRITE_EXTERNAL_STORAGE_AND_CAMERA_REQUEST_CODE = 3;
    private static final int WRITE_EXTERNAL_STORAGE_AND_VIDEO_REQUEST_CODE = 4;
    private static final int VIDEO_REQUEST_CODE = 5;
    private static final int CAMERA_REQUEST_CODE = 6;

    public static final String CLIENT_ID = "clientId";
    public static final String CUSTOMIZED_ID = "customizedId";
    public static final String CLIENT_INFO = "clientInfo";
    public static final String UPDATE_CLIENT_INFO = "updateClientInfo";

    public static final String CURRENT_CLIENT = "CURRENT_CLIENT";

    public static final String PRE_SEND_TEXT = "preSendText";
    public static final String PRE_SEND_IMAGE_PATH = "preSendImagePath";
    public static final String PRE_SEND_PRODUCT_CARD = "preSendProductCard";
    public static final String BOOL_IGNORE_CHECK_OTHER_ACTIVITY = "boolIgnoreCheckOtherActivity";
    public static final int REQUEST_CODE_CAMERA = 0;
    public static final int REQUEST_CODE_PHOTO = 1;
    public static final int REQUEST_CODE_VIDEO = 2;
    public static final int REQUEST_CODE_CHOOSE_VIDEO = 3;
    private static final int MESSAGE_PAGE_COUNT = 30; //消息每页加载数量
    private static final long AUTO_DISMISS_TOP_TIP_TIME = 2000; // TopTip 自动隐藏时间

    private MXController mController;

    // 控件
    private RelativeLayout mTitleRl;
    private RelativeLayout mBackRl;
    private TextView mBackTv;
    private ImageView mBackIv;
    private TextView mTitleTv;
    private TextView mRedirectHumanTv; // 转人工按钮
    private RelativeLayout mChatBodyRl;
    private LinearLayout mChatFootContentLL;
    private TextView mChatFootText;
    private ListView mConversationListView;
    private EditText mInputEt;
    private ImageButton mSendTextBtn;
    private View mEmojiSelectBtn;
    private View mPhotoSelectBtn;
    private View mCameraSelectBtn;
    private View mVideoSelectBtn;
    private View mVoiceBtn;
    private View mEvaluateBtn;
    private ProgressBar mLoadProgressBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View mEmojiSelectIndicator;
    private ImageView mEmojiSelectImg;
    private View mVoiceSelectIndicator;
    private ImageView mVoiceSelectImg;

    private final List<BaseMessage> mChatMessageList = new ArrayList<>();
    private MXChatAdapter mChatMsgAdapter;
    private MessageReceiver mMessageReceiver;
    private NetworkChangeReceiver mNetworkChangeReceiver;
    // 改变title状态
    private Handler mHandler;
    private MXSoundPoolManager mSoundPoolManager;

    // 是否已经加载数据的标识
    private boolean mHasLoadData = false;
    private boolean isPause;
    private boolean isDestroy;

    // 是否被拉黑
    private boolean isBlackState;

    private Agent mCurrentAgent; // 当前客服

    private MXCustomKeyboardLayout mCustomKeyboardLayout;
    private MXEvaluateLevelDialog mEvaluateDialog;
    private String mCameraPicPath;
    private Uri mCameraPicUri;
    private String mVideoPath;
    private Uri mVideoUri;

    private String mConversationId;

    private TextView mTopTipViewTv;
    private Runnable mAutoDismissTopTipRunnable;
    private View mNetStatusTopView;
    private String currentNetStatus = "connect";
    private View mRequestPermTopView;

    // 上一次发送机器人消息的时间戳
    private long mLastSendRobotMessageTime;
    private boolean mIsAllocatingAgent;
    private boolean isRequestOnlineLoading = false;
    private final List<BaseMessage> delaySendList = new ArrayList<>();

    private boolean isPopRecordPermissionTipDialog = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MXUtils.updateLanguage(this);
        mController = MXConfig.getController(this);
        mController.onConversationOpen();
        if (savedInstanceState != null) {
            mCameraPicPath = savedInstanceState.getString("mCameraPicPath");
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 保持屏幕长亮
        setContentView(R.layout.mx_activity_conversation);

        findViews();
        init();
        setListeners();
        registerReceiver();

        // 恢复之前未发送的文本消息
        String clientId = mController.getCurrentClientId();
        if (!TextUtils.isEmpty(clientId)) {
            String text = MXUtils.getUnSendTextMessage(this, clientId);
            mInputEt.setText(text);
            mInputEt.setSelection(mInputEt.getText().length());
        }

        MXConfig.getActivityLifecycleCallback().onActivityCreated(this, savedInstanceState);

        MXManager.getInstance(getApplicationContext()).getActiveConv(new OnGetActiveConvCallback() {
            @Override
            public void onSuccess(boolean isConvActive, boolean queueing, int position) {
                refreshConfig(isConvActive);
            }

            @Override
            public void onFailure(int code, String message) {
                refreshConfig(false);
            }
        });
    }

    private void refreshConfig(boolean isConvActive) {
        // 已经分配了对话的情况下，不再刷新配置
//        if (isConvActive) {
//            applyAfterRefreshConfig();
//            return;
//        }
        // 刷新配置
        mController.refreshEnterpriseConfig(new SimpleCallback() {
            @Override
            public void onSuccess() {
                MXManager mxManager = MXManager.getInstance(getApplicationContext());
                // 已分配客服了，就继续初始化聊天界面
                MXAgent agent = mxManager.getCurrentAgent();
                if (agent != null) {
                    applyAfterRefreshConfig();
                    return;
                }
                // 从询前表单、信息收集界面来的，就直接加载聊天消息
                if (getIntent() != null && getIntent().getBooleanExtra(BOOL_IGNORE_CHECK_OTHER_ACTIVITY, false)) {
                    applyAfterRefreshConfig();
                    return;
                }


                // 都不是，就继续初始化聊天界面
                applyAfterRefreshConfig();
            }

            @Override
            public void onFailure(int code, String message) {
                applyAfterRefreshConfig();
            }
        });
    }

    private void applyAfterRefreshConfig() {
        applyCustomUIConfig();

        // 服务器配置优先
        boolean isPhotoMsgOpen = mController.getEnterpriseConfig().isPhotoMsgOpen && MXConfig.isPhotoSendOpen;
        boolean isCameraMsgOpen = mController.getEnterpriseConfig().isPhotoMsgOpen && MXConfig.isCameraImageSendOpen;
        mPhotoSelectBtn.setVisibility(isPhotoMsgOpen ? View.VISIBLE : View.GONE);
        mCameraSelectBtn.setVisibility(isCameraMsgOpen ? View.VISIBLE : View.GONE);
        mVideoSelectBtn.setVisibility(mController.getEnterpriseConfig().isVideoMsgOpen ? View.VISIBLE : View.GONE);

        boolean ipAllowed = mController.getEnterpriseConfig().ip_allowed;
        if (!ipAllowed) {
            mChatFootContentLL.setVisibility(View.GONE);
            mChatFootText.setVisibility(View.VISIBLE);
        }else {
            mChatFootContentLL.setVisibility(View.VISIBLE);
            mChatFootText.setVisibility(View.GONE);
        }

        // 设置顾客上线，请求分配客服
        setClientOnline();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("mCameraPicPath", mCameraPicPath);
        MXConfig.getActivityLifecycleCallback().onActivitySaveInstanceState(this, outState);
    }

    /**
     * 如果配置了界面相关的 config，在这里应用
     */
    private void applyCustomUIConfig() {
        if (!TextUtils.isEmpty(MXConfig.ui.backgroundColor)) {
            findViewById(R.id.root_rl).setBackgroundColor(Color.parseColor(MXConfig.ui.backgroundColor));
            mConversationListView.setBackgroundColor(Color.parseColor(MXConfig.ui.backgroundColor));
        }
        if (MXConfig.ui.backNavIcon != null) {
            mBackIv.setImageBitmap(MXConfig.ui.backNavIcon);
        }
        if (MXConfig.ui.backNavWidth != 0) {
            RelativeLayout.LayoutParams backIvParams = (RelativeLayout.LayoutParams) mBackIv.getLayoutParams();
            backIvParams.width = MXUtils.dip2px(this, MXConfig.ui.backNavWidth);
            mBackIv.setLayoutParams(backIvParams);
        }
        if (MXConfig.ui.backNavHeight != 0) {
            RelativeLayout.LayoutParams backIvParams = (RelativeLayout.LayoutParams) mBackIv.getLayoutParams();
            backIvParams.height = MXUtils.dip2px(this, MXConfig.ui.backNavHeight);
            mBackIv.setLayoutParams(backIvParams);
        }
        if (MXConfig.ui.backNavMarginLeft != 0) {
            RelativeLayout.LayoutParams backIvParams = (RelativeLayout.LayoutParams) mBackIv.getLayoutParams();
            backIvParams.leftMargin = MXUtils.dip2px(this, MXConfig.ui.backNavMarginLeft);
            mBackIv.setLayoutParams(backIvParams);
        }
        if (!TextUtils.isEmpty(MXConfig.ui.navRightButtonTxt)) {
            TextView rightTv = findViewById(R.id.right_tv);
            rightTv.setVisibility(View.VISIBLE);
            rightTv.setText(MXConfig.ui.navRightButtonTxt);
            if (MXConfig.ui.navRightButtonOnClickListener != null) {
                rightTv.setOnClickListener(MXConfig.ui.navRightButtonOnClickListener);
            }
        }
        if (!TextUtils.isEmpty(MXConfig.ui.navRightButtonImageUrl)) {
            ImageView rightIv = findViewById(R.id.right_iv);
            rightIv.setVisibility(View.VISIBLE);
            int defaultWidthHeight = MXUtils.dip2px(this, 32);
            MXImage.displayImage(this, rightIv, MXConfig.ui.navRightButtonImageUrl, R.drawable.mx_ic_holder_light, R.drawable.mx_ic_holder_light, defaultWidthHeight, defaultWidthHeight, new MXImageLoader.MQDisplayImageListener() {
                @Override
                public void onSuccess(View view, String path) {

                }
            });
            if (MXConfig.ui.navRightButtonImageWidth != 0) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rightIv.getLayoutParams();
                params.width = MXUtils.dip2px(this, MXConfig.ui.navRightButtonImageWidth);
                rightIv.setLayoutParams(params);
            }
            if (MXConfig.ui.navRightButtonImageHeight != 0) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rightIv.getLayoutParams();
                params.height = MXUtils.dip2px(this, MXConfig.ui.navRightButtonImageHeight);
                rightIv.setLayoutParams(params);
            }
            if (MXConfig.ui.navRightButtonOnClickListener != null) {
                rightIv.setOnClickListener(MXConfig.ui.navRightButtonOnClickListener);
            }
        }
        if (MXConfig.DEFAULT != MXConfig.ui.backArrowIconResId) {
            mBackIv.setImageResource(MXConfig.ui.backArrowIconResId);
        }

        // 处理标题栏背景色
        MXUtils.applyCustomUITintDrawable(mTitleRl, android.R.color.white, R.color.mx_activity_title_bg, MXConfig.ui.titleBackgroundResId);

        // 处理标题、返回、返回箭头颜色
        MXUtils.applyCustomUITextAndImageColor(R.color.mx_activity_title_textColor, MXConfig.ui.titleTextColorResId, null, mBackTv, mTitleTv, mRedirectHumanTv);

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

        // 处理底部功能按钮图片
        MXUtils.tintPressedIndicator(findViewById(R.id.photo_select_iv), R.drawable.mx_ic_image_normal, R.drawable.mx_ic_image_active);
        MXUtils.tintPressedIndicator(findViewById(R.id.camera_select_iv), R.drawable.mx_ic_camera_normal, R.drawable.mx_ic_camera_active);
        MXUtils.tintPressedIndicator(findViewById(R.id.evaluate_select_iv), R.drawable.mx_ic_evaluate_normal, R.drawable.mx_ic_evaluate_active);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 在已经加载数据的情况下,重新进入界面,需要再次打开服务
        if (mHasLoadData) {
            mController.openService();
        }
        MXConfig.getActivityLifecycleCallback().onActivityStarted(this);
        mController.onConversationStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPause = false;
        MXConfig.getActivityLifecycleCallback().onActivityResumed(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPause = true;
        MXConfig.getActivityLifecycleCallback().onActivityPaused(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mChatMsgAdapter != null) {
            mChatMsgAdapter.stopPlayVoice();
            MXAudioPlayerManager.release();
        }
        if (mChatMessageList != null && mChatMessageList.size() > 0) {
            mController.saveConversationOnStopTime(mChatMessageList.get(mChatMessageList.size() - 1).getCreatedOn());
        } else {
            mController.saveConversationOnStopTime(System.currentTimeMillis());
        }
        MXConfig.getActivityLifecycleCallback().onActivityStopped(this);
        mController.onConversationStop();
    }

    @Override
    protected void onDestroy() {
        MXUtils.closeKeyboard(this);
        try {
            mSoundPoolManager.release();
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
            unregisterReceiver(mNetworkChangeReceiver);
        } catch (Exception e) {
            //有些时候会出现未注册就取消注册的情况，暂时不知道为什么
        }
        isDestroy = true;
        cancelAllDownload();
        mController.onConversationClose();

        // 保存未发送的文本消息
        String clientId = mController.getCurrentClientId();
        if (!TextUtils.isEmpty(clientId)) {
            String msg = mInputEt.getText().toString().trim();
            MXUtils.setUnSendTextMessage(this, clientId, msg);
        }

        MXConfig.getActivityLifecycleCallback().onActivityDestroyed(this);
        // 移除自定义
        if (MXConfig.ui.backNavIcon != null) {
            mBackIv.setImageBitmap(null);
        }

        if (!TextUtils.isEmpty(MXConfig.ui.navRightButtonTxt)) {
            TextView rightTv = findViewById(R.id.right_tv);
            rightTv.setOnClickListener(null);
        }

        if (MXConfig.isCloseSocketAfterDestroy) {
            MXManager.getInstance(this).closeService();
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //如果在表情选择的时候按下 Back 键，隐藏表情 panel
        if (keyCode == KeyEvent.KEYCODE_BACK && mCustomKeyboardLayout.isEmotionKeyboardVisible()) {
            mCustomKeyboardLayout.closeEmotionKeyboard();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (MXConfig.ui.navBackButtonOnClickListener != null) {
            MXConfig.ui.navBackButtonOnClickListener.onClick(null);
        }
    }

    private void init() {
        if (mController == null) {
            mController = new ControllerImpl(this);
        }
        MXTimeUtils.init(this);
        // 初始化路径
        if (TextUtils.isEmpty(MXUtils.DOWNLOAD_DIR)) {
            File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (externalFilesDir != null) {
                MXUtils.DOWNLOAD_DIR = externalFilesDir.getAbsolutePath();
            }
        }

        // handler
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
            }
        };

        mSoundPoolManager = MXSoundPoolManager.getInstance(this);
        mChatMsgAdapter = new MXChatAdapter(MXConversationActivity.this, mChatMessageList, mConversationListView);
        mConversationListView.setAdapter(mChatMsgAdapter);

        mVoiceBtn.setVisibility(MXConfig.isVoiceSwitchOpen ? View.VISIBLE : View.GONE);
        mEmojiSelectBtn.setVisibility(MXConfig.isEmojiSendOpen ? View.VISIBLE : View.GONE);
        mEvaluateBtn.setVisibility(View.GONE); // 无论是否配置是否显示，这里都隐藏，然后在分配对话成功后，再根据配置是否显示

        mCustomKeyboardLayout.init(this, mInputEt, this);
        isDestroy = false;
        mTitleTv.setVisibility(MXConfig.ui.isShowTitle ? View.VISIBLE : View.INVISIBLE);
    }

    private void findViews() {
        mTitleRl = findViewById(R.id.title_rl);
        mBackRl = findViewById(R.id.back_rl);
        mBackTv = findViewById(R.id.back_tv);
        mBackIv = findViewById(R.id.back_iv);
        mRedirectHumanTv = findViewById(R.id.redirect_human_tv);
        mChatBodyRl = findViewById(R.id.chat_body_rl);

        mChatFootContentLL = findViewById(R.id.chat_foot_content_ll);
        mChatFootText = findViewById(R.id.chat_foot_text);

        mConversationListView = findViewById(R.id.messages_lv);
        mInputEt = findViewById(R.id.input_et);
        mEmojiSelectBtn = findViewById(R.id.emoji_select_btn);
        mCustomKeyboardLayout = findViewById(R.id.customKeyboardLayout);
        mSendTextBtn = findViewById(R.id.send_text_btn);
        mPhotoSelectBtn = findViewById(R.id.photo_select_btn);
        mCameraSelectBtn = findViewById(R.id.camera_select_btn);
        mVideoSelectBtn = findViewById(R.id.video_select_btn);
        mVoiceBtn = findViewById(R.id.mic_select_btn);
        mEvaluateBtn = findViewById(R.id.evaluate_select_btn);
        mLoadProgressBar = findViewById(R.id.progressbar);
        mTitleTv = findViewById(R.id.title_tv);
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        mEmojiSelectIndicator = findViewById(R.id.emoji_select_indicator);
        mEmojiSelectImg = findViewById(R.id.emoji_select_img);
        mVoiceSelectIndicator = findViewById(R.id.conversation_voice_indicator);
        mVoiceSelectImg = findViewById(R.id.conversation_voice_img);
    }

    private void setListeners() {
        mBackRl.setOnClickListener(this);
        mRedirectHumanTv.setOnClickListener(this);
        mSendTextBtn.setOnClickListener(this);
        mPhotoSelectBtn.setOnClickListener(this);
        mCameraSelectBtn.setOnClickListener(this);
        mVideoSelectBtn.setOnClickListener(this);
        mVoiceBtn.setOnClickListener(this);
        mEvaluateBtn.setOnClickListener(this);
        // 绑定 EditText 的监听器
        mInputEt.addTextChangedListener(inputTextWatcher);
        mInputEt.setOnTouchListener(this);
        mInputEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mSendTextBtn.performClick();
                    MXUtils.closeKeyboard(MXConversationActivity.this);
                    return true;
                }
                return false;
            }
        });
        // 表情
        mEmojiSelectBtn.setOnClickListener(this);
        // 对话列表，单击「隐藏键盘」、「表情 panel」
        mConversationListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if (MotionEvent.ACTION_DOWN == arg1.getAction()) {
                    mCustomKeyboardLayout.closeAllKeyboard();
                    hideEmojiSelectIndicator();
                    hideVoiceSelectIndicator();
                }
                return false;
            }
        });
        // 添加长按复制功能
        mConversationListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Object isRichText = arg1.getTag();
                String content = mChatMessageList.get(arg2).getContent();
                if (!TextUtils.isEmpty(content)) {
                    // 如果是富文本，需要移除标签后再复制
                    if (isRichText != null) {
                        try {
                            JSONArray contentArray = new JSONArray(content);
                            JSONObject contentObj = contentArray.getJSONObject(0);
                            content = contentObj.getString("body");
                        } catch (Exception e) {
                            // ignore
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            content = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY).toString().trim();
                        } else {
                            content = Html.fromHtml(content).toString().trim();
                        }
                    }
                    MXUtils.clip(MXConversationActivity.this, content);
                    MXUtils.show(MXConversationActivity.this, R.string.mx_copy_success);
                    return true;
                }
                return false;
            }
        });
        // 下拉刷新
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (MXConfig.isLoadMessagesFromNativeOpen) {
                    loadMoreDataFromDatabase();
                } else {
                    loadMoreDataFromService();
                }
            }
        });
    }

    /**
     * 注册广播
     */
    private void registerReceiver() {
        // 注册消息接收
        mMessageReceiver = new MessageReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MXMessageManager.ACTION_AGENT_INPUTTING);
        intentFilter.addAction(MXMessageManager.ACTION_NEW_MESSAGE_RECEIVED);
        intentFilter.addAction(MXMessageManager.ACTION_INVITE_EVALUATION);
        intentFilter.addAction(MXMessageManager.ACTION_AGENT_CHANGE_EVENT);
        intentFilter.addAction(MXMessageManager.ACTION_AGENT_STATUS_UPDATE_EVENT);
        intentFilter.addAction(MXMessageManager.ACTION_BLACK_ADD);
        intentFilter.addAction(MXMessageManager.ACTION_BLACK_DEL);
        intentFilter.addAction(MXMessageManager.ACTION_QUEUEING_INIT_CONV);
        intentFilter.addAction(MXMessageManager.ACTION_END_CONV_AGENT);
        intentFilter.addAction(MXMessageManager.ACTION_END_CONV_TIMEOUT);
        intentFilter.addAction(MXMessageManager.ACTION_SOCKET_OPEN);
        intentFilter.addAction(MXMessageManager.ACTION_SOCKET_RECONNECT);
        intentFilter.addAction(MXMessageManager.ACTION_RECALL_MESSAGE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, intentFilter);

        // 网络监听
        mNetworkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkChangeReceiver, mFilter);
    }

    /**
     * 将 title 改为 正在输入
     */
    protected void changeTitleToInputting() {
        mTitleTv.setText(getResources().getString(R.string.mx_title_inputting));

        updateAgentOnlineOfflineStatusAndRedirectHuman();
    }

    /**
     * 将 title 改为 正在分配客服
     */
    protected void changeTitleToAllocatingAgent() {
        mTitleTv.setText(getResources().getString(R.string.mx_allocate_agent));

        hiddenAgentStatusAndRedirectHuman();
    }

    /**
     * 将 title 改为没有网络状态
     */
    protected void changeTitleToNetErrorState() {
        mTitleTv.setText(getResources().getString(R.string.mx_net_status_not_work_title));

        hiddenAgentStatusAndRedirectHuman();
    }

    /**
     * 将 title 改为未知错误状态
     */
    protected void changeTitleToUnknownErrorState() {
        mTitleTv.setText(getResources().getString(R.string.mx_title_unknown_error));

        hiddenAgentStatusAndRedirectHuman();
    }

    /**
     * 添加 转接客服 的消息 Tip 到列表
     *
     * @param agentNickName 客服名字
     */
    protected void addDirectAgentMessageTip(String agentNickName) {
        AgentChangeMessage agentChangeMessage = new AgentChangeMessage();
        agentChangeMessage.setAgentNickname(agentNickName);
        mChatMessageList.add(mChatMessageList.size(), agentChangeMessage);
        mChatMsgAdapter.notifyDataSetChanged();
    }

    /**
     * 添加 被拉黑 的消息 Tip 到列表
     */
    protected void addBlacklistTip(int blackTipRes) {
        isBlackState = true;
//        changeTitleToNoAgentState();
        BaseMessage blacklistMessage = new BaseMessage();
        blacklistMessage.setItemViewType(BaseMessage.TYPE_TIP);
        blacklistMessage.setContent(getResources().getString(blackTipRes));
        mChatMsgAdapter.addMQMessage(blacklistMessage);
    }

    /**
     * 弹出顶部 Tip
     *
     * @param contentRes tip 文本内容的资源 id
     */
    private void popTopTip(final int contentRes) {
        if (mTopTipViewTv == null) {
            mTopTipViewTv = (TextView) getLayoutInflater().inflate(R.layout.mx_top_pop_tip, null);
            mTopTipViewTv.setText(contentRes);
            int height = getResources().getDimensionPixelOffset(R.dimen.mx_top_tip_height);
            mChatBodyRl.addView(mTopTipViewTv, ViewGroup.LayoutParams.MATCH_PARENT, height);
            ViewCompat.setTranslationY(mTopTipViewTv, -height); // 初始化位置
            ViewCompat.animate(mTopTipViewTv).translationY(0).setDuration(300).start();
            if (mAutoDismissTopTipRunnable == null) {
                mAutoDismissTopTipRunnable = new Runnable() {
                    @Override
                    public void run() {
                        popTopTip(contentRes);
                    }
                };
            }
            mHandler.postDelayed(mAutoDismissTopTipRunnable, AUTO_DISMISS_TOP_TIP_TIME);
        } else {
            mHandler.removeCallbacks(mAutoDismissTopTipRunnable);
            ViewCompat.animate(mTopTipViewTv).translationY(-mTopTipViewTv.getHeight()).setListener(new ViewPropertyAnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(View view) {
                    mChatBodyRl.removeView(mTopTipViewTv);
                    mTopTipViewTv = null;
                }
            }).setDuration(300).start();
        }
    }

    // net_not_work、socket_reconnect
    private void addNetStatusTopTip(String status) {
        try {
            // 如果没有网络，都处理成没网状态
            if (!MXUtils.isNetworkAvailable(this)) {
                status = "net_not_work";
            }
            // 状态没变化，不处理
            if (TextUtils.equals(status, currentNetStatus)) {
                return;
            }
            this.currentNetStatus = status;
            if (mNetStatusTopView == null) {
                mNetStatusTopView = getLayoutInflater().inflate(R.layout.mx_net_status_top_pop_tip, null);
                int height = getResources().getDimensionPixelOffset(R.dimen.mx_top_tip_height);
                mChatBodyRl.addView(mNetStatusTopView, ViewGroup.LayoutParams.MATCH_PARENT, height);
                ViewCompat.setTranslationY(mNetStatusTopView, -height); // 初始化位置
                ViewCompat.animate(mNetStatusTopView).translationY(0).setDuration(300).start();
            }
            ImageView iconIv = mNetStatusTopView.findViewById(R.id.icon_iv);
            TextView contentTv = mNetStatusTopView.findViewById(R.id.content_tv);
            iconIv.clearColorFilter();
            // 没有网络
            if (TextUtils.equals(status, "net_not_work")) {
                contentTv.setText(R.string.mx_title_net_not_work);
                iconIv.setColorFilter(getResources().getColor(R.color.mx_error_primary));
                mNetStatusTopView.setBackgroundResource(R.color.mx_error_light);
            }
            // 正在重连
            else if (TextUtils.equals(status, MXMessageManager.ACTION_SOCKET_RECONNECT)) {
                // 更改标题
                mTitleTv.setText(getResources().getString(R.string.mx_net_status_reconnect_title));
                contentTv.setText(R.string.mx_net_status_reconnect);
                iconIv.setColorFilter(getResources().getColor(R.color.mx_warning_primary));
                mNetStatusTopView.setBackgroundResource(R.color.mx_warning_light);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeNetStatusTopTip() {
        if (mNetStatusTopView != null) {
            try {
                mChatBodyRl.removeView(mNetStatusTopView);
                mNetStatusTopView = null;

                // 断网后，返回重新进入， 又有网了刷新 Agent
                setCurrentAgent(mController.getCurrentAgent());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.currentNetStatus = "normal";
    }

    /**
     * 设置当前agent
     *
     * @param newAgent
     */
    private void setCurrentAgent(Agent newAgent) {
        Agent oldAgent = mCurrentAgent;
        mCurrentAgent = newAgent;

        // 没有客服接待时，需要处理的逻辑
        if (mCurrentAgent == null) {
        } else {
            String agentName = newAgent.getNickname();
            // 兼容数据
            if (TextUtils.isEmpty(agentName) || TextUtils.equals(agentName, "null")) {
                agentName = getResources().getString(R.string.mx_title_default);
            }
            mTitleTv.setText(agentName);
            // 调小字号，尽量显示全部内容
            if (!TextUtils.isEmpty(newAgent.getNickname())) {
                if (newAgent.getNickname().length() >= 16) {
                    mTitleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                } else {
                    mTitleTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.mx_titlebar_textSize));
                }
            }
            updateAgentOnlineOfflineStatusAndRedirectHuman();
        }
    }

    /**
     * 给列表添加不同对话的分隔线
     *
     * @param mChatMessageList
     */
    private void refreshConversationDivider(List<BaseMessage> mChatMessageList) {
        if (mChatMessageList.size() > 1) {
            Iterator<BaseMessage> messageIterator = mChatMessageList.iterator();
            while (messageIterator.hasNext()) {
                BaseMessage baseMessage = messageIterator.next();
                if (baseMessage.getItemViewType() == BaseMessage.TYPE_CONV_DIVIDER) {
                    messageIterator.remove();
                }
            }
            for (int i = mChatMessageList.size() - 1; i > 0; i--) {
                BaseMessage preMessage = mChatMessageList.get(i);
                BaseMessage message = mChatMessageList.get(i - 1);
                if (preMessage.getConversationId() != message.getConversationId() && preMessage.getConversationId() != 0 && message.getConversationId() != 0) {
                    BaseMessage convDividerMessage = new BaseMessage();
                    convDividerMessage.setCreatedOn(message.getCreatedOn());
                    convDividerMessage.setItemViewType(BaseMessage.TYPE_CONV_DIVIDER);
                    mChatMessageList.add(i, convDividerMessage);
                }
            }
        }
    }

    /**
     * 从服务器获取更多消息并加载
     */
    private void loadMoreDataFromService() {
        // 最早消息的创建时间
        long lastMessageCreateOn = System.currentTimeMillis();
        if (mChatMessageList.size() > 0)
            lastMessageCreateOn = mChatMessageList.get(0).getCreatedOn();
        // 获取该时间之前的消息
        mController.getMessageFromService(lastMessageCreateOn, MESSAGE_PAGE_COUNT, new OnGetMessageListCallBack() {
            @Override
            public void onSuccess(final List<BaseMessage> messageList) {
                // 根据设置，过滤语音消息
                cleanMessages(messageList);
                if (mController.getEnterpriseConfig().isHideConversationHistory) {
                    keepCurrentConvMessageList(messageList);
                }
                //添加时间戳
                MXTimeUtils.refreshMQTimeItem(messageList);
                // 添加 ConDivider
                refreshConversationDivider(messageList);
                mChatMsgAdapter.loadMoreMessage(cleanDupMessages(mChatMessageList, messageList));
                mConversationListView.setSelection(messageList.size());
                mSwipeRefreshLayout.setRefreshing(false);
                // 没有消息后，禁止下拉加载
                if (messageList.size() == 0) {
                    mSwipeRefreshLayout.setEnabled(false);
                }
            }

            @Override
            public void onFailure(int code, String responseString) {
                mChatMsgAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    /**
     * 从数据库更多消息并加载
     */
    private void loadMoreDataFromDatabase() {
        // 最早消息的创建时间
        long lastMessageCreateOn = System.currentTimeMillis();
        if (mChatMessageList.size() > 0)
            lastMessageCreateOn = mChatMessageList.get(0).getCreatedOn();
        // 获取该时间之前的消息
        mController.getMessagesFromDatabase(lastMessageCreateOn, MESSAGE_PAGE_COUNT, new OnGetMessageListCallBack() {
            @Override
            public void onSuccess(final List<BaseMessage> messageList) {
                // 根据设置，过滤语音消息
                cleanMessages(messageList);
                //添加时间戳
                MXTimeUtils.refreshMQTimeItem(messageList);
                mChatMsgAdapter.loadMoreMessage(cleanDupMessages(mChatMessageList, messageList));
                mConversationListView.setSelection(messageList.size());
                mSwipeRefreshLayout.setRefreshing(false);
                // 没有消息后，禁止下拉加载
                if (messageList.size() == 0) {
                    mSwipeRefreshLayout.setEnabled(false);
                }
            }

            @Override
            public void onFailure(int code, String responseString) {
                mChatMsgAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    /**
     * 过滤掉列表存在的消息
     *
     * @param messageList    列表中的消息
     * @param newMessageList 加载的新消息
     * @return
     */
    private List<BaseMessage> cleanDupMessages(List<BaseMessage> messageList, List<BaseMessage> newMessageList) {
        Iterator<BaseMessage> iterator = newMessageList.iterator();
        while (iterator.hasNext()) {
            BaseMessage newMessage = iterator.next();
            if (messageList.contains(newMessage)) {
                iterator.remove();
            }
        }
        return newMessageList;
    }

    /**
     * 转人工按钮
     */
    private void clickTransferHumanBtn() {
        MXManager.getInstance(getApplicationContext()).clickTransferHuman(mConversationId);
    }

    /**
     * 设置顾客上线
     */
    private void setClientOnline() {
        if (isRequestOnlineLoading) {
            return;
        }

        isRequestOnlineLoading = true;
        if (mCurrentAgent == null) {
            mIsAllocatingAgent = true;

            // Title 显示正在分配客服
            changeTitleToAllocatingAgent();

            // 从 intent 获取 clientId、customizedId 和 clientInfo
            Intent intent = getIntent();
            String clientId = null;
            String customizedId = null;
            if (intent != null) {
                clientId = getIntent().getStringExtra(CLIENT_ID);
                customizedId = getIntent().getStringExtra(CUSTOMIZED_ID);
            }

            // 上线
            mController.setCurrentClientOnline(clientId, customizedId, new OnClientOnlineCallback() {

                @Override
                public void onSuccess(Agent agent, String conversationId, List<BaseMessage> conversationMessageList, boolean isShowTransferHumanBtn) {
                    mIsAllocatingAgent = false;

                    setCurrentAgent(agent);

                    mConversationId = conversationId;
                    mMessageReceiver.setConversationId(conversationId);

                    // 根据设置，过滤语音消息
                    cleanMessages(conversationMessageList);
                    if (mController.getEnterpriseConfig().isHideConversationHistory) {
                        keepCurrentConvMessageList(conversationMessageList);
                    }

                    mChatMessageList.clear();
                    mChatMessageList.addAll(conversationMessageList);
                    setOrUpdateClientInfo();

                    loadData();
                    // 分配成功后，根据配置，判断是否显示评价按钮
                    mEvaluateBtn.setVisibility(View.VISIBLE);
                    mRedirectHumanTv.setVisibility(isShowTransferHumanBtn ? View.VISIBLE : View.GONE);
                    // 发送待发送的消息
                    sendDelayMessages();
                    isRequestOnlineLoading = false;
                }

                @Override
                public void onFailure(final int code, final String message) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mIsAllocatingAgent = false;

                            if (ErrorCode.NET_NOT_WORK == code) {
                                changeTitleToNetErrorState();
                            } else if (ErrorCode.NO_AGENT_ONLINE == code) {
                                setCurrentAgent(null);
                                // 没有分配到客服，也根据设置是否上传顾客信息
                                setOrUpdateClientInfo();
                            } else if (ErrorCode.BLACKLIST == code) {
                                setCurrentAgent(null);
                                isBlackState = true;
                            } else if (ErrorCode.CANCEL == code) {
                                // 请求取消
                            } else {
                                changeTitleToUnknownErrorState();
                                // 使用对话框显示完整错误信息
                                showErrorMessage(code, message);
                            }
                            // 如果没有加载数据，则加载数据
                            if (!mHasLoadData) {
                                getMessageDataFromDatabaseAndLoad(null);
                            }
                            if (ErrorCode.NO_AGENT_ONLINE == code) {
                                // 发送待发送的消息
                                sendDelayMessages();
                            }
                            isRequestOnlineLoading = false;
                        }
                    });
                }
            });
        } else {
            setCurrentAgent(mCurrentAgent);
            isRequestOnlineLoading = false;
        }
    }

    /**
     * 发送延迟消息
     */
    private void sendDelayMessages() {
        if (delaySendList.size() != 0) {
            for (BaseMessage delaySendMessage : delaySendList) {
                delaySendMessage.setCreatedOn(System.currentTimeMillis()); // 更新消息时间
                sendMessage(delaySendMessage);
            }
            delaySendList.clear();
        }
    }

    /**
     * 根据设置是否上传或者更新顾客信息
     */
    private void setOrUpdateClientInfo() {
        if (getIntent() != null) {
            Serializable clientInfoSerializable = getIntent().getSerializableExtra(CLIENT_INFO);
            if (clientInfoSerializable != null) {
                HashMap<String, String> clientInfo = (HashMap<String, String>) clientInfoSerializable;
                mController.setClientInfo(clientInfo, null);
            }
            Serializable updateClientInfoSerializable = getIntent().getSerializableExtra(UPDATE_CLIENT_INFO);
            if (updateClientInfoSerializable != null) {
                HashMap<String, String> clientInfo = (HashMap<String, String>) updateClientInfoSerializable;
                mController.updateClientInfo(clientInfo, null);
            }
        }
    }

    /**
     * 检查是否需要切换当前顾客
     *
     * @param onFinishCallback
     */
    private void checkIfNeedUpdateClient(final OnFinishCallback onFinishCallback) {
        // 从 intent 获取 clientId、customizedId 和 clientInfo
        Intent intent = getIntent();
        String clientId = null;
        String customizedId = null;
        if (intent != null) {
            clientId = getIntent().getStringExtra(CLIENT_ID);
            customizedId = getIntent().getStringExtra(CUSTOMIZED_ID);
        }
        // 如果有传 id，要切换顾客身份
        if (!TextUtils.isEmpty(clientId) || !TextUtils.isEmpty(customizedId)) {
            String clientOrCustomizedId = TextUtils.isEmpty(clientId) ? customizedId : clientId;
            MXManager.getInstance(this).setCurrentClient(clientOrCustomizedId, new SuccessCallback() {
                @Override
                public void onSuccess() {
                    onFinishCallback.onFinish();
                }

                @Override
                public void onFailure(int code, String message) {
                    onFinishCallback.onFinish();
                }
            });
        } else {
            onFinishCallback.onFinish();
        }
    }

    /**
     * 拉取最新消息，并从数据库加载显示
     * PS：注意切换用户
     */
    private void getMessageFromServiceAndLoad() {
        checkIfNeedUpdateClient(new OnFinishCallback() {
            @Override
            public void onFinish() {
                // 先通过获取未读接口，从服务器拉取最新消息, 不用关心结果，有新消息会保存到本地数据库，加载消息的时候会取出来
                MXManager.getInstance(MXConversationActivity.this).getUnreadMessages(new OnGetMessageListCallback() {
                    @Override
                    public void onSuccess(List<MXMessage> messageList) {
                        getMessageDataFromDatabaseAndLoad(null);
                    }

                    @Override
                    public void onFailure(int code, String message) {
                        getMessageDataFromDatabaseAndLoad(null);
                    }
                });
            }
        });
    }

    /**
     * 从数据库获取消息并加载
     */
    private void getMessageDataFromDatabaseAndLoad(@Nullable OnFinishCallback onFinishCallback) {
        // 从数据库获取数据
        mController.getMessagesFromDatabase(System.currentTimeMillis(), MESSAGE_PAGE_COUNT, new OnGetMessageListCallBack() {

            @Override
            public void onSuccess(List<BaseMessage> messageList) {
                // 根据设置，过滤语音消息
                cleanMessages(messageList);
                if (mController.getEnterpriseConfig().isHideConversationHistory) {
                    keepCurrentConvMessageList(messageList);
                }
                mChatMessageList.addAll(messageList);
                loadData();
                if (onFinishCallback != null) {
                    onFinishCallback.onFinish();
                }
            }

            @Override
            public void onFailure(int code, String responseString) {
                if (onFinishCallback != null) {
                    onFinishCallback.onFinish();
                }
            }
        });
    }

    private void keepCurrentConvMessageList(List<BaseMessage> messageList) {
        if (messageList.size() > 0 && !TextUtils.isEmpty(mConversationId)) {
            try {
                long convId = Long.parseLong(mConversationId);
                // 只保留与当前对话 id 相同的消息
                Iterator<BaseMessage> iterator = messageList.iterator();
                while (iterator.hasNext()) {
                    BaseMessage message = iterator.next();
                    if (message.getConversationId() != convId) {
                        iterator.remove();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getClientAvatarUrl() {
        if (getIntent() != null) {
            Serializable clientInfoSerializable = getIntent().getSerializableExtra(CLIENT_INFO);
            if (clientInfoSerializable != null) {
                HashMap<String, String> clientInfo = (HashMap<String, String>) clientInfoSerializable;
                if (clientInfo.containsKey("avatar")) {
                    return clientInfo.get("avatar");
                }
            }
        }
        return "";
    }

    /**
     * 加载消息到列表中
     */
    private void loadData() {
        // 添加 ConDivider
        refreshConversationDivider(mChatMessageList);
        // 添加TimeItem
        MXTimeUtils.refreshMQTimeItem(mChatMessageList);
        // 加载到UI
        mLoadProgressBar.setVisibility(View.GONE);
        Iterator<BaseMessage> messageIterator = mChatMessageList.iterator();


        String clientAvatarUrl = getClientAvatarUrl();

        while (messageIterator.hasNext()) {
            BaseMessage message = messageIterator.next();
            // 将正在发送显示为已发送
            if (BaseMessage.STATE_SENDING.equals(message.getStatus())) {
                message.setStatus(BaseMessage.STATE_ARRIVE);
            }
            // 如果是黑名单状态，不显示结束对话的消息
            else if (BaseMessage.TYPE_ENDING.equals(message.getType()) && isBlackState) {
                messageIterator.remove();
            }

            // 处理设置客户头像后，第一次进来时没有头像
            if (MXConfig.isShowClientAvatar && !TextUtils.isEmpty(clientAvatarUrl) && message.getItemViewType() == BaseMessage.TYPE_CLIENT) {
                message.setAvatar(clientAvatarUrl);
            }
            // 补充没有头像的欢迎消息、企业消息
            if (MXConfig.isShowAgentAvatar && !TextUtils.equals(message.getFromType(), BaseMessage.TYPE_FROM_CLIENT)
                    && TextUtils.isEmpty(message.getAvatar())) {
                message.setAvatar(mController.getEnterpriseConfig().avatar);
            }
        }
        if (isBlackState) {
            addBlacklistTip(R.string.mx_blacklist_tips);
        }

        MXUtils.scrollListViewToBottom(mConversationListView);
        mChatMsgAdapter.downloadAndNotifyDataSetChanged(mChatMessageList);
        mChatMsgAdapter.notifyDataSetChanged();

        if (!mHasLoadData) {
            onLoadDataComplete(MXConversationActivity.this, mCurrentAgent);
        }
        mHasLoadData = true;
    }

    /**
     * 数据加载完成后的回调
     *
     * @param mqConversationActivity 当前 Activity
     * @param agent                  当前客服，可能为 null
     */
    protected void onLoadDataComplete(MXConversationActivity mqConversationActivity, Agent agent) {
        sendPreMessage();
    }

    private void sendPreMessage() {
        if (getIntent() != null) {
            String preSendTextContent = getIntent().getStringExtra(PRE_SEND_TEXT);
            String preSendImageFilePath = getIntent().getStringExtra(PRE_SEND_IMAGE_PATH);
            Bundle preSendProductCardBundle = getIntent().getBundleExtra(PRE_SEND_PRODUCT_CARD);
            if (!TextUtils.isEmpty(preSendTextContent)) {
                // 加入到待发送，等分配成功，并且 socket 连上的时候发送
                TextMessage preSendMsg = new TextMessage(preSendTextContent);
                delaySendList.add(preSendMsg);
            }
            if (!TextUtils.isEmpty(preSendImageFilePath)) {
                File imageFile = new File(preSendImageFilePath);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !imageFile.exists()) {
                    return;
                }
                PhotoMessage imageMessage = new PhotoMessage();
                imageMessage.setLocalPath(imageFile.getAbsolutePath());
                delaySendList.add(imageMessage);
            }
            // 清空 intent 里面的数据,因为排队成功可能还会再发一次,如果为空就不再发了
            getIntent().removeExtra(PRE_SEND_TEXT);
            getIntent().removeExtra(PRE_SEND_IMAGE_PATH);
            getIntent().removeExtra(PRE_SEND_PRODUCT_CARD);
        }
    }

    private void addRequestPermissionTopTip(int contentRes) {
        try {
            if (mRequestPermTopView == null) {
                mRequestPermTopView = getLayoutInflater().inflate(R.layout.mx_request_storage_top_pop_tip, null);
                ((TextView) mRequestPermTopView.findViewById(R.id.content_tv)).setText(contentRes);
                mChatBodyRl.addView(mRequestPermTopView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeRequestPermissionTopTip() {
        if (mRequestPermTopView != null) {
            try {
                mChatBodyRl.removeView(mRequestPermTopView);
                mRequestPermTopView = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back_rl) {
            MXUtils.closeKeyboard(this);
            // 返回按钮
            onBackPressed();
        } else if (id == R.id.emoji_select_btn) {
            // 表情按钮

            if (mCustomKeyboardLayout.isEmotionKeyboardVisible()) {
                hideEmojiSelectIndicator();
            } else {
                showEmojiSelectIndicator();
            }

            hideVoiceSelectIndicator();

            mCustomKeyboardLayout.toggleEmotionOriginKeyboard();
        } else if (id == R.id.send_text_btn) {

            if (!checkSendable()) {
                return;
            }

            String msg = mInputEt.getText().toString();
            createAndSendTextMessage(msg);

        } else if (id == R.id.photo_select_btn) {
            if (!checkSendable()) {
                return;
            }

            // Android 10 以下需要申请存储权限
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            ) {
                addRequestPermissionTopTip(R.string.mx_content_request_storage_permission_below_10);
                checkStoragePermission();
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || checkStoragePermission()) {
                // 选择图片
                hideEmojiSelectIndicator();
                hideVoiceSelectIndicator();
                chooseFromPhotoPicker();
            }
        } else if (id == R.id.camera_select_btn) {
            if (!checkSendable()) {
                return;
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                if (!checkCameraPermission()) {
                    addRequestPermissionTopTip(R.string.mx_content_request_camera_permission);
                    MXUtils.show(this, R.string.mx_camera_no_permission);
                    return;
                }
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    checkStoragePermission();
                    addRequestPermissionTopTip(R.string.mx_content_request_storage_permission_below_10);
                    return;
                }
            } else {
                if (!checkCameraPermission()) {
                    addRequestPermissionTopTip(R.string.mx_content_request_camera_permission);
                    MXUtils.show(this, R.string.mx_camera_no_permission);
                    return;
                }
            }
            hideEmojiSelectIndicator();
            hideVoiceSelectIndicator();
            // 打开相机
            choosePhotoFromCamera();
        } else if (id == R.id.video_select_btn) {
            if (!checkSendable()) {
                return;
            }

            hideEmojiSelectIndicator();
            hideVoiceSelectIndicator();
            popVideoSelectDialog();
        } else if (id == R.id.mic_select_btn) {
            if (!checkSendable()) {
                return;
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && !isPopRecordPermissionTipDialog) {
                addRequestPermissionTopTip(R.string.mx_content_request_record_permission);
                isPopRecordPermissionTipDialog = true;
                mVoiceBtn.performClick();
                return;
            }

            if (checkAudioPermission()) {
                if (mCustomKeyboardLayout.isVoiceKeyboardVisible()) {
                    hideVoiceSelectIndicator();
                } else {
                    showVoiceSelectIndicator();
                }

                hideEmojiSelectIndicator();

                mCustomKeyboardLayout.toggleVoiceOriginKeyboard();
            }
        } else if (id == R.id.evaluate_select_btn) {
            hideEmojiSelectIndicator();
            hideVoiceSelectIndicator();
            showEvaluateDialog();
        } else if (id == R.id.redirect_human_tv) {
            clickTransferHumanBtn();
        }
    }

    /**
     * 获取当前顾客在排队队列中的位置
     */
//    private void getClientPositionInQueue() {
//        // 避免多次获取排队位置，先移除
//        mHandler.removeMessages(WHAT_GET_CLIENT_POSITION_IN_QUEUE);
//
//        if (mController.getIsWaitingInQueue() && MQUtils.isNetworkAvailable(getApplicationContext())) {
//            mController.getClientPositionInQueue(new OnClientPositionInQueueCallback() {
//                @Override
//                public void onSuccess(int position) {
//                    if (position > 0) {
//                        addRedirectQueueLeaveMsg(position);
//                        sendGetClientPositionInQueueMsg();
//                    } else {
//                        MQManager.getInstance(MQConversationActivity.this).resetCurrentAgent();
//                        setClientOnline(true);
//                    }
//                }
//
//                @Override
//                public void onFailure(int code, String message) {
//                    sendGetClientPositionInQueueMsg();
//                }
//            });
//        }
//    }

    /**
     * 延迟15秒获取当前顾客在排队队列中的位置
     */
//    private void sendGetClientPositionInQueueMsg() {
//        // 避免多次获取排队位置，先移除
//        mHandler.removeMessages(WHAT_GET_CLIENT_POSITION_IN_QUEUE);
//
//        if (mController.getIsWaitingInQueue() && MQUtils.isNetworkAvailable(getApplicationContext())) {
//            changeTitleToQueue();
//            mHandler.sendEmptyMessageDelayed(WHAT_GET_CLIENT_POSITION_IN_QUEUE, 15 * 1000);
//        }
//    }

    /**
     * 检查存储权限
     *
     * @return true, 已经获取权限;false,没有权限,尝试获取
     */
    private boolean checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }

    /**
     * 检查相机权限
     *
     * @return true, 已经获取权限;false,没有权限,尝试获取
     */
    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,},
                    CAMERA_REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }

    /**
     * 检查存储权限 和 相机权限
     *
     * @return true, 已经获取权限;false,没有权限,尝试获取
     */
    private boolean checkStorageAndCameraPermission(int request) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,},
                    request);
            return false;
        } else {
            return true;
        }
    }

    /**
     * 相机权限
     *
     * @return true, 已经获取权限;false,没有权限,尝试获取
     */
    private boolean checkCameraPermission(int requestCode) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,},
                    requestCode);
            return false;
        } else {
            return true;
        }
    }

    /**
     * 检查录音权限
     *
     * @return true, 已经获取权限;false,没有权限,尝试获取
     */
    private boolean checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO_REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }

    private void showEvaluateDialog() {
        // 如果没有正在录音才弹出评价对话框
        if (mCustomKeyboardLayout.isRecording()) {
            return;
        }
        mCustomKeyboardLayout.closeAllKeyboard();
        if (!TextUtils.isEmpty(mConversationId)) {
            mController.refreshEnterpriseConfig(new SimpleCallback() {
                @Override
                public void onSuccess() {
                    // 获取评价的配置详细信息
                    mController.getEvaluateConfig(mController.getEnterpriseConfig().serviceEvaluationConfig.getEvaluation_level(),
                            new OnEvaluateConfigCallback() {
                                @Override
                                public void onFailure(int code, String message) {
                                    MXUtils.showSafe(MXConversationActivity.this, R.string.mx_evaluate_failure);
                                }

                                @Override
                                public void onSuccess(List<MXEvaluateConfig> config) {
                                    if (mEvaluateDialog == null) {
                                        mEvaluateDialog = new MXEvaluateLevelDialog(MXConversationActivity.this);
                                        mEvaluateDialog.setCallback(MXConversationActivity.this);
                                    }
                                    mEvaluateDialog.init(
                                            mController.getEnterpriseConfig().serviceEvaluationConfig.getProblem_feedback(),
                                            mController.getEnterpriseConfig().serviceEvaluationConfig.getEvaluation_level(),
                                            mController.getEnterpriseConfig().serviceEvaluationConfig.getPrompt_text(),
                                            config);
                                    mEvaluateDialog.show();
                                }
                            });
                }

                @Override
                public void onFailure(int code, String message) {
                    MXUtils.showSafe(MXConversationActivity.this, R.string.mx_evaluate_failure);
                }
            });


        }
    }

    private void showEmojiSelectIndicator() {
        mEmojiSelectIndicator.setVisibility(View.VISIBLE);
        mEmojiSelectImg.setImageResource(R.drawable.mx_ic_emoji_active);
        mEmojiSelectImg.setColorFilter(getResources().getColor(R.color.mx_indicator_selected));
    }

    private void hideEmojiSelectIndicator() {
        mEmojiSelectIndicator.setVisibility(View.GONE);
        mEmojiSelectImg.setImageResource(R.drawable.mx_ic_emoji_normal);
        mEmojiSelectImg.clearColorFilter();
    }

    private void showVoiceSelectIndicator() {
        mVoiceSelectIndicator.setVisibility(View.VISIBLE);
        mVoiceSelectImg.setImageResource(R.drawable.mx_ic_mic_active);
        mVoiceSelectImg.setColorFilter(getResources().getColor(R.color.mx_indicator_selected));
    }

    private void hideVoiceSelectIndicator() {
        mVoiceSelectIndicator.setVisibility(View.GONE);
        mVoiceSelectImg.setImageResource(R.drawable.mx_ic_mic_normal);
        mVoiceSelectImg.clearColorFilter();
    }


    /**
     * 从本地选择图片
     */
    private void chooseFromPhotoPicker() {
        // 弹窗访问提示
        boolean isNeedShowPermissionDialog = getSharedPreferences("mx_permission", Context.MODE_PRIVATE).getBoolean("isNeedShowPermissionDialog", true);
        if (isNeedShowPermissionDialog && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            String title = getResources().getString(R.string.mx_title_send_photo);
            String content = getResources().getString(R.string.mx_content_send_photo);
            new MXConfirmDialog(this, title, content, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getSharedPreferences("mx_permission", Context.MODE_PRIVATE).edit().putBoolean("isNeedShowPermissionDialog", false).apply();
                    chooseFromPhotoPicker();
                }
            }, null).show();
        } else {
            // Android 10 直接跳转系统组件
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent intent = new Intent();
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, REQUEST_CODE_PHOTO);
            } else {
                try {
                    startActivityForResult(MXPhotoPickerActivity.newIntent(this, null, 3, null, getString(R.string.mx_send)), REQUEST_CODE_PHOTO);
                } catch (Exception e) {
                    MXUtils.show(this, R.string.mx_photo_not_support);
                }
            }
        }
    }

    private void popVideoSelectDialog() {
        List<Map<String, String>> list = new ArrayList<>();
        Map<String, String> videoCamera = new HashMap<String, String>();
        videoCamera.put("name", getResources().getString(R.string.mx_dialog_select_camera_video));
        videoCamera.put("value", getResources().getString(R.string.mx_dialog_select_camera_video));
        list.add(videoCamera);
        Map<String, String> videoPhoto = new HashMap<String, String>();
        videoPhoto.put("name", getResources().getString(R.string.mx_dialog_select_gallery));
        videoPhoto.put("value", getResources().getString(R.string.mx_dialog_select_gallery));
        list.add(videoPhoto);
        new MXListDialog(this, R.string.mx_dialog_select_video_title, list, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                switch (position) {
                    case 0:
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                            if (!checkCameraPermission(VIDEO_REQUEST_CODE)) {
                                addRequestPermissionTopTip(R.string.mx_content_request_camera_permission);
                                return;
                            }
                            if (!checkCameraPermission()) {
                                MXUtils.show(MXConversationActivity.this, R.string.mx_camera_no_permission);
                                return;
                            }
                            if (ContextCompat.checkSelfPermission(MXConversationActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                checkStoragePermission();
                                addRequestPermissionTopTip(R.string.mx_content_request_storage_permission_below_10);
                                return;
                            }
                        } else {
                            if (!checkCameraPermission(VIDEO_REQUEST_CODE)) {
                                addRequestPermissionTopTip(R.string.mx_content_request_camera_permission);
                                return;
                            }
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            if (checkCameraPermission(VIDEO_REQUEST_CODE)) {
                                recordVideoFromCamera();
                            }
                        } else {
                            if (checkStorageAndCameraPermission(WRITE_EXTERNAL_STORAGE_AND_VIDEO_REQUEST_CODE)) {
                                recordVideoFromCamera();
                            }
                        }
                        break;
                    case 1:
                        chooseVideoFromPicker();
                        break;
                }
            }
        }).show();
    }

    private void chooseVideoFromPicker() {
        // 弹窗访问提示
        boolean isNeedShowPermissionDialog = getSharedPreferences("mx_permission", Context.MODE_PRIVATE).getBoolean("isNeedShowVideoPermissionDialog", true);
        if (isNeedShowPermissionDialog) {
            String title = getResources().getString(R.string.mx_request_permission);
            String content = getResources().getString(R.string.mx_content_request_camera_permission);
            new MXConfirmDialog(this, title, content, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getSharedPreferences("mx_permission", Context.MODE_PRIVATE).edit().putBoolean("isNeedShowVideoPermissionDialog", false).apply();
                    chooseVideoFromPicker();
                }
            }, null).show();
        } else {
            try {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_CODE_CHOOSE_VIDEO);
            } catch (Exception e) {
                Toast.makeText(this, getResources().getString(R.string.mx_title_unknown_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 打开相机
     */
    private void choosePhotoFromCamera() {
        MXUtils.closeKeyboard(MXConversationActivity.this);

        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(MXUtils.getPicStorePath(this));
        file.mkdirs();
        String fileName = System.currentTimeMillis() + ".jpg";
        String path = MXUtils.getPicStorePath(this) + "/" + fileName;
        File imageFile = new File(path);
        mCameraPicPath = path;
        Uri uri;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures");
                contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                ContentResolver resolver = getContentResolver();
                Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                uri = resolver.insert(collection, contentValues);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ContentValues contentValues = new ContentValues(1);
                contentValues.put(MediaStore.Images.Media.DATA, imageFile.getAbsolutePath());
                uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            } else {
                uri = Uri.fromFile(imageFile);
            }
            mCameraPicUri = uri;
            camera.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(camera, MXConversationActivity.REQUEST_CODE_CAMERA);
        } catch (Exception e) {
            MXUtils.show(this, R.string.mx_photo_not_support);
        }
    }

    /**
     * 打开相机
     */
    private void recordVideoFromCamera() {
        MXUtils.closeKeyboard(MXConversationActivity.this);

        Intent camera = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        File file = new File(MXUtils.getPicStorePath(this));
        file.mkdirs();
        String path = MXUtils.getPicStorePath(this) + "/" + System.currentTimeMillis() + ".mp4";
        File videoFile = new File(path);
        mVideoPath = path;
        Uri uri = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // result 中处理
            } else {
                uri = Uri.fromFile(videoFile);
            }
            mCameraPicUri = uri;
            camera.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);    // MediaStore.EXTRA_VIDEO_QUALITY 表示录制视频的质量，从 0-1，越大表示质量越好，同时视频也越大
            if (uri != null) {
                camera.putExtra(MediaStore.EXTRA_OUTPUT, uri);    // 表示录制完后保存的录制，如果不写，则会保存到默认的路径，在onActivityResult()的回调，通过intent.getData中返回保存的路径
            }
            camera.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60);   // 设置视频录制的最长时间
            startActivityForResult(camera, MXConversationActivity.REQUEST_CODE_VIDEO);
        } catch (Exception e) {
            MXUtils.show(this, R.string.mx_photo_not_support);
        }
    }

    /**
     * 创建并发送TextMessage。如果没有客服在线，发送离线消息
     */
    private void createAndSendTextMessage(String msg) {
        //内容为空不发送，只有空格时也不发送
        if (TextUtils.isEmpty(msg.trim())) {
            return;
        }
        sendMessage(new TextMessage(msg));
    }

    /**
     * 创建并发送ImageMessage
     *
     * @param imageFile 需要上传的imageFile
     */
    private void createAndSendImageMessage(File imageFile) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !imageFile.exists()) {
            return;
        }
        PhotoMessage imageMessage = new PhotoMessage();
        imageMessage.setLocalPath(imageFile.getAbsolutePath());
        sendMessage(imageMessage);
    }

    private void createAndSendVideoMessage(File videoFile) {
        VideoMessage videoMessage = new VideoMessage();
        videoMessage.setLocalPath(videoFile.getAbsolutePath());
        sendMessage(videoMessage);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        removeRequestPermissionTopTip();
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // nothing
                } else {
                    MXUtils.show(this, R.string.mx_sdcard_no_permission);
                }
                break;
            }
            case RECORD_AUDIO_REQUEST_CODE: {
                isPopRecordPermissionTipDialog = false;
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mVoiceBtn.performClick();
                } else {
                    MXUtils.show(this, R.string.mx_recorder_no_permission);
                }
                break;
            }
            case WRITE_EXTERNAL_STORAGE_AND_VIDEO_REQUEST_CODE:
            case WRITE_EXTERNAL_STORAGE_AND_CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                            && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        if (requestCode == WRITE_EXTERNAL_STORAGE_AND_CAMERA_REQUEST_CODE) {
                            mCameraSelectBtn.performClick();
                        } else if (requestCode == WRITE_EXTERNAL_STORAGE_AND_VIDEO_REQUEST_CODE) {
                            mVideoSelectBtn.performClick();
                        }
                        // 有存储权限
                    } else {
                        MXUtils.show(this, R.string.mx_camera_or_storage_no_permission);
                    }
                } else {
                    MXUtils.show(this, R.string.mx_camera_or_storage_no_permission);
                }
                break;
            }
            case VIDEO_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    recordVideoFromCamera();
                } else {
                    MXUtils.show(this, R.string.mx_camera_no_permission);
                }
                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAMERA) {
                // 从 相机 获取的图片

                File cameraPicFile = getCameraPicFile();
                if (cameraPicFile != null) {
                    createAndSendImageMessage(cameraPicFile);
                }
            } else if (requestCode == REQUEST_CODE_PHOTO) {
                // 从 相册 获取的图片
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        ParcelFileDescriptor pfd = this.getContentResolver().openFileDescriptor(data.getData(), "r");
                        FileInputStream fileInputStream = new FileInputStream(pfd.getFileDescriptor());
                        String path = MXUtils.getPicStorePath(this) + "/" + System.currentTimeMillis();
                        File imageFile = new File(path);
                        FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
                        int len;
                        byte[] buffer = new byte[8192];
                        while ((len = fileInputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, len);
                        }
                        pfd.close();
                        fileInputStream.close();
                        fileOutputStream.close();
                        createAndSendImageMessage(imageFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ArrayList<String> selectedPhotos = MXPhotoPickerActivity.getSelectedImages(data);
                    for (String photoPath : selectedPhotos) {
                        createAndSendImageMessage(new File(photoPath));
                    }
                }
            } else if (requestCode == REQUEST_CODE_VIDEO) {
                // 复制到私有目录
                try {
                    ParcelFileDescriptor pfd = this.getContentResolver().openFileDescriptor(data.getData(), "r");
                    FileInputStream fileInputStream = new FileInputStream(pfd.getFileDescriptor());
                    String path = MXUtils.getPicStorePath(this) + "/" + System.currentTimeMillis() + ".mp4";
                    File videoFile = new File(path);
                    FileOutputStream fileOutputStream = new FileOutputStream(videoFile);
                    int len = 0;
                    byte[] buffer = new byte[8192];
                    while ((len = fileInputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                    }
                    pfd.close();
                    fileInputStream.close();
                    fileOutputStream.close();
                    createAndSendVideoMessage(videoFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == REQUEST_CODE_CHOOSE_VIDEO) {
                try {
                    ParcelFileDescriptor pfd = this.getContentResolver().openFileDescriptor(data.getData(), "r");
                    FileInputStream fileInputStream = new FileInputStream(pfd.getFileDescriptor());
                    String path = MXUtils.getPicStorePath(this) + "/" + System.currentTimeMillis() + ".mp4";
                    File videoFile = new File(path);
                    FileOutputStream fileOutputStream = new FileOutputStream(videoFile);
                    int len = 0;
                    byte[] buffer = new byte[8192];
                    while ((len = fileInputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                    }
                    pfd.close();
                    fileInputStream.close();
                    fileOutputStream.close();
                    // 不允许超过 100MB
                    if (videoFile.length() >= 1024 * 1024 * 50) {
                        Toast.makeText(this, R.string.mx_error_video_size, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    createAndSendVideoMessage(videoFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void startActivity(Intent intent) {
        // 如果当前系统中没有邮件客户端可供调用，程序会直接挂掉，系统抛出了ActivityNotFoundException
        if (intent.toString().contains("mailto")) {
            PackageManager pm = getPackageManager();
            // The first Method
            List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
            if (activities == null || activities.size() == 0) {
                // Do anything you like, or just return
                return;
            }
        }
        String dataString = null;
        try {
            dataString = intent.getDataString();
            if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                String s = intent.getData().getScheme();
                if (!TextUtils.isEmpty(s) && s.startsWith("http")) {
                    if (MXConfig.getOnLinkClickCallback() != null) {
                        MXConfig.getOnLinkClickCallback().onClick(this, intent, dataString);
                        return;
                    }
                }
            }
            super.startActivity(intent);
        } catch (Exception e) {
            boolean isNeedToastError;
            try {
                String url = URLUtil.guessUrl(dataString);
                if (!TextUtils.isEmpty(url) && url.startsWith("http")) {
                    if (MXConfig.getOnLinkClickCallback() != null) {
                        MXConfig.getOnLinkClickCallback().onClick(this, intent, dataString);
                        return;
                    }
                }
                isNeedToastError = false;
                intent.setData(Uri.parse(url));
                super.startActivity(intent);
            } catch (Exception e1) {
                e.printStackTrace();
                isNeedToastError = true;
            }
            if (isNeedToastError && !TextUtils.isEmpty(dataString)) {
                Toast.makeText(this, R.string.mx_title_unknown_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 调用父类的 startActivity 方法
     *
     * @param intent intent
     */
    public void superStartActivity(Intent intent) {
        super.startActivity(intent);
    }

    public File getCameraPicFile() {
        String sdState = Environment.getExternalStorageState();
        if (!sdState.equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }
        File imageFile = new File(mCameraPicPath);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && mCameraPicUri != null) {
            File file = null;
            try {
                ParcelFileDescriptor pfd = this.getContentResolver().openFileDescriptor(mCameraPicUri, "r");
                FileInputStream fileInputStream = new FileInputStream(pfd.getFileDescriptor());
                String path = MXUtils.getPicStorePath(this) + "/" + System.currentTimeMillis();
                file = new File(path);
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[8192];
                while ((len = fileInputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, len);
                }
                pfd.close();
                fileInputStream.close();
                fileOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                if (imageFile.exists()) {
                    return imageFile;
                }
            }
            return file;
        } else {
            if (imageFile.exists()) {
                return imageFile;
            } else {
                return null;
            }
        }
    }

    public File getVideoFile(Intent intent) {
        String sdState = Environment.getExternalStorageState();
        if (!sdState.equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }
        File videoFile = new File(mVideoPath);
        mVideoUri = intent.getData();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && mVideoUri != null) {
            // 从 uri 获取 path，SDK 内部会通过 path 拿到 uri
            String realFilePath = MXUtils.getRealFilePath(this, mVideoUri);
            // 如果 targetapi 不是 Android 10，realFilePath 将会是空
            if (!TextUtils.isEmpty(realFilePath)) {
                // 这里的 file 无法直接读取
                return new File(realFilePath);
            }
        }
        if (videoFile.exists()) {
            return videoFile;
        } else {
            return null;
        }
    }

    /**
     * 检查发送条件并且处理一些准备发送的状态
     *
     * @param message 待发送消息
     * @return true，可以发送；false，不能发送
     */
    private boolean checkAndPreSend(BaseMessage message) {
        // 数据还没有加载的时候
        if (mChatMsgAdapter == null) {
            return false;
        }
        // 状态改为「正在发送」，以便在数据列表中展示正在发送消息的状态
        message.setStatus(BaseMessage.STATE_SENDING);
        // 添加到对话列表
        mChatMessageList.add(message);
        mInputEt.setText("");

        // 清空未发送的文本消息
        String clientId = mController.getCurrentClientId();
        if (!TextUtils.isEmpty(clientId)) {
            MXUtils.setUnSendTextMessage(this, clientId, "");
        }

        MXTimeUtils.refreshMQTimeItem(mChatMessageList);
        mChatMsgAdapter.notifyDataSetChanged();
        return true;
    }

    private boolean checkSendable() {
        if (mIsAllocatingAgent) {
            MXUtils.show(this, R.string.mx_allocate_agent_tip);
            return false;
        }
        if (!mHasLoadData) {
            MXUtils.show(this, R.string.mx_data_is_loading);
            return false;
        }
        // 发送消息不提示网络状态
//        if (!MXManager.getInstance(this).isSocketConnect() && mCurrentAgent != null) {
//            MXUtils.show(this, R.string.mx_net_status_not_available);
//            addNetStatusTopTip(MXMessageManager.ACTION_SOCKET_RECONNECT);
//            return false;
//        }

        // 如果当前客服是机器人，则限制发送频率为1秒
        if (mCurrentAgent != null && mCurrentAgent.isRobot()) {
            if (System.currentTimeMillis() - mLastSendRobotMessageTime <= 1000) {
                MXUtils.show(this, R.string.mx_send_robot_msg_time_limit_tip);
                return false;
            }
        }
        return true;
    }

    /**
     * 发送消息
     *
     * @param message 消息
     */
    public void sendMessage(final BaseMessage message) {
        boolean isPreSendSuc = checkAndPreSend(message);
        if (!isPreSendSuc) {
            return;
        }

        // 开始发送
        mController.sendMessage(message, new OnMessageSendCallback() {
            @Override
            public void onSuccess(BaseMessage message, int state) {
                // 去除可能从 socket 收到的重复消息
                removeDupMessageFromSocket(message);

                renameVoiceFilename(message);

                refreshConversationDivider(mChatMessageList);
                // 刷新界面
                mChatMsgAdapter.notifyDataSetChanged();

                // 客服不在线的时候，会自动发送留言消息，这个时候要添加一个 tip 到列表
                if (ErrorCode.NO_AGENT_ONLINE == state) {
                }
                // 发送成功播放声音
                if (MXConfig.isSoundSwitchOpen) {
                    mSoundPoolManager.playSound(R.raw.mx_send_message);
                }
                if (mCurrentAgent != null && mCurrentAgent.isRobot()) {
                    mLastSendRobotMessageTime = System.currentTimeMillis();
                }
            }

            @Override
            public void onFailure(BaseMessage failureMessage, int code, String failureInfo) {
                if (code == ErrorCode.BLACKLIST) {
                    addBlacklistTip(R.string.mx_blacklist_tips);
                } else if (code == ErrorCode.QUEUEING) {
                } else if (code == ErrorCode.NO_AGENT_ONLINE) {
                } else if (code == ErrorCode.AREA_LIMIT) {
                    Toast.makeText(MXConversationActivity.this,  failureInfo, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MXConversationActivity.this, "code = " + code + "\n" + "message = " + message, Toast.LENGTH_SHORT).show();
                }
                mChatMsgAdapter.notifyDataSetChanged();
            }
        });
        MXUtils.scrollListViewToBottom(mConversationListView);
    }

    /**
     * 重发消息
     *
     * @param message 待重发的消息
     */
    public void resendMessage(final BaseMessage message) {
        // 开始重发
        message.setStatus(BaseMessage.STATE_SENDING);
        mController.resendMessage(message, new OnMessageSendCallback() {
            @Override
            public void onSuccess(BaseMessage message, int state) {
                renameVoiceFilename(message);
                updateResendMessage(message, 0);
            }

            @Override
            public void onFailure(BaseMessage failureMessage, int code, String failureInfo) {
                updateResendMessage(failureMessage, code);
                // 客服不在线的时候，会自动发送留言消息，这个时候要添加一个 tip 到列表
                if (ErrorCode.NO_AGENT_ONLINE == code) {
                    // 关闭留言功能的情况下，直接跳转到留言界面：如果当前客服是机器人就例外
//                    if (isEnableGoToMessageFormActivity()) {
//                        Intent intent = new Intent(MQConversationActivity.this, MQMessageFormActivity.class);
//                        startActivity(intent);
//                        finish();
//                    } else {
//                        addLeaveMessageTip();
//                    }
                }
            }
        });
    }

    private void updateResendMessage(BaseMessage message, int code) {
        // 重发失败，移动到列表最下面
        int messagePosition = mChatMessageList.indexOf(message); // 当前消息的位置
        mChatMessageList.remove(message);
        // 如果下一条 消息是黑名单 tip，黑名单的 tip 也要删除
        if (isBlackState && mChatMessageList.size() > messagePosition && mChatMessageList.get(messagePosition).getItemViewType() == BaseMessage.TYPE_TIP) {
            mChatMessageList.remove(messagePosition);
        }
        MXTimeUtils.refreshMQTimeItem(mChatMessageList);
        mChatMsgAdapter.addMQMessage(message);

        if (code == ErrorCode.BLACKLIST) {
            addBlacklistTip(R.string.mx_blacklist_tips);
        }
        scrollContentToBottom();
    }

    /**
     * 重命名本地语音文件
     *
     * @param message
     */
    private void renameVoiceFilename(BaseMessage message) {
        if (message instanceof VoiceMessage) {
            VoiceMessage voiceMessage = (VoiceMessage) message;
            MXAudioRecorderManager.renameVoiceFilename(MXConversationActivity.this, voiceMessage.getLocalPath(), voiceMessage.getContent());
            mChatMsgAdapter.downloadAndNotifyDataSetChanged(Arrays.asList(message));
        }
    }

    // 监听EditText输入框数据到变化
    private final TextWatcher inputTextWatcher = new MXSimpleTextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // 向服务器发送一个正在输入的函数
            if (!TextUtils.isEmpty(s)) {
                inputting(s.toString());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mSendTextBtn.setElevation(MXUtils.dip2px(MXConversationActivity.this, 3));
                }
                mSendTextBtn.setImageResource(R.drawable.mx_ic_send_icon_white);
                mSendTextBtn.setBackgroundResource(R.drawable.mx_shape_send_back_pressed);
            } else {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mSendTextBtn.setElevation(0);
                }
                mSendTextBtn.setImageResource(R.drawable.mx_ic_send_icon_grey);
                mSendTextBtn.setBackgroundResource(R.drawable.mx_shape_send_back_normal);
            }
        }
    };

    /**
     * 向服务器发送「顾客正在输入」的状态
     *
     * @param content 内容
     */
    private void inputting(String content) {
        mController.sendClientInputtingWithContent(content);
    }

    /**
     * 过滤语音消息
     *
     * @param messageList 消息列表
     */
    private void cleanMessages(List<BaseMessage> messageList) {
        if (messageList.size() > 0) {
            Iterator<BaseMessage> baseMessageIterator = messageList.iterator();
            while (baseMessageIterator.hasNext()) {
                BaseMessage baseMessage = baseMessageIterator.next();
                if (!MXConfig.isVoiceSwitchOpen && BaseMessage.TYPE_CONTENT_VOICE.equals(baseMessage.getContentType())) {
                    baseMessageIterator.remove();
                } else if (baseMessage.isWithdraw()) {
                    baseMessageIterator.remove();
                }
            }
        }
    }

    /**
     * 添加 评价 的消息 Tip 到列表
     *
     * @param level 评价的等级
     */
    protected void addEvaluateMessageTip(int isSolved, int level,
                                         List<MXEvaluateConfig.Tag> selectTagIds, String content,
                                         int evaluateLevel, List<MXEvaluateConfig> evaluateConfig) {
        EvaluateMessage evaluateMessage = new EvaluateMessage(isSolved, level, selectTagIds, content, evaluateLevel, evaluateConfig);
        mChatMsgAdapter.addMQMessage(evaluateMessage);
    }

    @Override
    public void executeEvaluate(int isSolved, int level, List<MXEvaluateConfig.Tag> selectTagIds, String content, int evaluateLevel, List<MXEvaluateConfig> evaluateConfig) {
        if (!checkSendable()) {
            return;
        }

        List<Integer> tagIds = new ArrayList<>();
        for (MXEvaluateConfig.Tag tag : selectTagIds) {
            tagIds.add(tag.getId());
        }

        mController.executeEvaluate(mConversationId, isSolved, level, tagIds, content, evaluateLevel, new SimpleCallback() {
            @Override
            public void onFailure(int code, String message) {
                MXUtils.showSafe(MXConversationActivity.this, R.string.mx_evaluate_failure);
            }

            @Override
            public void onSuccess() {
                addEvaluateMessageTip(isSolved, level, selectTagIds, content, evaluateLevel, evaluateConfig);
            }
        });
    }

    @Override
    public void onAudioRecorderFinish(int time, String filePath) {
        if (!checkSendable()) {
            return;
        }

        VoiceMessage voiceMessage = new VoiceMessage();
        voiceMessage.setDuration(time);
        voiceMessage.setLocalPath(filePath);
        sendMessage(voiceMessage);
    }

    @Override
    public void onAudioRecorderTooShort() {
        MXUtils.show(this, R.string.mx_record_record_time_is_short);
    }

    @Override
    public void scrollContentToBottom() {
        MXUtils.scrollListViewToBottom(mConversationListView);
    }

    @Override
    public void onAudioRecorderNoPermission() {
        MXUtils.show(this, R.string.mx_recorder_no_permission);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        hideEmojiSelectIndicator();
        hideVoiceSelectIndicator();
        return false;
    }

    /**
     * 修改客服在线状态和转人工按钮
     */
    private void updateAgentOnlineOfflineStatusAndRedirectHuman() {
        Agent agent = mController.getCurrentAgent();

        if (agent != null) {
            // 不显示在线状态
//            if (!agent.isOnline()) {
//                mTitleTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.mx_shape_agent_status_offline, 0);
//            } else if (agent.isOffDuty()) {
//                mTitleTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.mx_shape_agent_status_off_duty, 0);
//            } else {
//                mTitleTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.mx_shape_agent_status_online, 0);
//            }

            mEvaluateBtn.setVisibility(mController.getEnterpriseConfig().serviceEvaluationConfig.isEnableEvaluation(MXManager.getAppKey()) ? View.VISIBLE : View.GONE);
        } else {
            hiddenAgentStatusAndRedirectHuman();
        }
    }

    /**
     * 隐藏客服在线状态和转人工按钮
     */
    private void hiddenAgentStatusAndRedirectHuman() {
        mTitleTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    }

    public void onFileMessageDownloadFailure(FileMessage fileMessage, int code, String message) {
        // 避免界面销毁了还更新 UI
        if (isDestroy) {
            return;
        }

        popTopTip(R.string.mx_download_error);
    }

    public void onFileMessageExpired(FileMessage fileMessage) {
        // 避免界面销毁了还更新 UI
        if (isDestroy) {
            return;
        }

        popTopTip(R.string.mx_expired_top_tip);
    }

    /**
     * 退出界面后，取消所有下载
     */
    private void cancelAllDownload() {
        for (BaseMessage message : mChatMessageList) {
            if (message instanceof FileMessage) {
                MXConfig.getController(this).cancelDownload(((FileMessage) message).getUrl());
            }
        }
    }

    @Override
    public void onQuickBtnClicked(long convId, Integer quickBtnId) {
        mController.sendQuickBtnClicked(convId, quickBtnId);
    }

    @Override
    public void onSendMessage(String message) {
        sendMessage(new TextMessage(message));
    }

    @Override
    public void photoPreview(String url) {
        startActivity(MXPhotoPreviewActivity.newIntent(this, MXUtils.getImageDir(this), url));
    }

    private class MessageReceiver extends com.mixdesk.mixdesksdk.controller.MessageReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
        }

        @Override
        public void receiveNewMsg(BaseMessage message) {
            MXConversationActivity.this.receiveNewMsg(message);
        }

        @Override
        public void recallMessage(long id, String nickname) {
            BaseMessage recallMessage = new BaseMessage();
            recallMessage.setId(id);
            mChatMessageList.remove(recallMessage);
            // 根据开关判断是否显示「已撤回」的提示
            if (!MXConfig.getController(MXConversationActivity.this).getEnterpriseConfig().isNotDisplayWithdrawTip) {
                TipMessage recallTipMessage = new TipMessage();
                recallTipMessage.setContent(getResources().getString(R.string.mx_recall_msg));
                mChatMessageList.add(recallTipMessage);
            }
            mChatMsgAdapter.notifyDataSetChanged();
        }

        @Override
        public void changeTitleToInputting(int duration) {
            MXConversationActivity.this.changeTitleToInputting();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MXConversationActivity.this.setCurrentAgent(mCurrentAgent);
                }
            }, duration * 1000);

        }

        @Override
        public void addDirectAgentMessageTip(String agentNickname) {
            MXConversationActivity.this.addDirectAgentMessageTip(agentNickname);
        }

        @Override
        public void setCurrentAgent(Agent agent) {
            MXConversationActivity.this.setCurrentAgent(agent);
        }

        @Override
        public void setShowTransHumanBtn(boolean isShowTransferHumanBtn) {
            MXConversationActivity.this.mRedirectHumanTv.setVisibility(
                    isShowTransferHumanBtn ? View.VISIBLE : View.GONE);
        }

        @Override
        public void inviteEvaluation() {
            if (!checkSendable()) {
                return;
            }

            showEvaluateDialog();
        }

        @Override
        public void setNewConversationId(String newConversationId) {
            mConversationId = newConversationId;
        }

        @Override
        public void updateAgentOnlineOfflineStatus() {
            MXConversationActivity.this.updateAgentOnlineOfflineStatusAndRedirectHuman();
        }

        @Override
        public void blackAdd() {
            isBlackState = true;
        }

        @Override
        public void blackDel() {
            isBlackState = false;
        }

        @Override
        public void initConv(long convId) {
            mConversationId = String.valueOf(convId);
            mMessageReceiver.setConversationId(mConversationId);
            setCurrentAgent(mController.getCurrentAgent());
//            sendDelayMessages();
        }

        @Override
        public void socketOpen() {
            removeNetStatusTopTip();
        }

        @Override
        public void socketReconnect() {
            addNetStatusTopTip(MXMessageManager.ACTION_SOCKET_RECONNECT);
        }
    }

    /**
     * 处理收到的新消息
     *
     * @param baseMessage 新消息
     */
    private void receiveNewMsg(BaseMessage baseMessage) {
        if (mChatMsgAdapter != null && !isDupMessage(baseMessage)) {
            // 如果是配置了不显示语音，收到语音消息直接过滤
            if (!MXConfig.isVoiceSwitchOpen && BaseMessage.TYPE_CONTENT_VOICE.equals(baseMessage.getContentType())) {
                return;
            }

            // 被拉黑状态，不显示结束对话消息
            if (BaseMessage.TYPE_ENDING.equals(baseMessage.getType()) && isBlackState) {
                return;
            }
            // 补充企业头像
            if (!TextUtils.equals(baseMessage.getFromType(), BaseMessage.TYPE_FROM_CLIENT)
                    && TextUtils.isEmpty(baseMessage.getAvatar())) {
                baseMessage.setAvatar(mController.getEnterpriseConfig().avatar);
            }
            mChatMessageList.add(baseMessage);
            MXTimeUtils.refreshMQTimeItem(mChatMessageList);

            if (baseMessage instanceof VoiceMessage) {
                mChatMsgAdapter.downloadAndNotifyDataSetChanged(Arrays.asList(baseMessage));
            } else {
                mChatMsgAdapter.notifyDataSetChanged();
            }

            int lastVisiblePosition = mConversationListView.getLastVisiblePosition();
            // -2 因为是先添加
            if (lastVisiblePosition == (mChatMsgAdapter.getCount() - 2)) {
                MXUtils.scrollListViewToBottom(mConversationListView);
            }
            // 在界面中播放声音
            if (!isPause && MXConfig.isSoundSwitchOpen) {
                mSoundPoolManager.playSound(R.raw.mx_new_message);
            }

            // 保存最后一条消息时间
            mController.saveConversationLastMessageTime(baseMessage.getCreatedOn());
        }

    }

    /**
     * 消息是否已经在列表中
     *
     * @param baseMessage
     * @return true，已经存在与列表；false，不存在
     */
    private boolean isDupMessage(BaseMessage baseMessage) {
        for (BaseMessage message : mChatMessageList) {
            if (message.equals(baseMessage)) {
                return true;
            }
        }
        return false;
    }

    private void removeDupMessageFromSocket(BaseMessage successMessage) {
        Iterator<BaseMessage> iterator = mChatMessageList.iterator();
        while (iterator.hasNext()) {
            BaseMessage message = iterator.next();
            if (successMessage != message && successMessage.getId() == message.getId()) {
                iterator.remove();
                break;
            }
        }
    }

    /**
     * 监听网络
     */
    private class NetworkChangeReceiver extends BroadcastReceiver {
        // 第一次进入的时候，会立即收到广播，需要避免以下
        private boolean isFirstReceiveBroadcast = true;

        @Override
        public void onReceive(Context arg0, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                if (!isFirstReceiveBroadcast) {
                    // 有网络
                    if (MXUtils.isNetworkAvailable(getApplicationContext())) {
                        // 断网后，返回重新进入， 又有网了刷新 Agent
                        setCurrentAgent(mController.getCurrentAgent());
                    }
                    // 没有网络
                    else {
                        changeTitleToNetErrorState();
                        addNetStatusTopTip("net_not_work");
                    }
                } else {
                    isFirstReceiveBroadcast = false;
                }
            }
        }

    }

    /**
     * 显示完整的错误信息
     *
     * @param code    错误代码
     * @param message 错误消息
     */
    private void showErrorMessage(int code, String message) {
        String title = "请求失败";
        String content = "错误代码: " + code;
        if (message != null && !message.isEmpty()) {
            content += "\n\n详细信息:\n" + message;
        }

        new android.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton("确定", null)
                .show();
    }
}
