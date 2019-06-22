package com.example.sembi.logingui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

// TODO Currently Not In Use
public class Family_tree_profile extends View {


    private String sFullName;
    private Drawable imageDrawable;

    private Paint myPaint, picPaint, fullNamePaint, fullNameBorderPaint;

    public Family_tree_profile(Context context, AttributeSet attrs) {
        super(context, attrs);

        picPaint = new Paint();
        fullNameBorderPaint = new Paint();
        fullNamePaint = new Paint();

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.Family_tree_profile, 0, 0);

        try {
            //get the text and colors specified using the names in attrs.xml
            sFullName = a.getString(R.styleable.Family_tree_profile_fullName);
            imageDrawable = a.getDrawable(R.styleable.Family_tree_profile_drawable);
        } finally {
            a.recycle();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        int w = this.getMeasuredWidth(),
                h = this.getMeasuredHeight();

        picPaint.setStyle(Style.FILL);
        picPaint.setAntiAlias(true);
        picPaint.setColor(Color.BLUE);
        int circleRadius = Math.min(2 * h / 3, w);
        canvas.drawArc(w / 2 - circleRadius / 2, h / 2 - circleRadius / 2, w / 2 + circleRadius / 2, h / 2 + circleRadius / 2, 0, 360, true, picPaint);
    }


    //Getters & Setters

    public String getsFullName() {
        return sFullName;
    }

    public void setsFullName(String sFullName) {
        this.sFullName = sFullName;

        invalidate();
        requestLayout();
    }

    public Drawable getImageDrawabale() {
        return imageDrawable;
    }

    public void setImageDrawable(Drawable imageReference) {
        this.imageDrawable = imageReference;

        invalidate();
        requestLayout();
    }

    public Paint getPicPaint() {
        return picPaint;
    }

    public Paint getFullNamePaint() {
        return fullNamePaint;
    }

    public Paint getFullNameBorderPaint() {
        return fullNameBorderPaint;
    }
}