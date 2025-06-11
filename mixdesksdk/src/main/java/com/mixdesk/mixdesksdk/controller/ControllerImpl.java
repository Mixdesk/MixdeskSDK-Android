package com.mixdesk.mixdesksdk.controller;

import android.content.Context;
import android.text.TextUtils;

import com.mixdesk.core.MXManager;
import com.mixdesk.core.bean.MXAgent;
import com.mixdesk.core.bean.MXEnterpriseConfig;
import com.mixdesk.core.bean.MXMessage;
import com.mixdesk.core.bean.MXEvaluateConfig;
import com.mixdesk.core.callback.OnClientInfoCallback;
import com.mixdesk.core.callback.OnGetMessageListCallback;
import com.mixdesk.core.callback.OnProgressCallback;
import com.mixdesk.core.callback.OnGetEvaluateConfigCallback;
import com.mixdesk.mixdesksdk.callback.OnClientOnlineCallback;
import com.mixdesk.mixdesksdk.callback.OnDownloadFileCallback;
import com.mixdesk.mixdesksdk.callback.OnEvaluateConfigCallback;
import com.mixdesk.mixdesksdk.callback.OnGetMessageListCallBack;
import com.mixdesk.mixdesksdk.callback.OnMessageSendCallback;
import com.mixdesk.mixdesksdk.callback.SimpleCallback;
import com.mixdesk.mixdesksdk.model.Agent;
import com.mixdesk.mixdesksdk.model.BaseMessage;
import com.mixdesk.mixdesksdk.model.PhotoMessage;
import com.mixdesk.mixdesksdk.model.VideoMessage;
import com.mixdesk.mixdesksdk.model.VoiceMessage;
import com.mixdesk.mixdesksdk.util.MXUtils;

import java.util.List;
import java.util.Map;

public class ControllerImpl implements MXController {

    public Context context;

    public ControllerImpl(Context context) {
        this.context = context;
    }

    @Override
    public void sendMessage(final BaseMessage message, final OnMessageSendCallback onMessageSendCallback) {
        // 发送回调
        com.mixdesk.core.callback.OnMessageSendCallback onMQMessageSendCallback = new com.mixdesk.core.callback.OnMessageSendCallback() {
            @Override
            public void onSuccess(MXMessage mcMessage, int state) {
                MXUtils.parseMQMessageIntoBaseMessage(mcMessage, message);
                if (onMessageSendCallback != null) {
                    onMessageSendCallback.onSuccess(message, state);
                }
            }

            @Override
            public void onFailure(MXMessage failureMessage, int code, String response) {
                MXUtils.parseMQMessageIntoBaseMessage(failureMessage, message);
                if (onMessageSendCallback != null) {
                    onMessageSendCallback.onFailure(message, code, response);
                }
            }
        };

        // 开始发送
        if (BaseMessage.TYPE_CONTENT_TEXT.equals(message.getContentType())) {
            String content = message.getContent();
            MXManager.getInstance(context).sendTextMessage(content, onMQMessageSendCallback);
        } else if (BaseMessage.TYPE_CONTENT_PHOTO.equals(message.getContentType())) {
            PhotoMessage photoMessage = (PhotoMessage) message;
            MXManager.getInstance(context).sendPhotoMessage(photoMessage.getLocalPath(), onMQMessageSendCallback);
        } else if (BaseMessage.TYPE_CONTENT_VOICE.equals(message.getContentType())) {
            VoiceMessage voiceMessage = (VoiceMessage) message;
            MXManager.getInstance(context).sendVoiceMessage(voiceMessage.getLocalPath(), onMQMessageSendCallback);
        } else if (BaseMessage.TYPE_CONTENT_VIDEO.equals(message.getContentType())) {
            VideoMessage voiceMessage = (VideoMessage) message;
            MXManager.getInstance(context).sendVideoMessage(voiceMessage.getLocalPath(), onMQMessageSendCallback);
        } else if (BaseMessage.TYPE_CONTENT_HYBRID.equals(message.getContentType())) {
            message.setStatus(BaseMessage.STATE_FAILED);
            onMessageSendCallback.onFailure(message, 0, "");
        }
    }

    @Override
    public void resendMessage(final BaseMessage baseMessage, final OnMessageSendCallback onMessageSendCallback) {
        final long preId = baseMessage.getId();
        sendMessage(baseMessage, new OnMessageSendCallback() {
            @Override
            public void onSuccess(BaseMessage message, int state) {
                if (onMessageSendCallback != null) {
                    onMessageSendCallback.onSuccess(message, state);
                }
                // 重发成功后删除之前保存的消息
                MXManager.getInstance(context).deleteMessage(preId);
            }

            @Override
            public void onFailure(BaseMessage failureMessage, int code, String failureInfo) {
                if (onMessageSendCallback != null) {
                    onMessageSendCallback.onFailure(failureMessage, code, failureInfo);
                }
                // 重发失败后删除之前保存的消息
                MXManager.getInstance(context).deleteMessage(preId);
            }
        });
    }

