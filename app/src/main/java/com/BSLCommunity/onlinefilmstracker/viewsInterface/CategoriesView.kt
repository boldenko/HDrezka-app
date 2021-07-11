package com.BSLCommunity.onlinefilmstracker.viewsInterface

import android.util.ArrayMap

interface CategoriesView {
    fun setCategories(categories: ArrayMap<String, ArrayList<Pair<String, String>>>)

    fun showList()
}