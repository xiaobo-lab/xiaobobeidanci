package com.xiaobo.xiaobobeidanci.model;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class UserData {
    private final SharedPreferences dataFile;
    private final Set<Word> staredWords;
    private Vocabulary nowVocabulary;
    private final Set<Vocabulary> localVocabularies;
    private int gap;
    private float delay;

    public UserData(Context context) {
        dataFile = context.getSharedPreferences("userData", Context.MODE_PRIVATE);
        nowVocabulary = Vocabulary.getLoader().load(dataFile.getString("nowVocabulary", ""));
        gap = dataFile.getInt("gap", 20);
        delay = dataFile.getFloat("delay", 1.f);
        localVocabularies = new HashSet<>();
        for (String t : dataFile.getStringSet("localVocabularies", new HashSet<>()))
            localVocabularies.add(Vocabulary.getLoader().load(t));
        staredWords = new HashSet<>();
        for (String t : dataFile.getStringSet("staredWords", new HashSet<>()))
            staredWords.add(Word.getLoader().load(t));
    }

    public Vocabulary getNowVocabulary() {
        return nowVocabulary;
    }

    public void setNowVocabulary(Vocabulary newVocabulary) {
        if (null != nowVocabulary)
            localVocabularies.add(nowVocabulary);
        for (Vocabulary t : localVocabularies) {
            if (t.equals(newVocabulary)) {
                nowVocabulary = t;
                return;
            }
        }
        localVocabularies.add(newVocabulary);
        nowVocabulary = newVocabulary;
    }

    public int getGap() {
        return gap;
    }

    public void setGap(int gap) {
        this.gap = gap;
    }

    public float getDelay() {
        return delay;
    }

    public void setDelay(float delay) {
        this.delay = delay;
    }

    public void saveGapAndDelay() {
        SharedPreferences.Editor editor = dataFile.edit();
        editor.putInt("gap", gap);
        editor.putFloat("delay", delay);
        editor.apply();
    }

    public Set<Word> getStaredWords() {
        return staredWords;
    }

    public void addStaredWords(Word word) {
        word.setStared(true);
        staredWords.add(word);
    }

    public void removeStaredWords(Word word) {
        word.setStared(false);
        staredWords.remove(word);
    }

    public void saveStaredWords() {
        SharedPreferences.Editor editor = dataFile.edit();
        Set<String> t = new HashSet<>();
        for (Word word : staredWords) t.add(word.toString());
        editor.putStringSet("staredWords", t);
        editor.apply();
    }

    public void saveVocabularies() {
        SharedPreferences.Editor editor = dataFile.edit();
        Set<String> t = new HashSet<>();
        for (Vocabulary v : localVocabularies) t.add(v.toString());
        editor.putStringSet("localVocabularies", t);
        if (null != nowVocabulary) {
            editor.putString("nowVocabulary", nowVocabulary.toString());
        }
        editor.apply();
    }

}
