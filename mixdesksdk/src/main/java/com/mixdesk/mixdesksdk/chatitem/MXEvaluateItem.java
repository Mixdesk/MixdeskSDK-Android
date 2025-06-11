package com.mixdesk.mixdesksdk.chatitem;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mixdesk.core.bean.MXEvaluateConfig;
import com.mixdesk.mixdesksdk.R;
import com.mixdesk.mixdesksdk.model.EvaluateMessage;
import com.mixdesk.mixdesksdk.util.MXUtils;
import com.mixdesk.mixdesksdk.widget.MXBaseCustomCompositeView;

import java.util.List;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/5/23 下午4:54
 * 描述:评价消息item
 */
public class MXEvaluateItem extends MXBaseCustomCompositeView {
    private ImageView mLevelProblemIv;
    private TextView mLevelProblemTv;
    private ImageView mLevelIv;
    private TextView mLevelTv;
    private TextView mContentTv;
    private LinearLayout mTagsContainer;


    public MXEvaluateItem(Context context) {
        super(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mx_item_msg_evaluate;
    }

    @Override
    protected void initView() {
        // 问题解决选择
        mLevelProblemTv = getViewById(R.id.tv_problem_content);
        mLevelProblemIv = getViewById(R.id.iv_msg_evaluate_problem);

        // 级别选择
        mLevelIv = getViewById(R.id.iv_msg_evaluate_level);
        mLevelTv = getViewById(R.id.tv_msg_evaluate_level_content);

        // 选择的tag
        mTagsContainer = getViewById(R.id.tags_container);

        mContentTv = getViewById(R.id.tv_msg_evaluate_content);
    }

    @Override
    protected void setListener() {
    }

    @Override
    protected void processLogic() {
    }

    public void setMessage(EvaluateMessage evaluateMessage) {
        mLevelProblemTv.setVisibility(View.GONE);
        mLevelProblemIv.setVisibility(View.GONE);
        if (evaluateMessage.isSolved() == 1) {
            mLevelProblemTv.setText(R.string.mx_problem_solved);
            mLevelProblemIv.setImageResource(R.drawable.mx_evaluate_solved_line_color);
            mLevelProblemTv.setVisibility(View.VISIBLE);
            mLevelProblemIv.setVisibility(View.VISIBLE);
        } else if (evaluateMessage.isSolved() == 0) {
            mLevelProblemTv.setText(R.string.mx_problem_unsolved);
            mLevelProblemIv.setImageResource(R.drawable.mx_evaluate_unsolved_line_color);
            mLevelProblemIv.setPadding(0, MXUtils.dip2px(getContext(), 2), 0, 0);
            mLevelProblemTv.setVisibility(View.VISIBLE);
            mLevelProblemIv.setVisibility(View.VISIBLE);
        }

        int bitMapCol = evaluateMessage.getLevel() + 1;
        if (evaluateMessage.getEvaluateLevel() == 3) {
            if (evaluateMessage.getLevel() == 0) {
                bitMapCol = 1;
            } else if (evaluateMessage.getLevel() == 1) {
                bitMapCol = 3;
            } else if (evaluateMessage.getLevel() == 2) {
                bitMapCol = 5;
            }
        }
        mLevelIv.setImageBitmap(
                getLevelSprite(
                        bitMapCol,
                        4));

        // 从配置中获取当前评价等级对应的名称
        String levelName = "";
        List<MXEvaluateConfig> configList = evaluateMessage.getEvaluateConfig();
        for (MXEvaluateConfig config : configList) {
            if (config.getLevel() == evaluateMessage.getLevel()) {
                levelName = config.getName();
                break;
            }
        }
        mLevelTv.setText(levelName);


        mTagsContainer.removeAllViews();
        // 设置选择的tag
        if (evaluateMessage.getSelectTagIds() != null && !evaluateMessage.getSelectTagIds().isEmpty()) {
            // 创建圆角背景
            GradientDrawable normalDrawable = new GradientDrawable();
            normalDrawable.setColor(getResources().getColor(R.color.mx_evaluate_not_enabled));
            normalDrawable.setCornerRadius(MXUtils.dip2px(getContext(), 5));

            LinearLayout.LayoutParams tagParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            tagParams.setMargins(MXUtils.dip2px(getContext(), 5), MXUtils.dip2px(getContext(), 5),
                    MXUtils.dip2px(getContext(), 5), MXUtils.dip2px(getContext(), 5));

            // 先测量父容器最大宽度
            int maxWidth = ((Activity) getContext()).findViewById(android.R.id.content).getWidth()
                    - mTagsContainer.getPaddingLeft() - mTagsContainer.getPaddingRight();

            LinearLayout currentLine = createNewLine();
            mTagsContainer.addView(currentLine);

            int usedWidth = 0;

            for (MXEvaluateConfig.Tag tag : evaluateMessage.getSelectTagIds()) {
                TextView tagTv = new TextView(getContext());
                tagTv.setHeight(MXUtils.dip2px(getContext(), 30));
                tagTv.setText(tag.getName());
                tagTv.setPadding(MXUtils.dip2px(getContext(), 10),
                        MXUtils.dip2px(getContext(), 5),
                        MXUtils.dip2px(getContext(), 10),
                        MXUtils.dip2px(getContext(), 5));
                tagTv.setTextColor(getResources().getColor(R.color.mx_activity_title_textColor));
                tagTv.setBackground(normalDrawable);
                tagTv.setLayoutParams(tagParams);

                // 先测量宽度
                tagTv.measure(0, 0);
                int tagWidth = tagTv.getMeasuredWidth() + tagParams.leftMargin + tagParams.rightMargin;

                if (usedWidth + tagWidth > maxWidth) {
                    // 换行
                    currentLine = createNewLine();
                    mTagsContainer.addView(currentLine);
                    usedWidth = 0;
                }
                currentLine.addView(tagTv);
                usedWidth += tagWidth;
            }
        }

        final String context = evaluateMessage.getContent();
        if (!TextUtils.isEmpty(context)) {
            mContentTv.setVisibility(View.VISIBLE);
            mContentTv.setText(context);
        } else {
            mContentTv.setVisibility(View.GONE);
        }
    }

    private Bitmap getLevelSprite(int col, int row) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        int spriteWidth = 134;
        int spriteHeight = 132;
        if (row == 3) {
            spriteHeight = 127; // 雪碧图不标准，这里单独处理了下
        }
        int left = (col - 1) * spriteWidth;
        int top = (row - 1) * spriteHeight;
        options.inJustDecodeBounds = false;
        options.inScaled = false;
        Bitmap spriteSheet = BitmapFactory.decodeResource(getResources(), R.drawable.mx_evaluate_sprite_sheet, options);
        return Bitmap.createBitmap(spriteSheet, left, top, spriteWidth, spriteHeight);
    }

    // 新建一行
    private LinearLayout createNewLine() {
        LinearLayout line = new LinearLayout(getContext());
        line.setOrientation(LinearLayout.HORIZONTAL);
        line.setGravity(android.view.Gravity.CENTER);
        line.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        return line;
    }
}
