package com.sharpdroid.registro.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.sharpdroid.registro.Interfaces.Absence;
import com.sharpdroid.registro.Interfaces.Absences;
import com.sharpdroid.registro.R;

import java.util.List;

import static com.sharpdroid.registro.Utils.Metodi.getUndoneCountAbsences;
import static com.sharpdroid.registro.Utils.Metodi.getUndoneCountDelays;
import static com.sharpdroid.registro.Utils.Metodi.getUndoneCountExits;

public class AllAbsencesAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private Absences absences;
    private LayoutInflater mInflater;

    public AllAbsencesAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    public void setAbsences(Absences absences) {
        this.absences = absences;
        notifyDataSetChanged();
    }

    public void clear() {
        this.absences = null;
    }

    @Override
    public int getGroupCount() {
        return 3;
    }

    @Override
    public int getChildrenCount(int i) {
        switch (i) {
            case 0:
                return absences.getAbsences().size();
            case 1:
                return absences.getDelays().size();
            case 2:
                return absences.getExits().size();
        }
        return 0;
    }

    @Override
    public List getGroup(int i) {
        switch (i) {
            case 0:
                return absences.getAbsences();
            case 1:
                return absences.getDelays();
            case 2:
                return absences.getExits();
            default:
                return null;
        }
    }

    @Override
    public List getChild(int i, int i1) {
        return (List) getGroup(i).get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int p_parent, int p_child) {
        return p_child;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int group_pos, boolean expanded, View view, ViewGroup viewGroup) {
        if (view == null)
            view = mInflater.inflate(R.layout.adapter_expandable_group, viewGroup, false);
        FrameLayout attive = (FrameLayout) view.findViewById(R.id.attive);
        TextView text = (TextView) view.findViewById(R.id.attive_text);

        int count = 0;

        switch (group_pos) {
            case 0:
                count = getUndoneCountAbsences(absences.getAbsences());
                break;
            case 1:
                count = getUndoneCountDelays(absences.getDelays());
                break;
            case 2:
                //count = getUndoneCountExits(absences.getExits());
                break;
        }

        if (count == 0) {
            attive.setVisibility(View.GONE);
        } else {
            attive.setVisibility(View.VISIBLE);
            text.setText(mContext.getResources().getQuantityString(R.plurals.attiva, count, count));
        }

        return view;
    }

    @Override
    public View getChildView(int group_pos, int child_pos, boolean last, View view, ViewGroup viewGroup) {
        if (view == null)
            view = mInflater.inflate(R.layout.adapter_expandable_child, viewGroup, false);

        Absence absence = absences.getAbsences().get(child_pos);

        FrameLayout attive = (FrameLayout) view.findViewById(R.id.attive);
        TextView from, to, justification;

        from = (TextView) view.findViewById(R.id.from);
        to = (TextView) view.findViewById(R.id.to);
        justification = (TextView) view.findViewById(R.id.justification);

        from.setText(absence.getFrom());
        to.setText(absence.getTo());
        if (absence.getJustification() != null && !absence.getJustification().isEmpty())
            justification.setText(absence.getJustification());

        if (absence.isDone()) attive.setVisibility(View.GONE);
        else attive.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

}
