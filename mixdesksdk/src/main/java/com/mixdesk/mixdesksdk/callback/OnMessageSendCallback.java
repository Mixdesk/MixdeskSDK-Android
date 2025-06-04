package com.mixdesk.mixdesksdk.callback;


import com.mixdesk.mixdesksdk.model.BaseMessage;

public interface OnMessageSendCallback {
    void onSuccess(BaseMessage message, int state);

    void onFailure(BaseMessage failureMessage, int code, String failureInfo);
}
