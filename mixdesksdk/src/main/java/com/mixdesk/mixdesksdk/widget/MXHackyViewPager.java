package com.mixdesk.mixdesksdk.widget;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/3/1 下午6:21
 * 描述:解决PhotoView缩放冲突的ViewPager
 */
public class MXHackyViewPager extends ViewPager {

    public MXHackyViewPager(Context context) {
        super(context);
    }

    public MXHackyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }
}