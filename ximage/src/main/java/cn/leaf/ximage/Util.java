package cn.leaf.ximage;

import android.graphics.BitmapFactory;

/**
 * Created by leaf on 2016/9/7.
 */
public class Util {

    public static boolean isEmpty(Object obj) {
        return obj == null || obj.toString().equalsIgnoreCase("null") || obj.toString().length() == 0;
    }

    public static int[] getImageSize(String imagePath) {
        int[] res = new int[2];
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;
        BitmapFactory.decodeFile(imagePath, options);
        res[0] = options.outWidth;
        res[1] = options.outHeight;
        return res;
    }
}