    @Override
    public void getMessageFromService(long lastMessageCreateOn, int length, final OnGetMessageListCallBack onGetMessageListCallBack) {
        MXManager.getInstance(context).getMessageFromService(lastMessageCreateOn, length, new OnGetMessageListCallback() {
            @Override
            public void onSuccess(List<MXMessage> MXMessageList) {
                List<BaseMessage> messageList = MXUtils.parseMQMessageToChatBaseList(MXMessageList);
                if (onGetMessageListCallBack != null) {
                    onGetMessageListCallBack.onSuccess(messageList);
                }
            }

            @Override
            public void onFailure(int code, String message) {
                if (onGetMessageListCallBack != null) {
                    onGetMessageListCallBack.onFailure(code, message);
                }
            }
        });
    }

    @Override
    public void getMessagesFromDatabase(long lastMessageCreateOn, int length, final OnGetMessageListCallBack onGetMessageListCallBack) {
        MXManager.getInstance(context).getMessageFromDatabase(lastMessageCreateOn, length, new OnGetMessageListCallback() {

            @Override
            public void onSuccess(List<MXMessage> MXMessageList) {
                List<BaseMessage> messageList = MXUtils.parseMQMessageToChatBaseList(MXMessageList);
                if (onGetMessageListCallBack != null) {
                    onGetMessageListCallBack.onSuccess(messageList);
                }
            }

            @Override
            public void onFailure(int code, String message) {
                if (onGetMessageListCallBack != null) {
                    onGetMessageListCallBack.onFailure(code, message);
                }
            }
        });
    }

    @Override
    public void setCurrentClientOnline(String clientId, String customizedId, final OnClientOnlineCallback onClientOnlineCallback) {
        // scheduler
        com.mixdesk.core.callback.OnClientOnlineCallback onlineCallback = new com.mixdesk.core.callback.OnClientOnlineCallback() {
            @Override
            public void onSuccess(MXAgent mqAgent, String conversationId, List<MXMessage> conversationMessageList, boolean isShowTransferHumanBtn) {
                Agent agent = MXUtils.parseMQAgentToAgent(mqAgent);
                List<BaseMessage> messageList = MXUtils.parseMQMessageToChatBaseList(conversationMessageList);
                if (onClientOnlineCallback != null) {
                    onClientOnlineCallback.onSuccess(agent, conversationId, messageList, isShowTransferHumanBtn);
                }
            }

            @Override
            public void onFailure(int code, String message) {
                if (onClientOnlineCallback != null) {
                    onClientOnlineCallback.onFailure(code, message);
                }
            }
        };

        if (!TextUtils.isEmpty(clientId)) {
            MXManager.getInstance(context).setClientOnlineWithClientId(clientId, onlineCallback);
        } else if (!TextUtils.isEmpty(customizedId)) {
            MXManager.getInstance(context).setClientOnlineWithCustomizedId(customizedId, onlineCallback);
        } else {
            MXManager.getInstance(context).setCurrentClientOnline(onlineCallback);
        }
    }

    @Override
    public void setClientInfo(Map<String, String> clientInfo, final SimpleCallback onClientInfoCallback) {
        MXManager.getInstance(context).setClientInfo(clientInfo, new OnClientInfoCallback() {
            @Override
            public void onSuccess() {
                if (onClientInfoCallback != null) {
                    onClientInfoCallback.onSuccess();
                }
            }

            @Override
            public void onFailure(int code, String message) {
                if (onClientInfoCallback != null) {
                    onClientInfoCallback.onFailure(code, message);
                }
            }
        });
    }

    @Override
    public void updateClientInfo(Map<String, String> updateClientInfo, final SimpleCallback onClientInfoCallback) {
        MXManager.getInstance(context).updateClientInfo(updateClientInfo, new OnClientInfoCallback() {
            @Override
            public void onSuccess() {
                if (onClientInfoCallback != null) {
                    onClientInfoCallback.onSuccess();
                }
            }

            @Override
            public void onFailure(int code, String message) {
                if (onClientInfoCallback != null) {
                    onClientInfoCallback.onFailure(code, message);
                }
            }
        });
    }

