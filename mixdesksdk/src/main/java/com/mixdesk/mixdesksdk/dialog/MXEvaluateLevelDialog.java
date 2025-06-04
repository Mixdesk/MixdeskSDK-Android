package com.mixdesk.mixdesksdk.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mixdesk.core.bean.MXEvaluateConfig;
import com.mixdesk.mixdesksdk.R;
import com.mixdesk.mixdesksdk.util.MXUtils;

import java.util.ArrayList;
import java.util.List;

public class MXEvaluateLevelDialog extends Dialog implements View.OnClickListener {

    private final TextView mTipTv;
    private final EditText mContentEt;
    private final TextView mConfirmTv;
    private Callback mCallback;
    private LinearLayout layoutSolved;
    private LinearLayout layoutUnsolved;
    private Bitmap[] bitmapResolves = new Bitmap[5];
    private Bitmap[] bitmapUnResolves = new Bitmap[5];
    private ImageView[] imageViews = new ImageView[5];
    private int prevSelectLevel = -1;
    private List<MXEvaluateConfig> evaluateConfig;
    private boolean isSolved = false;

    private Activity activity;


    private int isSolvedVal = -1;
    private int selectedLevel = -1;
    private List<MXEvaluateConfig.Tag> selectedTagIds = new ArrayList<>();
    private int evaluateLevel;

    /**
     * @param activity
     */
    public MXEvaluateLevelDialog(Activity activity) {
        super(activity, R.style.MQDialog);

        this.activity = activity;
        MXUtils.updateLanguage(activity);

        setContentView(R.layout.mx_dialog_evaluate);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        setCancelable(true);

        mTipTv = findViewById(R.id.tv_evaluate_tip);
        mContentEt = findViewById(R.id.et_evaluate_content);
        findViewById(R.id.tv_evaluate_cancel).setOnClickListener(this);
        mConfirmTv = findViewById(R.id.tv_evaluate_confirm);
        mConfirmTv.setOnClickListener(this);


        layoutSolved = findViewById(R.id.layout_solved);
        layoutUnsolved = findViewById(R.id.layout_unsolved);

        for (int i = 0; i < 5; i++) {
            bitmapUnResolves[i] = getLevelSprite(activity, i + 1, 3);
            bitmapResolves[i] = getLevelSprite(activity, i + 1, 4);
        }

        layoutSolved.setOnClickListener(v -> {
            isSolved = true;
            isSolvedVal = 1;
            updateSelectionState();
        });

        layoutUnsolved.setOnClickListener(v -> {
            isSolved = false;
            isSolvedVal = 0;
            updateSelectionState();
        });
    }

    private Bitmap getBitmap(Bitmap[] bitmaps, int level) {
        if (this.evaluateLevel == 3) {
            if (level == 0) {
                return bitmaps[0];
            } else if (level == 1) {
                return bitmaps[2];
            } else if (level == 3) {
                return bitmaps[4];
            }
        }
        return bitmaps[level];
    }

