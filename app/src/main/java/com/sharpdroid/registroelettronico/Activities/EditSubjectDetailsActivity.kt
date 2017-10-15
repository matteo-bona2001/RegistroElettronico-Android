package com.sharpdroid.registroelettronico.Activities

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.*
import butterknife.ButterKnife
import com.afollestad.materialdialogs.MaterialDialog
import com.orm.SugarRecord
import com.sharpdroid.registroelettronico.Adapters.Holders.Holder
import com.sharpdroid.registroelettronico.Databases.Entities.Subject
import com.sharpdroid.registroelettronico.Databases.Entities.SubjectInfo
import com.sharpdroid.registroelettronico.Databases.Entities.Teacher
import com.sharpdroid.registroelettronico.R
import com.sharpdroid.registroelettronico.Utils.Account
import com.sharpdroid.registroelettronico.Utils.Metodi
import com.sharpdroid.registroelettronico.Views.Cells.HeaderCell
import com.sharpdroid.registroelettronico.Views.Cells.ShadowCell
import com.sharpdroid.registroelettronico.Views.Cells.ValueDetailsCell
import kotlinx.android.synthetic.main.activity_edit_subject_details.*

class EditSubjectDetailsActivity : AppCompatActivity() {

    var subjectInfo: SubjectInfo? = null
    private lateinit var adapter: EditSubjectAdapter
    private var rowCount: Int = 0
    private var rowInfo: Int = 0
    private var rowTitle: Int = 0
    private var rowNotes: Int = 0
    private var rowClassroom: Int = 0
    private var rowSeparator: Int = 0
    private var rowTeachers: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_subject_details)
        ButterKnife.bind(this)

        // toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }


        init(intent.getLongExtra("code", -1))

    }

    internal fun init(code: Long) {
        if (code == -1L) return
        val temp = SugarRecord.findById(Subject::class.java, code)
        subjectInfo = temp.getInfo(this) ?: SubjectInfo((Account.with(this).user.toString() + "" + code.toString()).toLong(), 0f, "", "", "", temp, Account.with(this).user)
        subjectInfo?.subject?.teachers = SugarRecord.findWithQuery(Teacher::class.java, "select * from TEACHER where TEACHER.ID IN (select SUBJECT_TEACHER.TEACHER from SUBJECT_TEACHER where SUBJECT_TEACHER.SUBJECT=?)", code.toString())
        title = Metodi.capitalizeEach(subjectInfo?.description.or(subjectInfo?.subject?.description!!))

        rowCount = 0
        rowInfo = rowCount++
        rowTitle = rowCount++
        rowNotes = rowCount++
        rowClassroom = rowCount++
        rowSeparator = rowCount++
        rowTeachers = rowCount++

        rowCount += subjectInfo?.subject?.teachers!!.size

        adapter = EditSubjectAdapter {
            val builder = MaterialDialog.Builder(this)

            when (it) {
                rowTitle -> {
                    builder.title("Nome")
                    builder.content("Modifica il nome della materia")
                    builder.input("Inserisci un nuovo nome", Metodi.capitalizeEach(subjectInfo!!.description.or(subjectInfo!!.subject.description)), true, { _, _ -> })
                    builder.onPositive { dialog, _ -> subjectInfo!!.description = dialog.inputEditText!!.text.toString(); adapter.notifyDataSetChanged() }
                }
                rowNotes -> {
                    builder.title("Dettagli")
                    builder.content("Modifica i dettagli della materia")
                    builder.input("Inserisci un nuovo nome", subjectInfo!!.details, true, { _, _ -> })
                    builder.onPositive { dialog, _ -> subjectInfo!!.details = dialog.inputEditText!!.text.toString(); adapter.notifyDataSetChanged() }

                }
                rowClassroom -> {
                    builder.title("Classe")
                    builder.content("Modifica la classe della materia")
                    builder.input("Inserisci una nuova classe", subjectInfo!!.classroom, true, { _, _ -> })
                    builder.onPositive { dialog, _ -> subjectInfo!!.classroom = dialog.inputEditText!!.text.toString(); adapter.notifyDataSetChanged() }
                }
            }

            builder.positiveText("Salva")
            builder.neutralText("Annulla")
            builder.show()
        }
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

    }

    internal fun apply() {
        SugarRecord.update(subjectInfo)
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) super.onBackPressed()
        if (item.itemId == R.id.apply) apply()
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.edit_subject_menu, menu)
        return true
    }

    private inner class EditSubjectAdapter(val listener: (position: Int) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var v: View? = null
            when (viewType) {
                0 -> {
                    v = HeaderCell(parent.context)
                    v.setBackgroundColor(Color.WHITE)
                }
                1 -> {
                    v = ValueDetailsCell(parent.context)
                    v.setBackgroundColor(Color.WHITE)
                }
                2 -> v = ShadowCell(parent.context)
            }
            return Holder(v)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (getItemViewType(position)) {
                0 -> (holder.itemView as HeaderCell).setText(if (position == rowInfo) "Informazioni" else "Professori")
                1 -> when (position) {
                    rowTitle -> (holder.itemView as ValueDetailsCell).setTextAndValue(Metodi.capitalizeEach(subjectInfo!!.description.or(subjectInfo!!.subject.description)).or("Senza nome"), "Nome", true)
                    rowNotes -> (holder.itemView as ValueDetailsCell).setTextAndValue(Metodi.capitalizeEach(subjectInfo!!.details).or("Aggiungi dettagli"), "Dettagli", true)
                    rowClassroom -> (holder.itemView as ValueDetailsCell).setTextAndValue(Metodi.capitalizeEach(subjectInfo!!.classroom).or("Aggiungi classe"), "Dettagli", true)
                    else -> (holder.itemView as ValueDetailsCell).setText(Metodi.capitalizeEach(subjectInfo!!.subject.teachers[position - rowTeachers - 1].teacherName), true)
                }
            }
            if (getItemViewType(position) == 1 && position in rowTitle..rowClassroom) {
                val attrs = intArrayOf(android.R.attr.selectableItemBackground)
                val ta = obtainStyledAttributes(attrs)
                val drawableFromTheme = ta.getDrawable(0)
                val wrapper = LayerDrawable(arrayOf(ColorDrawable(Color.WHITE), drawableFromTheme))
                ta.recycle()
                holder.itemView.setBackgroundDrawable(wrapper)
                holder.itemView.setOnClickListener { view ->
                    view.postDelayed({ listener.invoke(position) }, ViewConfiguration.getTapTimeout().toLong())
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            return if (position == rowInfo || position == rowTeachers) 0
            else if (position == rowSeparator) 2
            else 1
        }

        override fun getItemCount(): Int {
            return rowCount
        }
    }
}

private fun String?.or(s: String): String = if (isNullOrEmpty()) s else this!!
