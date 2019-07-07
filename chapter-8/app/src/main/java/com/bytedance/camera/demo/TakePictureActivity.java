package com.bytedance.camera.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bytedance.camera.demo.utils.Utils;

import java.io.File;
import java.io.IOException;

public class TakePictureActivity extends AppCompatActivity {
    private static final String TAG = "TakePictureActivity";
    private ImageView imageView;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PERMISSION_REQUEST_CODE = 20;
    private static final int CAMERA_REQUEST_CODE=1;

    private static final int REQUEST_EXTERNAL_STORAGE = 101;

    private File imageFile;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_picture);
        imageView = findViewById(R.id.img);
        findViewById(R.id.btn_picture).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(TakePictureActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(TakePictureActivity.this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //todo 在这里申请相机、存储的权限
                String[] permission=new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                };
                ActivityCompat.requestPermissions(this,permission,PERMISSION_REQUEST_CODE);
            } else {
                takePicture();
                Intent refresh=new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,Uri.parse("file://"+(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)).toString()));
//        refresh.setData(Uri.fromFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)));
                sendBroadcast(refresh);
            }
        });

    }

    private void takePicture() {
        //todo 打开相机
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager())!=null){
            imageFile= Utils.getOutputMediaFile(Utils.MEDIA_TYPE_IMAGE);
            if (imageFile!=null){
                Uri uri= FileProvider.getUriForFile(this,getPackageName(),imageFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
            }
            startActivityForResult(intent,REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setPic();
        }
    }

    private void setPic() {
        //todo 根据imageView裁剪
        int targetw=imageView.getWidth();
        int targeth=imageView.getHeight();
        //todo 根据缩放比例读取文件，生成Bitmap
        BitmapFactory.Options bmOptions=new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds=true;

        BitmapFactory.decodeFile(imageFile.getAbsolutePath(),bmOptions);
        int photoW=bmOptions.outWidth;
        int photoH=bmOptions.outHeight;
        int scaleFactor=Math.min(photoH/targeth,photoW/targetw);

        bmOptions.inJustDecodeBounds=false;
        bmOptions.inSampleSize=scaleFactor;
        bmOptions.inPurgeable=true;

        Bitmap bitmap=BitmapFactory.decodeFile(imageFile.getAbsolutePath(),bmOptions);
        //todo 如果存在预览方向改变，进行图片旋转
        try {
            ExifInterface exifInterface = new ExifInterface(imageFile.getAbsolutePath());
            Matrix matrix=new Matrix();
            int angle=0;
            int orientation=exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);
            switch (orientation){
                case ExifInterface.ORIENTATION_ROTATE_90:
                    angle=90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    angle=180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    angle=270;
                    break;
                default:
                    break;
            }
            matrix.postRotate(angle);
            bitmap=Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
            imageView.setImageBitmap(bitmap);
        }
        catch (IOException ioe){
            Log.e(TAG, "setPic: ", ioe);
        }
        //todo 如果存在预览方向改变，进行图片旋转
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                //todo 判断权限是否已经授予
                if (grantResults[0]==PackageManager.PERMISSION_GRANTED&&grantResults[1]==PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "484884", Toast.LENGTH_SHORT).show();
                }
//                ContextCompat.checkSelfPermission(TakePictureActivity.this,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                break;
            }
        }
    }
}
