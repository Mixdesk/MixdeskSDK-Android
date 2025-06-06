package com.mixdesk.mixdesksdk.chatitem;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mixdesk.mixdesksdk.R;
import com.mixdesk.mixdesksdk.imageloader.MXImage;
import com.mixdesk.mixdesksdk.imageloader.MXImageLoader;
import com.mixdesk.mixdesksdk.model.BaseMessage;
import com.mixdesk.mixdesksdk.model.FileMessage;
import com.mixdesk.mixdesksdk.model.PhotoMessage;
import com.mixdesk.mixdesksdk.model.VideoMessage;
import com.mixdesk.mixdesksdk.model.VoiceMessage;
import com.mixdesk.mixdesksdk.util.MXAudioPlayerManager;
import com.mixdesk.mixdesksdk.util.MXConfig;
import com.mixdesk.mixdesksdk.util.MXDownloadManager;
import com.mixdesk.mixdesksdk.util.MXEmotionUtil;
import com.mixdesk.mixdesksdk.util.MXUtils;
import com.mixdesk.mixdesksdk.widget.MXBaseCustomCompositeView;
import com.mixdesk.mixdesksdk.widget.MXImageView;

import java.io.File;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/5/23 下午5:16
 * 描述:气泡消息基类
 */
public abstract class MXBaseBubbleItem extends MXBaseCustomCompositeView implements MXChatFileItem.Callback {
    protected TextView contentText;
    protected MXImageView contentImage;
    protected TextView voiceContentTv;
    protected ImageView voiceAnimIv;
    protected View voiceContainerRl;
    protected MXChatFileItem chatFileItem;
    protected MXChatVideoItem chatVideoItem;
    protected View unreadCircle;
    protected MXImageView usAvatar;
    protected RelativeLayout chatBox;

    protected int mMinItemWidth;
    protected int mMaxItemWidth;

    protected int mImageWidth;
    protected int mImageHeight;

    protected Callback mCallback;

    public MXBaseBubbleItem(Context context, Callback callback) {
        super(context);
        mCallback = callback;
    }

    @Override
    protected void initView() {
        contentText = getViewById(R.id.content_text);
        contentImage = getViewById(R.id.content_pic);
        voiceContentTv = getViewById(R.id.tv_voice_content);
        voiceAnimIv = getViewById(R.id.iv_voice_anim);
        voiceContainerRl = getViewById(R.id.rl_voice_container);
        chatFileItem = getViewById(R.id.file_container);
        chatVideoItem = getViewById(R.id.video_container);
        usAvatar = getViewById(R.id.us_avatar_iv);
        chatBox = getViewById(R.id.chat_box);
    }

    @Override
    protected void processLogic() {
        int screenWidth = MXUtils.getScreenWidth(getContext());
        mMaxItemWidth = (int) (screenWidth * 0.5f);
        mMinItemWidth = (int) (screenWidth * 0.18f);

        mImageWidth = screenWidth / 3;
        mImageHeight = mImageWidth;
    }

    protected void applyConfig(boolean isisLeft) {
        configChatBubbleBg(contentText, isisLeft);
        configChatBubbleTextColor(contentText, isisLeft);
        configChatBubbleBg(voiceContentTv, isisLeft);
        configChatBubbleTextColor(voiceContentTv, isisLeft);
    }

    /**
     * 如果开发者有配置气泡的颜色，改变气泡颜色
     *
     * @param view
     * @param isLeft
     */
    private void configChatBubbleBg(View view, boolean isLeft) {
        if (isLeft) {
            if (!TextUtils.isEmpty(MXConfig.ui.leftChatBubbleColor)) {
                MXUtils.applyCustomUITintDrawable(view, Color.parseColor(MXConfig.ui.leftChatBubbleColor));
            } else {
                MXUtils.applyCustomUITintDrawable(view, R.color.mx_chat_left_bubble_final, R.color.mx_chat_left_bubble, MXConfig.ui.leftChatBubbleColorResId);
            }
        } else {
            if (!TextUtils.isEmpty(MXConfig.ui.rightChatBubbleColor)) {
                MXUtils.applyCustomUITintDrawable(view, Color.parseColor(MXConfig.ui.rightChatBubbleColor));
            } else {
                MXUtils.applyCustomUITintDrawable(view, R.color.mx_chat_right_bubble_final, R.color.mx_chat_right_bubble, MXConfig.ui.rightChatBubbleColorResId);
            }
        }
    }

