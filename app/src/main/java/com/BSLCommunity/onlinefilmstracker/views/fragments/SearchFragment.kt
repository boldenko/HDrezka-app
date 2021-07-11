package com.BSLCommunity.onlinefilmstracker.views.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.BSLCommunity.onlinefilmstracker.R
import com.BSLCommunity.onlinefilmstracker.presenters.SearchPresenter
import com.BSLCommunity.onlinefilmstracker.utils.FragmentOpener
import com.BSLCommunity.onlinefilmstracker.views.interfaces.OnFragmentInteractionListener
import com.BSLCommunity.onlinefilmstracker.viewsInterface.FilmListCallView
import com.BSLCommunity.onlinefilmstracker.viewsInterface.SearchView

class SearchFragment : Fragment(), SearchView, FilmListCallView {
    private lateinit var currentView: View
    private lateinit var searchPresenter: SearchPresenter
    private lateinit var filmsListFragment: FilmsListFragment
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var imm: InputMethodManager
    private lateinit var hintLayout: LinearLayout
    private lateinit var fragmentListener: OnFragmentInteractionListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentListener = context as OnFragmentInteractionListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        currentView = inflater.inflate(R.layout.fragment_search, container, false)
        Log.d("FRAGMENT_TEST", "search init")

        filmsListFragment = FilmsListFragment()
        filmsListFragment.setCallView(this)
        childFragmentManager.beginTransaction().replace(R.id.fragment_search_fcv_container, filmsListFragment).commit()

        autoCompleteTextView = currentView.findViewById(R.id.fragment_search_act_suggest)
        imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        hintLayout = currentView.findViewById(R.id.fragment_search_ll_hint)

        initSearchViews()

        return currentView
    }

    override fun onFilmsListCreated() {
        searchPresenter = SearchPresenter(this, filmsListFragment)
        searchPresenter.initFilms()
        filmsListFragment.setProgressBarState(false)
        super.onStart()
    }

    private fun initSearchViews() {
        // pressed enter
        autoCompleteTextView.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                autoCompleteTextView.dismissDropDown()
                val text: String = autoCompleteTextView.text.toString()

                Log.d("TEXT_TEST", text)
                //проверяем ведденный текст
                if (text.isEmpty()) {
                    Toast.makeText(context, "Enter film name!", Toast.LENGTH_SHORT).show()
                } else {
                    hintLayout.visibility = View.GONE
                    searchPresenter.setQuery(text)
                }
            }
            false
        }

        // entered text
        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!autoCompleteTextView.isPerformingCompletion) {
                    searchPresenter.getFilms(s.toString())
                }
            }
        })

        // pressed on film
        autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
            imm.hideSoftInputFromWindow(autoCompleteTextView.windowToken, 0)
            autoCompleteTextView.dismissDropDown()
            FragmentOpener.openFilm(searchPresenter.activeSearchFilms[position], this, fragmentListener)
        }
    }

    override fun redrawSearchFilms(films: ArrayList<String>) {
        context?.let {
            autoCompleteTextView.setAdapter(ArrayAdapter(it, R.layout.search_item, R.id.text_view_list_item, films))
            autoCompleteTextView.showDropDown()

        }
    }

    override fun triggerEnd() {
        searchPresenter.getNextFilms()
    }
}