    public void init(
            Boolean problemFeedback,
            Integer evaluateLevel,
            String tip,
            List<MXEvaluateConfig> config) {
        this.evaluateLevel = evaluateLevel;
        this.evaluateConfig = config;
        this.mContentEt.setText("");
        this.mContentEt.clearFocus();
        this.isSolvedVal = -1;
        this.selectedLevel = -1;
        this.selectedTagIds.clear();

        if (!TextUtils.isEmpty(tip)) {
            mTipTv.setText(tip);
        }

        View problemFeedbackLayout = findViewById(R.id.problem_feedback);
        if (problemFeedback != null && problemFeedback) {
            problemFeedbackLayout.setVisibility(View.VISIBLE);
        } else {
            problemFeedbackLayout.setVisibility(View.GONE);
        }


        LinearLayout levelContainer = findViewById(R.id.evaluate_level_img_container);
        // 为每个level创建ImageView
        for (MXEvaluateConfig evaluateConfig : config) {
            ImageView levelImage = new ImageView(activity);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    MXUtils.dip2px(activity, 44),
                    MXUtils.dip2px(activity, 44));
            params.setMargins(MXUtils.dip2px(activity, 10), 0, 0, 0);
            levelImage.setLayoutParams(params);

            // 设置level图片
            levelImage.setImageBitmap(getBitmap(bitmapUnResolves, evaluateConfig.getLevel()));
            levelImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            levelImage.setTag(evaluateConfig);

            // 添加点击事件
            levelImage.setOnClickListener(v -> {
                ImageView clickedImage = (ImageView) v;
                clickedImage.setImageBitmap(bitmapResolves[evaluateConfig.getLevel()]);
                // clear
                if (prevSelectLevel >= 0 && prevSelectLevel != evaluateConfig.getLevel()) {
                    imageViews[prevSelectLevel].setImageBitmap(getBitmap(bitmapUnResolves, prevSelectLevel));
                    selectedTagIds.clear();
                }
                prevSelectLevel = evaluateConfig.getLevel();
                selectedLevel = evaluateConfig.getLevel(); // 记录选中的level

                MXEvaluateConfig selectedConfig = (MXEvaluateConfig) v.getTag();
                // 显示选中的level名称
                TextView levelNameTv = findViewById(R.id.tv_level_name);
                levelNameTv.setText(selectedConfig.getName());
                levelNameTv.setVisibility(View.VISIBLE);

                // 显示tags
                LinearLayout tagsContainer = findViewById(R.id.tags_container);
                tagsContainer.removeAllViews();
                tagsContainer.setVisibility(View.VISIBLE);

                LinearLayout.LayoutParams tagParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                tagParams.setMargins(MXUtils.dip2px(activity, 5), MXUtils.dip2px(activity, 2),
                         0, MXUtils.dip2px(activity, 2));

                // 创建圆角背景
                GradientDrawable normalDrawable = new GradientDrawable();
                normalDrawable.setColor(activity.getResources().getColor(R.color.mx_item_pressed));
                normalDrawable.setCornerRadius(MXUtils.dip2px(activity, 5));

                // 先测量父容器最大宽度
                int maxWidth = tagsContainer.getWidth()
                        - tagsContainer.getPaddingLeft() - tagsContainer.getPaddingRight();

                LinearLayout currentLine = createNewLine();
                tagsContainer.addView(currentLine);

                int usedWidth = 0;

                for (MXEvaluateConfig.Tag tag : selectedConfig.getTags()) {
                    CheckBox tagTv = new CheckBox(activity);
                    tagTv.setText(tag.getName());
                    tagTv.setTag(tag); // 绑定tag的id
                    tagTv.setButtonDrawable(null); // 移除默认的checkbox图标
                    tagTv.setHeight(MXUtils.dip2px(getContext(), 30));
                    tagTv.setPadding(MXUtils.dip2px(activity, 10),
                            MXUtils.dip2px(activity, 0),
                            MXUtils.dip2px(activity, 10),
                            MXUtils.dip2px(activity, 0));
                    tagTv.setTextColor(activity.getResources().getColor(R.color.mx_activity_title_textColor));
                    tagTv.setBackground(normalDrawable);
                    tagTv.setLayoutParams(tagParams);

                    // 设置选中状态的背景颜色
                    tagTv.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        GradientDrawable drawable = new GradientDrawable();
                        if (isChecked) {
                            drawable.setColor(activity.getResources().getColor(R.color.mx_colorPrimary));
                            buttonView.setTextColor(activity.getResources().getColor(R.color.mx_white));
                            selectedTagIds.add((MXEvaluateConfig.Tag) buttonView.getTag()); // 添加选中的tag id
                        } else {
                            drawable.setColor(activity.getResources().getColor(R.color.mx_item_pressed));
                            buttonView.setTextColor(activity.getResources().getColor(R.color.mx_activity_title_textColor));
                            selectedTagIds.remove((MXEvaluateConfig.Tag) buttonView.getTag()); // 移除取消选中的tag id
                        }
                        drawable.setCornerRadius(MXUtils.dip2px(activity, 5));
                        buttonView.setBackground(drawable);
                    });

                    // 先测量宽度
                    tagTv.measure(0, 0);
                    int tagWidth = tagTv.getMeasuredWidth() + tagParams.leftMargin + tagParams.rightMargin;

                    if (usedWidth + tagWidth > maxWidth) {
                        // 换行
                        currentLine = createNewLine();
                        tagsContainer.addView(currentLine);
                        usedWidth = 0;
                    }
                    currentLine.addView(tagTv);
                    usedWidth += tagWidth;
                }
            });

            imageViews[evaluateConfig.getLevel()] = levelImage;
            levelContainer.addView(levelImage);
        }
    }

    private void updateSelectionState() {
        layoutSolved.setSelected(isSolved);
        layoutUnsolved.setSelected(!isSolved);

        // 更新文本颜色
        int selectedColor = getContext().getResources().getColor(R.color.mx_chat_event_gray);
        int unselectedColor = getContext().getResources().getColor(R.color.mx_evaluate_hint);

        ((TextView) findViewById(R.id.tv_solved)).setTextColor(isSolved ? selectedColor : unselectedColor);
        ((TextView) findViewById(R.id.tv_unsolved)).setTextColor(isSolved ? unselectedColor : selectedColor);
    }

    @Override
    public void show() {
        super.show();
        if (getWindow() != null) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_evaluate_confirm && mCallback != null) {
            if (selectedLevel == -1) {
                // 请选择评价等级
                MXUtils.show(getContext(), R.string.mx_evaluate_require_level);
                return;
            }
            // 判断当前选择的等级配置，是否必须选择tag
            if (evaluateConfig != null && selectedTagIds.isEmpty()) {
                for (MXEvaluateConfig config : evaluateConfig) {
                    if (config.getLevel() == selectedLevel) {
                        if (config.isRequired()) {
                            MXUtils.show(getContext(), R.string.mx_evaluate_require_tag);
                            return;
                        }
                    }
                }
            }

            MXUtils.closeKeyboard(this);
            dismiss();

            String content = mContentEt.getText().toString().trim();
            List<MXEvaluateConfig.Tag> copySelectedTagIds = new ArrayList<>(selectedTagIds);
            List<MXEvaluateConfig> copyEvaluateConfig = new ArrayList<>(evaluateConfig);
            mCallback.executeEvaluate(isSolvedVal, selectedLevel, copySelectedTagIds, content, evaluateLevel, copyEvaluateConfig);
            clear();
        } else if (v.getId() == R.id.tv_evaluate_cancel) {
            dismiss();
            clear();
        }
    }

    private void clear() {
        // 重置状态
        LinearLayout levelContainer = findViewById(R.id.evaluate_level_img_container);
        levelContainer.removeAllViews();
        LinearLayout tagsContainer = findViewById(R.id.tags_container);
        tagsContainer.removeAllViews();
        TextView levelNameTv = findViewById(R.id.tv_level_name);
        levelNameTv.setVisibility(View.GONE);

        layoutSolved.setSelected(false);
        layoutUnsolved.setSelected(false);

        MXUtils.closeKeyboard(this);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    private Bitmap getLevelSprite(Activity activity, int col, int row) {
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
        Bitmap spriteSheet = BitmapFactory.decodeResource(activity.getResources(), R.drawable.mx_evaluate_sprite_sheet, options);
        return Bitmap.createBitmap(spriteSheet, left, top, spriteWidth, spriteHeight);
    }

    private LinearLayout createNewLine() {
        LinearLayout line = new LinearLayout(activity);
        line.setOrientation(LinearLayout.HORIZONTAL);
        line.setGravity(android.view.Gravity.CENTER);
        line.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        return line;
    }

    public interface Callback {
        /**
         * @param isSolved 是否解决问题 -1 未选择
         * @param level    评价等级
         * @param tagIds   选中的标签ID列表
         * @param content  评价内容
         */
        void executeEvaluate(int isSolved, int level, List<MXEvaluateConfig.Tag> tagIds, String content, int evaluateLevel, List<MXEvaluateConfig> evaluateConfig);
    }
}