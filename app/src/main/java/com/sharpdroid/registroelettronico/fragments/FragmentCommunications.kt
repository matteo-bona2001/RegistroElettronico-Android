package com.sharpdroid.registroelettronico.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.*
import com.afollestad.materialdialogs.MaterialDialog
import com.sharpdroid.registroelettronico.BuildConfig
import com.sharpdroid.registroelettronico.NotificationManager
import com.sharpdroid.registroelettronico.R
import com.sharpdroid.registroelettronico.adapters.CommunicationAdapter
import com.sharpdroid.registroelettronico.database.entities.Communication
import com.sharpdroid.registroelettronico.database.room.DatabaseHelper
import com.sharpdroid.registroelettronico.utils.Account
import com.sharpdroid.registroelettronico.utils.EventType
import com.sharpdroid.registroelettronico.utils.Metodi.downloadAttachment
import com.sharpdroid.registroelettronico.utils.Metodi.dp
import com.sharpdroid.registroelettronico.utils.Metodi.openFile
import com.sharpdroid.registroelettronico.utils.Metodi.updateBacheca
import com.sharpdroid.registroelettronico.viewModels.CommunicationViewModel
import com.sharpdroid.registroelettronico.views.EmptyFragment
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration
import kotlinx.android.synthetic.main.coordinator_swipe_recycler.*
import kotlinx.android.synthetic.main.coordinator_swipe_recycler.view.*
import java.io.File

class FragmentCommunications : Fragment(), SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener, NotificationManager.NotificationReceiver, CommunicationAdapter.DownloadListener {
    private lateinit var snackbar: Snackbar

    override fun didReceiveNotification(code: Int, args: Array<in Any>) {
        snackbar = Snackbar.make(coordinator_layout, R.string.download_in_corso,
                Snackbar.LENGTH_INDEFINITE)
        when (code) {
            EventType.UPDATE_BACHECA_START -> {
                if (!swiperefresh.isRefreshing) swiperefresh.isRefreshing = true
            }
            EventType.UPDATE_BACHECA_OK,
            EventType.UPDATE_BACHECA_KO -> {
                if (swiperefresh.isRefreshing) swiperefresh.isRefreshing = false
            }
            EventType.DOWNLOAD_FILE_START -> {
                snackbar.show()
            }
            EventType.DOWNLOAD_FILE_OK -> {
                val file = File(DatabaseHelper.database.communicationsDao().getInfo(args[0] as Long)?.path)
                with(snackbar) {
                    setText(activity.getString(R.string.file_downloaded, file.name))
                    setAction(R.string.open) { openFile(activity, file, Snackbar.make(coordinator_layout, context.resources.getString(R.string.missing_app, file.name), Snackbar.LENGTH_SHORT)) }
                    duration = Snackbar.LENGTH_SHORT
                    show()
                }
            }
            EventType.DOWNLOAD_FILE_KO -> {
                with(snackbar) {
                    setText("File non scaricato")
                    duration = Snackbar.LENGTH_SHORT
                    show()
                }
            }
            else -> { // Ignore
            }
        }
    }

    private lateinit var mRVAdapter: CommunicationAdapter
    private lateinit var emptyHolder: EmptyFragment
    private val viewModel by lazy {
        ViewModelProviders.of(this)[CommunicationViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.coordinator_swipe_recycler, container, false)
        emptyHolder = EmptyFragment(context)
        emptyHolder.visibility = View.GONE
        emptyHolder.setTextAndDrawable("Nessuna comunicazione", R.drawable.ic_assignment)
        layout.coordinator_layout.addView(emptyHolder)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        NotificationManager.instance.addObserver(this, EventType.UPDATE_BACHECA_START, EventType.UPDATE_BACHECA_OK, EventType.UPDATE_BACHECA_KO, EventType.DOWNLOAD_FILE_START, EventType.DOWNLOAD_FILE_OK, EventType.DOWNLOAD_FILE_KO)
        setHasOptionsMenu(true)

        swiperefresh.setOnRefreshListener(this)
        swiperefresh.setColorSchemeResources(
                R.color.bluematerial,
                R.color.redmaterial,
                R.color.greenmaterial,
                R.color.orangematerial)
        activity.title = getString(R.string.communications)

        with(recycler) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(HorizontalDividerItemDecoration.Builder(context).colorResId(R.color.divider).size(dp(1)).build())
            itemAnimator = null
            isVerticalScrollBarEnabled = true

            mRVAdapter = CommunicationAdapter(this@FragmentCommunications)
            adapter = mRVAdapter
        }

        if (savedInstanceState == null)
            download()

        viewModel
                .getCommunications(Account.with(context).user)
                .observe(this, Observer {
                    addCommunications(it ?: emptyList())
                })
    }

    override fun onCommunicationClick(communication: Communication) {
        with(DatabaseHelper.database.communicationsDao().getInfo(communication.myId) ?: return) {
            val builder = MaterialDialog.Builder(activity).title(title).content(content.trim())

            if (communication.hasAttachment) {
                builder.neutralText(if (path.isEmpty() || !File(path).exists()) "SCARICA" else "APRI")
                builder.onNeutral { _, _ ->
                    if (path.isNotEmpty() && File(path).exists()) {
                        openFile(activity, File(path), Snackbar.make(coordinator_layout, context.resources.getString(R.string.missing_app, File(path).name), Snackbar.LENGTH_SHORT))
                    } else {
                        downloadAttachment(activity, communication)
                    }
                }
            }

            builder.positiveText("OK")
            builder.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)

        val searchView = menu.getItem(0).actionView as SearchView

        searchView.maxWidth = Integer.MAX_VALUE
        searchView.setOnQueryTextListener(this)
    }

    private fun addCommunications(communications: List<Communication>) {
        mRVAdapter.clear()
        mRVAdapter.addAll(communications)

        emptyHolder.visibility = if (communications.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onRefresh() {
        download()
    }

    private fun download() {
        updateBacheca(activity)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        NotificationManager.instance.removeObserver(this, EventType.UPDATE_BACHECA_START, EventType.UPDATE_BACHECA_OK, EventType.UPDATE_BACHECA_KO, EventType.DOWNLOAD_FILE_START, EventType.DOWNLOAD_FILE_OK, EventType.DOWNLOAD_FILE_KO)
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        mRVAdapter.filter.filter(query)
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        mRVAdapter.filter.filter(newText)
        return false
    }
}
