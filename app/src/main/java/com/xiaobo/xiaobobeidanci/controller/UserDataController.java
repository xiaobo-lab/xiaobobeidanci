package com.xiaobo.xiaobobeidanci.controller;

import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.xiaobo.xiaobobeidanci.model.UserData;
import com.xiaobo.xiaobobeidanci.model.Vocabulary;
import com.xiaobo.xiaobobeidanci.view.MainActivity;

import java.lang.ref.WeakReference;
import java.util.List;


public class UserDataController {
    UserData userData;
    private final MainActivity context;

    public UserDataController(MainActivity context) {
        this.context = context;
    }

    public void readUserData() {
        userData = new UserData(context);
        MainActivity.AdvanceViewHolder holder = context.getAdvanceViewHolder();
        holder.gapEditText.setText(String.valueOf(userData.getGap()));
        holder.delayEditText.setText(String.valueOf(userData.getDelay()));
        new Worker0(this).execute();
        if (null == userData.getNowVocabulary())
            Toast.makeText(context, "请点击高级按钮，选择一本词汇书", Toast.LENGTH_SHORT).show();
        else
            context.getGroupNumber().setText(String.valueOf(userData.getNowVocabulary().getGroupNumber()));
    }

    public void saveVocabularies() {
        if (null != userData)
            userData.saveVocabularies();
    }

    public void setGroupNumber(int groupNumber) {
        if (null != userData && userData.getNowVocabulary() != null) userData.getNowVocabulary().setGroupNumber(groupNumber);
    }

    public void updateUserDataToView() {
        if (null == userData.getNowVocabulary()) return;
        context.getGroupNumber().setText(String.valueOf(userData.getNowVocabulary().getGroupNumber()));
        context.setShowGroupCount(userData.getNowVocabulary().getGroupCount());
        context.setShowWordsCount(userData.getNowVocabulary().getWordsCount());
        context.setShowStaredCount(userData.getStaredWords().size());
    }

    private void updateUserDataToView0(List<Vocabulary> vocabularies) {
        if (userData.getNowVocabulary() == null) {
            vocabularies.add(0, new Vocabulary(-1, "请选择词汇书", "", 0, 0));
            context.setVocabularyList(vocabularies, new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) return;
                    updateUserDataToView1(vocabularies.get(position));
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });
        } else {
            vocabularies.remove(userData.getNowVocabulary());
            vocabularies.add(0, userData.getNowVocabulary());
            context.setVocabularyList(vocabularies, new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    updateUserDataToView1(vocabularies.get(position));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });
        }

    }

    private void updateUserDataToView1(Vocabulary newVocabulary) {
        String t = context.getGroupNumber().getText().toString();
        if (!(null == t || "".equals(t)) && userData.getNowVocabulary() != null)
            userData.getNowVocabulary().setGroupNumber(Integer.parseInt(t));
        userData.setNowVocabulary(newVocabulary);
        userData.saveVocabularies();
        updateUserDataToView();
        context.getLoad().callOnClick();
    }

    private static class Worker0 extends AsyncTask<Void, Void, List<Vocabulary>> {
        private final WeakReference<UserDataController> weakReference;

        public Worker0(UserDataController weakReference) {
            this.weakReference = new WeakReference<>(weakReference);
        }

        @Override
        protected List<Vocabulary> doInBackground(Void... voids) {
            return Vocabulary.getLoader().load();
        }

        @Override
        protected void onPostExecute(List<Vocabulary> vocabularies) {
            weakReference.get().updateUserDataToView0(vocabularies);
        }
    }


}
