package com.BSLCommunity.onlinefilmstracker.views.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.BSLCommunity.onlinefilmstracker.R
import com.BSLCommunity.onlinefilmstracker.models.UserModel
import com.BSLCommunity.onlinefilmstracker.objects.Film
import com.BSLCommunity.onlinefilmstracker.objects.WatchLater
import com.BSLCommunity.onlinefilmstracker.presenters.WatchLaterPresenter
import com.BSLCommunity.onlinefilmstracker.utils.FragmentOpener
import com.BSLCommunity.onlinefilmstracker.interfaces.IMsg
import com.BSLCommunity.onlinefilmstracker.interfaces.OnFragmentInteractionListener
import com.BSLCommunity.onlinefilmstracker.objects.UserData
import com.BSLCommunity.onlinefilmstracker.views.adapters.WatchLaterRecyclerViewAdapter
import com.BSLCommunity.onlinefilmstracker.views.viewsInterface.WatchLaterView

class WatchLaterFragment : Fragment(), WatchLaterView {
    private lateinit var currentView: View
    private lateinit var watchLaterPresenter: WatchLaterPresenter
    private lateinit var listView: RecyclerView
    private lateinit var fragmentListener: OnFragmentInteractionListener
    private lateinit var progressBar: ProgressBar
    private lateinit var msgView: TextView
    private lateinit var scrollView: NestedScrollView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentListener = context as OnFragmentInteractionListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        currentView = inflater.inflate(R.layout.fragment_watch_later_list, container, false)
        Log.d("FRAGMENT_TEST", "watch init")

        listView = currentView.findViewById(R.id.fragment_watch_later_list_rv)
        progressBar = currentView.findViewById(R.id.fragment_watch_later_list_pb_loading)
        msgView = currentView.findViewById(R.id.fragment_watch_later_list_tv_msg)

        scrollView = currentView.findViewById(R.id.fragment_watch_later_nsv_films)
        scrollView.setOnScrollChangeListener(object : NestedScrollView.OnScrollChangeListener {
            override fun onScrollChange(v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
                val view = scrollView.getChildAt(scrollView.childCount - 1)
                val diff = view.bottom - (scrollView.height + scrollView.scrollY)

                if (diff == 0) {
                    setProgressBarState(true)
                    watchLaterPresenter.getNextWatchLater()
                }
            }
        })

        if (UserData.isLoggedIn == true) {
            listView.layoutManager = LinearLayoutManager(context)
            watchLaterPresenter = WatchLaterPresenter(this)
            watchLaterPresenter.initList()
        } else {
            showMsg(IMsg.MsgType.NOT_AUTHORIZED)
        }

        return currentView
    }

    override fun setWatchLaterList(list: ArrayList<WatchLater>) {
        listView.adapter = WatchLaterRecyclerViewAdapter(list, ::listCallback)
        progressBar.visibility = View.GONE
    }

    private fun listCallback(film: Film) {
        FragmentOpener.openFilm(film, this, fragmentListener)
    }

    override fun showMsg(msg: IMsg.MsgType) {
        msgView.visibility = View.VISIBLE
        listView.visibility = View.GONE
        progressBar.visibility = View.GONE

        var text = ""
        when (msg) {
            IMsg.MsgType.NOT_AUTHORIZED -> text = "Данный раздел доступен только зарегистрированным пользователям"
            IMsg.MsgType.NOTHING_ADDED -> text = "В этот раздел попадают все просматриваемые тобой сериалы"
        }

        msgView.text = text
    }

    override fun redrawWatchLaterList() {
        listView.adapter?.notifyDataSetChanged()
    }

    override fun setProgressBarState(state: Boolean) {
        if (state) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
        }
    }
}