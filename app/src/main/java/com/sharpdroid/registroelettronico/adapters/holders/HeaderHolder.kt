package com.sharpdroid.registroelettronico.adapters.holders

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.adapter_header.view.*

class HeaderHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var content: TextView = itemView.content
}
