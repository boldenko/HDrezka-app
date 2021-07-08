package com.BSLCommunity.onlinefilmstracker.views.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import androidx.recyclerview.widget.RecyclerView
import com.BSLCommunity.onlinefilmstracker.R
import com.BSLCommunity.onlinefilmstracker.presenters.SearchPresenter
import com.BSLCommunity.onlinefilmstracker.viewsInterface.SearchView

class SearchFragment : Fragment(), SearchView {
    private lateinit var currentView: View
    private lateinit var searchPresenter: SearchPresenter
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var imm: InputMethodManager
    private lateinit var hintLayout: LinearLayout
    private lateinit var filmsLayout: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        currentView = inflater.inflate(R.layout.fragment_search, container, false)
        searchPresenter = SearchPresenter(this)

        autoCompleteTextView = currentView.findViewById(R.id.fragment_search_act_suggest)
        imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        hintLayout = currentView.findViewById(R.id.fragment_search_ll_hint)
        filmsLayout = currentView.findViewById(R.id.fragment_search_rv_films)
        initSearch()

        return currentView
    }

    private fun initSearch() {
        // pressed enter
        autoCompleteTextView.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                autoCompleteTextView.dismissDropDown()
                val text: String = autoCompleteTextView.text.toString()

                //проверяем ведденный текст
                if (text.isNotEmpty()) {
                    Toast.makeText(context, "Enter film name!", Toast.LENGTH_SHORT).show()
                } else {
                    hintLayout.visibility = View.GONE
                    // http://hdrezka.tv/search/?do=search&subaction=search&q=fgkgd
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
                searchPresenter.fetchFilms(s.toString())
            }
        })

        // pressed on film
        autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
            imm.hideSoftInputFromWindow(autoCompleteTextView.windowToken, 0)
        }
    }

    override fun redrawSearchFilms(films: ArrayList<String>) {
        context?.let {
            autoCompleteTextView.setAdapter(ArrayAdapter(it, R.layout.search_item, R.id.text_view_list_item, films))
            autoCompleteTextView.showDropDown()

        }
    }

    override fun setFilms() {
        TODO("Not yet implemented")
    }
}