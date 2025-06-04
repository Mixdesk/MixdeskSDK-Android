package com.mixdesk.mixdesksdk.callback;


import com.mixdesk.mixdesksdk.model.Agent;
import com.mixdesk.mixdesksdk.model.BaseMessage;

import java.util.List;

public interface OnClientOnlineCallback extends OnFailureCallBack {
    void onSuccess(Agent agent, String conversationId, List<BaseMessage> messageList,boolean isShowTransferHumanBtn);
}
