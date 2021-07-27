package com.falcofemoralis.hdrezkaapp.views.fragments

import android.content.Context
import android.os.Bundle
import android.util.ArrayMap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.interfaces.IConnection
import com.falcofemoralis.hdrezkaapp.interfaces.OnFragmentInteractionListener
import com.falcofemoralis.hdrezkaapp.objects.Actor
import com.falcofemoralis.hdrezkaapp.objects.Film
import com.falcofemoralis.hdrezkaapp.presenters.ActorPresenter
import com.falcofemoralis.hdrezkaapp.utils.ExceptionHelper
import com.falcofemoralis.hdrezkaapp.utils.FragmentOpener
import com.falcofemoralis.hdrezkaapp.views.adapters.FilmsListRecyclerViewAdapter
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.ActorView
import com.squareup.picasso.Picasso

class ActorFragment : Fragment(), ActorView {
    private lateinit var currentView: View
    private lateinit var actorPresenter: ActorPresenter
    private lateinit var fragmentListener: OnFragmentInteractionListener
    private val ACTOR_ARG = "actor"

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentListener = context as OnFragmentInteractionListener
    }


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

        if (actor.age != null) {
            actor.birthday += " (${actor.age})"
        }

        if (actor.diedOnAge != null) {
            actor.deathday += " (${getString(R.string.death_in)} ${actor.diedOnAge})"
        }

        setInfo(actor.birthday, R.id.fragment_actor_films_tv_borndate, R.string.birthdate)
        setInfo(actor.birthplace, R.id.fragment_actor_films_tv_bornplace, R.string.birthplace)
        setInfo(actor.deathday, R.id.fragment_actor_films_tv_dieddate, R.string.deathdate)
        setInfo(actor.deathplace, R.id.fragment_actor_films_tv_diedplace, R.string.deathplace)
    }

    private fun setInfo(data: String?, viewId: Int, textId: Int) {
        val v = currentView.findViewById<TextView>(viewId)
        if (data != null) {
            v.text = getString(textId, data)
        } else {
            v.visibility = View.GONE
        }
    }

    override fun setCareersList(careers: ArrayList<Pair<String, ArrayList<Film>>>) {
        val container: LinearLayout = currentView.findViewById(R.id.fragment_actor_ll_films)

        for (career in careers) {
            val layout = layoutInflater.inflate(R.layout.inflate_actor_career_layout, null)

            layout.findViewById<TextView>(R.id.career_header).text = career.first
            val recyclerView: RecyclerView = layout.findViewById(R.id.career_films)
            recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
            recyclerView.adapter = FilmsListRecyclerViewAdapter(requireContext(), career.second, ::listCallback)

            container.addView(layout)
        }
    }

    override fun showConnectionError(type: IConnection.ErrorType) {
        ExceptionHelper.showToastError(requireContext(), type)
    }

    private fun listCallback(film: Film) {
        FragmentOpener.openWithData(this, fragmentListener, film, "film")
    }
}