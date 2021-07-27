package com.falcofemoralis.hdrezkaapp.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.interfaces.IConnection
import com.falcofemoralis.hdrezkaapp.objects.Actor
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.presenters.ActorPresenter
import com.falcofemoralis.hdrezkaapp.utils.ExceptionHelper
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.ActorView
import com.squareup.picasso.Picasso

class ActorFragment : Fragment(), ActorView {
    private lateinit var currentView: View
    private lateinit var actorPresenter: ActorPresenter
    private val ACTOR_ARG = "actor"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        currentView = inflater.inflate(R.layout.fragment_actor, container, false)

        actorPresenter = ActorPresenter(this, (arguments?.getSerializable(ACTOR_ARG) as Actor?)!!)
        actorPresenter.initActorData()

        return currentView
    }

    override fun setBaseInfo(actor: Actor) {
        currentView.findViewById<TextView>(R.id.fragment_actor_films_tv_name).text = actor.name
        currentView.findViewById<TextView>(R.id.fragment_actor_films_tv_name_orig).text = actor.nameOrig
        Picasso.get().load(actor.photo).into(currentView.findViewById<ImageView>(R.id.fragment_actor_films_iv_photo))
        currentView.findViewById<TextView>(R.id.fragment_actor_films_tv_career).text = getString(R.string.career, actor.careers)

        if(actor.age != null){
            actor.birthday += " (${actor.age})"
        }

        if(actor.diedOnAge != null){
            actor.deathday += " (${getString(R.string.death_in)} ${actor.diedOnAge})"
        }

        setInfo(actor.birthday, R.id.fragment_actor_films_tv_borndate, R.string.birthdate)
        setInfo(actor.birthplace, R.id.fragment_actor_films_tv_bornplace, R.string.birthplace)
        setInfo(actor.deathday, R.id.fragment_actor_films_tv_dieddate, R.string.deathdate)
        setInfo(actor.deathplace, R.id.fragment_actor_films_tv_diedplace, R.string.deathplace)
    }

    private fun setInfo(data: String?, viewId: Int, textId: Int){
        val v = currentView.findViewById<TextView>(viewId)
        if(data != null){
            v.text = getString(textId, data)
        } else{
            v.visibility = View.GONE
        }
    }

    override fun showConnectionError(type: IConnection.ErrorType) {
        ExceptionHelper.showToastError(requireContext(), type)
    }
}