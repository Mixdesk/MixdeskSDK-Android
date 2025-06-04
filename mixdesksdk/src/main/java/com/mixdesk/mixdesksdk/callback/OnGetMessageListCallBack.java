package com.mixdesk.mixdesksdk.callback;


import com.mixdesk.mixdesksdk.model.BaseMessage;

import java.util.List;

public interface OnGetMessageListCallBack extends OnFailureCallBack {

    void onSuccess(List<BaseMessage> messageList);

}
