package com.mixdesk.mixdesksdk.util;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.mixdesk.mixdesksdk.R;
import com.mixdesk.mixdesksdk.activity.MXConversationActivity;
import com.mixdesk.mixdesksdk.activity.MXPhotoPreviewActivity;
//import com.mixdesk.mixdesksdk.chatitem.MQClueCardItem;
import com.mixdesk.mixdesksdk.chatitem.MXConvDividerItem;
import com.mixdesk.mixdesksdk.chatitem.MXHybridItem;
import com.mixdesk.mixdesksdk.model.BaseMessage;
//import com.mixdesk.mixdesksdk.model.ClueCardMessage;
import com.mixdesk.mixdesksdk.model.EvaluateMessage;
import com.mixdesk.mixdesksdk.model.FileMessage;
import com.mixdesk.mixdesksdk.model.HybridMessage;
//import com.mixdesk.mixdesksdk.model.RobotMessage;
import com.mixdesk.mixdesksdk.model.TipMessage;
import com.mixdesk.mixdesksdk.model.VoiceMessage;
import com.mixdesk.mixdesksdk.chatitem.MXAgentItem;
import com.mixdesk.mixdesksdk.chatitem.MXBaseBubbleItem;
import com.mixdesk.mixdesksdk.chatitem.MXClientItem;
import com.mixdesk.mixdesksdk.chatitem.MXEvaluateItem;
//import com.mixdesk.mixdesksdk.chatitem.MQNoAgentItem;
//import com.mixdesk.mixdesksdk.chatitem.MQRobotItem;
import com.mixdesk.mixdesksdk.chatitem.MXTimeItem;
import com.mixdesk.mixdesksdk.chatitem.MXTipItem;

import java.io.File;
import java.util.List;

public class MXChatAdapter extends BaseAdapter implements MXBaseBubbleItem.Callback {
    private static final String TAG = MXChatAdapter.class.getSimpleName();

    private final MXConversationActivity mConversationActivity;
    private final List<BaseMessage> mMessageList;
    private final ListView mListView;

    private static final int NO_POSITION = -1;
    private int mCurrentPlayingItemPosition = NO_POSITION;
    private final int mCurrentDownloadingItemPosition = NO_POSITION;

