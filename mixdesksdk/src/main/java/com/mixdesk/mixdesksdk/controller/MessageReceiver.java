package com.mixdesk.mixdesksdk.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.mixdesk.core.MXMessageManager;
import com.mixdesk.core.bean.MXAgent;
import com.mixdesk.core.bean.MXMessage;
import com.mixdesk.mixdesksdk.model.Agent;
import com.mixdesk.mixdesksdk.model.BaseMessage;
import com.mixdesk.mixdesksdk.util.MXUtils;

public abstract class MessageReceiver extends BroadcastReceiver {
    private String mConversationId;

    public void setConversationId(String conversationId) {
        mConversationId = conversationId;
    }

    // 可以创建通知的消息
//    private boolean isValidMessage(MQMessage mqMessage, boolean isContainsRobotMessage) {
//        if (mqMessage == null) {
//            return false;
//        }
//        boolean isValid = false;
//        if (!TextUtils.isEmpty(mqMessage.getContent())
//                && TextUtils.equals(mqMessage.getFrom_type(), MQMessage.TYPE_FROM_AGENT)
//                && TextUtils.equals(mqMessage.getContent_type(), MQMessage.TYPE_CONTENT_TEXT)
//        ) {
//            return true;
//        }
//        if (isContainsRobotMessage
//                && TextUtils.equals(mqMessage.getFrom_type(), MQMessage.TYPE_FROM_ROBOT)
//                && TextUtils.equals(mqMessage.getSub_type(), MQMessage.SUB_TYPE_MESSAGE)
//        ) {
//            return true;
//        }
//        return isValid;
//    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        MXMessageManager messageManager = MXMessageManager.getInstance(context);
        BaseMessage baseMessage;

        // 接收新消息
        if (MXMessageManager.ACTION_NEW_MESSAGE_RECEIVED.equals(action)) {
            // 从 intent 获取消息 id
            String msgId = intent.getStringExtra("msgId");
            // 从 MCMessageManager 获取消息对象
            MXMessage message = messageManager.getMQMessage(msgId);
            if (message != null) {
//                boolean isValidMessage = isValidMessage(message, true);
                //处理消息，并发送广播
                baseMessage = MXUtils.parseMQMessageToBaseMessage(message);
                if (baseMessage != null) {
                    receiveNewMsg(baseMessage);
                }
            }
        }
        // 撤回消息
        else if (MXMessageManager.ACTION_RECALL_MESSAGE.equals(action)) {
            String nickname = intent.getStringExtra("nickname");
            long id = intent.getLongExtra("id", -1);
            recallMessage(id, nickname);
        }

        // 客服正在输入
        else if (MXMessageManager.ACTION_AGENT_INPUTTING.equals(action)) {
            int duration = intent.getIntExtra("duration", 2);
            changeTitleToInputting(duration);
        }

        // 客服转接
        else if (MXMessageManager.ACTION_AGENT_CHANGE_EVENT.equals(action)) {
            // 更新标题栏
            MXAgent mqAgent = messageManager.getCurrentAgent();

            // 如果顾客被转接，才添加 Tip
            boolean isClientDirect = intent.getBooleanExtra("client_is_redirected", false);
            boolean isShowTransferHumanBtn = intent.getBooleanExtra("show_transfer_human_btn", false);
            if (isClientDirect) {
                addDirectAgentMessageTip(mqAgent.getNickname());
            }

            Agent agent = MXUtils.parseMQAgentToAgent(mqAgent);
            setCurrentAgent(agent);
            setShowTransHumanBtn(isShowTransferHumanBtn);

            String conversationId = intent.getStringExtra("conversation_id");
            if (!TextUtils.isEmpty(conversationId)) {
                mConversationId = conversationId;
                setNewConversationId(conversationId);
            }
        }
        // 评价邀请
        else if (MXMessageManager.ACTION_INVITE_EVALUATION.equals(action)) {
            String conversationId = intent.getStringExtra("conversation_id");
            if (TextUtils.equals(conversationId, mConversationId)) {
                inviteEvaluation();
            }
        }
        // 客服状态变更
        else if (MXMessageManager.ACTION_AGENT_STATUS_UPDATE_EVENT.equals(action)) {
            updateAgentOnlineOfflineStatus();
        }
        // 添加到黑名单
        else if (MXMessageManager.ACTION_BLACK_ADD.equals(action)) {
            blackAdd();
        }
        // 从黑名单中删除
        else if (MXMessageManager.ACTION_BLACK_DEL.equals(action)) {
            blackDel();
        }
        // 连接上ws
        else if (TextUtils.equals(MXMessageManager.ACTION_SOCKET_OPEN, action)) {
            socketOpen();
        }
        // ws重新链接
        else if (TextUtils.equals(MXMessageManager.ACTION_SOCKET_RECONNECT, action)) {
            socketReconnect();
        }
        // 初始化对话
        else if (TextUtils.equals(MXMessageManager.ACTION_QUEUEING_INIT_CONV, action)) {
            long convId = intent.getLongExtra("convId", 0);
            if (convId > 0) {
                initConv(convId);
            }
        }
    }

    public abstract void receiveNewMsg(BaseMessage message);

    public abstract void recallMessage(long id, String nickname);

    public abstract void changeTitleToInputting(int duration);

    public abstract void addDirectAgentMessageTip(String agentNickname);

    public abstract void setCurrentAgent(Agent agent);

    public abstract void setShowTransHumanBtn(boolean isShowTransferHumanBtn);

    public abstract void inviteEvaluation();

    public abstract void setNewConversationId(String newConversationId);

    public abstract void updateAgentOnlineOfflineStatus();

    public abstract void blackAdd();

    public abstract void blackDel();

    public abstract void initConv(long convId);

    public abstract void socketOpen();

    public abstract void socketReconnect();
}
