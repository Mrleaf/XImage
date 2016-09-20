package cn.leaf.ximage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 图片压缩
 * Created by leaf on 2016/9/8.
 */
public class IMGCompression {
    private static final String TAG = "IMGCompression";
    private static String DISK_CACHE = "disk_cache";
    private OnCompressionListener mListener;
    private File mFile;
    private List<File> mFileList;
    private final File mCacheDir;
    private String mSavePath;
    private static IMGCompression IMG;

    /**
     *
     * @param mCacheDir
     */
    public IMGCompression(File mCacheDir) {
        this.mCacheDir = mCacheDir;
    }

    /**
     *
     * @param context
     * @return
     */
    public static IMGCompression get(Context context){
        if(IMG==null)
            IMG = new IMGCompression(getPhotoCacheDir(context,DISK_CACHE));
        return IMG;
    }

    /**
     * 要压缩的文件
     * @param file
     * @return
     */
    public IMGCompression loadFile(File file){
        this.mFile = file;
        return this;
    }

    /**
     * 要压缩的文件 批量
     * @param fileList
     * @return
     */
    public IMGCompression loadFile(List<File> fileList){
        this.mFileList = fileList;
        return this;
    }

    /**
     * 监听
     * @param listener
     * @return
     */
    public IMGCompression setListener(OnCompressionListener listener){
        this.mListener = listener;
        return this;
    }

    /**
     * 保存路径
     * @param savePath
     * @return
     */
    public IMGCompression setSavePath(String savePath){
        this.mSavePath = savePath;
        return this;
    }

