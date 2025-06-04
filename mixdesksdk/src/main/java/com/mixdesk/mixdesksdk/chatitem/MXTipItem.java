package com.mixdesk.mixdesksdk.chatitem;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import com.mixdesk.mixdesksdk.R;
import com.mixdesk.mixdesksdk.model.AgentChangeMessage;
import com.mixdesk.mixdesksdk.model.BaseMessage;
import com.mixdesk.mixdesksdk.widget.MXBaseCustomCompositeView;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/5/23 下午4:08
 * 描述:提示消息item，包括「没有客服在线的提示」、「客服转接的提示」
 */
public class MXTipItem extends MXBaseCustomCompositeView {
    private TextView mContentTv;

    public MXTipItem(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mx_item_msg_tip;
    }

    @Override
    protected void initView() {
        mContentTv = getViewById(R.id.content_tv);
    }

    @Override
    protected void setListener() {
    }

    @Override
    protected void processLogic() {
    }

    public void setMessage(BaseMessage baseMessage) {
        if (baseMessage instanceof AgentChangeMessage) {
            setDirectionMessageContent(baseMessage.getAgentNickname());
        } else {
            mContentTv.setText(baseMessage.getContent());
        }
    }

    private void setDirectionMessageContent(String agentNickName) {
        if (agentNickName != null) {
            String text = String.format(getResources().getString(R.string.mx_direct_content), agentNickName);
            int start = text.indexOf(agentNickName);
            SpannableStringBuilder style = new SpannableStringBuilder(text);
            style.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.mx_chat_direct_agent_nickname_textColor)), start, start + agentNickName.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            mContentTv.setText(style);
        }
    }
}
