package com.BSLCommunity.onlinefilmstracker.views.viewsInterface

import android.util.ArrayMap

interface CategoriesView {
    fun setCategories(categories: ArrayMap<String, ArrayList<Pair<String, String>>>)

    fun showList()
}