    /**
     * 开始压缩
     * @return
     */
    public IMGCompression start(){
        if(mListener!=null)
            mListener.onStart();
        if(mFile!=null){
            Observable.just(mFile)
                    .map(new Func1<File,File>() {
                        @Override
                        public File call(File file) {
                            return compress(file);
                        }
                    })
                    .subscribeOn(Schedulers.io())// 指定 subscribe() 发生在 IO 线程
                    .observeOn(AndroidSchedulers.mainThread())// 指定 Subscriber 的回调发生在主线程
                    .doOnError(new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            if (mListener != null)
                                mListener.onError(throwable);
                        }
                    })
                    .onErrorResumeNext(Observable.<File>empty())
                    .filter(new Func1<File, Boolean>() {
                        @Override
                        public Boolean call(File file) {
                            return file != null;
                        }
                    })
                    .subscribe(new Action1<File>() {
                        @Override
                        public void call(File file) {
                            if(mListener!=null){
                                List<File> list = new ArrayList<File>();
                                list.add(file);
                                mListener.onSuccess(list);
                            }

                        }
                    });
        }else if(mFileList!=null&&mFileList.size()>0){
            Observable.just(mFileList)
                    .map(new Func1<List<File>,List<File>>() {
                        @Override
                        public List<File> call(List<File> fileList) {
                            List<File> list = new ArrayList<File>();
                            for(File file:fileList){
                                list.add(compress(file));
                            }
                            return list;
                        }
                    })
                    .subscribeOn(Schedulers.io())// 指定 subscribe() 发生在 IO 线程
                    .observeOn(AndroidSchedulers.mainThread())// 指定 Subscriber 的回调发生在主线程
                    .doOnError(new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            if (mListener != null)
                                mListener.onError(throwable);
                        }
                    })
                    .onErrorResumeNext(Observable.<List<File>>empty())
                    .filter(new Func1<List<File>, Boolean>() {
                        @Override
                        public Boolean call(List<File> file) {
                            return file != null;
                        }
                    })
                    .subscribe(new Action1<List<File>>() {
                        @Override
                        public void call(List<File> file) {
                            if (mListener != null)
                                mListener.onSuccess(file);
                        }
                    });
        }


        return this;
    }

    /**
     *
     * @param context
     * @param cacheName
     * @return
     */
    private static File getPhotoCacheDir(Context context, String cacheName) {
        File cacheDir = context.getCacheDir();
        if (cacheDir != null) {
            File result = new File(cacheDir, cacheName);
            if (!result.mkdirs() && (!result.exists() || !result.isDirectory())) {
                return null;
            }
            return result;
        }
        return null;
    }


    /**
     * 图片压缩
     * @param file
     * @return
     */
    private File compress(@NonNull File file) {
        String thumb = mSavePath;
        if(Util.isEmpty(mSavePath)){
            thumb = mCacheDir.getAbsolutePath() + File.separator + System.currentTimeMillis()+".jpg";
        }
        double size;
        String filePath = file.getAbsolutePath();
        int angle = getImageSpinAngle(filePath);
        int width = getImageSize(filePath)[0];
        int height = getImageSize(filePath)[1];
        int thumbW = width;
        int thumbH = height;
        int maxLength = thumbW > thumbH ? thumbW : thumbH;
        double scale = thumbW > thumbH?((double) height / width):((double) width / height);
        //        常见照片比例
        //        1:1--1
        //        4:5--0.8
        //        3:4--0.75
        //        13:17--0.7647058823529411
        //        11:15--0.7333333333333333
        //        8:11--0.7272727272727273
        //        5:7--0.7142857142857143
        //        2:3--0.6666666666666666
        //        5:8--0.625
        //        3:5--0.6
        //        9:16--0.5625
        //        1:2--0.5
        //        3:7--0.428571429

        //QQ  960px
        //Wechat 1280px

        if(scale<=1 && scale >= 0.5625){
            //1:1---9:16  包含大部分常见比例图片
            Log.i(TAG + "-1", "scale：" + scale + "size：" + file.length() / 1024);
            if (file.length() / 1024 < 100) return file;
            if(maxLength <= 1280){
                size = (width * height) / Math.pow(1280, 2) * 150;
                size = size < 60 ? 60 : size;
            }else{
                double multiple = maxLength / 1280.0;
                thumbW = width>=height?1280:(int)(width / multiple);
                thumbH = width>=height?(int)(height / multiple):1280;
                size = (thumbW * thumbH) / Math.pow(2560, 2) * 300;
                size = size < 60 ? 60 : size;
            }
        }else if(scale<0.5625 && scale > 0.4285){
            //9:16---- 3:7
            Log.i(TAG + "-2", "scale：" + scale + "size：" + file.length() / 1024);
            if (file.length() / 1024 < 150) return file;
            if(maxLength <= 1280){
                size = (thumbW * thumbH) / (1440.0 * 2560.0) * 300;
                size = size < 100 ? 100 : size;
            }else{
                double multiple = maxLength / 1280.0;
                thumbW = width>=height?1280:(int)(width / multiple);
                thumbH = width>=height?(int)(height / multiple):1280;
                size = (thumbW * thumbH) / (1440.0 * 2560.0) * 300;
                size = size < 100 ? 100 : size;
            }
        }else{
            //长图
            Log.i(TAG+"-3", "scale：" + scale + "size：" + file.length() / 1024);
            if(file.length()/1024<300)return file;
            if(width<=1280||height<=1280){
                size = ((thumbW * thumbH) / (maxLength * (1280 / scale))) * 500;
                size = size < file.length()/1024 ? size : file.length()/1024;
            }else{
                double multiple = width>height?height / 1280.0:width/1280.0;
                thumbW =(int) (width / multiple);
                thumbH = (int)(height / multiple);
                size = ((thumbW * thumbH) / (maxLength * (1280 / scale))) * 500;
                size = size < file.length()/1024 ? size : file.length()/1024;
            }

        }

        return compress(file,filePath, thumb, thumbW, thumbH, angle, (long) size);
    }



    /**
     * 获取图像长宽
     *
     * @param imagePath the path of image
     */
    private int[] getImageSize(String imagePath) {
        int[] res = new int[2];
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;
        BitmapFactory.decodeFile(imagePath, options);
        res[0] = options.outWidth;
        res[1] = options.outHeight;
        return res;
    }

    /**
     * 获取指定大小的图像
     *
     * @param imagePath
     * @param width
     * @param height
     * @return {@link Bitmap}
     */
    private Bitmap compress(String imagePath, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        int outH = options.outHeight;
        int outW = options.outWidth;
        int inSampleSize = 1;

        if (outH > height || outW > width) {
            int halfH = outH / 2;
            int halfW = outW / 2;

            while ((halfH / inSampleSize) > height && (halfW / inSampleSize) > width) {
                inSampleSize *= 2;
            }
        }

        options.inSampleSize = inSampleSize;

        options.inJustDecodeBounds = false;

        int heightRatio = (int) Math.ceil(options.outHeight / (float) height);
        int widthRatio = (int) Math.ceil(options.outWidth / (float) width);

        if (heightRatio > 1 || widthRatio > 1) {
            if (heightRatio > widthRatio) {
                options.inSampleSize = heightRatio;
            } else {
                options.inSampleSize = widthRatio;
            }
        }
        options.inJustDecodeBounds = false;
        Bitmap bit =  BitmapFactory.decodeFile(imagePath, options);
        return Bitmap.createScaledBitmap(bit, width, height, true);
    }


    /**
     * 获得图像旋转角
     *
     * @param path
     * @return
     */
    private static int getImageSpinAngle(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 指定参数压缩图片
     *
     * @param originalFile 原图文件
     * @param originalPath 原图路径
     * @param thumbFilePath  保存临时路径
     * @param width
     * @param height
     * @param angle          旋转角度
     * @param size           压缩大小
     * @return
     */
    private File compress(File originalFile,String originalPath, String thumbFilePath, int width, int height, int angle, long size) {
        Bitmap thbBitmap = compress(originalPath, width, height);

        thbBitmap = rotatingImage(angle, thbBitmap);
        File file = saveImage(thumbFilePath, thbBitmap, size);
        if(originalFile.length()<=file.length())
            return originalFile;
        else
            return file;
    }

    /**
     * 旋转图片
     *
     * @param angle  旋转的角度
     * @param bitmap 图片
     * @return
     */
    private static Bitmap rotatingImage(int angle, Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * 保存图片到指定路径
     *
     * @param filePath  储存路径
     * @param bitmap    图片
     * @param size      期望大小
     * @return
     */
    private File saveImage(String filePath, Bitmap bitmap, long size) {
        if(Util.isEmpty(bitmap)){
            Log.e(TAG, "bitmap 不能为空");
            return null;
        }
        File result = new File(filePath.substring(0, filePath.lastIndexOf("/")));
        if (!result.exists() && !result.mkdirs()) return null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        int options = 100;
        bitmap.compress(Bitmap.CompressFormat.JPEG, options, stream);
        while (stream.toByteArray().length / 1024 > size && options > 50) {
            stream.reset();
            options -= 6;
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, stream);
        }
        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(stream.toByteArray());
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new File(filePath);
    }
}
