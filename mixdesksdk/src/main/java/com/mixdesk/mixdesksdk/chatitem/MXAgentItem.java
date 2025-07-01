package com.mixdesk.mixdesksdk.chatitem;

import android.app.Activity;
import android.content.Context;

import com.mixdesk.mixdesksdk.R;
import com.mixdesk.mixdesksdk.model.BaseMessage;
import com.mixdesk.mixdesksdk.util.MXConfig;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/5/23 下午5:17
 * 描述:客服消息item
 */
public class MXAgentItem extends MXBaseBubbleItem {

    public MXAgentItem(Context context, Callback calllback) {
        super(context, calllback);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mx_item_chat_left;
    }

    @Override
    protected void initView() {
        super.initView();

        unreadCircle = getViewById(R.id.unread_view);
    }

    public void setMessage(BaseMessage baseMessage, int position, Activity activity) {
        super.setMessage(baseMessage, position, activity);
        if (!MXConfig.isShowAgentAvatar) {
            usAvatar.setVisibility(GONE);
        }
    }

    @Override
    protected void setListener() {
    }

    @Override
    protected void processLogic() {
        super.processLogic();
        applyConfig(true);
    }
}
