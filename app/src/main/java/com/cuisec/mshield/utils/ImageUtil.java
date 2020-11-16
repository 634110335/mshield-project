package com.cuisec.mshield.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.util.Log;

public class ImageUtil {

    // 图片等比例尺寸压缩
    public static Bitmap scaleBitmap(Bitmap bitmap, int width) {
        bitmap = autoResize(bitmap);
        int ratio;
        if (bitmap.getWidth() <= width) {//小于指定width不压缩
            ratio = 100;
        } else {
            //*100来提高压缩精度
            ratio = bitmap.getWidth() * 100 / width;
        }

        // 压缩Bitmap到对应尺寸
        Bitmap result = Bitmap.createBitmap(width, bitmap.getHeight() * 100 / ratio, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Rect rect = new Rect(0, 0, width, bitmap.getHeight() * 100 / ratio);
        canvas.drawBitmap(bitmap, null, rect, null);
        return result;
    }

    // 去掉图像边缘多余的透明点
    public static Bitmap autoResize(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        //依次循环对图像的像素进行处理
        int x1 = width;
        int y1 = height;
        int x2 = 0;
        int y2 = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb = bitmap.getPixel(i, j);
                if (rgb != 0) { // 不透明
                    if (i < x1) x1 = i;
                    if (j < y1) y1 = j;
                    if (i > x2) x2 = i;
                    if (j > y2) y2 = j;
                }
            }
        }

        if (x1 > 0 && x2 > x1 && y1 > 0 && y2 > y1) {
            return Bitmap.createBitmap(bitmap, x1, y1, x2 - x1 + 1, y2 - y1 + 1);
        } else {
            return bitmap;
        }
    }

    // 图像透明处理
    public static Bitmap transparency(Bitmap bitmap, int threshold, @ColorInt int color) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // 创建二值化图像
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // 依次循环，对图像的像素进行处理
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                // 得到当前像素的值
                int col = bitmap.getPixel(i, j);

                // 得到图像的像素RGB的值
                int red = (col & 0x00FF0000) >> 16;
                int green = (col & 0x0000FF00) >> 8;
                int blue = (col & 0x000000FF);

                // 用公式X = 0.3×R+0.59×G+0.11×B计算出X代替原来的RGB
                int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                if (gray > threshold) {
                    result.setPixel(i, j, 0X00);
                } else {
                    result.setPixel(i, j, color);
                }
            }
        }
        return autoResize(result);
    }

    public static boolean isBitmapEmpty(Bitmap mBitmap) {

        int mWidth = mBitmap.getWidth();
        int mHeight = mBitmap.getHeight();
        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                int color = mBitmap.getPixel(j, i);
                if (color != 0) {
                    Log.e("onViewClicked", "i:" + i + "j:" + j + " isImageEmpty: " + color);
                    return false;
                }
            }
        }
        return true;
    }

}
