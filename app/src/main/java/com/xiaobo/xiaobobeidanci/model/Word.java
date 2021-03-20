package com.xiaobo.xiaobobeidanci.model;

import android.media.MediaPlayer;

import com.xiaobo.xiaobobeidanci.model.tool.HttpTool;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Word {
    // 英文单词
    private final String word;
    // 音标
    private final String symbol;
    // 中文意思
    private final String paraphrase;
    // 音频文件路径
    private final String sound;
    // 音频文件
    private final MediaPlayer mediaSound;
    // 是否第一次加载音频
    private boolean first;
    // 例句
    private final String exampleSentence;
    // 收藏
    private boolean stared;

    private static final WordsLoader loader = new WordsLoader();

    public static WordsLoader getLoader() {
        return loader;
    }

    private Word(String word, String symbol, String paraphrase, String sound, String exampleSentence) {
        this.word = word;
        this.symbol = symbol;
        this.paraphrase = paraphrase;
        this.sound = sound;
        this.exampleSentence = exampleSentence;
        mediaSound = new MediaPlayer();
        first = true;
        stared = false;
    }

    public boolean isStared() {
        return stared;
    }

    public void setStared(boolean stared) {
        this.stared = stared;
    }

    public String getWord() {
        return word;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getParaphrase() {
        return paraphrase;
    }

    public MediaPlayer getSound() throws IOException {
        if (first) {
            mediaSound.setDataSource(sound);
            mediaSound.prepare();
            first = false;
        }
        return mediaSound;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Word word1 = (Word) o;
        return word.equals(word1.word);
    }

    @Override
    public int hashCode() {
        return Objects.hash(word);
    }

    @Override
    public String toString() {
        return String.format(Locale.CHINA,
                "{\"word\":\"%s\",\"symbol\":\"%s\",\"paraphrase\":\"%s\",\"sound\":\"%s\",\"exampleSentence\":\"%s\",\"stared\":%b}",
                word, symbol, paraphrase, sound, exampleSentence, stared);
    }

    public String getExampleSentence() {
        return exampleSentence;
    }

    /**
     * 用来加载指定词汇书的指定页数据
     */
    public static class WordsLoader {
        private WordsLoader() {
        }

        public Word load(String json) {
            if (null == json || "".equals(json)) return null;
            Word word = null;
            try {
                JSONObject t = new JSONObject(json);
                word = new Word(t.getString("word"), t.getString("symbol"),
                        t.getString("paraphrase").replace("\\n", "\n"), t.getString("sound"),
                        t.getString("exampleSentence"));
                word.setStared(t.getBoolean("stared"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return word;
        }

        public List<Word> load(Vocabulary vocabulary, int groupNumber) {
            List<Word> words = new ArrayList<>();
            if (vocabulary == null || groupNumber > vocabulary.getGroupCount() || groupNumber < 1)
                return words;
            String result = null;
            try {
                result = HttpTool.GET(String.format(Locale.CHINA, "%s%d.json", vocabulary.getUrl(), groupNumber - 1));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (null == result) return words;
            try {
                JSONArray wordList = new JSONArray(result);
                for (int i = 0, l = wordList.length(); i < l; i++) {
                    JSONObject t = wordList.getJSONObject(i);
                    words.add(new Word(t.getString("word"), t.getString("symbol"),
                            t.getString("paraphrase").replace("\\n", "\n"), t.getString("sound"),
                            t.getString("exampleSentence")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return words;
        }
    }
}
