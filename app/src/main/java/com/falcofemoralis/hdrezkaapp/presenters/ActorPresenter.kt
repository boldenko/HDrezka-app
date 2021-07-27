package com.falcofemoralis.hdrezkaapp.presenters

import com.falcofemoralis.hdrezkaapp.models.ActorModel
import com.falcofemoralis.hdrezkaapp.objects.Actor
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

                withContext(Dispatchers.Main) {
                    actorView.setBaseInfo(actor)
                }
            } catch (e: Exception) {
                ExceptionHelper.catchException(e, actorView)
            }
        }
    }
}