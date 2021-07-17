package com.falcofemoralis.hdrezkaapp.presenters

import android.util.Log
import com.falcofemoralis.hdrezkaapp.interfaces.IMsg
import com.falcofemoralis.hdrezkaapp.interfaces.IProgressState
import com.falcofemoralis.hdrezkaapp.models.FilmModel
import com.falcofemoralis.hdrezkaapp.models.WatchLaterModel
import com.falcofemoralis.hdrezkaapp.objects.WatchLater
import com.falcofemoralis.hdrezkaapp.utils.ExceptionHelper.catchException
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.WatchLaterView
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
            try {
                loadedWatchLaterList = WatchLaterModel.getWatchLaterList()
                Log.d("WC_TEST", "downloaded new list!")
                Log.d("WC_TEST", loadedWatchLaterList.toString())

                withContext(Dispatchers.Main) {
                    if (loadedWatchLaterList.size > 0) {
                        Log.d("WC_TEST", "${loadedWatchLaterList.size}")

                        watchLaterView.setWatchLaterList(activeWatchLaterList)
                        getNextWatchLater()
                    } else {
                        watchLaterView.showMsg(IMsg.MsgType.NOTHING_FOUND)
                    }
                }
            } catch (e: Exception) {
                catchException(e, watchLaterView)
                return@launch
            }
        }
    }

    fun getNextWatchLater() {
        watchLaterView.setProgressBarState(IProgressState.StateType.LOADING)

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
                try {
                    item.posterPath = FilmModel.getFilmPosterByLink(item.filmLInk)

                    if (index == dataToLoad.size - 1) {
                        withContext(Dispatchers.Main) {
                            activeWatchLaterList.addAll(dataToLoad)
                            watchLaterView.redrawWatchLaterList()
                            watchLaterView.setProgressBarState(IProgressState.StateType.LOADED)
                        }
                    }
                } catch (e: Exception) {
                    catchException(e, watchLaterView)
                    return@launch
                }
            }
        }
    }

    fun updateList(){
        activeWatchLaterList.clear()
        initList()
    }
}