package com.mixdesk.mixdesksdk.callback;

import android.content.Intent;

import com.mixdesk.mixdesksdk.activity.MXConversationActivity;

/**
 * OnePiece
 * Created by xukq on 9/7/16.
 */
public interface OnLinkClickCallback {

    void onClick(MXConversationActivity conversationActivity, Intent intent, String url);

}
