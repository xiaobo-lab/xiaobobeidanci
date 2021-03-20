package com.xiaobo.xiaobobeidanci.model.tool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class HttpTool {
    private HttpTool() {
    }

    /**
     * 发送一个GET请求
     *
     * @param url 请求的url
     * @return 成功返回 response 的内容，失败返回null
     */
    public static String GET(String url) throws IOException {
        HttpURLConnection connection = CONNECT(url, "GET");
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            for (String t; (t = reader.readLine()) != null; ) builder.append(t);
        }
        return builder.toString();
    }

    public static String POST(String url, String content) throws IOException {
        HttpURLConnection connection = CONNECT(url, "POST");
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()))) {
            writer.write(content);
        }
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            for (String t; (t = reader.readLine()) != null; ) builder.append(t);
        }
        return builder.toString();
    }

    public static HttpURLConnection CONNECT(String url, String method) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(method);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(true);
        connection.connect();
        return connection;
    }
}
