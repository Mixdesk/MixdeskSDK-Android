package com.mixdesk.mixdesksdk.dialog;

import android.app.Dialog;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.mixdesk.mixdesksdk.R;
import com.mixdesk.mixdesksdk.util.MXUtils;

public class MXConfirmDialog extends Dialog {

    private final TextView titleTv;
    private final TextView contentTv;
    private final View confirmBtn;
    private final View cancelBtn;

    public MXConfirmDialog(@NonNull Context context, String title, String content, @Nullable final View.OnClickListener onPositiveClickListener, @Nullable final View.OnClickListener onNegativeClickListener) {
        super(context, R.style.MQDialog);
        MXUtils.updateLanguage(context);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.mx_dialog_confirm);

        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        titleTv = findViewById(R.id.tv_comfirm_title);
        contentTv = findViewById(R.id.et_evaluate_content);
        confirmBtn = findViewById(R.id.tv_evaluate_confirm);
        cancelBtn = findViewById(R.id.tv_evaluate_cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (onNegativeClickListener != null) {
                    onNegativeClickListener.onClick(v);
                }
            }
        });
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (onPositiveClickListener != null) {
                    onPositiveClickListener.onClick(v);
                }
            }
        });

        titleTv.setText(title);
        contentTv.setText(content);
    }
}