    private final Runnable mNotifyDataSetChangedRunnable = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged();
        }
    };

    public MXChatAdapter(MXConversationActivity conversationActivity, List<BaseMessage> messageList, ListView listView) {
        mConversationActivity = conversationActivity;
        mMessageList = messageList;
        mListView = listView;
    }

    public void addMQMessage(BaseMessage baseMessage) {
        mMessageList.add(baseMessage);
        notifyDataSetChanged();
    }

    public void addMQMessage(BaseMessage baseMessage, int location) {
        mMessageList.add(location, baseMessage);
        notifyDataSetChanged();
    }

    public void loadMoreMessage(List<BaseMessage> baseMessages) {
        mMessageList.addAll(0, baseMessages);
        notifyDataSetChanged();
        downloadAndNotifyDataSetChanged(baseMessages);
    }

    @Override
    public int getItemViewType(int position) {
        return mMessageList.get(position).getItemViewType();
    }

    @Override
    public int getViewTypeCount() {
        return BaseMessage.MAX_TYPE;
    }

    @Override
    public int getCount() {
        return mMessageList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final BaseMessage mcMessage = mMessageList.get(position);

        if (convertView == null) {
            switch (getItemViewType(position)) {
                case BaseMessage.TYPE_AGENT:
                    convertView = new MXAgentItem(mConversationActivity, this);
                    break;
                case BaseMessage.TYPE_CLIENT:
                    convertView = new MXClientItem(mConversationActivity, this);
                    break;
                case BaseMessage.TYPE_TIME:
                    convertView = new MXTimeItem(mConversationActivity);
                    break;
                case BaseMessage.TYPE_TIP:
                    convertView = new MXTipItem(mConversationActivity);
                    break;
                case BaseMessage.TYPE_EVALUATE:
                    convertView = new MXEvaluateItem(mConversationActivity);
                    break;
                case BaseMessage.TYPE_HYBRID:
                    convertView = new MXHybridItem(mConversationActivity, mConversationActivity);
                    break;
                case BaseMessage.TYPE_RICH_TEXT:
                    convertView = new MXHybridItem(mConversationActivity, mConversationActivity);
                    break;
                case BaseMessage.TYPE_CONV_DIVIDER:
                    convertView = new MXConvDividerItem(mConversationActivity, mcMessage.getCreatedOn());
                    break;
            }
        }

        if (getItemViewType(position) == BaseMessage.TYPE_AGENT) {
            ((MXAgentItem) convertView).setMessage(mcMessage, position, mConversationActivity);
        } else if (getItemViewType(position) == BaseMessage.TYPE_CLIENT) {
            ((MXClientItem) convertView).setMessage(mcMessage, position, mConversationActivity);
        } else if (getItemViewType(position) == BaseMessage.TYPE_HYBRID) {
            ((MXHybridItem) convertView).setMessage((HybridMessage) mcMessage, mConversationActivity);
        } else if (getItemViewType(position) == BaseMessage.TYPE_TIME) {
            ((MXTimeItem) convertView).setMessage(mcMessage);
        } else if (getItemViewType(position) == BaseMessage.TYPE_TIP) {
            ((MXTipItem) convertView).setMessage(mcMessage);
        } else if (getItemViewType(position) == BaseMessage.TYPE_EVALUATE) {
            ((MXEvaluateItem) convertView).setMessage((EvaluateMessage) mcMessage);
        } else if (getItemViewType(position) == BaseMessage.TYPE_RICH_TEXT) {
            ((MXHybridItem) convertView).setMessage((HybridMessage) mcMessage, mConversationActivity);
        }

        return convertView;
    }

    public void downloadAndNotifyDataSetChanged(List<BaseMessage> baseMessages) {
        for (BaseMessage baseMessage : baseMessages) {
            if (baseMessage instanceof VoiceMessage) {
                final VoiceMessage voiceMessage = (VoiceMessage) baseMessage;
                // 根据本地文件路径判断本地文件是否存在
                File localFile = null;
                if (!TextUtils.isEmpty(voiceMessage.getLocalPath())) {
                    localFile = new File(voiceMessage.getLocalPath());
                }

                // 如果本地文件存在则直接赋值，如果本地文件不存在则根据url获取文件
                File voiceFile;
                if (localFile != null && localFile.exists()) {
                    voiceFile = localFile;
                } else {
                    voiceFile = MXAudioRecorderManager.getCachedVoiceFileByUrl(mConversationActivity, voiceMessage.getUrl());
                }

                // 如果声音文件已经存在则不下载
                if (voiceFile != null && voiceFile.exists()) {
                    setVoiceMessageDuration(voiceMessage, voiceFile.getAbsolutePath());
                    notifyDataSetChanged();
                } else {
                    MXDownloadManager.getInstance(mConversationActivity).downloadVoice(voiceMessage.getUrl(), new MXDownloadManager.Callback() {
                        @Override
                        public void onSuccess(File file) {
                            setVoiceMessageDuration(voiceMessage, file.getAbsolutePath());
                            mListView.post(mNotifyDataSetChangedRunnable);
                        }

                        @Override
                        public void onFailure() {
                        }
                    });
                }
            }
        }
    }

    @Override
    public void setVoiceMessageDuration(VoiceMessage voiceMessage, String audioFilePath) {
        voiceMessage.setLocalPath(audioFilePath);
        voiceMessage.setDuration(MXAudioPlayerManager.getDurationByFilePath(mConversationActivity, audioFilePath));
    }

    @Override
    public void scrollContentToBottom() {
        mConversationActivity.scrollContentToBottom();
    }

    @Override
    public boolean isLastItemAndVisible(int position) {
        return position == mListView.getLastVisiblePosition() && mListView.getLastVisiblePosition() == getCount() - 1;
    }

    @Override
    public void photoPreview(String url) {
        mConversationActivity.startActivity(MXPhotoPreviewActivity.newIntent(mConversationActivity, MXUtils.getImageDir(mConversationActivity), url));
    }

    @Override
    public void startPlayVoiceAndRefreshList(VoiceMessage voiceMessage, int position) {
        MXAudioPlayerManager.playSound(voiceMessage.getLocalPath(), new MXAudioPlayerManager.Callback() {
            @Override
            public void onError() {
                mCurrentPlayingItemPosition = NO_POSITION;
                notifyDataSetChanged();
            }

            @Override
            public void onCompletion() {
                mCurrentPlayingItemPosition = NO_POSITION;
                notifyDataSetChanged();
            }
        });

        // 设置已读状态
        voiceMessage.setIsRead(true);
        MXConfig.getController(mConversationActivity).updateMessage(voiceMessage.getId(), true);

        mCurrentPlayingItemPosition = position;
        notifyDataSetChanged();
    }

    @Override
    public void stopPlayVoice() {
        MXAudioPlayerManager.stop();
        mCurrentPlayingItemPosition = NO_POSITION;
        notifyDataSetChanged();
    }

    @Override
    public void setCurrentDownloadingItemPosition(int currentPlayingItemPosition) {
        mCurrentPlayingItemPosition = currentPlayingItemPosition;
    }

    @Override
    public int getCurrentDownloadingItemPosition() {
        return mCurrentDownloadingItemPosition;
    }

    @Override
    public int getCurrentPlayingItemPosition() {
        return mCurrentPlayingItemPosition;
    }

    @Override
    public void resendFailedMessage(BaseMessage failedMessage) {
        notifyDataSetInvalidated();
        mConversationActivity.resendMessage(failedMessage);
    }

    @Override
    public void onFileMessageDownloadFailure(FileMessage fileMessage, int code, String message) {
        mConversationActivity.onFileMessageDownloadFailure(fileMessage, code, message);
    }

    @Override
    public void onFileMessageExpired(FileMessage fileMessage) {
        mConversationActivity.onFileMessageExpired(fileMessage);
    }

    @Override
    public void onClueCardMessageSendSuccess(BaseMessage message) {
        mMessageList.remove(message);
        TipMessage clueCardSendTipMessage = new TipMessage();
        clueCardSendTipMessage.setContent(mConversationActivity.getString(R.string.mx_submit_success));
        mMessageList.add(clueCardSendTipMessage);
        notifyDataSetChanged();
    }
}
