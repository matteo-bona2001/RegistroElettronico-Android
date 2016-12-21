package com.sharpdroid.registro.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.sharpdroid.registro.Adapters.Holders.LessonHolder;
import com.sharpdroid.registro.Interfaces.Lesson;
import com.sharpdroid.registro.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.sharpdroid.registro.Utils.Metodi.beautifyName;

public class LessonsAdapter extends RecyclerView.Adapter<LessonHolder> {
    private final SimpleDateFormat formatter = new SimpleDateFormat("d MMM", Locale.getDefault());

    private Context mContext;

    private List<Lesson> lessons;

    public LessonsAdapter(Context mContext) {
        this.mContext = mContext;
        lessons = new ArrayList<>();
    }

    @Override
    public LessonHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new LessonHolder(LayoutInflater.from(mContext).inflate(R.layout.adapter_lessons, parent, false));
    }

    @Override
    public void onBindViewHolder(LessonHolder holder, int position) {
        Lesson lesson = lessons.get(position);
        holder.content.setText(beautifyName(lesson.getContent().trim()));
        holder.date.setText(formatter.format(lesson.getDate()));
    }

    public void addAll(List<Lesson> list) {
        lessons = list;
        Collections.reverse(lessons);
        notifyDataSetChanged();
    }

    public void clear() {
        lessons.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return (lessons.size() > 5) ? 5 : lessons.size();
    }

}