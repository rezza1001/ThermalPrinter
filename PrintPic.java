package com.multiaccess.chipsakti.component.printer;
/**
 * Created by Rezza on 8/23/17.
 */

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class PrintPic {
    public Canvas canvas = null;
    public Paint paint = null;
    public Bitmap bm = null;
    public int width;
    public float length = 0.0F;
    public byte[] bitbuf = null;

    public PrintPic() {
    }

    public int getLength() {
        return (int)this.length + 20;
    }

    public void initCanvas(int w) {
        int h = 10 * w;
        this.bm = Bitmap.createBitmap(w, h, Config.ARGB_4444);
        this.canvas = new Canvas(this.bm);
        this.canvas.drawColor(-1);
        this.width = w;
        this.bitbuf = new byte[this.width / 8];
    }

    public void initPaint() {
        this.paint = new Paint();
        this.paint.setAntiAlias(true);
//        this.paint.setColor(-16777216);
        this.paint.setStyle(Style.STROKE);
    }

    public void drawImage(float x, float y, Bitmap bm) {
        try {
//            BitmapFactory.decodeResource(getResources(), R.drawable.img)
            Bitmap e = bm;
            this.canvas.drawBitmap(e, x, y, (Paint)null);
            if(this.length < y + (float)e.getHeight()) {
                this.length = y + (float)e.getHeight();
            }
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }

    public void printPng() {
        File f = new File("/mnt/sdcard/0.png");
        FileOutputStream fos = null;
        Bitmap nbm = Bitmap.createBitmap(this.bm, 0, 0, this.width, this.getLength());

        try {
            fos = new FileOutputStream(f);
            nbm.compress(CompressFormat.PNG, 50, fos);
        } catch (FileNotFoundException var5) {
            var5.printStackTrace();
        }

    }

    public byte[] printDraw() {
        Bitmap nbm = Bitmap.createBitmap(this.bm, 0, 0, this.width, this.getLength());
        byte[] imgbuf = new byte[this.width / 8 * this.getLength() + 8];
        boolean s = false;
        imgbuf[0] = 29;
        imgbuf[1] = 118;
        imgbuf[2] = 48;
        imgbuf[3] = 0;
        imgbuf[4] = (byte)(this.width / 8);
        imgbuf[5] = 0;
        imgbuf[6] = (byte)(this.getLength() % 256);
        imgbuf[7] = (byte)(this.getLength() / 256);
        int var23 = 7;

        for(int i = 0; i < this.getLength(); ++i) {
            int t;
            for(t = 0; t < this.width / 8; ++t) {
                int c0 = nbm.getPixel(t * 8 + 0, i);
                byte p0;
                if(c0 == -1) {
                    p0 = 0;
                } else {
                    p0 = 1;
                }

                int c1 = nbm.getPixel(t * 8 + 1, i);
                byte p1;
                if(c1 == -1) {
                    p1 = 0;
                } else {
                    p1 = 1;
                }

                int c2 = nbm.getPixel(t * 8 + 2, i);
                byte p2;
                if(c2 == -1) {
                    p2 = 0;
                } else {
                    p2 = 1;
                }

                int c3 = nbm.getPixel(t * 8 + 3, i);
                byte p3;
                if(c3 == -1) {
                    p3 = 0;
                } else {
                    p3 = 1;
                }

                int c4 = nbm.getPixel(t * 8 + 4, i);
                byte p4;
                if(c4 == -1) {
                    p4 = 0;
                } else {
                    p4 = 1;
                }

                int c5 = nbm.getPixel(t * 8 + 5, i);
                byte p5;
                if(c5 == -1) {
                    p5 = 0;
                } else {
                    p5 = 1;
                }

                int c6 = nbm.getPixel(t * 8 + 6, i);
                byte p6;
                if(c6 == -1) {
                    p6 = 0;
                } else {
                    p6 = 1;
                }

                int c7 = nbm.getPixel(t * 8 + 7, i);
                byte p7;
                if(c7 == -1) {
                    p7 = 0;
                } else {
                    p7 = 1;
                }

                int value = p0 * 128 + p1 * 64 + p2 * 32 + p3 * 16 + p4 * 8 + p5 * 4 + p6 * 2 + p7;
                this.bitbuf[t] = (byte)value;
            }

            for(t = 0; t < this.width / 8; ++t) {
                ++var23;
                imgbuf[var23] = this.bitbuf[t];
            }
        }

        return imgbuf;
    }

}
