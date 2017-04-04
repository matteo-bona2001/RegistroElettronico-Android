package com.sharpdroid.registroelettronico.Adapters;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.sharpdroid.registroelettronico.Activities.MarkSubjectDetailActivity;
import com.sharpdroid.registroelettronico.Databases.RegistroDB;
import com.sharpdroid.registroelettronico.Interfaces.Client.Average;
import com.sharpdroid.registroelettronico.R;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import devlight.io.library.ArcProgressStackView;

import static com.sharpdroid.registroelettronico.Utils.Metodi.MessaggioVoto;
import static com.sharpdroid.registroelettronico.Utils.Metodi.getMediaColor;
import static com.sharpdroid.registroelettronico.Utils.Metodi.getPossibileSubjectTarget;

public class MedieAdapter extends RecyclerView.Adapter<MedieAdapter.MedieHolder> {
    final private String TAG = MedieAdapter.class.getSimpleName();

    private final List<Average> CVDataList;
    private final Context mContext;

    private final RegistroDB db;
    private int period;

    public MedieAdapter(Context context, List<Average> CVDataList, RegistroDB db) {
        this.mContext = context;
        this.CVDataList = CVDataList;
        this.db = db;
    }

    public void addAll(Collection<Average> list, int p) {
        CVDataList.addAll(list);
        this.period = p;
        notifyDataSetChanged();
    }

    public void clear() {
        CVDataList.clear();
        notifyDataSetChanged();
    }

    @Override
    public MedieHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_medie_grid, parent, false);
        return new MedieHolder(v);
    }


    @Override
    public void onBindViewHolder(MedieHolder ViewHolder, int position) {
        final Average avg = CVDataList.get(position);

        ViewHolder.mTextViewMateria.setText(WordUtils.capitalizeFully(avg.name));

        ViewHolder.mCardViewMedia.setOnClickListener(v -> mContext.startActivity(new Intent(mContext, MarkSubjectDetailActivity.class).putExtra("data", new Gson().toJson(db.getMarks(avg.code))).putExtra("period", period)));

        if (avg.avg != 0f) {
            ViewHolder.mTextViewMedia.setText(String.format(Locale.getDefault(), "%.2f", avg.avg));

            float target = avg.target;

            if (target <= 0) {

                String t = PreferenceManager.getDefaultSharedPreferences(mContext)
                        .getString("voto_obiettivo", "8");

                if (t.equals("Auto")) {
                    int tar = getPossibileSubjectTarget(avg.avg);
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("target", tar);
                    db.editSubject(avg.code, contentValues);
                    target = tar;

                } else target = Float.parseFloat(t);

            }
            List<ArcProgressStackView.Model> models = new ArrayList<>();
            models.add(new ArcProgressStackView.Model("media", avg.avg * 10, ContextCompat.getColor(mContext, getMediaColor(avg.avg, target))));

            ViewHolder.mArcProgressStackView.setModels(models);

            String obbiettivo_string = MessaggioVoto(target, avg.avg, avg.count);
            ViewHolder.mTextViewDesc.setText(obbiettivo_string);

        } else {
            List<ArcProgressStackView.Model> models = new ArrayList<>();
            models.add(new ArcProgressStackView.Model("media", 100, ContextCompat.getColor(mContext, R.color.intro_blue)));
            ViewHolder.mArcProgressStackView.setModels(models);
            ViewHolder.mTextViewMedia.setText("-");
            ViewHolder.mTextViewDesc.setText(mContext.getString(R.string.nessun_voto_numerico));
        }
    }

    @Override
    public int getItemCount() {
        return CVDataList.size();
    }

    class MedieHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.cardview_medie)
        CardView mCardViewMedia;
        @BindView(R.id.progressvoti)
        ArcProgressStackView mArcProgressStackView;
        @BindView(R.id.materia)
        TextView mTextViewMateria;
        @BindView(R.id.media)
        TextView mTextViewMedia;
        @BindView(R.id.descrizione)
        TextView mTextViewDesc;

        MedieHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