    @Override
    public void sendClientInputtingWithContent(String content) {
        MXManager.getInstance(context).sendClientInputtingWithContent(content);
    }

    @Override
    public void getEvaluateConfig(Integer evaluationType, OnEvaluateConfigCallback callback) {
        MXManager.getInstance(context).getEvaluateConfig(evaluationType, new OnGetEvaluateConfigCallback() {
            @Override
            public void onFailure(int code, String message) {
                if (callback != null) {
                    callback.onFailure(code, message);
                }
            }

            @Override
            public void onSuccess(List<MXEvaluateConfig> config, Boolean fk) {
                if (callback != null) {
                    callback.onSuccess(config);
                }
            }
        });
    }

    @Override
    public void executeEvaluate(String conversationId, int isSolved, int level, List<Integer> tagIds,
                                String content, int evaluateLevel, final SimpleCallback simpleCallback) {
        MXManager.getInstance(context).executeEvaluate(
                conversationId,
                isSolved,
                level,
                tagIds,
                content,
                evaluateLevel,
                new com.mixdesk.core.callback.SimpleCallback() {

                    @Override
                    public void onFailure(int code, String message) {
                        if (simpleCallback != null) {
                            simpleCallback.onFailure(code, message);
                        }
                    }

                    @Override
                    public void onSuccess() {
                        if (simpleCallback != null) {
                            simpleCallback.onSuccess();
                        }
                    }
                });
    }

    @Override
    public Agent getCurrentAgent() {
        MXAgent mqAgent = MXManager.getInstance(context).getCurrentAgent();
        return MXUtils.parseMQAgentToAgent(mqAgent);
    }

    @Override
    public void updateMessage(long messageId, boolean isRead) {
        MXManager.getInstance(context).updateMessage(messageId, isRead);
    }

    @Override
    public void saveConversationOnStopTime(long stopTime) {
        MXManager.getInstance(context).saveConversationOnStopTime(stopTime);
    }

    @Override
    public void saveConversationLastMessageTime(long lastMessageTime) {
        MXManager.getInstance(context).saveConversationLastMessageTime(lastMessageTime);
    }

    @Override
    public void downloadFile(BaseMessage fileMessage, final OnDownloadFileCallback onDownloadFileCallback) {
        MXMessage message = MXUtils.parseBaseMessageToMQMessage(fileMessage);
        MXManager.getInstance(context).downloadFile(message, new OnProgressCallback() {
            @Override
            public void onSuccess() {
                if (onDownloadFileCallback == null) {
                    return;
                }
                onDownloadFileCallback.onSuccess(null);
            }

            @Override
            public void onProgress(int progress) {
                if (onDownloadFileCallback == null) {
                    return;
                }
                onDownloadFileCallback.onProgress(progress);
            }

            @Override
            public void onFailure(int code, String message) {
                if (onDownloadFileCallback == null) {
                    return;
                }
                onDownloadFileCallback.onFailure(code, message);
            }
        });
    }

    @Override
    public void cancelDownload(String url) {
        MXManager.getInstance(context).cancelDownload(url);
    }

    @Override
    public void onConversationClose() {
        MXManager.getInstance(context).onConversationClose();
    }

    @Override
    public void onConversationOpen() {
        MXManager.getInstance(context).onConversationOpen();
    }

    @Override
    public void onConversationStart() {
        MXManager.getInstance(context).onConversationStart();
    }

    @Override
    public void onConversationStop() {
        MXManager.getInstance(context).onConversationStop();
    }

    @Override
    public void closeService() {
        MXManager.getInstance(context).closeService();
    }

    @Override
    public void openService() {
        MXManager.getInstance(context).openService();
    }

    @Override
    public void refreshEnterpriseConfig(final SimpleCallback simpleCallback) {
        MXManager.getInstance(context).refreshEnterpriseConfig(new com.mixdesk.core.callback.SimpleCallback() {
            @Override
            public void onFailure(int code, String message) {
                if (simpleCallback != null) {
                    simpleCallback.onFailure(code, message);
                }
            }

            @Override
            public void onSuccess() {
                if (simpleCallback != null) {
                    simpleCallback.onSuccess();
                }
            }
        });
    }

    @Override
    public MXEnterpriseConfig getEnterpriseConfig() {
        return MXManager.getInstance(context).getEnterpriseConfig();
    }

    @Override
    public String getCurrentClientId() {
        return MXManager.getInstance(context).getCurrentClientId();
    }

    @Override
    public void sendQuickBtnClicked(long convId, Integer quickBtnId) {
        MXManager.getInstance(context).sendQuickBtnClicked(convId, quickBtnId);
    }

}
