package com.BSLCommunity.onlinefilmstracker.presenters

import com.BSLCommunity.onlinefilmstracker.models.ActorModel
import com.BSLCommunity.onlinefilmstracker.models.BookmarksModel
import com.BSLCommunity.onlinefilmstracker.models.CommentsModel
import com.BSLCommunity.onlinefilmstracker.models.FilmModel
import com.BSLCommunity.onlinefilmstracker.objects.Actor
import com.BSLCommunity.onlinefilmstracker.objects.Comment
import com.BSLCommunity.onlinefilmstracker.objects.Film
import com.BSLCommunity.onlinefilmstracker.viewsInterface.FilmView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FilmPresenter(private val filmView: FilmView, private val film: Film) {
    private val activeComments: ArrayList<Comment> = ArrayList()
    private val loadedComments: ArrayList<Comment> = ArrayList()
    private var commentsPage = 1
    val COMMENTS_PER_AGE = 18

    fun initFilmData() {
        GlobalScope.launch {
            if (!film.hasMainData) {
                FilmModel.getMainData(film)
            }

            if (!film.hasAdditionalData) {
                FilmModel.getAdditionalData(film)
            }

            withContext(Dispatchers.Main) {
                filmView.setFilmBaseData(film)
                film.genres?.let { filmView.setGenres(it) }
                film.countries?.let { filmView.setCountries(it) }
                film.directors?.let { filmView.setDirectors(it) }
                film.bookmarks?.let { filmView.setBookmarksList(it) }
                filmView.setFilmLink(film.link)
                film.seriesSchedule?.let { filmView.setSchedule(it) }
                film.collection?.let { filmView.setCollection(it) }
                film.related?.let { filmView.setRelated(it) }
            }
        }
    }

    fun initFullSizeImage() {
        film.fullSizePosterPath?.let { filmView.setFullSizeImage(it) }
    }

    fun initActors() {
        if (film.actorsLinks != null) {

            val actors = arrayOfNulls<Actor>(film.actorsLinks!!.size)

            for ((index, actorLink) in film.actorsLinks!!.withIndex()) {
                GlobalScope.launch {
                    actors[index] = ActorModel.getActorMainInfo(actorLink)

                    if (index == film.actorsLinks!!.size - 1) {
                        withContext(Dispatchers.Main) {
                            val list: ArrayList<Actor?> = ArrayList()
                            for (item in actors) {
                                list.add(item)
                            }
                            filmView.setActors(list)
                        }
                    }
                }
            }
        }
    }

    fun initPlayer() {
        filmView.setPlayer(film.link)
    }

    fun setBookmark(bookmarkId: String) {
        film.filmId?.let {
            GlobalScope.launch {
                BookmarksModel.postBookmark(it, bookmarkId)
            }
        }
    }

    fun initComments() {
        filmView.setCommentsList(activeComments)
        getNextComments()
    }

    fun getNextComments() {
        filmView.setCommentsProgressState(true)

        if(loadedComments.size > 0){
            for((index, comment) in (loadedComments.clone() as ArrayList<Comment>).withIndex()){
                activeComments.add(comment)
                loadedComments.removeAt(0)

                if(index == COMMENTS_PER_AGE - 1){
                    break
                }
            }

            filmView.redrawComments()
            filmView.setCommentsProgressState(false)
        } else{
            GlobalScope.launch {
                film.filmId?.let {
                    CommentsModel.getCommentsFromPage(commentsPage, it)
                }?.let {
                    loadedComments.addAll(it)
                }
                commentsPage++

                withContext(Dispatchers.Main){
                    getNextComments()
                }
            }
        }
    }
}