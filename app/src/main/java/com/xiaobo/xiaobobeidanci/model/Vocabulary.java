package com.xiaobo.xiaobobeidanci.model;

import com.xiaobo.xiaobobeidanci.model.tool.HttpTool;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 词汇书
 */
public class Vocabulary {
    // id
    private final int id;
    // 名称
    private final String name;
    // 词汇书链接
    private final String url;
    // 单词数量
    private final int wordsCount;
    // 单词组数
    private final int groupCount;
    // 用户正在浏览的组号
    private int groupNumber;

    private static final VocabularyLoader loader = new VocabularyLoader();

    public static VocabularyLoader getLoader() {
        return loader;
    }

    public Vocabulary(int id, String name, String url, int wordsCount, int groupCount) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.wordsCount = wordsCount;
        this.groupCount = groupCount;
    }

    public int getGroupNumber() {
        return groupNumber;
    }

    public void setGroupNumber(int groupNumber) {
        this.groupNumber = groupNumber;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public int getWordsCount() {
        return wordsCount;
    }

    public int getGroupCount() {
        return groupCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vocabulary that = (Vocabulary) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format(Locale.CHINA,
                "{\"id\":%d,\"name\":\"%s\",\"url\":\"%s\",\"wordsCount\":%d,\"groupCount\":%d, \"groupNumber\":%d}",
                id, name, url, wordsCount, groupCount, groupNumber);
    }

    /**
     * 用来加载词汇书数据
     */
    public static class VocabularyLoader {
        private static final String URL = "http://localhost:8000/download/info.json";

        private VocabularyLoader() {}

        public Vocabulary load(String data) {
            if (null == data || "".equals(data)) return null;
            Vocabulary vocabulary = null;
            try {
                JSONObject t = new JSONObject(data);
                vocabulary =  new Vocabulary(t.getInt("id"),
                        t.getString("name"), t.getString("url"),
                        t.getInt("wordsCount"), t.getInt("groupCount"));
                vocabulary.setGroupNumber(t.getInt("groupNumber"));
                return vocabulary;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return vocabulary;
        }

        public List<Vocabulary> load() {
            List<Vocabulary> vocabularies = new ArrayList<>();
            String result = null;
            try {
                result = HttpTool.GET(URL);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (null == result) return vocabularies;
            try {
                JSONArray list = new JSONArray(result);
                for (int i = 0, length = list.length(); i < length; i++) {
                    JSONObject t = list.getJSONObject(i);
                    vocabularies.add(new Vocabulary(t.getInt("id"),
                            t.getString("name"), t.getString("url"),
                            t.getInt("wordsCount"), t.getInt("groupCount")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return vocabularies;
        }
    }
}
