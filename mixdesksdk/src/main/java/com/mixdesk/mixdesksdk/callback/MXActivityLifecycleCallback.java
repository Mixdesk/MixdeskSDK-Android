package com.mixdesk.mixdesksdk.callback;

import android.os.Bundle;

import com.mixdesk.mixdesksdk.activity.MXConversationActivity;

/**
 * OnePiece
 * Created by xukq on 7/13/16.
 */
public interface MXActivityLifecycleCallback {

    void onActivityCreated(MXConversationActivity activity, Bundle savedInstanceState);

    void onActivityStarted(MXConversationActivity activity);

    void onActivityResumed(MXConversationActivity activity);

    void onActivityPaused(MXConversationActivity activity);

    void onActivityStopped(MXConversationActivity activity);

    void onActivitySaveInstanceState(MXConversationActivity activity, Bundle outState);

    void onActivityDestroyed(MXConversationActivity activity);

}
