package com.vp.detail.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vp.detail.DetailActivity
import com.vp.detail.model.MovieDetail
import com.vp.detail.model.MovieList
import com.vp.detail.service.DetailService
import retrofit2.Call
import retrofit2.Response
import javax.inject.Inject
import javax.security.auth.callback.Callback

class DetailsViewModel @Inject constructor(private val detailService: DetailService) : ViewModel() {
    private val gson = Gson()

    private val details: MutableLiveData<MovieDetail> = MutableLiveData()
    private val title: MutableLiveData<String> = MutableLiveData()
    private val loadingState: MutableLiveData<LoadingState> = MutableLiveData()

    fun title(): LiveData<String> = title

    fun details(): LiveData<MovieDetail> = details

    fun state(): LiveData<LoadingState> = loadingState

    fun fetchDetails() {
        loadingState.value = LoadingState.IN_PROGRESS
        detailService.getMovie(DetailActivity.queryProvider.getMovieId()).enqueue(object : Callback, retrofit2.Callback<MovieDetail> {
            override fun onResponse(call: Call<MovieDetail>?, response: Response<MovieDetail>?) {
                details.postValue(response?.body())

                response?.body()?.title?.let {
                    title.postValue(it)
                }

                loadingState.value = LoadingState.LOADED
            }

            override fun onFailure(call: Call<MovieDetail>?, t: Throwable?) {
                details.postValue(null)
                loadingState.value = LoadingState.ERROR
            }
        })
    }

    enum class LoadingState {
        IN_PROGRESS, LOADED, ERROR
    }

    /*
    * 5. The chosen ones. Uso de las sharedpreferences para guardar la lista de favoritos
    * */
    fun getListOfFavoriteMovies(prefs: SharedPreferences): List<MovieList> {
        val movieList: MutableList<MovieList>

        val json = prefs.getString("favMovies", "")

        movieList = when {
            json.isNullOrEmpty() -> mutableListOf()
            else -> gson.fromJson(json, object : TypeToken<List<MovieList>>() {}.type)
        }

        return movieList
    }

    fun saveFavoriteMovie(movieDetail: MovieDetail, movieID: String, prefs: SharedPreferences) {
        val movie = MovieList(movieDetail.title, movieDetail.year, movieID,movieDetail.poster)
        val movieList: MutableList<MovieList>

        val json = prefs.getString("favMovies", "")

        movieList = when {
            json.isNullOrEmpty() -> mutableListOf()
            else -> gson.fromJson(json, object : TypeToken<List<MovieList>>() {}.type)
        }

        if (!movieList.contains(movie)) {
            movieList.add(movie)
        }

        val jsonList = gson.toJson(movieList)
        prefs.edit().putString("favMovies",jsonList).apply()
    }

    fun deleteFavoriteMovie(movieDetail: MovieDetail, movieID: String, prefs: SharedPreferences) {
        val movie = MovieList(movieDetail.title, movieDetail.year, movieID,movieDetail.poster)
        val movieList: MutableList<MovieList>

        val json = prefs.getString("favMovies", "")

        movieList = when {
            json.isNullOrEmpty() -> mutableListOf()
            else -> gson.fromJson(json, object : TypeToken<List<MovieList>>() {}.type)
        }

        if (movieList.contains(movie)) {
            movieList.remove(movie)
        }

        val jsonList = gson.toJson(movieList)
        prefs.edit().putString("favMovies",jsonList).apply()
    }
}