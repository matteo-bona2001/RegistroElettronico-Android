package com.sharpdroid.registroelettronico.Adapters;

import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sharpdroid.registroelettronico.Interfaces.API.FileTeacher;
import com.sharpdroid.registroelettronico.Interfaces.API.Folder;
import com.sharpdroid.registroelettronico.Interfaces.Client.FileElement;
import com.sharpdroid.registroelettronico.R;

import org.apache.commons.lang3.text.WordUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.sharpdroid.registroelettronico.Utils.Metodi.Delimeters;

// DONE: 19/01/2017 Risolvere crash quando si chiude una cartella e poi si scorre verso il basso

public class FolderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    final static String TAG = FolderAdapter.class.getSimpleName();

    private final SimpleDateFormat formatter = new SimpleDateFormat("MMM d, yyyy", Locale.ITALIAN);
    private FileElement fileElements = new FileElement();
    private FragmentManager fragmentManager;
    private Listener listener;

    public FolderAdapter(FragmentManager fragmentManager, Listener listener) {
        this.fragmentManager = fragmentManager;
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case R.layout.adapter_folder:
                return new FileTeacherHolder(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));
            default:
                return new SubheaderHolder(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int layout = getItemViewType(position);

        FileElement fe = fileElements.get(position);

        switch (layout) {
            case R.layout.adapter_folder:

                Folder f = (Folder) fe;

                FileTeacherHolder folderHolder = (FileTeacherHolder) holder;

                folderHolder.layout.setOnClickListener(view -> {
                    if (listener != null)
                        listener.onFolderClick(f, view);
                });

                folderHolder.title.setText(f.getName().trim());
                folderHolder.date.setText(formatter.format(f.getLast()));
                break;
            default:
                SubheaderHolder subHolder = (SubheaderHolder) holder;

                FileTeacher ft = (FileTeacher) fe;
                String profHeader = ft.getName();
                subHolder.divider.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
                subHolder.teacher.setText(WordUtils.capitalizeFully(profHeader, Delimeters));

                break;
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (fileElements.get(position) instanceof FileTeacher)
            return R.layout.adapter_header_divider_72_padding;
        else return R.layout.adapter_folder;
    }

    @Override
    public int getItemCount() {
        return fileElements.size();
    }

    public void setFileTeachers(List<FileTeacher> fileteachers) {
        fileElements.clear();
        fileElements.ConvertFileTeachertoFileElement(fileteachers);
        notifyDataSetChanged();
    }

    public interface Listener {
        void onFolderClick(Folder f, View container);
    }

    class SubheaderHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.content)
        TextView teacher;
        @BindView(R.id.divider)
        View divider;

        SubheaderHolder(View layout) {
            super(layout);
            ButterKnife.bind(this, layout);
        }
    }

    class FileTeacherHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.date)
        TextView date;
        @BindView(R.id.relative_layout)
        View layout;

        FileTeacherHolder(View layout) {
            super(layout);
            ButterKnife.bind(this, layout);
        }
    }

}
