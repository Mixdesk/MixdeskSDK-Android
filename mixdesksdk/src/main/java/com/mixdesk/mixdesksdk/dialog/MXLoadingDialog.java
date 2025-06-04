package com.mixdesk.mixdesksdk.dialog;

import android.app.Activity;
import android.app.Dialog;

import com.mixdesk.mixdesksdk.R;
import com.mixdesk.mixdesksdk.util.MXUtils;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/3/4 上午11:03
 * 描述:
 */
public class MXLoadingDialog extends Dialog {

    public MXLoadingDialog(Activity activity) {
        super(activity, R.style.MQDialog);
        MXUtils.updateLanguage(activity);
        setContentView(R.layout.mx_dialog_loading);
    }
}
