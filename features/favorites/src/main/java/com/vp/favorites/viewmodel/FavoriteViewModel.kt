package com.vp.favorites.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vp.favorites.model.MovieList

class FavoriteViewModel: ViewModel() {
    private val gson = Gson()

    fun getListOfFavoriteMovies(prefs: SharedPreferences): List<MovieList> {
        val movieList: MutableList<MovieList>

        val json = prefs.getString("favMovies", "")

        movieList = when {
            json.isNullOrEmpty() -> mutableListOf()
            else -> gson.fromJson(json, object : TypeToken<List<MovieList>>() {}.type)
        }

        return movieList
    }
}