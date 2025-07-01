package com.mixdesk.mixdesksdk.chatitem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mixdesk.core.bean.MXMessage;
import com.mixdesk.mixdesksdk.R;
import com.mixdesk.mixdesksdk.activity.MXConversationActivity;
import com.mixdesk.mixdesksdk.activity.MXPhotoPreviewActivity;
import com.mixdesk.mixdesksdk.imageloader.MXImage;
import com.mixdesk.mixdesksdk.imageloader.MXImageLoader;
import com.mixdesk.mixdesksdk.model.BaseMessage;
import com.mixdesk.mixdesksdk.model.HybridMessage;
import com.mixdesk.mixdesksdk.model.PhotoMessage;
import com.mixdesk.mixdesksdk.util.MXConfig;
import com.mixdesk.mixdesksdk.util.MXUtils;
import com.mixdesk.mixdesksdk.util.RichText;
import com.mixdesk.mixdesksdk.widget.MXBaseCustomCompositeView;
import com.mixdesk.mixdesksdk.widget.MXImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * OnePiece
 * Created by xukq on 11/19/18.
 */
public class MXHybridItem extends MXBaseCustomCompositeView implements RichText.OnImageClickListener {

    private MXImageView mAvatarIv;
    private LinearLayout mContainerLl;
    private ViewGroup mOperationLl;
    private LinearLayout mRootLl;
    private int mPadding;
    private int mTextSize;
    private OnCallbackListener mOnCallbackListener;

    private HybridMessage mHybridMessage;

    public MXHybridItem(Context context, OnCallbackListener onCallbackListener) {
        super(context);
        this.mOnCallbackListener = onCallbackListener;
    }

    @Override
    public void onImageClicked(String url, String imgLink) {
        try {
            if (TextUtils.isEmpty(imgLink)) {
                getContext().startActivity(MXPhotoPreviewActivity.newIntent(getContext(), MXUtils.getImageDir(getContext()), url));
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(imgLink));
                getContext().startActivity(intent);
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), R.string.mx_title_unknown_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mx_item_hybrid;
    }

