package cn.leaf.example;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.leaf.ximage.IMGCompression;
import cn.leaf.ximage.OnCompressionListener;
import cn.leaf.ximage.Util;

public class MainActivity extends Activity {

    public static final String dir = Environment.getExternalStorageDirectory()+ "/AA";
    public static final String cameraFile = "/background.jpg";
    public static final String nativeFile = "/native.jpg";
    public static final String libjpegFile = "/libjpeg.jpg";
    private TextView fileSize,imageSize;
    private ImageView image;
    private ListView listView;
    private Button button,button1;
    private NativeAdapter adapter;
    private List<File> dataList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataList = new ArrayList<File>();
        fileSize = (TextView) findViewById(R.id.file_size);
        imageSize = (TextView) findViewById(R.id.image_size);
        image = (ImageView) findViewById(R.id.image);
        listView = (ListView) findViewById(R.id.list);
        adapter = new NativeAdapter(dataList,this);
        listView.setAdapter(adapter);
        button = (Button)findViewById(R.id.but);
        button1 = (Button)findViewById(R.id.but1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //下面这句指定调用相机拍照后的照片存储的路径
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri
                        .fromFile(new File(MainActivity.dir, MainActivity.cameraFile)));
                startActivityForResult(intent, 2);
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // 如果是直接从相册获取
            case 1:
                if (data != null) {
                    Uri uri = data.getData();
                    String path = "";
                    if (!Util.isEmpty(uri.getAuthority())) {
                        Cursor cursor = getContentResolver().query(uri,
                                new String[]{MediaStore.Images.Media.DATA}, null, null, null);
                        if (cursor == null) {
                            return;
                        }
                        cursor.moveToFirst();
                        path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                        cursor.close();
                    } else {
                        path = uri.getPath();
                    }
                    File imgFile = new File(path);
                    fileSize.setText(imgFile.length() / 1024 + "k");
                    imageSize.setText(Util.getImageSize(imgFile.getPath())[0] + " * " + Util.getImageSize(imgFile.getPath())[1]);
                    getBitmap(imgFile);
                }
                break;
            case 2:
                File temp = new File(MainActivity.dir,MainActivity.cameraFile);
                if (temp != null) {
                    fileSize.setText(temp.length() / 1024 + "k");
                    imageSize.setText(Util.getImageSize(temp.getPath())[0] + " * " + Util.getImageSize(temp.getPath())[1]);
                    getBitmap(temp);
                }
                break;
            default:
                break;
        }
    }

    private void getBitmap(File file){
        Glide.with(this).load(file.getPath()).into(image);
        //单张图片
        one(file);
        //多张图片
//        more(file);
    }
    private void one(File file){
        String str =  file.getName().substring(0, file.getName().indexOf("."));

        IMGCompression.get(this).loadFile(file)
                .setSavePath(MainActivity.dir + "/" + str + ".jpg")//单张路径（不填也有默认路径）
                .setListener(new OnCompressionListener() {
                    @Override
                    public void onStart() {
                        dataList.clear();
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onSuccess(List<File> fileList) {
                        dataList.addAll(fileList);
                        Log.e("---", dataList.size() + "");
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("---", e.getMessage());
                    }
                }).start();
    }
    private void more(File file){
        List<File> list = new ArrayList<File>();
        list.add(file);
        File temp = new File(Environment.getExternalStorageDirectory()+ "/test/test","/41.jpg");
        list.add(temp);
        temp = new File(Environment.getExternalStorageDirectory()+ "/test/test","/18.png");
        list.add(temp);
        temp = new File(Environment.getExternalStorageDirectory()+ "/test/test","/21.jpg");
        list.add(temp);
        temp = new File(Environment.getExternalStorageDirectory()+ "/test/test","/56.png");
        list.add(temp);


        IMGCompression.get(this).loadFile(list)
                .setListener(new OnCompressionListener() {
                    @Override
                    public void onStart() {
                        dataList.clear();
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onSuccess(List<File> fileList) {
                        dataList.addAll(fileList);
                        Log.e("---", dataList.size() + "");
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("---", e.getMessage());
                    }
                }).start();
    }
    public class NativeAdapter extends BaseAdapter {

        private Context mContext;
        private List<File> mListData;
        public NativeAdapter(List<File> listData, Context context) {
            this.mContext = context;
            this.mListData = listData;
        }
        @Override
        public int getCount() {
            return mListData!=null?mListData.size():0;
        }

        @Override
        public Object getItem(int position) {
            return mListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHoder hoder;
            if (convertView == null) {
                hoder = new ViewHoder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.activity_native_item, null);
                hoder.mImageView = (ImageView)convertView.findViewById(R.id.imgView);
                hoder.thumbFileSize = (TextView) convertView.findViewById(R.id.thumb_file_size);
                hoder.thumbImageSize = (TextView) convertView.findViewById(R.id.thumb_image_size);
                convertView.setTag(hoder);
            } else {
                hoder = (ViewHoder) convertView.getTag();
            }
            File file = mListData.get(position);
            Log.e("图片路径--"+position, file.getPath());
            Glide.with(MainActivity.this).load(file.getPath()).into(hoder.mImageView);
            hoder.thumbFileSize.setText(file.length() / 1024 + "kb");
            hoder.thumbImageSize.setText(Util.getImageSize(file.getPath())[0] + " * "
                    + Util.getImageSize(file.getPath())[1]);
            return convertView;
        }

        public class ViewHoder {
            ImageView mImageView;
            TextView thumbFileSize,thumbImageSize;
        }
    }
}
