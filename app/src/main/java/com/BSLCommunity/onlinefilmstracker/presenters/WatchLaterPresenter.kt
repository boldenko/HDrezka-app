package com.BSLCommunity.onlinefilmstracker.presenters

import com.BSLCommunity.onlinefilmstracker.models.FilmModel
import com.BSLCommunity.onlinefilmstracker.models.WatchLaterModel
import com.BSLCommunity.onlinefilmstracker.objects.WatchLater
import com.BSLCommunity.onlinefilmstracker.views.interfaces.IMsg
import com.BSLCommunity.onlinefilmstracker.viewsInterface.WatchLaterView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WatchLaterPresenter(private val watchLaterView: WatchLaterView) {
    private var loadedWatchLaterList: ArrayList<WatchLater> = ArrayList()
    private var activeWatchLaterList: ArrayList<WatchLater> = ArrayList()
    private val ITEMS_PER_PAGE = 9

    fun initList() {
        GlobalScope.launch {
            loadedWatchLaterList = WatchLaterModel.getWatchLaterList()

            withContext(Dispatchers.Main) {
                if (loadedWatchLaterList.size > 0) {
                    watchLaterView.setWatchLaterList(activeWatchLaterList)
                    getNextWatchLater()
                } else {
                    watchLaterView.showMsg(IMsg.MsgType.NOTHING_FOUND)
                }
            }
        }
    }

    fun getNextWatchLater() {
        watchLaterView.setProgressBarState(true)

        val dataToLoad: ArrayList<WatchLater> = ArrayList()
        if (loadedWatchLaterList.size > 0) {
            for ((index, item) in (loadedWatchLaterList.clone() as ArrayList<WatchLater>).withIndex()) {
                dataToLoad.add(item)
                loadedWatchLaterList.removeAt(0)

                if (index == ITEMS_PER_PAGE - 1) {
                    break
                }
            }

            loadFilmData(dataToLoad)
        }
    }

    private fun loadFilmData(dataToLoad: ArrayList<WatchLater>) {
        for ((index, item) in dataToLoad.withIndex()) {
            GlobalScope.launch {
                item.posterPath = FilmModel.getFilmPosterByLink(item.filmLInk)

                if (index == dataToLoad.size - 1) {
                    withContext(Dispatchers.Main) {
                        activeWatchLaterList.addAll(dataToLoad)
                        watchLaterView.redrawWatchLaterList()
                        watchLaterView.setProgressBarState(false)
                    }
                }
            }
        }
    }
}