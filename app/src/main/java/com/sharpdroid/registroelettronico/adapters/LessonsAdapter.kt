package com.sharpdroid.registroelettronico.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sharpdroid.registroelettronico.R
import com.sharpdroid.registroelettronico.adapters.holders.LessonHolder
import com.sharpdroid.registroelettronico.database.entities.Lesson
import java.text.SimpleDateFormat
import java.util.*

class LessonsAdapter(private val mContext: Context) : RecyclerView.Adapter<LessonHolder>() {
    private val formatter = SimpleDateFormat("d MMM", Locale.ITALIAN)
    private val lessons = ArrayList<Lesson>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            LessonHolder(LayoutInflater.from(mContext).inflate(R.layout.adapter_lessons, parent, false))

    override fun onBindViewHolder(holder: LessonHolder, position: Int) {
        val lesson = lessons[position]
        if (lesson.mArgument.isNullOrBlank()) {
            holder.content.visibility = View.GONE
        } else {
            holder.content.text = lesson.mArgument
        }
        holder.date.text = formatter.format(lesson.mDate)
    }

    fun addAll(list: Collection<Lesson>) {
        lessons.addAll(list)
        notifyDataSetChanged()
    }

    fun clear() {
        lessons.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount() = lessons.size
}