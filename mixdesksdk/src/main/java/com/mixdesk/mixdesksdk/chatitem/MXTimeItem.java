package com.mixdesk.mixdesksdk.chatitem;

import android.content.Context;
import android.widget.TextView;

import com.mixdesk.mixdesksdk.R;
import com.mixdesk.mixdesksdk.model.BaseMessage;
import com.mixdesk.mixdesksdk.util.MXTimeUtils;
import com.mixdesk.mixdesksdk.widget.MXBaseCustomCompositeView;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/5/23 下午4:20
 * 描述:
 */
public class MXTimeItem extends MXBaseCustomCompositeView {
    private TextView mContentTv;

    public MXTimeItem(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mx_item_chat_time;
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
        mContentTv.setText(MXTimeUtils.parseTime(baseMessage.getCreatedOn()));
    }
}
