package com.tony.accountapp;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class WidgetConfigActivity extends AppCompatActivity {

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private EditText textInput;
    private Button selectImageButton;

    private String selectedImagePath = null;
    private int selectedColor = 0xFFFFFFFF; // 默认白色
    private static final int REQUEST_IMAGE_PICK = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_widget_config);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.widget_config_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        textInput = findViewById(R.id.widget_text_input);
        selectImageButton = findViewById(R.id.btn_select_image);
        Button saveButton = findViewById(R.id.btn_config_save);
        Button cancelButton = findViewById(R.id.btn_config_cancel);

        selectImageButton.setOnClickListener(v -> openImagePicker());
        saveButton.setOnClickListener(v -> saveConfiguration());
        cancelButton.setOnClickListener(v -> finish());

        // 加载已有配置（如果有）
        loadConfiguration();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                selectedImagePath = getImagePath(selectedImageUri);
                selectImageButton.setText("已选择图片");
            }
        }
    }

    private String getImagePath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            String path = cursor.getString(columnIndex);
            cursor.close();
            return path;
        }
        return null;
    }

    private void saveConfiguration() {
        String text = textInput.getText().toString().isEmpty() ? "Widget Text" : textInput.getText().toString();

        SharedPreferences prefs = getSharedPreferences("widget_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String widgetKey = "widget_" + appWidgetId;
        editor.putString(widgetKey + "_text", text);
        editor.putInt(widgetKey + "_color", selectedColor);
        if (selectedImagePath != null) {
            editor.putString(widgetKey + "_image", selectedImagePath);
        }
        editor.apply();

        // 通知 Widget 更新
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        manager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_text);

        setResult(RESULT_OK);
        finish();
    }

    private void loadConfiguration() {
        SharedPreferences prefs = getSharedPreferences("widget_prefs", MODE_PRIVATE);
        String widgetKey = "widget_" + appWidgetId;

        String savedText = prefs.getString(widgetKey + "_text", "Widget Text");
        selectedColor = prefs.getInt(widgetKey + "_color", 0xFFFFFFFF);
        selectedImagePath = prefs.getString(widgetKey + "_image", null);

        textInput.setText(savedText);
        if (selectedImagePath != null) {
            selectImageButton.setText("已选择图片");
        }
    }
}

