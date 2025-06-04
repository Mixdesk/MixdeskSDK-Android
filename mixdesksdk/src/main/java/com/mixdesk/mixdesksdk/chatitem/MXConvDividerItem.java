package com.mixdesk.mixdesksdk.chatitem;

import android.content.Context;
import android.widget.TextView;

import com.mixdesk.mixdesksdk.R;
import com.mixdesk.mixdesksdk.util.MXTimeUtils;
import com.mixdesk.mixdesksdk.widget.MXBaseCustomCompositeView;

public class MXConvDividerItem extends MXBaseCustomCompositeView {

    private TextView contentTv;

    public MXConvDividerItem(Context context, long createTime) {
        super(context);
        contentTv.setText(MXTimeUtils.partLongToMonthDay(createTime));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mx_item_conv_divider;
    }

    @Override
    protected void initView() {
        contentTv = findViewById(R.id.content_tv);
    }

    @Override
    protected void setListener() {
    }

    @Override
    protected void processLogic() {

    }

}