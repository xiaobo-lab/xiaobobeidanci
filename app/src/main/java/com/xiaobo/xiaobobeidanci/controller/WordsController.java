package com.xiaobo.xiaobobeidanci.controller;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.widget.Toast;

import com.xiaobo.xiaobobeidanci.R;
import com.xiaobo.xiaobobeidanci.model.UserData;
import com.xiaobo.xiaobobeidanci.model.Word;
import com.xiaobo.xiaobobeidanci.view.MainActivity;
import com.xiaobo.xiaobobeidanci.view.WordContentAdapter;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class WordsController {
    private final UserData userData;
    private final MainActivity context;
    private final Worker1 worker = new Worker1();

    public WordsController(UserDataController userDataController, MainActivity context) {
        userData = userDataController.userData;
        this.context = context;
    }

    public void loadWords(int groupNumber) {
        new Worker0(this).execute(groupNumber);
    }

    public void removeStar(Word word) {
        userData.removeStaredWords(word);
        context.setShowStaredCount(userData.getStaredWords().size());
    }

    public void addStar(Word word) {
        userData.addStaredWords(word);
        context.setShowStaredCount(userData.getStaredWords().size());
    }

    public void saveStaredWords() {
        userData.saveStaredWords();
    }

    private void loadWords0(List<Word> words) {
        WordContentAdapter adapter = new WordContentAdapter(context, userData.getGap(), words, this);
        MainActivity.AdvanceViewHolder holder = context.getAdvanceViewHolder();
        holder.gapButton.setOnClickListener(v -> {
            context.clearAllEditTextFocus();
            String t = context.getAdvanceViewHolder().gapEditText.getText().toString();
            if (null == t || "".equals(t)) return;
            int gap = Integer.parseInt(t);
            if (gap <= 0) {
                Toast.makeText(context, "间距不允许设置为负数", Toast.LENGTH_SHORT).show();
                return;
            }
            adapter.setPadding(gap);
            userData.setGap(gap);
            userData.saveGapAndDelay();
        });

        holder.orderPlay.setOnClickListener(v -> {
            context.clearAllEditTextFocus();
            String t = context.getAdvanceViewHolder().delayEditText.getText().toString();
            if (null == t || "".equals(t)) return;
            float delay = Float.parseFloat(t);
            if (delay <= 0) {
                Toast.makeText(context, "延迟不允许设置为负数", Toast.LENGTH_SHORT).show();
                return;
            }
            userData.setDelay(delay);
            userData.saveGapAndDelay();
            if (worker.isFinished() || worker.isCanceled()) {
                worker.setWords(adapter.getWords());
                worker.setDelay(userData.getDelay());
                worker.onStart();
            }
        });
        holder.pause.setOnClickListener(v -> {
            context.clearAllEditTextFocus();
            userData.setDelay(Float.parseFloat(context.getAdvanceViewHolder().delayEditText.getText().toString()));
            userData.saveGapAndDelay();
            if (worker.isPlaying()) {
                worker.onStop();
                holder.pause.setText(R.string.resume);
            } else if (worker.isStopping()) {
                context.clearAllEditTextFocus();
                String text = holder.delayEditText.getText().toString();
                if (null == text || "".equals(text)) {
                    text = "1.0";
                    holder.delayEditText.setText(text);
                }
                float delay = Float.parseFloat(text);
                worker.onResume();
                worker.setDelay(delay);
                userData.setDelay(delay);
                userData.saveGapAndDelay();
                holder.pause.setText(R.string.stop);
            }
        });
        holder.cancel.setOnClickListener(v -> {
            context.clearAllEditTextFocus();
            userData.setDelay(Float.parseFloat(context.getAdvanceViewHolder().delayEditText.getText().toString()));
            userData.saveGapAndDelay();
            if (!worker.isCanceled() && !worker.isFinished()) {
                worker.cancel();
                holder.pause.setText(R.string.stop);
            }
        });
        context.getShuffle().setOnClickListener(v -> {
            context.clearAllEditTextFocus();
            adapter.shuffle();
        });
        context.getWordsContent().setAdapter(adapter);
    }

    private static class Worker0 extends AsyncTask<Integer, Void, List<Word>> {
        private final WeakReference<WordsController> weakReference;

        public Worker0(WordsController weakReference) {
            this.weakReference = new WeakReference<>(weakReference);
        }

        @Override
        protected void onPostExecute(List<Word> words) {
            weakReference.get().loadWords0(words);
        }

        @Override
        protected List<Word> doInBackground(Integer... integers) {
            int groupNumber = integers[0];
            UserData userData = weakReference.get().userData;
            if (groupNumber == 0) return new ArrayList<>(userData.getStaredWords());
            List<Word> words = Word.getLoader().load(weakReference.get().userData.getNowVocabulary(), groupNumber);
            for (Word t : weakReference.get().userData.getStaredWords())
                for (Word word : words)
                    if (t.equals(word))
                        word.setStared(true);
            return words;
        }
    }

    private static class Worker1 extends Thread {
        private final AtomicInteger status;
        private List<Word> words;
        private final AtomicReference<Float> delay;
        private boolean started = false;

        public Worker1() {
            status = new AtomicInteger(3);
            delay = new AtomicReference<>();
        }

        public void setWords(List<Word> words) {
            if (isFinished() || isCanceled())
                this.words = words;
        }

        public void setDelay(float delay) {
            if (isFinished() || isCanceled())
                this.delay.set(delay);
        }

        public boolean isFinished() {
            return status.get() == 3;
        }

        public boolean isCanceled() {
            return status.get() == 2;
        }

        public boolean isStopping() {
            return status.get() == 1;
        }

        public boolean isPlaying() {
            return status.get() == 0;
        }

        public void onStop() {
            status.set(1);
        }

        public void onResume() {
            status.set(0);
        }

        public void cancel() {
            status.set(2);
        }

        public void onStart() {
            onResume();
            if (!started) {
                start();
                started = true;
            }
        }

        @Override
        public void run() {
            while (true) {
                if (isFinished() || isCanceled()) {
                    try {
                        Thread.sleep(0);
                    } catch (InterruptedException e) {
                    }
                    continue;
                }
                for (Word t : words) {
                    while (status.get() == 1) ;
                    if (status.get() == 2) break;
                    try {
                        MediaPlayer sound = t.getSound();
                        sound.start();
                        while (sound.isPlaying()) ;
                    } catch (IOException e) { }
                    while (status.get() == 1) ;
                    if (status.get() == 2) break;
                    try {
                        Thread.sleep((long) (delay.get() * 1000));
                    } catch (InterruptedException e) {
                    }
                }
                if (status.get() != 2) status.set(3);
            }
        }
    }
}