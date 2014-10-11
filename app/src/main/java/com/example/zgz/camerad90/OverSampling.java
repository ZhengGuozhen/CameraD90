package com.example.zgz.camerad90;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

/**
 * Created by Administrator on 2014/9/29 0029.
 */
public class OverSampling {

    static public Bitmap convert(Bitmap bitmap){

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int w2 = w/2;
        int h2 = h/2;

        Bitmap bitmap2 = Bitmap.createBitmap(w2, h2, Bitmap.Config.ARGB_8888);

        int color[]=new int[w*2];
        int color2[]=new int[w2];
        int aa=0,rr=0,gg=0,bb=0;
        int i=0,j=0;

        for (j = 0; j < h; j+=2) {
            bitmap.getPixels(color,0,w,0,j,w,2);

            for (i = 0; i < w; i+=2) {
//                rr = Color.red(color[i])+Color.red(color[i+1])
//                        +Color.red(color[i+w])+Color.red(color[i+w+1]);
//                gg = Color.green(color[i])+Color.green(color[i+1])
//                        +Color.green(color[i+w])+Color.green(color[i+w+1]);
//                bb = Color.blue(color[i])+Color.blue(color[i+1])
//                        +Color.blue(color[i+w])+Color.blue(color[i+w+1]);
//
//                color2[i/2] = Color.rgb(rr/4,gg/4,bb/4);

                aa = ((color[i] >> 24) & 0xFF) + ((color[i+1] >> 24) & 0xFF) + ((color[i+w] >> 24) & 0xFF) + ((color[i+w+1] >> 24) & 0xFF);
                rr = ((color[i] >> 16) & 0xFF) + ((color[i+1] >> 16) & 0xFF) + ((color[i+w] >> 16) & 0xFF) + ((color[i+w+1] >> 16) & 0xFF);
                gg = ((color[i] >> 8) & 0xFF) + ((color[i+1] >> 8) & 0xFF) + ((color[i+w] >> 8) & 0xFF) + ((color[i+w+1] >> 8) & 0xFF);
                bb = (color[i] & 0xFF) + (color[i+1] & 0xFF) + (color[i+w] & 0xFF) + (color[i+w+1] & 0xFF);
                color2[i>>1] = (aa>>2)<<24 | (rr>>2)<<16 | (gg>>2)<<8 | (bb>>2);

            }

            bitmap2.setPixels(color2,0,w2,0,j/2,w2,1);
        }

        return bitmap2;
    }
}
