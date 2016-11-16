package com.sharpdroid.registro.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.sharpdroid.registro.Interfaces.Absences;
import com.sharpdroid.registro.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AllAbsencesAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private Absences absences;
    private LayoutInflater mInflater;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());

    public AllAbsencesAdapter(Context mContext) {
        this.mContext = mContext;
        mInflater = LayoutInflater.from(mContext);
    }

    public Absences getAbsences() {
        return absences;
    }

    public void setAbsences(Absences absences) {
        // TODO: 16/11/2016 test
        this.absences = new Absences(absences.getAbsences(),absences.getDelays(),absences.getExits());
        /*
        this.absences.setAbsences(absences.getAbsences());
        this.absences.setDelays(absences.getDelays());
        this.absences.setExits(absences.getExits());*/
        notifyDataSetChanged();
    }

    public void clear() {
        absences = null;
        notifyDataSetChanged();
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
                //return absences.getDelays();
            case 2:
                //return absences.getExits();
            default:
                return null;
        }
    }

    @Override
    public List getChild(int i, int i1) {
        return (List) ((List) getGroup(i)).get(i1);
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

        // TODO: 16/11/2016 views
        List groupData = (List) getGroup(group_pos);

        return view;
    }

    @Override
    public View getChildView(int group_pos, int child_pos, boolean last, View view, ViewGroup viewGroup) {
        if (view == null)
            view = mInflater.inflate(R.layout.adapter_expandable_child, viewGroup, false);

        // TODO: 16/11/2016 views

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

}
