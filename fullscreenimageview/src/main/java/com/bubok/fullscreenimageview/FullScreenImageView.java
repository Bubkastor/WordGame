package com.bubok.fullscreenimageview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import uk.co.senab.photoview.PhotoViewAttacher;


public class FullScreenImageView extends ImageView {

    private Context context;
    private Activity activity;
    private boolean isFullScreen = true;

    private View viewInflate;
    private PhotoViewAttacher mAttacher;



    public FullScreenImageView(Context context) {
        super(context);
        this.context = context;
        setBackgroundColor(0xFFFFFF);
    }

    public FullScreenImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setBackgroundColor(0xFFFFFF);
    }


    public FullScreenImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        setBackgroundColor(0xFFFFFF);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        setFullScreen();

        return super.onTouchEvent(event);
    }

    private void initInflate(ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        viewInflate = layoutInflater.inflate(R.layout.image_view, parent, false);
        viewInflate.findViewById(R.id.closeButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setFullScreen();
            }
        });
        ImageView imageView = (ImageView) viewInflate.findViewById(R.id.image);
        Bitmap bitmap = ((BitmapDrawable) this.getDrawable()).getBitmap();
        imageView.setImageBitmap(bitmap);
        mAttacher = new PhotoViewAttacher(imageView);
    }

    private void setFullScreen() {
        if (this.isFullScreen) {
            View rootView = getRootView();
            View v = rootView.findViewById(android.R.id.content);
            if (v instanceof ViewGroup) {
                if (viewInflate == null)
                    initInflate((ViewGroup) v);

                ((ViewGroup) v).addView(viewInflate);
            }

        } else {
            View rootView = getRootView();
            View v = rootView.findViewById(android.R.id.content);
            if (v instanceof ViewGroup) {
                ((ViewGroup) v).removeView(viewInflate);
            }

        }

        isFullScreen = !isFullScreen;
    }
}
