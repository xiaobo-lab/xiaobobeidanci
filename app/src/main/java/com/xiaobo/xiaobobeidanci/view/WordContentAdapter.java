package com.xiaobo.xiaobobeidanci.view;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiaobo.xiaobobeidanci.R;
import com.xiaobo.xiaobobeidanci.controller.WordsController;
import com.xiaobo.xiaobobeidanci.lib.DrawerLayout;
import com.xiaobo.xiaobobeidanci.model.Word;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class WordContentAdapter extends BaseAdapter {
    private final WordsController controller;
    private final Context context;
    private final List<Word> words;
    private int padding;


    public WordContentAdapter(Context context, int padding, List<Word> words, WordsController controller) {
        this.context = context;
        this.words = words;
        this.padding = padding;
        this.controller = controller;
    }

    public void shuffle() {
        Collections.shuffle(words);
        notifyDataSetChanged();
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

    public List<Word> getWords() {
        return words;
    }

    @Override
    public int getCount() {
        return words.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.content, parent, false);
        TextView textViewWord = view.findViewById(R.id.text_view_word);
        TextView textViewSymbol = view.findViewById(R.id.text_view_symbol);
        TextView textViewParaphrase = view.findViewById(R.id.text_view_paraphrase);
        ImageView imageViewShowMean = view.findViewById(R.id.image_view_show_mean);
        ImageView imageViewShowSentence = view.findViewById(R.id.image_view_show_sentence);
        ImageView imageViewStart = view.findViewById(R.id.image_view_start);
        DrawerLayout drawer = view.findViewById(R.id.drawer);
        RelativeLayout surface = view.findViewById(R.id.surface);
        surface.setPadding(0, padding, 0, padding);
        Word word = words.get(position);
        imageViewStart.setImageResource(word.isStared() ? R.drawable.start : R.drawable.unstart);
        textViewWord.setText(word.getWord());
        textViewSymbol.setText(word.getSymbol());
        textViewParaphrase.setText(word.getParaphrase());
        imageViewShowMean.setOnClickListener(new View.OnClickListener() {
            private final DrawerLayout.OnScrollStartListener moveUp = () -> imageViewShowMean.setImageResource(R.drawable.array_down);
            private final DrawerLayout.OnScrollStartListener moveDown = () -> imageViewShowMean.setImageResource(R.drawable.array_up);

            @Override
            public void onClick(View v) {
                int height = surface.getHeight();
                if (drawer.getMoveCount() % 2 == 1) {
                    drawer.setOnScrollStart(moveUp);
                    drawer.startScroll(0, -height, 500, true);
                } else {
                    drawer.setOnScrollStart(moveDown);
                    drawer.startScroll(0, height, 500, false);
                }
            }
        });
        textViewWord.setOnClickListener(v -> {
            try {
                word.getSound().start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        imageViewShowSentence.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(word.getExampleSentence()).create().show();
        });
        imageViewStart.setOnClickListener(v -> {
            if (word.isStared()) {
                imageViewStart.setImageResource(R.drawable.unstart);
                controller.removeStar(word);
            } else {
                imageViewStart.setImageResource(R.drawable.start);
                controller.addStar(word);
            }
            controller.saveStaredWords();
        });
        return view;
    }
}
