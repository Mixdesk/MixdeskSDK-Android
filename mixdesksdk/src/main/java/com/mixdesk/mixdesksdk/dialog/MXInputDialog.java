package com.mixdesk.mixdesksdk.dialog;

import android.app.Dialog;
import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.mixdesk.mixdesksdk.R;
import com.mixdesk.mixdesksdk.util.MXUtils;

public class MXInputDialog extends Dialog {

    private final TextView titleTv;
    private final EditText inputEt;
    private final View confirmBtn;
    private final View cancelBtn;

    public MXInputDialog(@NonNull Context context, String title, String input, String hint, int inputType, final OnContentChangeListener onContentChangeListener) {
        super(context, R.style.MQDialog);
        MXUtils.updateLanguage(context);
        setCanceledOnTouchOutside(true);
        setContentView(R.layout.mx_dialog_input);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        titleTv = findViewById(R.id.tv_comfirm_title);
        inputEt = findViewById(R.id.et_evaluate_content);
        confirmBtn = findViewById(R.id.tv_evaluate_confirm);
        cancelBtn = findViewById(R.id.tv_evaluate_cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                onContentChangeListener.onContentChange(inputEt.getText().toString());
            }
        });

        titleTv.setText(title);
        inputEt.setText(input);
        inputEt.setHint(hint);
        inputEt.setInputType(inputType);
        MXUtils.openKeyboard(inputEt);
    }

    public interface OnContentChangeListener {
        void onContentChange(String content);
    }
}
