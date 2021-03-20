package com.xiaobo.xiaobobeidanci.lib;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;

import androidx.annotation.Nullable;

import com.xiaobo.xiaobobeidanci.R;

/**
 * @author xiaobo
 */
public class DrawerLayout extends LinearLayout {
    private OnScrollStartListener onScrollStart;
    private OnScrollEndListener onScrollEnd;
    private ViewGroup.LayoutParams layoutParams;
    private final Scroller scroller;
    private int initHeight = -1;
    private int initWidth = -1;
    private boolean first;
    private int moveCount;

    /**
     * 开始滚动整个 DrawerLayout，可能会涉及到其它组件的移动，
     * 其它组件的移动无动画效果且无法与当前组件同时进行
     *
     * @param dx       x偏移量,正值向右移动
     * @param dy       y偏移量,正值向下移动
     * @param duration 运动时间(ms)
     * @param first    是否优先移动当前组件
     * @attention 移动原理：
     * 保持当前控件位于所占区域的右下角，通过扩大或者缩小控件所占区域实现移动
     */
    public void startScroll(int dx, int dy, final int duration, boolean first) {
        if (!scroller.isFinished()) return;
        moveCount++;
        this.first = first;
        if (null != onScrollStart) onScrollStart.onScrollStart();
        scroller.startScroll(getScrollX(), getScrollY(), -dx, -dy, duration);
        if (!first) {
            layoutParams.height -= scroller.getFinalY() - scroller.getStartY();
            layoutParams.width -= scroller.getFinalX() - scroller.getStartX();
            setLayoutParams(layoutParams);
        }
        invalidate();
    }

    /**
     * 获得布局的原始高度
     *
     * @attention 请使用本方法代替getHeight，因为真正的height一直在变化
     */
    public int getInitHeight() {
        return initHeight;
    }

    /**
     * 获得布局的原始宽度
     *
     * @attention 请使用本方法代替getWeight，因为真正的weight一直在变化
     */
    public int getInitWidth() {
        return initWidth;
    }

    /**
     * 获得当前控件的移动次数
     */
    public int getMoveCount() {
        return moveCount;
    }

    public void setOnScrollStart(OnScrollStartListener onScrollStart) {
        this.onScrollStart = onScrollStart;
    }

    public void setOnScrollEnd(OnScrollEndListener onScrollEnd) {
        this.onScrollEnd = onScrollEnd;
    }

    public boolean isScrolling() {
        return !scroller.isFinished();
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            if (scroller.isFinished()) {
                if (null != onScrollEnd) onScrollEnd.onScrollEnd();
                if (first) {
                    layoutParams.height -= scroller.getFinalY() - scroller.getStartY();
                    layoutParams.width -= scroller.getFinalX() - scroller.getStartX();
                    setLayoutParams(layoutParams);
                }
            }
            postInvalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (initHeight == -1) {
            layoutParams = getLayoutParams();
            initHeight = getMeasuredHeight();
            initWidth = getMeasuredWidth();
            layoutParams.height = initHeight;
            layoutParams.width = initWidth;
        }
    }

    public DrawerLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray attrsArr = context.obtainStyledAttributes(attrs, R.styleable.DrawerLayout);
        switch (attrsArr.getInteger(R.styleable.DrawerLayout_interpolator, 0)) {
            case 1:
                scroller = new Scroller(context, new AnticipateInterpolator());
                break;
            case 2:
                scroller = new Scroller(context, new BounceInterpolator());
                break;
            case 3:
                scroller = new Scroller(context, new AnticipateOvershootInterpolator());
                break;
            case 4:
                scroller = new Scroller(context, new OvershootInterpolator());
                break;
            case 5:
                scroller = new Scroller(context, new LinearInterpolator());
                break;
            case 6:
                scroller = new Scroller(context, new DecelerateInterpolator());
                break;
            case 7:
                scroller = new Scroller(context, new AccelerateInterpolator());
                break;
            default:
                scroller = new Scroller(context, new AccelerateDecelerateInterpolator());
                break;
        }
        attrsArr.recycle();
    }

    public interface OnScrollStartListener {
        void onScrollStart();
    }

    public interface OnScrollEndListener {
        void onScrollEnd();
    }
}
