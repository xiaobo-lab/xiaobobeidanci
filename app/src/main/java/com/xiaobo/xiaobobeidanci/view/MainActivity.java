package com.xiaobo.xiaobobeidanci.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaobo.xiaobobeidanci.R;
import com.xiaobo.xiaobobeidanci.controller.CheckUpdateInfo;
import com.xiaobo.xiaobobeidanci.controller.UserDataController;
import com.xiaobo.xiaobobeidanci.controller.WordsController;
import com.xiaobo.xiaobobeidanci.controller.SendUserInfo;
import com.xiaobo.xiaobobeidanci.model.Vocabulary;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {
    private static final String[] PERMISSIONS = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    private static boolean first = true;
    private UserDataController userData;
    private WordsController words;

    private AdvanceViewHolder advanceViewHolder;
    private AlertDialog updateDialog;
    private UpdateViewHolder updateViewHolder;

    private EditText groupNumber;
    private ListView wordsContent;
    private Button shuffle;
    private Button load;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
        wordsContent = findViewById(R.id.words_content);
        shuffle = findViewById(R.id.shuffle);
        groupNumber = findViewById(R.id.group_number);
        View advanceView = LayoutInflater.from(this).inflate(R.layout.advance_view, null);
        View updateView = LayoutInflater.from(this).inflate(R.layout.update_view, null);
        advanceViewHolder = new AdvanceViewHolder(advanceView);
        updateViewHolder = new UpdateViewHolder(updateView);
        updateDialog = new AlertDialog.Builder(this).setView(updateView).create();
        load = findViewById(R.id.load);
        load.setOnClickListener(v -> {
            clearAllEditTextFocus();
            String input = groupNumber.getText().toString();
            if (null == input || "".equals(input)) return;
            int t = Integer.parseInt(input);
            words.loadWords(t);
            userData.setGroupNumber(t);
            userData.saveVocabularies();
        });
        findViewById(R.id.advance).setOnClickListener(v -> {
            clearAllEditTextFocus();
            if (advanceView.getParent() != null)
                ((ViewGroup) advanceView.getParent()).removeView(advanceView);
            new AlertDialog.Builder(this).setView(advanceView).create().show();
        });

        userData = new UserDataController(this);
        userData.readUserData();
        words = new WordsController(userData, this);

        if (first) {
            new CheckUpdateInfo(this).execute();
            new SendUserInfo(getUUID()).execute();
            MainActivity.setFirst(false);
        }
    }

    public static void setFirst(boolean first) {
        MainActivity.first = first;
    }

    public ListView getWordsContent() {
        return wordsContent;
    }

    public Button getShuffle() {
        return shuffle;
    }

    public Button getLoad() {
        return load;
    }

    public EditText getGroupNumber() {
        return groupNumber;
    }

    public AdvanceViewHolder getAdvanceViewHolder() {
        return advanceViewHolder;
    }

    public void showUpdateView() {
        updateDialog.show();
    }

    public UpdateViewHolder getUpdateViewHolder() {
        return updateViewHolder;
    }

    public void setShowWordsCount(int count) {
        advanceViewHolder.showWordsCount.setText(String.format(Locale.CHINA, getString(R.string.show_words_count), count));
    }

    public void setShowGroupCount(int count) {
        advanceViewHolder.showGroupCount.setText(String.format(Locale.CHINA, getString(R.string.show_group_count), count));
    }

    public void setShowStaredCount(int count) {
        advanceViewHolder.showStaredCount.setText(String.format(Locale.CHINA, getString(R.string.show_stared_count), count));
    }

    public void setVocabularyList(List<Vocabulary> vocabularyList, AdapterView.OnItemSelectedListener listener) {
        List<String> items = new ArrayList<>();
        for (Vocabulary t : vocabularyList) items.add(t.getName());
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items.toArray(new String[0]));
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        advanceViewHolder.selectVocabularySpinner.setAdapter(arrayAdapter);
        advanceViewHolder.selectVocabularySpinner.setOnItemSelectedListener(listener);
    }

    public void clearAllEditTextFocus() {
        groupNumber.clearFocus();
        advanceViewHolder.gapEditText.clearFocus();
        advanceViewHolder.delayEditText.clearFocus();
    }

    public static class UpdateViewHolder {
        public final ProgressBar progressBar;
        public final TextView progressTextView;
        public final TextView updateInfo;

        UpdateViewHolder(View view) {
            progressBar = view.findViewById(R.id.progress_bar);
            progressTextView = view.findViewById(R.id.progress_text_view);
            updateInfo = view.findViewById(R.id.update_info);
        }
    }

    public static class AdvanceViewHolder {
        public final Spinner selectVocabularySpinner;
        public final EditText delayEditText;
        public final Button orderPlay;
        public final Button pause;
        public final Button cancel;
        public final EditText gapEditText;
        public final Button gapButton;
        public final TextView showWordsCount;
        public final TextView showGroupCount;
        public final TextView showStaredCount;

        AdvanceViewHolder(View view) {
            selectVocabularySpinner = view.findViewById(R.id.select_vocabulary_spinner);
            delayEditText = view.findViewById(R.id.delay_edit_text);
            orderPlay = view.findViewById(R.id.order_play);
            pause = view.findViewById(R.id.pause);
            cancel = view.findViewById(R.id.cancel);
            gapEditText = view.findViewById(R.id.gap_edit_text);
            gapButton = view.findViewById(R.id.gap_button);
            showWordsCount = view.findViewById(R.id.show_words_count);
            showGroupCount = view.findViewById(R.id.show_group_count);
            showStaredCount = view.findViewById(R.id.show_stared_count);
        }
    }

    private void requestPermission() {
        for (int i = 0; i < PERMISSIONS.length; i++)
            if (ContextCompat.checkSelfPermission(this, PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{PERMISSIONS[i]}, i);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "请授予权限", Toast.LENGTH_SHORT).show();
            new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                    }
                    System.exit(0);
                }
            }.start();
        }
    }

    private String getUUID() {
        File dir = new File(Environment.getExternalStorageDirectory(), ".uuidx");
        if (!dir.mkdirs() && !dir.exists()) {
            dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        }
        File file = new File(dir, "21131cbd-4f31-4098-8541-310329c9b69d");
        try {
            if (!file.createNewFile() && !file.exists()) return getSharedPreferencesUUID();
        } catch (IOException e) {
            e.printStackTrace();
            return getSharedPreferencesUUID();
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String uuid = reader.readLine();
            if (null == uuid || "".equals(uuid)) {
                uuid = UUID.randomUUID().toString();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    writer.write(uuid);
                    writer.flush();
                }
            }
            return uuid;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return getSharedPreferencesUUID();
    }

    private String getSharedPreferencesUUID() {
        SharedPreferences preferences = getSharedPreferences("uuid", MODE_PRIVATE);
        String uuid = preferences.getString("uuid", "");
        if ("".equals(uuid)) {
            SharedPreferences.Editor editor = preferences.edit();
            uuid = UUID.randomUUID().toString();
            editor.putString("uuid", uuid);
            editor.apply();
        }
        return uuid;
    }
}