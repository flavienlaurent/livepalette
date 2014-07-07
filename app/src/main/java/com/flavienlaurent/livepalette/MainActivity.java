package com.flavienlaurent.livepalette;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.PaletteItem;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends Activity implements TextureView.SurfaceTextureListener {

    private ClipboardManager mClipboardManager;

    @InjectView(R.id.texture)
    TextureView mTextureView;
    @InjectView(R.id.vibrant)
    ColorView mVibrantView;
    @InjectView(R.id.dark_vibrant)
    ColorView mDarkVibrantView;
    @InjectView(R.id.light_vibrant)
    ColorView mLightVibrantView;
    @InjectView(R.id.muted)
    ColorView mMutedView;
    @InjectView(R.id.dark_muted)
    ColorView mDarkMutedView;
    @InjectView(R.id.light_muted)
    ColorView mLightMutedView;

    private Camera mCamera;

    private Bitmap mBitmap = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mClipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        mTextureView.setSurfaceTextureListener(this);

        bindColorViewsListener();
    }

    private View.OnClickListener mOnColorViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ColorView colorView = (ColorView) v;
            String hexColor = colorView.getHexColor();
            mClipboardManager.setPrimaryClip(ClipData.newPlainText("hex_color", hexColor));
            Toast.makeText(MainActivity.this, hexColor + " in clipboard.", Toast.LENGTH_SHORT).show();
        }
    };

    private void bindColorViewsListener() {
        mVibrantView.setOnClickListener(mOnColorViewClickListener);
        mMutedView.setOnClickListener(mOnColorViewClickListener);
        mLightMutedView.setOnClickListener(mOnColorViewClickListener);
        mLightVibrantView.setOnClickListener(mOnColorViewClickListener);
        mDarkMutedView.setOnClickListener(mOnColorViewClickListener);
        mDarkVibrantView.setOnClickListener(mOnColorViewClickListener);
    }

    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        try {
            mCamera = Camera.open();
        } catch (RuntimeException e) {
            // Something bad happened
        }
        if(mCamera == null) {
            return;
        }
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            mCamera.setParameters(parameters);
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
        } catch (IOException ioe) {
            // Something bad happened
        }
    }

    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // Ignored, Camera does all the work for us
    }

    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mCamera.stopPreview();
        mCamera.release();
        return true;
    }

    private long mLastUpdate = 0;

    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        long updateTime = System.currentTimeMillis();
        if(mLastUpdate <= 0 || updateTime - mLastUpdate >= 500) {//1/2s
            mLastUpdate = updateTime;
            mTextureView.getBitmap(mBitmap);
            Palette.generateAsync(mBitmap, new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    if(palette == null) {
                        mVibrantView.setBackgroundColor(Color.TRANSPARENT);
                        mMutedView.setBackgroundColor(Color.TRANSPARENT);
                        mLightMutedView.setBackgroundColor(Color.TRANSPARENT);
                        mLightVibrantView.setBackgroundColor(Color.TRANSPARENT);
                        mDarkMutedView.setBackgroundColor(Color.TRANSPARENT);
                        mDarkVibrantView.setBackgroundColor(Color.TRANSPARENT);
                        return;
                    }
                    setColor(mVibrantView, palette.getVibrantColor());
                    setColor(mMutedView, palette.getVibrantColor());
                    setColor(mLightMutedView, palette.getLightMutedColor());
                    setColor(mLightVibrantView, palette.getLightVibrantColor());
                    setColor(mDarkMutedView, palette.getDarkMutedColor());
                    setColor(mDarkVibrantView, palette.getDarkVibrantColor());
                }
            });
        }
    }


    private void setColor(ColorView view, PaletteItem color) {
        if(view != null && color != null) {
            view.setColor(color.getRgb());
        } else if(view != null) {
            view.setColor(Color.TRANSPARENT);
        }
    }
}
