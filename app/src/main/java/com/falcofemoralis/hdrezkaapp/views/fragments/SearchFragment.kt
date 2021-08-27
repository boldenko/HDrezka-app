package com.falcofemoralis.hdrezkaapp.views.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import com.falcofemoralis.hdrezkaapp.R
import com.falcofemoralis.hdrezkaapp.constants.DeviceType
import com.falcofemoralis.hdrezkaapp.interfaces.IConnection
import com.falcofemoralis.hdrezkaapp.interfaces.IProgressState
import com.falcofemoralis.hdrezkaapp.interfaces.OnFragmentInteractionListener
import com.falcofemoralis.hdrezkaapp.objects.SettingsData
import com.falcofemoralis.hdrezkaapp.presenters.SearchPresenter
import com.falcofemoralis.hdrezkaapp.utils.ExceptionHelper
import com.falcofemoralis.hdrezkaapp.utils.FragmentOpener
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.FilmListCallView
import com.falcofemoralis.hdrezkaapp.views.viewsInterface.SearchView

class SearchFragment : Fragment(), SearchView, FilmListCallView {
    private lateinit var currentView: View
    private lateinit var searchPresenter: SearchPresenter
    private lateinit var filmsListFragment: FilmsListFragment
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var imm: InputMethodManager
    private lateinit var hintLayout: LinearLayout
    private lateinit var fragmentListener: OnFragmentInteractionListener
    private lateinit var clearBtn: TextView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentListener = context as OnFragmentInteractionListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        currentView = inflater.inflate(R.layout.fragment_search, container, false)
        clearBtn = currentView.findViewById(R.id.fragment_search_tv_clear)

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
        searchPresenter = SearchPresenter(this, filmsListFragment, requireContext())
        searchPresenter.initFilms()
        filmsListFragment.setProgressBarState(IProgressState.StateType.LOADED)
        super.onStart()
    }

    override fun dataInited() {

    }

    private fun initSearchViews() {
        // pressed enter
        autoCompleteTextView.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                autoCompleteTextView.dismissDropDown()
                val text: String = autoCompleteTextView.text.toString()

                //проверяем ведденный текст
                if (text.isEmpty()) {
                    Toast.makeText(context, getString(R.string.enter_film_name), Toast.LENGTH_SHORT).show()
                } else {
                    hintLayout.visibility = View.GONE
                    imm.hideSoftInputFromWindow(autoCompleteTextView.windowToken, 0)
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

                    if (s.toString().isNotEmpty()) {
                        clearBtn.visibility = View.VISIBLE
                    } else {
                        clearBtn.visibility = View.GONE
                    }
                }
            }
        })

        // pressed on film
        autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
            imm.hideSoftInputFromWindow(autoCompleteTextView.windowToken, 0)
            autoCompleteTextView.dismissDropDown()
            FragmentOpener.openWithData(this, fragmentListener, searchPresenter.activeSearchFilms[position], "film")
        }

        clearBtn.setOnClickListener {
            autoCompleteTextView.setText("")
        }

        if (SettingsData.deviceType == DeviceType.TV) {
            autoCompleteTextView.setOnClickListener {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }

          //  autoCompleteTextView.requestFocus()
        }
    }

    override fun redrawSearchFilms(films: ArrayList<String>) {
        context?.let {
            autoCompleteTextView.setAdapter(ArrayAdapter(it, R.layout.search_item, R.id.text_view_list_item, films))
            autoCompleteTextView.showDropDown()

        }
    }

    override fun showConnectionError(type: IConnection.ErrorType, errorText: String) {
        ExceptionHelper.showToastError(requireContext(), type, errorText)
    }

    override fun triggerEnd() {
        searchPresenter.getNextFilms()
    }
}