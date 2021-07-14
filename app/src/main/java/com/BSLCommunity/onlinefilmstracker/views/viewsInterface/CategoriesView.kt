package com.BSLCommunity.onlinefilmstracker.views.viewsInterface

import android.util.ArrayMap
import com.BSLCommunity.onlinefilmstracker.interfaces.IConnection

interface CategoriesView : IConnection{
    fun setCategories(categories: ArrayMap<String, ArrayList<Pair<String, String>>>)

    fun showList()
}