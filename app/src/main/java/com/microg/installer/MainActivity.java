package com.microg.installer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SELECT_FILE = 1;
    private static final int REQUEST_CODE_INSTALL_PERMISSION = 2;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 3;
    private static final String TAG = "MicroGInstaller";

    private TextView statusText;
    private TextView filePathText;
    private Button selectButton;
    private Button installButton;

    private String selectedFilePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        filePathText = findViewById(R.id.filePathText);
        selectButton = findViewById(R.id.selectButton);
        installButton = findViewById(R.id.installButton);

        selectButton.setOnClickListener(v -> {
            if (checkStoragePermission()) {
                openFileSelector();
            } else {
                requestStoragePermission();
            }
        });

        installButton.setOnClickListener(v -> {
            if (selectedFilePath != null) {
                if (checkInstallPermission()) {
                    installMicroG();
                } else {
                    requestInstallPermission();
                }
            } else {
                Toast.makeText(this, R.string.no_file, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == 
                   PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_STORAGE_PERMISSION);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_STORAGE_PERMISSION);
        }
    }

    private boolean checkInstallPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getPackageManager().canRequestPackageInstalls();
        }
        return true;
    }

    private void requestInstallPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_INSTALL_PERMISSION);
        }
    }

    private void openFileSelector() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_file)), REQUEST_CODE_SELECT_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SELECT_FILE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                selectedFilePath = getPathFromUri(uri);
                if (selectedFilePath != null) {
                    filePathText.setText(selectedFilePath);
                    installButton.setEnabled(true);
                    statusText.setText(getString(R.string.select_file));
                }
            }
        } else if (requestCode == REQUEST_CODE_INSTALL_PERMISSION) {
            if (checkInstallPermission() && selectedFilePath != null) {
                installMicroG();
            } else {
                Toast.makeText(this, getString(R.string.error, "Permission refusée"), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (checkStoragePermission()) {
                openFileSelector();
            } else {
                Toast.makeText(this, getString(R.string.error, "Permission stockage refusée"), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getPathFromUri(Uri uri) {
        if (uri.getScheme().equals("file")) {
            return uri.getPath();
        }
        return uri.toString();
    }

    private void installMicroG() {
        if (selectedFilePath == null) {
            Toast.makeText(this, R.string.no_file, Toast.LENGTH_SHORT).show();
            return;
        }

        statusText.setText(getString(R.string.installing));
        selectButton.setEnabled(false);
        installButton.setEnabled(false);

        new InstallTask().execute(selectedFilePath);
    }

    private class InstallTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... paths) {
            String zipPath = paths[0];
            
            try {
                File tempDir = new File(getExternalFilesDir(null), "microg_temp");
                if (!tempDir.exists()) {
                    tempDir.mkdirs();
                }

                unzipFile(zipPath, tempDir.getAbsolutePath());

                File[] apkFiles = tempDir.listFiles((dir, name) -> name.endsWith(".apk"));
                if (apkFiles != null && apkFiles.length > 0) {
                    for (File apkFile : apkFiles) {
                        installApk(apkFile);
                    }
                    return true;
                } else {
                    Log.e(TAG, "No APK files found in the ZIP");
                    return false;
                }
            } catch (IOException e) {
                Log.e(TAG, "Error during installation", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            statusText.setText(success ? getString(R.string.success) : getString(R.string.error, "Erreur d'installation"));
            selectButton.setEnabled(true);
            installButton.setEnabled(selectedFilePath != null);
            
            if (success) {
                Toast.makeText(MainActivity.this, R.string.success, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, getString(R.string.error, "Erreur"), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void unzipFile(String zipFilePath, String outputFolder) throws IOException {
        File zipFile = new File(zipFilePath);
        File outputDir = new File(outputFolder);
        
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            
            while (zipEntry != null) {
                String entryName = zipEntry.getName();
                File outputFile = new File(outputDir, entryName);
                
                if (zipEntry.isDirectory()) {
                    outputFile.mkdirs();
                } else {
                    File parent = outputFile.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }
                    
                    try (BufferedInputStream bis = new BufferedInputStream(zipInputStream);
                         BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = bis.read(buffer)) > 0) {
                            bos.write(buffer, 0, len);
                        }
                    }
                }
                
                zipInputStream.closeEntry();
                zipEntry = zipInputStream.getNextEntry();
            }
        }
    }

    private void installApk(File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFileSelector();
            } else {
                Toast.makeText(this, getString(R.string.error, "Permission refusée"), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
