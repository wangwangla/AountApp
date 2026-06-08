package com.tony.accountapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.IconCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private static final String EXTRA_SHORTCUT_SOURCE = "shortcut_source";
    private static final String SOURCE_DYNAMIC = "dynamic";
    private static final String SOURCE_PINNED = "pinned";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        findViewById(R.id.home_setting).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
        setupShortcutDemoButtons();
//        publishDynamicShortcut();
        if (savedInstanceState == null) {
            handleShortcutIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleShortcutIntent(intent);
    }

    private void setupShortcutDemoButtons() {
        Button dynamicButton = findViewById(R.id.btn_dynamic_shortcut);
        Button pinnedButton = findViewById(R.id.btn_pinned_shortcut);

        dynamicButton.setOnClickListener(v -> publishDynamicShortcut());
        pinnedButton.setOnClickListener(v -> requestPinnedShortcut());
    }

    private void publishDynamicShortcut() {
        ShortcutInfoCompat shortcut = new ShortcutInfoCompat.Builder(this, "dynamic_add")
                .setShortLabel(getString(R.string.shortcut_dynamic_short))
                .setLongLabel(getString(R.string.shortcut_dynamic_long))
                .setIcon(IconCompat.createWithResource(this, R.drawable.ic_settings))
                .setIntent(createShortcutIntent(SOURCE_DYNAMIC))
                .build();

        ShortcutManagerCompat.setDynamicShortcuts(this, Collections.singletonList(shortcut));
    }

    private void requestPinnedShortcut() {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(this)) {
            Toast.makeText(this, R.string.shortcut_pinned_not_supported, Toast.LENGTH_SHORT).show();
            return;
        }

        ShortcutInfoCompat shortcut = new ShortcutInfoCompat.Builder(this, "pinned_add")
                .setShortLabel(getString(R.string.shortcut_pinned_short))
                .setLongLabel(getString(R.string.shortcut_pinned_long))
                .setIcon(IconCompat.createWithResource(this, R.drawable.ic_settings))
                .setIntent(createShortcutIntent(SOURCE_PINNED))
                .build();

        ShortcutManagerCompat.requestPinShortcut(this, shortcut, null);
    }

    private Intent createShortcutIntent(String source) {
        return new Intent(this, MainActivity.class)
                .setAction(Intent.ACTION_VIEW)
                .putExtra(EXTRA_SHORTCUT_SOURCE, source);
    }

    private void handleShortcutIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        Uri data = intent.getData();
        String source = intent.getStringExtra(EXTRA_SHORTCUT_SOURCE);

        if (data != null
                && "accountapp".equals(data.getScheme())
                && "shortcut".equals(data.getHost())
                && "/static".equals(data.getPath())) {
            Toast.makeText(this, R.string.shortcut_opened_static, Toast.LENGTH_SHORT).show();
            return;
        }

        if (SOURCE_DYNAMIC.equals(source)) {
            Toast.makeText(this, R.string.shortcut_opened_dynamic, Toast.LENGTH_SHORT).show();
        } else if (SOURCE_PINNED.equals(source)) {
            Toast.makeText(this, R.string.shortcut_opened_pinned, Toast.LENGTH_SHORT).show();
        }
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        hideSystemUI();
    }
}