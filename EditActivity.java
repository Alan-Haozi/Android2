package com.example.note;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class EditActivity extends AppCompatActivity {

    public static final int TAKE_PHOTO = 1;
    private static final int CHOICE_PHOTO=2;

    private EditText etTitle, etContent, etAuthor;
    private Uri imageUri;
    private Button butEditPicC,butEditPicP;
    private ImageView imgV;
    private DatabaseHelper DB;
    private File img;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        etTitle = findViewById(R.id.edit_EditText_title);
        etAuthor = findViewById(R.id.edit_EditText_author);
        etContent = findViewById(R.id.edit_EditText_Content);
        butEditPicC = findViewById(R.id.button_editC);
        butEditPicP = findViewById(R.id.button_editP);
        imgV = findViewById(R.id.imageViewEdit);

        initData(); //???????????????

        butEditPicC.setOnClickListener(new View.OnClickListener() { //????????????
            @Override
            public void onClick(View v) {
                img = new File(getExternalCacheDir(), getTime() + ".jpg"); //???????????????????????????????????????????????????
                try {
                    if (img.exists()){
                        img.delete();
                    }
                    img.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT>=24){
                    imageUri= FileProvider.getUriForFile(EditActivity.this,"com.example.takephoto.fileprovider",img); //??????Uri
                }
                else {
                    imageUri = Uri.fromFile(img); //????????????7.0????????????????????????????????????
                }
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, 1); //??????????????????
            }
        });

        butEditPicP.setOnClickListener(new View.OnClickListener() { //????????????
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(EditActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(EditActivity.this, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
                } else {
                    openAlbum();
                }
            }
        });
    }

    private void initData() { //??????????????????id???????????????????????????
        /*SharedPreferences pref = getSharedPreferences("author", MODE_PRIVATE);//????????????
        String name = pref.getString("name", "?????????");*/

        DB = new DatabaseHelper(this);
        Intent intent = getIntent();
        id = (String) intent.getSerializableExtra("id"); //??????????????????id
        Cursor cursor=DB.queryFromDbById(id);
        if(cursor.moveToFirst()){
            @SuppressLint("Range") String select_title=cursor.getString(cursor.getColumnIndex("title"));
            @SuppressLint("Range") String select_author=cursor.getString(cursor.getColumnIndex("author"));
            @SuppressLint("Range") String select_content=cursor.getString(cursor.getColumnIndex("content"));
            etTitle.setText(select_title);
            etAuthor.setText(select_author);
            etContent.setText(select_content);
            @SuppressLint("Range") byte[] in = cursor.getBlob(cursor.getColumnIndex("picture"));
            Bitmap bitmap=BitmapFactory.decodeByteArray(in,0,in.length);
            imgV.setImageBitmap(bitmap);
        }
    }

    //??????????????????
    private  void openAlbum(){ //??????????????????
        Intent  intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,CHOICE_PHOTO);
    }
    //

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    openAlbum();
                }else {
                    Toast.makeText(this,"You denied the permission",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data){ //?????????????????????
        String imagePath = null;
        Uri uri =data.getData();
        if(DocumentsContract.isDocumentUri(this,uri)){ //?????????document?????????Uri,?????????document id??????
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1]; //????????????????????????id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        }else if("content".equalsIgnoreCase(uri.getScheme())){ //?????????content?????????Uri,????????????????????????
            imagePath = getImagePath(uri,null);
        }else if("file".equalsIgnoreCase(uri.getScheme())){ //?????????file?????????Uri,????????????????????????
            imagePath = uri.getPath();
        }
        displayImage(imagePath); //????????????????????????
    }

    private void handleImageBeforeKitKat(Intent data){ //????????????????????????????????????
        Uri uri = data.getData();
        String imagePath = getImagePath(uri,null);
        displayImage(imagePath);
    }

    @SuppressLint("Range")
    private String getImagePath(Uri uri, String selection){ //???????????????????????????
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection,null, null);
        if(cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath){ //????????????
        if(imagePath != null){
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            imgV.setImageBitmap(bitmap);
        }else{
            Toast.makeText(this,"failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { //??????????????????????????????
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO:
                if (requestCode == 1) {
                    Bitmap bitmap = null;
                    try {
                        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        imgV.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CHOICE_PHOTO:
                if (resultCode == RESULT_OK) { //?????????????????????
                    if (Build.VERSION.SDK_INT >= 19) { //??????4.4?????????????????????????????????
                        handleImageOnKitKat(data);
                    } else {                           //??????4.4?????????????????????????????????
                        handleImageBeforeKitKat(data);
                    }
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //?????????
        getMenuInflater().inflate(R.menu.menu_edit, menu); //????????????
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_save: //??????????????????
                ContentValues values = new ContentValues();
                String title = etTitle.getText().toString(); //????????????????????????
                String author = etAuthor.getText().toString();
                String content = etContent.getText().toString();

                values.put("id", id);//?????????
                values.put("title", title);
                values.put("author", author);
                values.put("content", content);
                values.put("time", getTime());

                final ByteArrayOutputStream os = new ByteArrayOutputStream(); //?????????
                Bitmap bitmap = ((BitmapDrawable)imgV.getDrawable()).getBitmap();
                bitmap.compress(Bitmap.CompressFormat.PNG,100,os);
                values.put("picture",os.toByteArray());

                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(EditActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
                } else {
                    int row = DB.updataData(values, id); //??????????????????????????????????????????
                    values.clear();
                    if (row != -1){
                        Toast.makeText(EditActivity.this, "????????????", Toast.LENGTH_SHORT).show();
                        this.finish();
                    } else {
                        Toast.makeText(EditActivity.this, "????????????", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.menu_edit_cancel:
                Intent intent = new Intent(EditActivity.this, MainActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public String getTime() { //??????????????????
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy???MM???dd??? HH:mm:ss"); //????????????
        Date date = new Date();
        String time = simpleDateFormat.format(date); //??????????????????
        return time;
    }
}