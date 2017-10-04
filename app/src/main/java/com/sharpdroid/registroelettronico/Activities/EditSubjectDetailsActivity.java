package com.sharpdroid.registroelettronico.Activities;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.orm.SugarRecord;
import com.sharpdroid.registroelettronico.Databases.Entities.Subject;
import com.sharpdroid.registroelettronico.Databases.Entities.Teacher;
import com.sharpdroid.registroelettronico.R;
import com.sharpdroid.registroelettronico.Utils.Account;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.sharpdroid.registroelettronico.Utils.Metodi.capitalizeEach;

public class EditSubjectDetailsActivity extends AppCompatActivity {

    @BindView(R.id.name)
    TextInputEditText name;
    @BindView(R.id.professor)
    TextInputEditText prof;
    @BindView(R.id.professor2)
    TextInputEditText prof2;
    @BindView(R.id.classroom)
    TextInputEditText classroom;
    @BindView(R.id.notes)
    TextInputEditText notes;

    Subject subject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_subject_details);
        ButterKnife.bind(this);

        // toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        name.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_title), null, null, null);
        classroom.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_room), null, null, null);
        notes.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_description), null, null, null);
        prof.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_person_black), null, null, null);
        prof2.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_person_black), null, null, null);

        notes.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                apply();
                return true;
            }
            return false;
        });

        init(getIntent().getLongExtra("code", -1));
    }

    void init(long code) {
        if (code != -1) {
            subject = SugarRecord.findById(Subject.class, code);

            name.setText(capitalizeEach(subject.getDescription()));
            subject.setTeachers(SugarRecord.find(Teacher.class, "ID IN (select TEACHER from SUBJECT_TEACHER where PROFILE=? and SUBJECT=?)", String.valueOf(Account.Companion.with(this).getUser()), String.valueOf(code)));
            if (!subject.getTeachers().isEmpty()) {
                if (subject.getTeachers().size() > 0)
                    prof.setText(capitalizeEach(subject.getTeachers().get(0).getTeacherName(), true));
                if (subject.getTeachers().size() > 1)
                    prof2.setText(capitalizeEach(subject.getTeachers().get(1).getTeacherName(), true));
                else
                    prof2.setVisibility(View.GONE);
            }
            classroom.setText(subject.getClassroom());
            notes.setText(subject.getDetails());
        }
    }

    void apply() {
        String name, classroom, notes;

        name = this.name.getText().toString().trim();
        classroom = this.classroom.getText().toString().trim();
        notes = this.notes.getText().toString().trim();

        subject.setDescription(name);
        subject.setClassroom(classroom);
        subject.setDetails(notes);
        SugarRecord.update(subject);

        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();
        if (item.getItemId() == R.id.apply) apply();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_subject_menu, menu);
        return true;
    }

    @Override
    protected void onStop() {

        super.onStop();
    }
}
