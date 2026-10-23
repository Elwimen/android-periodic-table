package com.frozendevs.periodictable.widget;

import android.content.Context;
import android.util.AttributeSet;

public class AppBarLayout extends com.google.android.material.appbar.AppBarLayout {
    public AppBarLayout(Context context) {
        super(context);
    }

    public AppBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, 0);
    }
}
