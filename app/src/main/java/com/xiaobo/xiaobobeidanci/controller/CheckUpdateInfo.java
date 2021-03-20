package com.xiaobo.xiaobobeidanci.controller;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.xiaobo.xiaobobeidanci.model.tool.HttpTool;
import com.xiaobo.xiaobobeidanci.view.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.Locale;

public class CheckUpdateInfo extends AsyncTask<Void, Integer, Integer> {
    private static final String[] MESSAGE = new String[]{"无法获取当前版本信息", "获取更新文件失败", "获取更新失败"};
    private static final String URL = "http://localhost:8000/download/updateInfo.json";
    private final WeakReference<MainActivity> contextWeakReference;

    private String info;
    private File downloadFile;
    private boolean first = true;

    public CheckUpdateInfo(MainActivity context) {
        this.contextWeakReference = new WeakReference<>(context);
    }

    private void installApp(Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri apkUri = FileProvider.getUriForFile(context, "com.xiaobo.xiaobobeidanci.fileProvider", downloadFile);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            } else {
                intent.setDataAndType(Uri.fromFile(downloadFile), "application/vnd.android.package-archive");
            }
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(Integer result) {
        MainActivity context = contextWeakReference.get();
        if (null == context) return;
        if (result == -1) {
            installApp(context);
        } else if (result >= 0) {
            Toast.makeText(context, MESSAGE[result], Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        MainActivity context = contextWeakReference.get();
        if (null == context || values.length != 1) return;
        if (first) {
            context.showUpdateView();
            first = false;
        }
        MainActivity.UpdateViewHolder viewHolder = context.getUpdateViewHolder();
        viewHolder.updateInfo.setText(info);
        viewHolder.progressTextView.setText(String.format(Locale.CHINA, "进度:%d%%", values[0]));
        viewHolder.progressBar.setProgress(values[0]);
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        final Context context = contextWeakReference.get();
        if (null == context) return 0;
        final long nowVersionCode;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            nowVersionCode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? packageInfo.getLongVersionCode() : packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
        try {
            JSONObject json = new JSONObject(HttpTool.GET(URL));
            info = json.getString("info");
            String url = json.getString("url");
            long newVersionCode = json.getLong("versionCode");
            if (nowVersionCode >= newVersionCode) return -2;
            HttpURLConnection connection = HttpTool.CONNECT(url, "GET");
            String t = connection.getHeaderField("content-length");
            if (null == t || "".equals(t)) return 1;
            int contentLength = Integer.parseInt(t);
            // 这里的代码需要于file_paths.xml中的配置保持一致
            downloadFile = new File(context.getFilesDir() + "/app.apk");
            try (InputStream input = connection.getInputStream();
                 FileOutputStream output = new FileOutputStream(downloadFile)) {
                int len;
                int readLength = 0;
                byte[] content = new byte[1024];
                while ((len = input.read(content)) != -1) {
                    readLength += len;
                    output.write(content, 0, len);
                    publishProgress(readLength * 100 / contentLength);
                }
            }
            return -1;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return 2;
    }
}
