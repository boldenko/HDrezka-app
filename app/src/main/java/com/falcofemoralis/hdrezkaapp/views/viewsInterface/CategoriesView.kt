package com.falcofemoralis.hdrezkaapp.views.viewsInterface

import android.util.ArrayMap
import com.falcofemoralis.hdrezkaapp.interfaces.IConnection

interface CategoriesView : IConnection{
    fun setCategories(categories: ArrayMap<String, ArrayList<Pair<String, String>>>)

    fun showList()
}