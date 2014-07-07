package com.flavienlaurent.livepalette;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.util.Property;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ColorView extends LinearLayout {

    @InjectView(R.id.name)
    TextView mNameView;
    @InjectView(R.id.hex)
    TextView mHexView;

    private String mName;
    private int mBgColor = Color.TRANSPARENT;
    private GradientDrawable mBgDrawable;

    public ColorView(Context context) {
        super(context);
        init();
    }

    public ColorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs, 0);
        init();
    }

    public ColorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttrs(attrs, defStyle);
        init();
    }

    private void initAttrs(AttributeSet attrs, int defStyle) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ColorView, defStyle, 0);
        mName = a.getString(R.styleable.ColorView_colorName);
        a.recycle();
    }

    private void init() {
        inflate(getContext(), R.layout.view_color, this);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        ButterKnife.inject(this);

        mNameView.setText(mName);

        int padding = (int)getResources().getDimension(R.dimen.padding);
        setPadding(padding, padding, padding, padding);

        setBackgroundResource(R.drawable.circle);
        mBgDrawable = (GradientDrawable) getBackground();
    }

    public void setColor(int color) {
        mBgColor = color;

        //setBackgroundColor(color);
        ObjectAnimator animator = ObjectAnimator.ofInt(this, BG_COLOR_PROPERTY, color);
        animator.setEvaluator(sArgbEvaluator);
        animator.setDuration(450);
        animator.start();
        mHexView.setText(intColorToHex(color));
    }

    public String getHexColor() {
        return intColorToHex(mBgColor);
    }

    public int getColor() {
        return mBgColor;
    }

    private String intColorToHex(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }

    private static final ArgbEvaluator sArgbEvaluator = new ArgbEvaluator();

    private static final Property<ColorView, Integer> BG_COLOR_PROPERTY  =
            new Property<ColorView, Integer>(Integer.class, "BG_COLOR_PROPERTY") {

                @Override
                public void set(ColorView view, Integer color) {
                    view.mBgDrawable.setColor(color);
                }

                @Override
                public Integer get(ColorView view) {
                    return view.mBgColor;
                }
            };
}