    /**
     * 如果开发者有配置气泡内文字的颜色，改变气泡文字的颜色
     */
    private void configChatBubbleTextColor(TextView textView, boolean isLeft) {
        if (isLeft) {
            if (!TextUtils.isEmpty(MXConfig.ui.leftChatTextColor)) {
                MXUtils.applyCustomUITextAndImageColor(Color.parseColor(MXConfig.ui.leftChatTextColor), null, textView);
            } else {
                MXUtils.applyCustomUITextAndImageColor(R.color.mx_chat_left_textColor, MXConfig.ui.leftChatTextColorResId, null, textView);
            }
        } else {
            if (!TextUtils.isEmpty(MXConfig.ui.rightChatTextColor)) {
                MXUtils.applyCustomUITextAndImageColor(Color.parseColor(MXConfig.ui.rightChatTextColor), null, textView);
            } else {
                MXUtils.applyCustomUITextAndImageColor(R.color.mx_chat_right_textColor, MXConfig.ui.rightChatTextColorResId, null, textView);
            }
        }
    }

    public void setMessage(BaseMessage baseMessage, int position, Activity activity) {
        handleVisibilityByContentType(baseMessage);
        fillContent(baseMessage, position, activity);
    }

    /**
     * 根据消息类型，显示 item
     */
    private void handleVisibilityByContentType(BaseMessage baseMessage) {
        contentText.setVisibility(View.GONE);
        contentImage.setVisibility(View.GONE);
        voiceContainerRl.setVisibility(View.GONE);
        chatFileItem.setVisibility(View.GONE);
        chatVideoItem.setVisibility(View.GONE);
        switch (baseMessage.getContentType()) {
            case BaseMessage.TYPE_CONTENT_TEXT:
                contentText.setVisibility(View.VISIBLE);
                break;
            case BaseMessage.TYPE_CONTENT_PHOTO:
                contentImage.setVisibility(View.VISIBLE);
                break;
            case BaseMessage.TYPE_CONTENT_VOICE:
                voiceContainerRl.setVisibility(View.VISIBLE);
                break;
            case BaseMessage.TYPE_CONTENT_FILE:
                chatFileItem.setVisibility(View.VISIBLE);
                break;
            case BaseMessage.TYPE_CONTENT_VIDEO:
                chatVideoItem.setVisibility(VISIBLE);
                break;
            default:
                contentText.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void fillContent(BaseMessage baseMessage, final int position, Activity activity) {
        if (!TextUtils.isEmpty(baseMessage.getAvatar())) {
            MXImage.displayImage(activity, usAvatar, baseMessage.getAvatar(), R.drawable.mx_ic_holder_avatar, R.drawable.mx_ic_holder_avatar, 100, 100, null);
        }

        switch (baseMessage.getContentType()) {
            case BaseMessage.TYPE_CONTENT_TEXT:
                if (!TextUtils.isEmpty(baseMessage.getContent())) {
                    contentText.setText(MXEmotionUtil.getEmotionText(getContext(), baseMessage.getContent(), 20));
                }
                break;
            // 图片
            case BaseMessage.TYPE_CONTENT_PHOTO:
                String path = ((PhotoMessage) baseMessage).getLocalPath();
                boolean isLocalImageExist = MXUtils.isFileExist(path);

                final String url;
                if (isLocalImageExist) {
                    url = ((PhotoMessage) baseMessage).getLocalPath();
                } else {
                    url = ((PhotoMessage) baseMessage).getUrl();
                }

                MXImage.displayImage(activity, contentImage, url, R.drawable.mx_ic_holder_light, R.drawable.mx_ic_holder_light, mImageWidth, mImageHeight, new MXImageLoader.MQDisplayImageListener() {
                    @Override
                    public void onSuccess(View view, final String sucUrl) {
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (mCallback.isLastItemAndVisible(position)) {
                                    mCallback.scrollContentToBottom();
                                }
                            }
                        }, 500);

                        view.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                mCallback.photoPreview(url);
                            }
                        });
                    }
                });
                break;
            // 语音
            case BaseMessage.TYPE_CONTENT_VOICE:
                handleBindVoiceItem((VoiceMessage) baseMessage, position);
                break;
            // 文件
            case BaseMessage.TYPE_CONTENT_FILE:
                handleBindFileItem((FileMessage) baseMessage);
                break;
            // 视频
            case BaseMessage.TYPE_CONTENT_VIDEO:
                handleBindVideoItem((VideoMessage) baseMessage);
                break;
            // 默认文字消息处理
            default:
                contentText.setText(getResources().getString(R.string.mx_unknown_msg_tip));
                break;
        }
    }

    private void handleBindFileItem(final FileMessage fileMessage) {
        chatFileItem.initFileItem(this, fileMessage);
        switch (fileMessage.getFileState()) {
            case FileMessage.FILE_STATE_NOT_EXIST:
                chatFileItem.downloadInitState();
                break;
            case FileMessage.FILE_STATE_DOWNLOADING:
                chatFileItem.downloadingState();
                chatFileItem.setProgress(fileMessage.getProgress());
                break;
            case FileMessage.FILE_STATE_FINISH:
                chatFileItem.downloadSuccessState();
                break;
            case FileMessage.FILE_STATE_FAILED:
                chatFileItem.downloadFailedState();
                break;
        }
    }

    private void handleBindVideoItem(VideoMessage videoMessage) {
        chatVideoItem.setVideoMessage(videoMessage);
    }

    /**
     * 处理绑定声音类型的数据item
     *
     * @param voiceMessage
     * @param position
     */
    private void handleBindVoiceItem(final VoiceMessage voiceMessage, final int position) {
        voiceContainerRl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClickVoiceBtn(voiceMessage, position);
            }
        });

        // 处理录音文本和控件长度
        String duration = voiceMessage.getDuration() == VoiceMessage.NO_DURATION ? "" : voiceMessage.getDuration() + "s";
        voiceContentTv.setText(duration);
        ViewGroup.LayoutParams layoutParams = voiceContainerRl.getLayoutParams();
        if (voiceMessage.getDuration() == VoiceMessage.NO_DURATION) {
            voiceContentTv.setText("");
            layoutParams.width = mMinItemWidth;
        } else {
            voiceContentTv.setText(voiceMessage.getDuration() + "\"");
            layoutParams.width = (int) (mMinItemWidth + (mMaxItemWidth / 60f * voiceMessage.getDuration()));
        }
        voiceContainerRl.setLayoutParams(layoutParams);


        // 刷新录音播放状态
        if (mCallback.getCurrentPlayingItemPosition() != position) {
            if (voiceMessage.getItemViewType() == BaseMessage.TYPE_AGENT) {
                voiceAnimIv.setImageResource(R.drawable.mx_voice_left_normal);
                voiceAnimIv.setColorFilter(getResources().getColor(R.color.mx_chat_left_textColor));
            } else {
                voiceAnimIv.setImageResource(R.drawable.mx_voice_right_normal);
                voiceAnimIv.setColorFilter(getResources().getColor(R.color.mx_chat_right_textColor));
            }
        } else {
            if (voiceMessage.getItemViewType() == BaseMessage.TYPE_AGENT) {
                voiceAnimIv.setImageResource(R.drawable.mx_anim_voice_left_playing);
            } else {
                voiceAnimIv.setImageResource(R.drawable.mx_anim_voice_right_playing);
            }
            AnimationDrawable animationDrawable = (AnimationDrawable) voiceAnimIv.getDrawable();
            animationDrawable.start();
        }

        // 语音未读显示 小红点
        if (unreadCircle != null) {
            if (!voiceMessage.isRead()) {
                unreadCircle.setVisibility(View.VISIBLE);
            } else {
                unreadCircle.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 处理播放录音按钮的点击事件
     *
     * @param voiceMessage
     * @param position
     */
    private void handleClickVoiceBtn(VoiceMessage voiceMessage, int position) {
        if (TextUtils.isEmpty(voiceMessage.getLocalPath())) {
            mCallback.stopPlayVoice();

            downloadAndPlayVoice(voiceMessage, position);
            return;
        }

        if (MXAudioPlayerManager.isPlaying() && mCallback.getCurrentPlayingItemPosition() == position) {
            // 如果正在播放录音，并且当前正在播放录音的item是当前item，则停止播放录音

            mCallback.stopPlayVoice();
        } else {
            mCallback.startPlayVoiceAndRefreshList(voiceMessage, position);
        }
    }

    /**
     * 下载录音文件，并设置录音时长
     *
     * @param voiceMessage
     */
    private void downloadAndPlayVoice(final VoiceMessage voiceMessage, final int position) {
        mCallback.setCurrentDownloadingItemPosition(position);
        MXDownloadManager.getInstance(getContext()).downloadVoice(voiceMessage.getUrl(), new MXDownloadManager.Callback() {
            @Override
            public void onSuccess(File file) {
                mCallback.setVoiceMessageDuration(voiceMessage, file.getAbsolutePath());
                post(new Runnable() {
                    @Override
                    public void run() {
                        // 如果该文件对应的数据item的索引等于当前正在下载文件的索引，则播放录音
                        if (mCallback.getCurrentDownloadingItemPosition() == position) {
                            mCallback.startPlayVoiceAndRefreshList(voiceMessage, position);
                        }
                    }
                });
            }

            @Override
            public void onFailure() {
                MXUtils.showSafe(getContext(), R.string.mx_download_audio_failure);
            }
        });
    }

    @Override
    public void notifyDataSetChanged() {
        mCallback.notifyDataSetChanged();
    }

    @Override
    public void onFileMessageDownloadFailure(FileMessage fileMessage, int code, String message) {
        mCallback.onFileMessageDownloadFailure(fileMessage, code, message);
    }

    @Override
    public void onFileMessageExpired(FileMessage fileMessage) {
        mCallback.onFileMessageExpired(fileMessage);
    }

    public interface Callback {
        /**
         * 更新数据列表
         */
        void notifyDataSetChanged();

        /**
         * 滚动内容到底部
         */
        void scrollContentToBottom();

        /**
         * 获取当前正在播放的录音消息的位置
         *
         * @return
         */
        int getCurrentPlayingItemPosition();

        /**
         * 设置当前正在下载的录音消息的位置
         *
         * @param currentPlayingItemPosition
         * @return
         */
        void setCurrentDownloadingItemPosition(int currentPlayingItemPosition);

        /**
         * 获取当前正在下载的录音消息的位置
         */
        int getCurrentDownloadingItemPosition();

        /**
         * 是否是最后一个item，并且可见
         *
         * @param position
         * @return
         */
        boolean isLastItemAndVisible(int position);

        /**
         * 预览图片
         *
         * @param url 图片路径
         */
        void photoPreview(String url);

        /**
         * 停止播放录音
         */
        void stopPlayVoice();

        /**
         * 可是播放录音并刷新列表
         *
         * @param voiceMessage
         * @param position
         */
        void startPlayVoiceAndRefreshList(VoiceMessage voiceMessage, int position);

        /**
         * 设置录音本地文件地址和时长
         *
         * @param voiceMessage
         * @param audioFilePath
         */
        void setVoiceMessageDuration(VoiceMessage voiceMessage, String audioFilePath);

        /**
         * 重新发送失败的消息
         *
         * @param failedMessage
         */
        void resendFailedMessage(BaseMessage failedMessage);

        void onFileMessageDownloadFailure(FileMessage fileMessage, int code, String message);

        void onFileMessageExpired(FileMessage fileMessage);

        void onClueCardMessageSendSuccess(BaseMessage message);
    }
}
