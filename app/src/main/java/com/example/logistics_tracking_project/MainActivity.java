package com.example.logistics_tracking_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;



import java.io.File;
import java.io.IOException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import androidmads.library.qrgenearator.QRGSaver;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    EditText qrInput;
    Button qrButtonGenerate;
    ImageView imageOutput;
    Button qrSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Components();
        permission();
    }

    private void Components() {
        qrInput = findViewById(R.id.qr_input);
        qrButtonGenerate = findViewById(R.id.qr_button_generate);
        imageOutput = findViewById(R.id.image_output);
        qrSave = findViewById(R.id.qr_save);
        qrButtonGenerate.setOnClickListener(this);
        qrSave.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
         switch (view.getId()) {
             case R.id.qr_button_generate:
                 generate();
                 break;
             case R.id.qr_save:
                 save();
                 break;
         }
    }

    private void generate() {
        String text = qrInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            qrInput.setError("Enter something");
            return;
        }
        QRGEncoder qrgEncoder = new QRGEncoder(text, null, QRGContents.Type.TEXT, 150);
        Bitmap bitmap = qrgEncoder.getBitmap();
        imageOutput.setImageBitmap(bitmap);
    }

    private void save(){
        String text = qrInput.getText().toString().trim();
        if(TextUtils.isEmpty(text)){
            qrInput.setError("Enter something");
            return;
        }

        try {
            QRGEncoder qrgEncoder = new QRGEncoder(text,null, QRGContents.Type.TEXT,150);
            Bitmap bitmap = qrgEncoder.getBitmap();
            imageOutput.setImageBitmap(bitmap);

            String fileName = "QRCode_"+System.currentTimeMillis()+".jpg";
            File file = new File(Environment.getExternalStorageDirectory(),fileName);
            file.createNewFile();
            String saveLocation = file.getParent()+File.separator ;
            fileName = file.getName().substring(0,file.getName().indexOf("."));
            QRGSaver qrgSaver = new QRGSaver();
            qrgSaver.save(saveLocation,fileName,bitmap,QRGContents.ImageType.IMAGE_JPEG);
            Toast.makeText(this, "QR Code successfully saved in the external storage!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void permission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 0){
            if(grantResults.length > 0){
                for(int i=0;i<grantResults.length;i++)
                    if(grantResults[0] != PackageManager.PERMISSION_GRANTED)
                        finish();
            }else{
                finish();
            }
        }
    }
}