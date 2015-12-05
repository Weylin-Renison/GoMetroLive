package com.gometro.gometrolivedev;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Created by wprenison on 2015/10/21.
 * A class designed to be reused for manipulation of drawables
 * like dynamic rotations and adding text to drawbables ect.
 */
public class GraphicsHelper
{
    Context context;

    public GraphicsHelper(Context context)
    {
        this.context = context;
    }

    public Drawable rotateToDegrees(float degrees, int drawableResource)
    {
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), drawableResource);

        // Getting width & height of the given image.
        int w = bmp.getWidth();
        int h = bmp.getHeight();

        // Setting post rotate to bearing
        Matrix mtx = new Matrix();
        mtx.postRotate(degrees);

        // Rotating Bitmap
        Bitmap rotatedBMP = Bitmap.createBitmap(bmp, 0, 0, w, h, mtx, true);
        BitmapDrawable bmd = new BitmapDrawable(rotatedBMP);

        //Print bearing
//        Toast.makeText(this, "degrees: " + degrees, Toast.LENGTH_LONG).show();

        return bmd;
    }

    public Drawable writeOnDrawable(int drawableId, String text, int color, float textSize){

        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), drawableId).copy(Bitmap.Config.ARGB_8888, true);

        Paint paint = new Paint();
        paint.setStyle(Style.FILL);
        paint.setColor(color);
        paint.setTextSize(textSize);

        Canvas canvas = new Canvas(bm);
        canvas.drawText(text, bm.getWidth()/2.5f, bm.getHeight()/1.5f, paint);

        return new BitmapDrawable(bm);
    }

    public Drawable scaleDrawable(int drawableId,  int pixelWidth, int pixelHeight)
    {
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), drawableId).copy(Bitmap.Config.ARGB_8888, true);
        return new BitmapDrawable(Bitmap.createScaledBitmap(bm, pixelWidth, pixelHeight, true));
    }

    public Drawable scaleDrawable(Drawable drawable, int pixelWidth, int pixelHeight)
    {
        Bitmap bm = ((BitmapDrawable)drawable).getBitmap();
        return new BitmapDrawable(Bitmap.createScaledBitmap(bm, pixelWidth, pixelHeight, true));
    }
}
