package com.mixdesk.mixdesksdk.widget;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import com.mixdesk.mixdesksdk.R;
import com.mixdesk.mixdesksdk.model.MessageFormInputModel;
import com.mixdesk.mixdesksdk.util.MXSimpleTextWatcher;
import com.mixdesk.mixdesksdk.util.MXTimeUtils;
import com.mixdesk.mixdesksdk.util.MXUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/2/24 上午11:19
 * 描述:
 */
public class MXMessageFormInputLayout extends FrameLayout {

    private final MessageFormInputModel messageFormInputModel;
    private String value;
    private final String type;
    private String name;
    private final Set<String> valueSet = new HashSet<>();

    public MXMessageFormInputLayout(Context context, MessageFormInputModel messageFormInputModel) {
        super(context);
        this.messageFormInputModel = messageFormInputModel;
        type = messageFormInputModel.type;
        switch (messageFormInputModel.type) {
            case "number":
                initNumberInput(messageFormInputModel);
                break;
            case "radio":
                initRadioInput(messageFormInputModel);
                break;
            case "check":
            case "checkbox":
                initCheckBoxInput(messageFormInputModel);
                break;
            case "date":
            case "datetime":
                initDateInput(messageFormInputModel);
                break;
            case "string":
            case "text":
                // 性别特殊处理为单选
                if (TextUtils.equals(messageFormInputModel.key, "gender")) {
                    try {
                        messageFormInputModel.metainfo = new JSONArray(getResources().getString(R.string.mx_inquire_gender_choice));
                        initRadioInput(messageFormInputModel);
                        break;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            default:
                initStringInput(messageFormInputModel);
                break;
        }
    }

    public String getKey() {
        return messageFormInputModel.key;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    private void initStringInput(MessageFormInputModel messageFormInputModel) {
        View.inflate(getContext(), R.layout.mx_layout_form_input, this);
        TextView tipTv = findViewById(R.id.tip_tv);
        EditText contentEt = findViewById(R.id.content_et);
        if (!TextUtils.isEmpty(messageFormInputModel.placeholder)) {
            contentEt.setHint(messageFormInputModel.placeholder);
        }
        if (!TextUtils.isEmpty(messageFormInputModel.preFill)) {
            contentEt.setText(messageFormInputModel.preFill);
            contentEt.setSelection(messageFormInputModel.preFill.length());
        }

        contentEt.addTextChangedListener(new MXSimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                value = s.toString();
            }
        });

        tipTv.setText(MXUtils.keyToName(messageFormInputModel.name, getContext()));
        name = tipTv.getText().toString();
        contentEt.setHint(messageFormInputModel.placeholder);
        contentEt.setInputType(InputType.TYPE_CLASS_TEXT);
        if (messageFormInputModel.required) {
            SpannableStringBuilder tipSsb = new SpannableStringBuilder(tipTv.getText() + " *");
            tipSsb.setSpan(new ForegroundColorSpan(Color.RED), tipTv.getText().length() + 1, tipSsb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tipTv.setText(tipSsb);
        }
    }

    private void initNumberInput(MessageFormInputModel messageFormInputModel) {
        View.inflate(getContext(), R.layout.mx_layout_form_input, this);
        TextView tipTv = findViewById(R.id.tip_tv);
        EditText contentEt = findViewById(R.id.content_et);
        if (!TextUtils.isEmpty(messageFormInputModel.placeholder)) {
            contentEt.setHint(messageFormInputModel.placeholder);
        }
        if (!TextUtils.isEmpty(messageFormInputModel.preFill)) {
            contentEt.setText(messageFormInputModel.preFill);
            contentEt.setSelection(messageFormInputModel.preFill.length());
        }

        contentEt.addTextChangedListener(new MXSimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                value = s.toString();
            }
        });

        tipTv.setText(MXUtils.keyToName(messageFormInputModel.name, getContext()));
        name = tipTv.getText().toString();
        contentEt.setHint(messageFormInputModel.placeholder);
        contentEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (messageFormInputModel.required) {
            SpannableStringBuilder tipSsb = new SpannableStringBuilder(tipTv.getText() + " *");
            tipSsb.setSpan(new ForegroundColorSpan(Color.RED), tipTv.getText().length() + 1, tipSsb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tipTv.setText(tipSsb);
        }
    }

    private void initRadioInput(MessageFormInputModel messageFormInputModel) {
        View.inflate(getContext(), R.layout.mx_layout_form_radio, this);
        TextView tipTv = findViewById(R.id.tip_tv);
        RadioGroup radioGroup = findViewById(R.id.radio_group);
        tipTv.setText(MXUtils.keyToName(messageFormInputModel.name, getContext()));
        name = tipTv.getText().toString();

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = group.findViewById(checkedId);
                if (radioButton == null) {
                    return;
                }
                value = (String) radioButton.getTag();
            }
        });
        JSONArray choiceArray = messageFormInputModel.metainfo;
        if (choiceArray != null) {
            for (int i = 0; i < choiceArray.length(); i++) {
                RadioButton radioButton = (RadioButton) LayoutInflater.from(getContext()).inflate(R.layout.mx_item_form_radio_btn_left, null);
                String text = choiceArray.optString(i);
                radioButton.setText(text);
                radioButton.setTag(text);
                radioButton.setId(View.NO_ID);
                MXUtils.tintCompoundButton(radioButton, R.drawable.mx_radio_btn_uncheck, R.drawable.mx_radio_btn_checked);
                radioGroup.addView(radioButton, LinearLayout.LayoutParams.MATCH_PARENT, MXUtils.dip2px(getContext(), 32));
            }
        }
    }

    private void initCheckBoxInput(MessageFormInputModel messageFormInputModel) {
        View.inflate(getContext(), R.layout.mx_layout_form_check, this);
        TextView tipTv = findViewById(R.id.tip_tv);
        LinearLayout rootLl = findViewById(R.id.root_ll);
        tipTv.setText(MXUtils.keyToName(messageFormInputModel.name, getContext()));
        name = tipTv.getText().toString();
        JSONArray selectItemArrays = messageFormInputModel.metainfo;
        for (int i = 0; i < selectItemArrays.length(); i++) {
            CheckBox checkBox = (CheckBox) LayoutInflater.from(getContext()).inflate(R.layout.mx_item_form_checkbox, null);
            checkBox.setChecked(false);
            String text = selectItemArrays.optString(i);
            checkBox.setText(text);
            checkBox.setSingleLine();
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        String value = (String) buttonView.getTag();
                        valueSet.add(value);
                    } else {
                        valueSet.add(value);
                    }
                    value = Arrays.toString(valueSet.toArray());
                }
            });
            checkBox.setTag(text);
            MXUtils.tintCompoundButton(checkBox, R.drawable.mx_checkbox_uncheck, R.drawable.mx_checkbox_unchecked);
            rootLl.addView(checkBox, LinearLayout.LayoutParams.MATCH_PARENT, MXUtils.dip2px(getContext(), 32));
        }
    }

    private void initDateInput(MessageFormInputModel messageFormInputModel) {
        View.inflate(getContext(), R.layout.mx_layout_form_date, this);
        TextView tipTv = findViewById(R.id.tip_tv);
        final TextView contentTv = findViewById(R.id.content_tv);

        tipTv.setText(MXUtils.keyToName(messageFormInputModel.name, getContext()));
        name = tipTv.getText().toString();
        contentTv.setHint(messageFormInputModel.placeholder);
        if (messageFormInputModel.required) {
            SpannableStringBuilder tipSsb = new SpannableStringBuilder(tipTv.getText() + " *");
            tipSsb.setSpan(new ForegroundColorSpan(Color.RED), tipTv.getText().length() + 1, tipSsb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tipTv.setText(tipSsb);
        }
        contentTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cal = Calendar.getInstance();
                new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, final int year, final int month, final int dayOfMonth) {
                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.YEAR, year);
                        cal.set(Calendar.MONTH, month);
                        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        String time = MXTimeUtils.partLongToTime(cal.getTimeInMillis());
                        contentTv.setText(time);
                        value = MXTimeUtils.partLongToServiceTime(cal.getTimeInMillis());
                        new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                Calendar cal = Calendar.getInstance();
                                cal.set(Calendar.YEAR, year);
                                cal.set(Calendar.MONTH, month);
                                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                cal.set(Calendar.MINUTE, minute);
                                String time = MXTimeUtils.partLongToTime(cal.getTimeInMillis());
                                contentTv.setText(time);
                                value = MXTimeUtils.partLongToServiceTime(cal.getTimeInMillis());
                            }
                        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
                    }
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

}
