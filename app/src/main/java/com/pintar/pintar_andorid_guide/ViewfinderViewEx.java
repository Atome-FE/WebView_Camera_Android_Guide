/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pintar.pintar_andorid_guide;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.ScreenUtils;
import com.google.zxing.ResultPoint;

import java.util.ArrayList;
import java.util.List;


/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderViewEx extends FrameLayout {

    private static final long ANIMATION_DELAY = 80L;
    private static final int CURRENT_POINT_OPACITY = 0xA0;
    private static final int MAX_RESULT_POINTS = 20;
    private static final int POINT_SIZE = 6;

    private final Paint paint;
    private Bitmap resultBitmap;
    private final int maskColor;
    private final int resultColor;
    private final int borderColor;
    private final int cornerStroke;
    private final int cornerWidth;
    private int scannerAlpha;
    private List<ResultPoint> possibleResultPoints;
    private List<ResultPoint> lastPossibleResultPoints;

    private ImageView scanImage;
    private FrameLayout scanImageContainer;
    private ObjectAnimator scanObjectAnimator;


    // This constructor is used when the class is built from an XML resource.
    public ViewfinderViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setWillNotDraw(false);

        // Initialize these once for performance rather than calling them every time in onDraw().
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.color_qr_transparent);
        resultColor = resources.getColor(R.color.color_qr_transparent);
        borderColor = resources.getColor(com.blankj.utilcode.R.color.design_default_color_primary);
        ;
        scannerAlpha = 0;
        possibleResultPoints = new ArrayList<>(5);
        lastPossibleResultPoints = null;
        cornerStroke = DimensionExtensionKt.toPx(2);
        cornerWidth = DimensionExtensionKt.toPx(80);


        scanImage = new ImageView(context);
        scanImage.setImageResource(R.mipmap.ic_qr_scan);
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Rect frame = getFramingRect();

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // Draw the exterior (i.e. outside the framing rect) darkened
        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);


        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(CURRENT_POINT_OPACITY);
            canvas.drawBitmap(resultBitmap, null, frame, paint);
        } else {

            // Draw border
            float strokeWidth = paint.getStrokeWidth();
            Paint.Style style = paint.getStyle();

            paint.setColor(borderColor);
            paint.setStrokeWidth(DimensionExtensionKt.toDP(1));
            paint.setStyle(Paint.Style.STROKE);
//            canvas.drawRect(frame.left, frame.top, frame.right, frame.bottom, paint);

            paint.setStrokeWidth(cornerStroke);

            canvas.drawLine(frame.left, frame.top + (cornerStroke >> 1), frame.left + cornerWidth, frame.top + (cornerStroke >> 1), paint);
            canvas.drawLine(frame.left + (cornerStroke >> 1), frame.top, frame.left + (cornerStroke >> 1), frame.top + cornerWidth, paint);

            canvas.drawLine(frame.left, frame.bottom - (cornerStroke >> 1), frame.left + cornerWidth, frame.bottom - (cornerStroke >> 1), paint);
            canvas.drawLine(frame.left + (cornerStroke >> 1), frame.bottom, frame.left + (cornerStroke >> 1), frame.bottom - cornerWidth, paint);

            canvas.drawLine(frame.right, frame.top + (cornerStroke >> 1), frame.right - cornerWidth, frame.top + (cornerStroke >> 1), paint);
            canvas.drawLine(frame.right - (cornerStroke >> 1), frame.top, frame.right - (cornerStroke >> 1), frame.top + cornerWidth, paint);

            canvas.drawLine(frame.right, frame.bottom - (cornerStroke >> 1), frame.right - cornerWidth, frame.bottom - (cornerStroke >> 1), paint);
            canvas.drawLine(frame.right - (cornerStroke >> 1), frame.bottom, frame.right - (cornerStroke >> 1), frame.bottom - cornerWidth, paint);

            paint.setStrokeWidth(strokeWidth);
            paint.setStyle(style);

            postInvalidateDelayed(ANIMATION_DELAY, frame.left - POINT_SIZE, frame.top - POINT_SIZE, frame.right + POINT_SIZE, frame.bottom + POINT_SIZE);
        }
    }

    public void drawViewfinder() {
        Bitmap resultBitmap = this.resultBitmap;
        this.resultBitmap = null;
        if (resultBitmap != null) {
            resultBitmap.recycle();
        }

        if (scanImageContainer == null) {
            Rect framingRect = getFramingRect();
            if (framingRect != null) {
                scanImageContainer = new FrameLayout(getContext());
                LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, framingRect.height());
                layoutParams.leftMargin = DimensionExtensionKt.toPx(20);
                layoutParams.rightMargin = DimensionExtensionKt.toPx(20);
                layoutParams.topMargin = framingRect.top;
                addView(scanImageContainer, layoutParams);

                LayoutParams imageLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                scanImageContainer.addView(scanImage, imageLayoutParams);

                scanObjectAnimator = ObjectAnimator.ofFloat(scanImage, "y", -imageLayoutParams.height, layoutParams.height + imageLayoutParams.height);
                scanObjectAnimator.setDuration(4000);
                scanObjectAnimator.setRepeatCount(ValueAnimator.INFINITE);

                TextView hintText = new TextView(getContext());
                hintText.setGravity(Gravity.CENTER);
                hintText.setText(R.string.string_qr_hint);
                hintText.setBackgroundResource(R.drawable.shape_qr_hint_background);
                int padding = DimensionExtensionKt.toPx(12);
                hintText.setPadding(padding, padding, padding, padding);
                LayoutParams hintTextLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

                hintTextLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                hintTextLayoutParams.topMargin = framingRect.bottom + DimensionExtensionKt.toPx(48);

                hintTextLayoutParams.leftMargin = framingRect.left - DimensionExtensionKt.toPx(8);
                hintTextLayoutParams.rightMargin = framingRect.left - DimensionExtensionKt.toPx(8);

                hintText.setTextColor(Color.WHITE);
                hintText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                addView(hintText, hintTextLayoutParams);
            }
        }

        if (scanImageContainer != null) {
            if (!scanObjectAnimator.isStarted()) {
                scanObjectAnimator.start();
            }
            scanImageContainer.setVisibility(VISIBLE);
        }

        invalidate();
    }

    /**
     * Draw a bitmap with the result points highlighted instead of the live scanning display.
     *
     * @param barcode An image of the decoded barcode.
     */
    public void drawResultBitmap(Bitmap barcode) {
        resultBitmap = barcode;


        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        List<ResultPoint> points = possibleResultPoints;
        synchronized (points) {
            points.add(point);
            int size = points.size();
            if (size > MAX_RESULT_POINTS) {
                // trim it
                points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
            }
        }
    }

    public void onPause() {
        if (scanImageContainer != null) {
            scanImageContainer.setVisibility(GONE);
            scanObjectAnimator.cancel();
        }
    }

    public void onResume() {

    }

    private static Rect framingRect;

    static synchronized Rect getFramingRect() {
        if (framingRect == null) {

            int screenWidth = ScreenUtils.getScreenWidth();
            int screenHeight = ScreenUtils.getScreenHeight();

            int width;
            int height;

            //fix rect by xingbo.jie
            width = height = (int) ScreenUtils.getScreenWidth() - DimensionExtensionKt.toPx(48) * 2;

            int leftOffset = (screenWidth - width) / 2;
//            int topOffset = (screenHeight - height) / 2;
            int topOffset = DimensionExtensionKt.toPx(132);
            framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
        }
        return framingRect;
    }

}
