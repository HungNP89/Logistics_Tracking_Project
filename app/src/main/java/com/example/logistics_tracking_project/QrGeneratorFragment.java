package com.example.logistics_tracking_project;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.logistics_tracking_project.databinding.ActivityMainBinding;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import androidmads.library.qrgenearator.QRGSaver;

public class QrGeneratorFragment extends Fragment implements View.OnClickListener{

    private ActivityMainBinding binding;
    private boolean isReadPermissionGranted = false;
    private boolean isWritePermissionGranted = false;
    ActivityResultLauncher<String[]> mPermissionResultLauncher;


    EditText qrInput;
    Button qrButtonGenerate;
    ImageView imageOutput;
    Button qrSave;

    public QrGeneratorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View view =  inflater.inflate(R.layout.fragment_qr_generator, container, false);
       qrInput = view.findViewById(R.id.qr_input);
       qrButtonGenerate = view.findViewById(R.id.qr_button_generate);
       imageOutput = view.findViewById(R.id.image_output);
       qrSave = view.findViewById(R.id.qr_save);
       qrButtonGenerate.setOnClickListener(this);
       qrSave.setOnClickListener(this);

       mPermissionResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
           @Override
           public void onActivityResult(Map<String, Boolean> result) {
               if(result.get(Manifest.permission.READ_EXTERNAL_STORAGE) != null) {
                   isReadPermissionGranted = result.get(Manifest.permission.READ_EXTERNAL_STORAGE);
               }
               if(result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) != null) {
                   isWritePermissionGranted = result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE);
               }
           }
       });

       requestPermission();
       return view;
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
            Toast.makeText(getActivity(), "QR Code successfully saved in the external storage!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void requestPermission() {
        boolean minSDK = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;

        isReadPermissionGranted = ContextCompat.checkSelfPermission(
                getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED;

        isWritePermissionGranted = ContextCompat.checkSelfPermission(
                getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED;

        isWritePermissionGranted = isWritePermissionGranted || minSDK;

        List<String> permissionRequest = new ArrayList<String>();

        if(!isReadPermissionGranted) {
            permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if(!permissionRequest.isEmpty()) {
            mPermissionResultLauncher.launch(permissionRequest.toArray(new String[0]));
        }
    }

}