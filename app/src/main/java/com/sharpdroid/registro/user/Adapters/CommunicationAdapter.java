package com.sharpdroid.registro.user.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sharpdroid.registro.R;
import com.sharpdroid.registro.user.Entry.Communication;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Marco on 14/11/2016.
 */

class CommuicationAdapter extends RecyclerView.Adapter<CommuicationAdapter.CommunicationHolder> {
    final List<Communication> CVDataList;

    CommuicationAdapter(List<Communication> CVDataList) {
        this.CVDataList = CVDataList;
    }

    void addAll(Collection<Communication> list) {
        CVDataList.addAll(list);
        notifyDataSetChanged();
    }

    void clear() {
        CVDataList.clear();
        notifyDataSetChanged();
    }

    @Override
    public CommunicationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_communications, parent, false);
        return new CommunicationHolder(v);
    }

    @Override
    public void onBindViewHolder(CommunicationHolder ViewHolder, int i) {
        final Communication communication = CVDataList.get(ViewHolder.getAdapterPosition());
        ViewHolder.Title.setText(communication.getTitle());
        String datestring = communication.getDate().split("T")[0];
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALIAN);
            Date convertedCommitDate = formatter.parse(datestring);
            formatter = new SimpleDateFormat("dd/MM/YYYY", Locale.ITALIAN);
            ViewHolder.Date.setText(formatter.format(convertedCommitDate));
        } catch (ParseException e) {
            ViewHolder.Date.setText(datestring);
        }
        ViewHolder.Type.setText(communication.getType());
    }

    @Override
    public int getItemCount() {
        return CVDataList.size();
    }

    class CommunicationHolder extends RecyclerView.ViewHolder {
        final TextView Title;
        final TextView Date;
        final TextView Type;

        CommunicationHolder(View itemView) {
            super(itemView);
            Title = (TextView) itemView.findViewById(R.id.communication_title);
            Date = (TextView) itemView.findViewById(R.id.communication_date);
            Type = (TextView) itemView.findViewById(R.id.communication_type);
        }
    }
}
