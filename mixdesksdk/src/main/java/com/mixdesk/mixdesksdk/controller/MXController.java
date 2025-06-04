package com.mixdesk.mixdesksdk.controller;


import com.mixdesk.core.bean.MXEnterpriseConfig;
import com.mixdesk.mixdesksdk.callback.OnClientOnlineCallback;
import com.mixdesk.mixdesksdk.callback.OnDownloadFileCallback;
import com.mixdesk.mixdesksdk.callback.OnEvaluateConfigCallback;
import com.mixdesk.mixdesksdk.callback.OnGetMessageListCallBack;
import com.mixdesk.mixdesksdk.callback.OnMessageSendCallback;
import com.mixdesk.mixdesksdk.callback.SimpleCallback;
import com.mixdesk.mixdesksdk.model.Agent;
import com.mixdesk.mixdesksdk.model.BaseMessage;

import java.util.List;
import java.util.Map;

public interface MXController {
    void sendMessage(BaseMessage baseMessage, OnMessageSendCallback onMessageSendCallback);

    void resendMessage(BaseMessage baseMessage, OnMessageSendCallback onMessageSendCallback);

    /**
     * 从服务器获取历史消息
     *
     * @param messageCreateOn          获取该日期之前的消息
     * @param length                   获取的消息长度
     * @param onGetMessageListCallBack 回调
     */
    void getMessageFromService(final long messageCreateOn, final int length, final OnGetMessageListCallBack onGetMessageListCallBack);

    /**
     * 从本地服务器取历史消息
     *
     * @param lastMessageCreateOn      获取该日期之前的消息
     * @param length                   获取的消息长度
     * @param onGetMessageListCallBack 回调
     */
    void getMessagesFromDatabase(final long lastMessageCreateOn, final int length, final OnGetMessageListCallBack onGetMessageListCallBack);

    /**
     * 设置顾客上线
     *
     * @param clientId               Mixdesk顾客 id：如果传了，将用Mixdesk id 上线
     * @param customizedId           开发者用户 id：如果传了，将绑定开发者 id 上线
     * @param onClientOnlineCallback 回调
     */
    void setCurrentClientOnline(String clientId, String customizedId, OnClientOnlineCallback onClientOnlineCallback);

    /**
     * 设置当前顾客的自定义信息
     *
     * @param clientInfo           当前顾客的自定义信息
     * @param onClientInfoCallback 回调
     */
    void setClientInfo(Map<String, String> clientInfo, SimpleCallback onClientInfoCallback);

    /**
     * 更新当前顾客的自定义信息
     *
     * @param updateClientInfo     当前顾客的自定义信息
     * @param onClientInfoCallback 回调
     */
    void updateClientInfo(Map<String, String> updateClientInfo, SimpleCallback onClientInfoCallback);

    /**
     * 发送「顾客正在输入」状态
     *
     * @param content 内容
     */
    void sendClientInputtingWithContent(String content);

    /**
     * 获取评价配置
     *
     * @param evaluationType 评价等级
     * @param callback       回调
     */
    void getEvaluateConfig(Integer evaluationType, OnEvaluateConfigCallback callback);

    /**
     * 对客服进行评价
     *
     * @param conversationId 当前会话id
     * @param level          评价的等级
     * @param content        评价的内容
     * @param isSolved       是否解决 -1 未选择
     * @param tagIds         选择的tag
     * @param evaluateLevel  评价的等级配置
     * @param simpleCallback 评价的回调接口
     */
    void executeEvaluate(String conversationId, int isSolved, int level, List<Integer> tagIds,
                         String content, int evaluateLevel, SimpleCallback simpleCallback);

    /**
     * 关闭Mixdesk服务
     */
    void closeService();

    /**
     * 打开Mixdesk服务
     */
    void openService();

    /**
     * 获取当前客服
     *
     * @return 当前客服，如果没有返回 null
     */
    Agent getCurrentAgent();

    void updateMessage(long messageId, boolean isRead);

    /**
     * 保存聊天界面不可见时的最后一条消息的时间
     *
     * @param stopTime
     */
    void saveConversationOnStopTime(long stopTime);

    /**
     * 保存聊天界面收到最后一条消息的时间
     *
     * @param lastMessageTime
     */
    void saveConversationLastMessageTime(long lastMessageTime);

    /**
     * 下载文件
     *
     * @param fileMessage            文件消息
     * @param onDownloadFileCallback 回调
     */
    void downloadFile(BaseMessage fileMessage, OnDownloadFileCallback onDownloadFileCallback);

    /**
     * 取消下载
     *
     * @param url 下载 url
     */
    void cancelDownload(String url);

    /**
     * 刷新企业配置信息
     *
     * @param simpleCallback
     */
    void refreshEnterpriseConfig(SimpleCallback simpleCallback);

    MXEnterpriseConfig getEnterpriseConfig();

    /**
     * 对话界面开启时候的回调
     */
    void onConversationClose();

    /**
     * 对话界面关闭时候的回调
     */
    void onConversationOpen();

    void onConversationStart();

    void onConversationStop();

    /**
     * 获取当前用户id
     *
     * @return
     */
    String getCurrentClientId();

    void sendQuickBtnClicked(long convId, Integer quickBtnId);
}
