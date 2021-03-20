package com.xiaobo.xiaobobeidanci.controller;

import android.os.AsyncTask;

import com.xiaobo.xiaobobeidanci.model.tool.HttpTool;

import java.io.IOException;
import java.util.Locale;

public class SendUserInfo extends AsyncTask<Void, Void, Void> {
    private static final String SEND_URL = "http://localhost:8000/upload/";
    private final String uuid;

    public SendUserInfo(String uuid) {
        this.uuid = uuid;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            HttpTool.POST(SEND_URL, String.format(Locale.CHINA, "{\"uuid\":\"%s\"}", uuid));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
