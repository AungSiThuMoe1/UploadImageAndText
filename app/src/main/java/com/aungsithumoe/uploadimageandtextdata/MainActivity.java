package com.aungsithumoe.uploadimageandtextdata;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_GALLERY_CODE = 200;
    static final int MY_PERMISSIONS_REQUEST_READ_ExternalStorage = 100;
    ImageView imgUserPhoto;
    Button btnUpload;
    EditText edUsername,edPassword;
    File uploadFile = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imgUserPhoto = findViewById(R.id.img_user_name);
        btnUpload = findViewById(R.id.btn_upload);
        edUsername = findViewById(R.id.ed_username);
        edPassword= findViewById(R.id.ed_password);
        imgUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_ExternalStorage);
                } else {
                    pickFromGallery();
                }
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadToServer(uploadFile);
            }
        });

    }

    private void pickFromGallery() {
        //Create an Intent with action as ACTION_PICK
        Intent intent = new Intent(Intent.ACTION_PICK);
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.setType("image/*");
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        // Launching the Intent
        startActivityForResult(intent, REQUEST_GALLERY_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_GALLERY_CODE && resultCode == Activity.RESULT_OK)
        {
            Uri uri = data.getData();
            String imagePath = getRealPathFromURIPath(uri,MainActivity.this);
            Bitmap bitmap=resizeBitMapImage1(imagePath,600,300);
            uploadFile = new File(imagePath);
            OutputStream os;
            try {
                os = new FileOutputStream(uploadFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                os.flush();
                os.close();
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
            }

            imgUserPhoto.setImageBitmap(bitmap);
           /* Bitmap changeBitmap = null;
            try {
                changeBitmap = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver() , uri);
            } catch (IOException e) {
                e.printStackTrace();
            }*/

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_ExternalStorage: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    pickFromGallery();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

        }
    }


    private String getRealPathFromURIPath(Uri contentURI, Activity activity) {
        Cursor cursor = activity.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }

    }

    public static Bitmap resizeBitMapImage1(String filePath, int targetWidth, int targetHeight) {
        Bitmap bitMapImage = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);
            double sampleSize = 0;
            Boolean scaleByHeight = Math.abs(options.outHeight - targetHeight) >= Math
                    .abs(options.outWidth - targetWidth);
            if (options.outHeight * options.outWidth * 2 >= 1638) {
                sampleSize = scaleByHeight ? options.outHeight / targetHeight
                        : options.outWidth / targetWidth;
                sampleSize = (int) Math.pow(2d,
                        Math.floor(Math.log(sampleSize) / Math.log(2d)));
            }
            options.inJustDecodeBounds = false;
            options.inTempStorage = new byte[128];
            while (true) {
                try {
                    options.inSampleSize = (int) sampleSize;
                    bitMapImage = BitmapFactory.decodeFile(filePath, options);
                    break;
                } catch (Exception ex) {
                    try {
                        sampleSize = sampleSize * 2;
                    } catch (Exception ex1) {

                    }
                }
            }
        } catch (Exception ex) {

        }
        return bitMapImage;
    }

    private void uploadToServer(final File file){
        Retrofit retrofit = APIClient.getClient();
        UploadImage uploadImage= retrofit.create(UploadImage.class);
        if(file.exists())
        {
            RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"),file);
            MultipartBody.Part part = MultipartBody.Part.createFormData("file",file.getName(),fileReqBody);
            RequestBody description = RequestBody.create(MediaType.parse("text/plain"),file.getName());
            Call<UploadObject> call= uploadImage.uploadImage(part,description);
            call.enqueue(new Callback<UploadObject>() {
                @Override
                public void onResponse(Call<UploadObject> call, Response<UploadObject> response) {
                    Toast.makeText(getApplicationContext(),response.body().getSuccess(),Toast.LENGTH_LONG).show();
                    Users users = new Users(edUsername.getText().toString(),edPassword.getText().toString(),"http://192.168.1.5/uploads/"+file.getName());
                    Call<String> callCreateUser = APIClient.getClient().create(CreateUser.class).createUser(users);
                    callCreateUser.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            Toast.makeText(getApplicationContext(),response.body(),Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(getApplicationContext(),"Fail"+ t.getMessage(),Toast.LENGTH_LONG).show();
                            System.out.println("Fail:::" + t.getMessage());
                        }
                    });
                }

                @Override
                public void onFailure(Call<UploadObject> call, Throwable t) {
                    Toast.makeText(getApplicationContext(),"Fail"+ t.getMessage(),Toast.LENGTH_LONG).show();
                    System.out.println("Fail:::" + t.getMessage());
                }
            });
        }
    }

}