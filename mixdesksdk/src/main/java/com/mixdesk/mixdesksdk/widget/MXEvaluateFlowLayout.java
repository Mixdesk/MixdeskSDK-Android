package com.mixdesk.mixdesksdk.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * 专门为评价视图设计的自动换行布局
 * 当第一行放不下所有视图时，将最后两个视图（评级图标和评级文本）作为一个整体换到第二行
 */
public class MXEvaluateFlowLayout extends ViewGroup {

    public MXEvaluateFlowLayout(Context context) {
        super(context);
    }

    public MXEvaluateFlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MXEvaluateFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int realWidth = MeasureSpec.getSize(widthMeasureSpec);
        int childCount = this.getChildCount();
        
        if (childCount == 0) {
            setMeasuredDimension(0, 0);
            return;
        }

        // 测量所有可见的子视图
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
            }
        }

        int totalWidth = 0;
        int maxHeight = 0;
        
        // 计算所有可见子视图的总宽度和最大高度
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                totalWidth += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                maxHeight = Math.max(maxHeight, child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
            }
        }

        int resultWidth;
        int resultHeight;

        if (totalWidth <= realWidth || childCount < 4) {
            // 一行能放下所有视图，或者子视图数量少于4个
            resultWidth = Math.min(totalWidth, realWidth);
            resultHeight = maxHeight;
            
            // 设置位置信息（水平居中，垂直居中）
            int startX = (realWidth - Math.min(totalWidth, realWidth)) / 2;
            int currentX = startX;
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (child.getVisibility() != GONE) {
                    lp.x = currentX + lp.leftMargin;
                    // 垂直居中：(maxHeight - 子视图高度) / 2
                    lp.y = (maxHeight - child.getMeasuredHeight()) / 2 + lp.topMargin;
                    currentX += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                } else {
                    // GONE 状态的视图位置设为 0
                    lp.x = 0;
                    lp.y = 0;
                }
            }
        } else {
            // 需要换行，将最后两个可见视图（评级图标和评级文本）放到第二行
            // 先收集所有可见的视图
            int[] visibleChildren = new int[childCount];
            int visibleCount = 0;
            for (int i = 0; i < childCount; i++) {
                if (getChildAt(i).getVisibility() != GONE) {
                    visibleChildren[visibleCount++] = i;
                }
            }
            
            if (visibleCount < 2) {
                // 可见视图少于2个，按一行处理
                resultWidth = Math.min(totalWidth, realWidth);
                resultHeight = maxHeight;
                
                int startX = (realWidth - Math.min(totalWidth, realWidth)) / 2;
                int currentX = startX;
                for (int i = 0; i < childCount; i++) {
                    View child = getChildAt(i);
                    LayoutParams lp = (LayoutParams) child.getLayoutParams();
                    if (child.getVisibility() != GONE) {
                        lp.x = currentX + lp.leftMargin;
                        lp.y = (maxHeight - child.getMeasuredHeight()) / 2 + lp.topMargin;
                        currentX += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                    } else {
                        lp.x = 0;
                        lp.y = 0;
                    }
                }
            } else {
                // 将前面的视图放第一行，最后两个可见视图放第二行
                int firstLineCount = visibleCount - 2;
                int firstLineWidth = 0;
                int secondLineWidth = 0;
                
                // 计算第一行宽度
                for (int i = 0; i < firstLineCount; i++) {
                    View child = getChildAt(visibleChildren[i]);
                    LayoutParams lp = (LayoutParams) child.getLayoutParams();
                    firstLineWidth += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                }
                
                // 计算第二行宽度
                for (int i = firstLineCount; i < visibleCount; i++) {
                    View child = getChildAt(visibleChildren[i]);
                    LayoutParams lp = (LayoutParams) child.getLayoutParams();
                    secondLineWidth += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                }
                
                resultWidth = realWidth;
                resultHeight = maxHeight * 2; // 两行的高度
                
                // 设置第一行的位置
                int startX = (realWidth - firstLineWidth) / 2;
                int currentX = startX;
                for (int i = 0; i < firstLineCount; i++) {
                    View child = getChildAt(visibleChildren[i]);
                    LayoutParams lp = (LayoutParams) child.getLayoutParams();
                    lp.x = currentX + lp.leftMargin;
                    lp.y = (maxHeight - child.getMeasuredHeight()) / 2 + lp.topMargin;
                    currentX += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                }
                
                // 设置第二行的位置
                currentX = (realWidth - secondLineWidth) / 2;
                for (int i = firstLineCount; i < visibleCount; i++) {
                    View child = getChildAt(visibleChildren[i]);
                    LayoutParams lp = (LayoutParams) child.getLayoutParams();
                    lp.x = currentX + lp.leftMargin;
                    lp.y = maxHeight + (maxHeight - child.getMeasuredHeight()) / 2 + lp.topMargin;
                    currentX += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
                }
                
                // 设置不可见视图的位置
                for (int i = 0; i < childCount; i++) {
                    View child = getChildAt(i);
                    if (child.getVisibility() == GONE) {
                        LayoutParams lp = (LayoutParams) child.getLayoutParams();
                        lp.x = 0;
                        lp.y = 0;
                    }
                }
            }
        }

        setMeasuredDimension(resolveSize(resultWidth, widthMeasureSpec),
                resolveSize(resultHeight, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childCount = this.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = this.getChildAt(i);
            if (child.getVisibility() != GONE) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                child.layout(lp.x, lp.y, lp.x + child.getMeasuredWidth(), lp.y + child.getMeasuredHeight());
            }
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public static class LayoutParams extends MarginLayoutParams {
        int x = 0;
        int y = 0;

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }
} 