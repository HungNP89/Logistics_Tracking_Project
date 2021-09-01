package com.example.logistics_tracking_project;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class QrScanner extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE=223;
    private static final int READ_STORAGE_PERMISSION_CODE=144;
    private static final int WRITE_STORAGE_PERMISSION_CODE=144;
    private static final String TAG = "Tag";
    Button btnScan;
    ImageView imageView;
    TextView showText;

    ActivityResultLauncher<Intent> cameraLauncher;
    ActivityResultLauncher<Intent> storageLauncher;

    InputImage inputImage;
    BarcodeScanner scanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        btnScan = findViewById(R.id.btn_scan);
        imageView = findViewById(R.id.imageQR);
        showText = findViewById(R.id.show_text);

        scanner = BarcodeScanning.getClient();
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Intent data = result.getData();
                        try {
                            Bitmap image = (Bitmap) data.getExtras().get("data");
                            inputImage = InputImage.fromBitmap(image , 0);
                            inputImage = InputImage.fromFilePath(QrScanner.this,data.getData());
                            processImage();
                        } catch(Exception e) {
                            Log.d(TAG , "onActivityResult:" + e.getMessage());
                        }
                    }
                }
        );
        storageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Intent data = result.getData();
                        try {
                            inputImage = InputImage.fromFilePath(QrScanner.this,data.getData());
                        } catch(Exception e) {
                            Log.d(TAG , "onActivityResult:" + e.getMessage());
                        }
                    }
                }
        );

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] options = {"camera","gallery"};
                AlertDialog.Builder builder = new AlertDialog.Builder(QrScanner.this);
                builder.setTitle("Choose");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i==0) {
                            Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            cameraLauncher.launch(camera);
                        } else {
                            Intent storage = new Intent();
                            storage.setType("image/*");
                            storage.setAction(Intent.ACTION_GET_CONTENT);
                            storageLauncher.launch(storage);
                        }
                    }
                });
                builder.show();
            }
        });
    }

    private void processImage() {
        imageView.setVisibility(View.GONE);
        showText.setVisibility(View.VISIBLE);
        Task<List<Barcode>> result = scanner.process(inputImage)
                .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                    @Override
                    public void onSuccess(@NotNull List<Barcode> barcodes) {
                        for (Barcode barcode:barcodes) {
                            int valueType = barcode.getValueType();
                            switch (valueType) {
                                case Barcode.TYPE_TEXT:
                                    String info = barcode.getRawValue();
                                    showText.setText(info);
                                    break;
                                case Barcode.TYPE_URL:
                                    String title = barcode.getUrl().getTitle();
                                    String url = barcode.getUrl().getUrl();
                                    showText.setText("title" + title + "\n"+
                                                        "url" + url + "\n");
                                    break;
                                default:
                                    String data = barcode.getDisplayValue();
                                    showText.setText("Result:" + data);
                                    break;
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Log.d(TAG,"onFailure:" + e.getMessage());
                    }
                });
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkPermission(Manifest.permission.CAMERA,CAMERA_PERMISSION_CODE);
    }

    public void checkPermission(String permission , int request) {
        if (ContextCompat.checkSelfPermission(QrScanner.this,permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(QrScanner.this , new String[]{permission},request);
        }
    }

    @Override
    public void onRequestPermissionsResult(int request , @NonNull String[] permission , @NonNull int[] results) {
        super.onRequestPermissionsResult(request ,permission , results);

        if(request == CAMERA_PERMISSION_CODE) {
            if(!(results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(QrScanner.this, "Camera Permission Denied" , Toast.LENGTH_SHORT).show();
            } else {
                checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE,READ_STORAGE_PERMISSION_CODE);
            }
        } else if(request == READ_STORAGE_PERMISSION_CODE) {
            if(!(results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(QrScanner.this, "Storage Permission Denied" , Toast.LENGTH_SHORT).show();
            } else {
                checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,WRITE_STORAGE_PERMISSION_CODE);
            }
        } else if(request == WRITE_STORAGE_PERMISSION_CODE) {
            if(!(results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(QrScanner.this, "Storage Permission Denied" , Toast.LENGTH_SHORT).show();
            }
        }
    }
}