package com.mixdesk.mixdesksdk.dialog;

import android.app.Activity;
import android.app.Dialog;
import androidx.annotation.StringRes;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.mixdesk.mixdesksdk.R;
import com.mixdesk.mixdesksdk.util.MXUtils;

import java.util.List;
import java.util.Map;

public class MXListDialog extends Dialog {
    private final TextView mTitleTv;
    private final ListView mListview;

    public MXListDialog(Activity activity, @StringRes int titleResId, List<Map<String, String>> dataList, AdapterView.OnItemClickListener onItemClickListener) {
        this(activity, activity.getString(titleResId), dataList, onItemClickListener, true);
    }

    public MXListDialog(Activity activity, String title, List<Map<String, String>> dataList, final AdapterView.OnItemClickListener onItemClickListener, boolean isCancelable) {
        super(activity, R.style.MQDialog);
        MXUtils.updateLanguage(activity);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        setContentView(R.layout.mx_dialog_ticket_categry);
        mTitleTv = findViewById(R.id.tv_comfirm_title);
        mListview = findViewById(R.id.list_lv);

        setCanceledOnTouchOutside(isCancelable);
        setCancelable(isCancelable);

        mTitleTv.setText(title);

        mListview.setAdapter(new SimpleAdapter(activity, dataList,
                R.layout.mx_item_text_list, new String[]{"name"}, new int[]{android.R.id.text1}));
        mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(parent, view, position, id);
                }
                dismiss();
            }
        });
    }

}