    @Override
    protected void initView() {
        mAvatarIv = getViewById(R.id.iv_robot_avatar);
        mOperationLl = getViewById(R.id.ll_operation);
        mContainerLl = getViewById(R.id.ll_robot_container);
        mRootLl = getViewById(R.id.root_ll);
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void processLogic() {
        if (!TextUtils.isEmpty(MXConfig.ui.leftChatBubbleColor)) {
            MXUtils.applyCustomUITintDrawable(mContainerLl, Color.parseColor(MXConfig.ui.leftChatBubbleColor));
        } else {
            MXUtils.applyCustomUITintDrawable(mContainerLl, R.color.mx_chat_left_bubble_final, R.color.mx_chat_left_bubble, MXConfig.ui.leftChatBubbleColorResId);
        }
        mPadding = getResources().getDimensionPixelSize(R.dimen.mx_size_level2);
        mTextSize = getResources().getDimensionPixelSize(R.dimen.mx_textSize_level2);
    }

    public void setMessage(HybridMessage hybridMessage, Activity activity) {
        mRootLl.setVisibility(VISIBLE);
        mContainerLl.removeAllViews();
        mOperationLl.setVisibility(GONE);
        mContainerLl.setVisibility(VISIBLE);
        mAvatarIv.setVisibility(VISIBLE);
        mHybridMessage = hybridMessage;
        if (!TextUtils.isEmpty(mHybridMessage.getAvatar())) {
            MXImage.displayImage(activity, mAvatarIv, mHybridMessage.getAvatar(), R.drawable.mx_ic_holder_avatar, R.drawable.mx_ic_holder_avatar, 100, 100, null);
        }
        // 根据来源，调整展示
        View firstView = mRootLl.getChildAt(0);
        View secondVIew = mRootLl.getChildAt(1);
        // 先移除，然后根据来源，重新调整布局
        mRootLl.removeAllViews();
        int avatarWidthAndHeight = MXUtils.dip2px(activity, 35);
        int avatarMargin = MXUtils.dip2px(activity, 6);
        if (TextUtils.equals(hybridMessage.getFromType(), BaseMessage.TYPE_FROM_CLIENT)) {
            if (!MXConfig.isShowClientAvatar) {
                mAvatarIv.setVisibility(GONE);
            }
            mRootLl.setGravity(Gravity.RIGHT);
            // 头像需要调整到右边
            if (firstView instanceof MXImageView) {
                mRootLl.addView(secondVIew);
                LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(avatarWidthAndHeight, avatarWidthAndHeight);
                avatarParams.leftMargin = avatarMargin;
                mRootLl.addView(firstView, avatarParams);
            } else {
                mRootLl.addView(firstView);
                mRootLl.addView(secondVIew);
            }
        } else {
            if (!MXConfig.isShowAgentAvatar) {
                mAvatarIv.setVisibility(GONE);
            }
            mRootLl.setGravity(Gravity.LEFT);
            // 头像需要调整到左边
            if (firstView instanceof LinearLayout) {
                mRootLl.addView(secondVIew);
                LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(avatarWidthAndHeight, avatarWidthAndHeight);
                avatarParams.rightMargin = avatarMargin;
                mRootLl.addView(firstView, avatarParams);
            } else {
                mRootLl.addView(firstView);
                mRootLl.addView(secondVIew);
            }
        }
        fillContentLl(mHybridMessage.getContent());
    }

    private void fillContentLl(String content) {
        try {
            JSONArray contentArray = new JSONArray(content);
            for (int i = 0; i < contentArray.length(); i++) {
                JSONObject item = contentArray.getJSONObject(i);
                String type = item.getString("type");
                String subType = item.optString("sub_type");
                switch (type) {
                    case "rich_text":
                    case "text":
                        if (TextUtils.equals(subType, "button")) {
                            addOptionView(item.optJSONArray("tags"));
                        } else if (TextUtils.equals(subType, MXMessage.SUB_TYPE_QUICK_BTN)) {
                            JSONArray quickBtns = new JSONObject(mHybridMessage.getExtra()).optJSONArray("quick_btn");
                            addQuickButtonView(item.optString("body"), quickBtns);
                        } else {
                            addNormalOrRichTextView(item.getString("body"));
                        }
                        break;
                    case "choices":
                        addChoices(item.optJSONObject("body").optString("choices"));
                        break;
                    case "list":
                        fillContentLl(item.getString("body"));
                        break;
                    case "wait":
                        break;
                    case "photo_card":
                        addPhotoCardView(item.optJSONObject("body"));
                        break;
                    case "product_card":
                        addProductCardView(item.optJSONObject("body"));
                        break;
                    case "option": // 用户反馈按钮
                        addOptionView(item.optJSONArray("option"));
                        break;
                    case "photo":
                        if (TextUtils.equals(subType, MXMessage.SUB_TYPE_QUICK_BTN)) {
                            JSONArray quickBtns = new JSONObject(mHybridMessage.getExtra()).optJSONArray("quick_btn");
                            addPhotoView(item.optString("body"));
                            addOperableButton(quickBtns);
                        }
                        break;
                    default:
                        addNormalOrRichTextView(getContext().getString(R.string.mx_unknown_msg_tip));
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            addNormalOrRichTextView(getContext().getString(R.string.mx_unknown_msg_tip));
        }
    }

    /**
     * 添加普通的文本内容
     *
     * @param text
     */
    private void addNormalOrRichTextView(String text) {
        if (!TextUtils.isEmpty(text)) {
            TextView textView = new TextView(getContext());
            textView.setText(text);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
            textView.setTextColor(getResources().getColor(R.color.mx_chat_left_textColor));
            textView.setPadding(mPadding, mPadding, mPadding, mPadding);
            if (!TextUtils.isEmpty(MXConfig.ui.leftChatTextColor)) {
                MXUtils.applyCustomUITextAndImageColor(Color.parseColor(MXConfig.ui.leftChatTextColor), null, textView);
            } else {
                MXUtils.applyCustomUITextAndImageColor(R.color.mx_chat_left_textColor, MXConfig.ui.leftChatTextColorResId, null, textView);
            }
            mContainerLl.addView(textView);
            RichText richText = new RichText();
            richText.fromHtml(text).setOnImageClickListener(this).into(textView);
            textView.setTag("isRichText");

            // 添加操作按钮
            if (!TextUtils.isEmpty(mHybridMessage.getExtra())) {
                try {
                    JSONObject extraObj = new JSONObject(mHybridMessage.getExtra());
                    JSONArray operationArray = extraObj.optJSONArray("operator_msg");
                    if (operationArray != null && operationArray.length() != 0) {
                        mOperationLl.setVisibility(VISIBLE);
                        mOperationLl.removeAllViews();
                        for (int i = 0; i < operationArray.length(); i++) {
                            JSONObject item = operationArray.getJSONObject(i);
                            String name = item.optString("name");
                            final String type = item.optString("type");
                            final String value = item.optString("value");
                            View operationView = LayoutInflater.from(getContext()).inflate(R.layout.mx_item_action, null);
                            TextView operationTv = operationView.findViewById(R.id.content_tv);
                            operationTv.setText(name);
                            mOperationLl.addView(operationView);
                            operationView.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    try {
                                        if (TextUtils.equals(type, "copy")) {
                                            MXUtils.clip(getContext(), value);
                                            Toast.makeText(getContext(), R.string.mx_copy_success, Toast.LENGTH_SHORT).show();
                                        } else if (TextUtils.equals(type, "call")) {
                                            Intent intent = new Intent(Intent.ACTION_DIAL);
                                            intent.setData(Uri.parse("tel:" + value));
                                            getContext().startActivity(intent);
                                        } else if (TextUtils.equals(type, "link")) {
                                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(value));
                                            getContext().startActivity(intent);
                                        }
                                    } catch (Exception e) {
                                        Toast.makeText(getContext(), R.string.mx_title_unknown_error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        // 重新设置 LayoutParams，不然不生效
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        mOperationLl.setLayoutParams(params);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void addChoices(String choicesStr) throws JSONException {
        JSONArray choices = new JSONArray(choicesStr);
        for (int i = 0; i < choices.length(); i++) {
            final String text = choices.optString(i);
            if (!TextUtils.isEmpty(text)) {
                TextView itemTv = (TextView) View.inflate(getContext(), R.layout.mx_item_robot_menu, null);
                MXUtils.applyCustomUITextAndImageColor(R.color.mx_chat_robot_menu_item_textColor, MXConfig.ui.robotMenuItemTextColorResId, null, itemTv);
                itemTv.setText(text);
                itemTv.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        if (mCallback != null) {
//                            if (text.indexOf(".") == 1 && text.length() > 2) {
//                                mCallback.onClickRobotMenuItem(text.substring(2));
//                            } else {
//                                mCallback.onClickRobotMenuItem(text);
//                            }
//                        }
                    }
                });
                mContainerLl.addView(itemTv);
            }
        }
    }

    private void addPhotoCardView(JSONObject contentObj) {
        int screenWidth = MXUtils.getScreenWidth(getContext());
        int mImageWidth = (screenWidth / 3 * 2) - MXUtils.dip2px(getContext(), 16);
        int mImageHeight = mImageWidth;
        int margin = MXUtils.dip2px(getContext(), 12);

        ViewGroup.LayoutParams layoutParams = mContainerLl.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.width = mImageWidth;
        mContainerLl.setLayoutParams(layoutParams);
        mContainerLl.setBackgroundResource(R.drawable.mx_bg_card);

        String title = contentObj.optString("title");
        String description = contentObj.optString("description");
        final String target_url = contentObj.optString("target_url");
        String pic_url = contentObj.optString("pic_url");

        // 添加图片
        ImageView iv = new ImageView(getContext());
        iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        iv.setAdjustViewBounds(true);
        MXImage.displayImage((Activity) getContext(), iv, pic_url, R.drawable.mx_ic_holder_light, R.drawable.mx_ic_holder_light, mImageWidth, mImageHeight, new MXImageLoader.MQDisplayImageListener() {
            @Override
            public void onSuccess(View view, final String url) {

            }
        });
        LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ivParams.leftMargin = margin;
        ivParams.rightMargin = margin;
        mContainerLl.addView(iv, ivParams);
        // 设置点击跳转
        mContainerLl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    Uri uri = Uri.parse(target_url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    getContext().startActivity(intent);
                } catch (Exception e) {
                    MXUtils.show(getContext(), R.string.mx_title_unknown_error);
                }
            }
        });
        // 添加标题
        if (!TextUtils.isEmpty(title)) {
            TextView titleTv = new TextView(getContext());
            titleTv.setText(title);
            titleTv.setMaxLines(1);
            titleTv.setEllipsize(TextUtils.TruncateAt.END);
            titleTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.mx_textSize_level3));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.topMargin = MXUtils.dip2px(getContext(), 2);
            params.bottomMargin = MXUtils.dip2px(getContext(), 2);
            params.leftMargin = margin;
            params.rightMargin = margin;
            mContainerLl.addView(titleTv, params);
        }
        // 添加内容
        if (!TextUtils.isEmpty(description)) {
            TextView descriptionTv = new TextView(getContext());
            descriptionTv.setText(description);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.topMargin = MXUtils.dip2px(getContext(), 2);
            params.bottomMargin = MXUtils.dip2px(getContext(), 2);
            params.leftMargin = margin;
            params.rightMargin = margin;
            mContainerLl.addView(descriptionTv, params);
        }
    }

    private void addProductCardView(JSONObject contentObj) {
        int screenWidth = MXUtils.getScreenWidth(getContext());
        int mImageWidth = (screenWidth / 3 * 2) - MXUtils.dip2px(getContext(), 16);
        int mImageHeight = mImageWidth;

        ViewGroup.LayoutParams layoutParams = mContainerLl.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.width = mImageWidth;
        mContainerLl.setLayoutParams(layoutParams);
        mContainerLl.setBackgroundResource(R.drawable.mx_bg_card);

        View productCardContent = LayoutInflater.from(getContext()).inflate(R.layout.mx_item_product_card, null);
        mContainerLl.addView(productCardContent, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        String title = contentObj.optString("title");
        String description = contentObj.optString("description");
        final String product_url = contentObj.optString("product_url");
        String pic_url = contentObj.optString("pic_url");
        long sales_count = contentObj.optLong("sales_count");

        // 添加图片
        ImageView iv = productCardContent.findViewById(R.id.mx_pic_iv);
        MXImage.displayImage((Activity) getContext(), iv, pic_url, R.drawable.mx_ic_holder_light, R.drawable.mx_ic_holder_light, mImageWidth, mImageHeight, new MXImageLoader.MQDisplayImageListener() {
            @Override
            public void onSuccess(View view, final String url) {

            }
        });

        // 查看详情
        TextView detailTv = productCardContent.findViewById(R.id.mx_detail_tv);
        detailTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Uri uri = Uri.parse(product_url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    if (MXConfig.getOnLinkClickCallback() != null && getContext() instanceof MXConversationActivity) {
                        MXConfig.getOnLinkClickCallback().onClick((MXConversationActivity) getContext(), intent, product_url);
                    } else {
                        getContext().startActivity(intent);
                    }
                } catch (Exception e) {
                    if (!TextUtils.isEmpty(product_url)) {
                        MXUtils.show(getContext(), R.string.mx_title_unknown_error);
                    }
                }
            }
        });

        // 添加标题
        if (!TextUtils.isEmpty(title)) {
            TextView titleTv = productCardContent.findViewById(R.id.mx_title_tv);
            titleTv.setText(title);
        }
        // 添加内容
        if (!TextUtils.isEmpty(description)) {
            TextView descriptionTv = productCardContent.findViewById(R.id.mx_desc_tv);
            descriptionTv.setText(description);
        }
        // 销量
        if (sales_count != 0) {
            TextView countTv = productCardContent.findViewById(R.id.count_tv);
            countTv.setText(getResources().getString(R.string.mx_sale_count) + "：" + sales_count);
        }
    }

    private void addOptionView(JSONArray operationArray) {
        // 添加操作按钮
        try {
//            boolean is_trans_view = !mCallback.isLastMessage(mHybridMessage);
            boolean is_trans_view = false;
            if (is_trans_view) {
                mRootLl.setVisibility(GONE);
                return;
            }
            if (operationArray != null && operationArray.length() != 0) {
                mContainerLl.setVisibility(GONE);
                mAvatarIv.setVisibility(INVISIBLE);
                mOperationLl.setVisibility(VISIBLE);
                mOperationLl.removeAllViews();
                for (int i = 0; i < operationArray.length(); i++) {
                    JSONObject item = operationArray.getJSONObject(i);
                    final String name = item.optString("content");
                    View operationView = LayoutInflater.from(getContext()).inflate(R.layout.mx_item_fill_color_action, null);
                    TextView operationTv = operationView.findViewById(R.id.content_tv);
                    operationTv.setText(name);
                    mOperationLl.addView(operationView);
                    operationView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                mOnCallbackListener.onSendMessage(name);
                            } catch (Exception e) {
                                Toast.makeText(getContext(), R.string.mx_title_unknown_error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                // 重新设置 LayoutParams，不然不生效
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                mOperationLl.setLayoutParams(params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addPhotoView(String photoUrl){
        int screenWidth = MXUtils.getScreenWidth(getContext());
        int mImageWidth = screenWidth / 3;
        int mImageHeight = mImageWidth;

        ImageView iv = new ImageView(getContext());
        iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        iv.setAdjustViewBounds(true);
        MXImage.displayImage((Activity) getContext(), iv, photoUrl, R.drawable.mx_ic_holder_light, R.drawable.mx_ic_holder_light, mImageWidth, mImageHeight, new MXImageLoader.MQDisplayImageListener() {
            @Override
            public void onSuccess(View view, final String sucUrl) {
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        mOnCallbackListener.photoPreview(photoUrl);
                    }
                });
            }
        });
        mContainerLl.addView(iv);
    }

    private void addQuickButtonView(String text, JSONArray operableButtonsJson) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        textView.setTextColor(getResources().getColor(R.color.mx_chat_left_textColor));
        textView.setPadding(mPadding, mPadding, mPadding, mPadding);
        if (!TextUtils.isEmpty(MXConfig.ui.leftChatTextColor)) {
            MXUtils.applyCustomUITextAndImageColor(Color.parseColor(MXConfig.ui.leftChatTextColor), null, textView);
        } else {
            MXUtils.applyCustomUITextAndImageColor(R.color.mx_chat_left_textColor, MXConfig.ui.leftChatTextColorResId, null, textView);
        }
        mContainerLl.addView(textView);
        RichText richText = new RichText();
        richText.fromHtml(text).setOnImageClickListener(this).into(textView);

        addOperableButton(operableButtonsJson);
    }

    private void addOperableButton(JSONArray operableButtonsJson) {
        // 添加操作按钮
        try {
            if (operableButtonsJson != null && operableButtonsJson.length() != 0) {
                mOperationLl.setVisibility(VISIBLE);
                mOperationLl.removeAllViews();
                for (int i = 0; i < operableButtonsJson.length(); i++) {
                    final JSONObject item = operableButtonsJson.getJSONObject(i);
                    String name = item.optString("btn_text");
                    final Integer type = item.optInt("btn_type");
                    final Integer quickBtnId = item.optInt("id");
                    final String value = item.optString("content");
                    JSONObject style = item.optJSONObject("style");

                    View operationView = LayoutInflater.from(getContext()).inflate(R.layout.mx_item_action, null);
                    TextView operationTv = operationView.findViewById(R.id.content_tv);
                    operationTv.setText(name);


                    if (style != null) {
                        GradientDrawable normalDrawable = new GradientDrawable();
                        normalDrawable.setCornerRadius(MXUtils.dip2px(getContext(), 16));

                        String color = style.optString("color");
                        int bg = style.optInt("bg");
                        if (!color.isEmpty()) {
                            normalDrawable.setStroke(MXUtils.dip2px(getContext(), 0.8f), Color.parseColor(color));
                            operationTv.setTextColor(Color.parseColor(color));
                        }
                        if (bg == 1) { // 按钮样式 - 深色
                            normalDrawable.setColor(Color.parseColor(color));
                            operationTv.setTextColor(getResources().getColor(R.color.mx_activity_title_bg));
                        }
                        operationTv.setBackground(normalDrawable);
                    }

                    mOperationLl.addView(operationView);
                    operationView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                if (type == 1) { // 复制内容
                                    MXUtils.clip(getContext(), value);
                                    Toast.makeText(getContext(), R.string.mx_copy_success, Toast.LENGTH_SHORT).show();
                                } else if (type == 2) { //  拨打电话
                                    Intent intent = new Intent(Intent.ACTION_DIAL);
                                    intent.setData(Uri.parse("tel:" + value));
                                    getContext().startActivity(intent);
                                } else if (type == 3) { // 跳转链接
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(value));
                                    if (MXConfig.getOnLinkClickCallback() != null && getContext() instanceof MXConversationActivity) {
                                        MXConfig.getOnLinkClickCallback().onClick((MXConversationActivity) getContext(), intent, value);
                                    } else {
                                        getContext().startActivity(intent);
                                    }
                                } else if (type == 4) { // 发送消息
                                    mOnCallbackListener.onSendMessage(name);
                                    // 延时500ms 让消息先于onQuickBtnClicked发出去
                                    operationView.postDelayed(() -> {
                                        // 延时后执行的操作
                                    }, 500);
                                } else if (type == 5) { // 图片浮层
                                    onImageClicked(value, "");
                                } else if (type == 6) { // 自助问答 - 不需要处理什么，直接在clicked中会自动回复回来
                                    mOnCallbackListener.onSendMessage(name);
                                    // 延时500ms 让消息先于onQuickBtnClicked发出去
                                    operationView.postDelayed(() -> {
                                        // 延时后执行的操作
                                    }, 500);
                                }
                                mOnCallbackListener.onQuickBtnClicked(mHybridMessage.getConvId(), quickBtnId);
                            } catch (Exception e) {
                                Toast.makeText(getContext(), R.string.mx_title_unknown_error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                // 重新设置 LayoutParams，不然不生效
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                mOperationLl.setLayoutParams(params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 声明一个接口
    public interface OnCallbackListener {
        /**
         * 点击快捷按钮
         * @param quickBtnId 快捷按钮id
         */
        void onQuickBtnClicked(long convId,Integer quickBtnId);

        /**
         * 点击发送消息
         * @param message 消息内容
         */
        void onSendMessage(String message);

        /**
         * 预览图片
         *
         * @param url 图片路径
         */
        void photoPreview(String url);
    }
}
