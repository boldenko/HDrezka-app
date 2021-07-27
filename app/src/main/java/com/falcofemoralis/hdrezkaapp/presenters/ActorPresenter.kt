package com.falcofemoralis.hdrezkaapp.presenters

import com.falcofemoralis.hdrezkaapp.models.ActorModel
import com.falcofemoralis.hdrezkaapp.models.FilmModel
import com.falcofemoralis.hdrezkaapp.objects.Actor
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.utils.ExceptionHelper
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.ActorView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActorPresenter(
    private val actorView: ActorView,
    private var actor: Actor
) {
    fun initActorData() {
        GlobalScope.launch {
            try {
                ActorModel.getActorFilms(actor)

                actor.personCareerFilms?.let {
                    val overall = actor.personCareerFilms!!.size
                    val list: Array<Pair<String, ArrayList<Film>>?> = arrayOfNulls(overall)
                    var all = 0

                    for ((index, career) in actor.personCareerFilms!!.withIndex()) {
                        FilmModel.getFilmsData(career.second, career.second.size) { films ->
                            list[index] = Pair(career.first, films)

                            all++
                            if (all == overall) {
                                GlobalScope.launch {
                                    actor.personCareerFilms!!.clear()
                                    for (item in list) {
                                        if (item != null) {
                                            actor.personCareerFilms!!.add(item)
                                        }
                                    }

                                    withContext(Dispatchers.Main) {
                                        actorView.setBaseInfo(actor)
                                        actor.personCareerFilms?.let { actorView.setCareersList(it) }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                ExceptionHelper.catchException(e, actorView)
            }
        }
    }
}