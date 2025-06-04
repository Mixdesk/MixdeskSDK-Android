package com.mixdesk.mixdesksdk.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.mixdesk.core.MXManager;
import com.mixdesk.mixdesksdk.activity.MXConversationActivity;

import java.util.HashMap;

/**
 * OnePiece
 * Created by xukq on 3/18/16.
 */
public class MXIntentBuilder {

    private final Context mContext;
    private Intent mIntent;

    public MXIntentBuilder(Context context) {
        mContext = context;
        mIntent = getIntent(context, MXConversationActivity.class);
    }

    public MXIntentBuilder(Context context, Class<? extends MXConversationActivity> clazz) {
        mContext = context;
        mIntent = getIntent(context, clazz);
    }

    /**
     * @param context
     * @param clazz
     * @return
     */
    private Intent getIntent(Context context, Class<? extends MXConversationActivity> clazz) {
        mIntent = new Intent(context, clazz);
        return mIntent;
    }

    public MXIntentBuilder setClientId(String clientId) {
        mIntent.putExtra(MXConversationActivity.CLIENT_ID, clientId);
        checkClient(clientId);
        return this;
    }

    public MXIntentBuilder setCustomizedId(String customizedId) {
        mIntent.putExtra(MXConversationActivity.CUSTOMIZED_ID, customizedId);
        checkClient(customizedId);
        return this;
    }

    public MXIntentBuilder setClientInfo(HashMap<String, String> clientInfo) {
        mIntent.putExtra(MXConversationActivity.CLIENT_INFO, clientInfo);
        return this;
    }

    public MXIntentBuilder updateClientInfo(HashMap<String, String> clientInfo) {
        mIntent.putExtra(MXConversationActivity.UPDATE_CLIENT_INFO, clientInfo);
        return this;
    }

    public Intent build() {
        if (!(mContext instanceof Activity)) {
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return mIntent;
    }

    private void checkClient(String id) {
        String currentId = MXUtils.getString(mContext, MXConversationActivity.CURRENT_CLIENT, null);
        // 切换了用户,就默认不是回头客
        if (!TextUtils.equals(currentId, id)) {
            MXManager.getInstance(mContext).getEnterpriseConfig().survey.setHas_submitted_form(false);
        }
        MXUtils.putString(mContext, MXConversationActivity.CURRENT_CLIENT, id);
    }

}
