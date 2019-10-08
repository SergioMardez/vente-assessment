package com.vp.detail

import android.content.SharedPreferences
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.LiveData
import com.vp.detail.databinding.ActivityDetailBinding
import com.vp.detail.model.MovieDetail
import com.vp.detail.model.MovieList
import com.vp.detail.viewmodel.DetailsViewModel
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject
import kotlin.run

class DetailActivity : DaggerAppCompatActivity(), QueryProvider {
    private lateinit var prefs: SharedPreferences
    private lateinit var movieList: List<MovieList>

    private var menuSelected = true
    private lateinit var detailViewModel: DetailsViewModel
    private lateinit var liveDetail: LiveData<MovieDetail>

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail)
        detailViewModel = ViewModelProviders.of(this, factory).get(DetailsViewModel::class.java)

        binding.viewModel = detailViewModel
        queryProvider = this
        binding.lifecycleOwner = this

        detailViewModel.fetchDetails()
        detailViewModel.title().observe(this, Observer {
            supportActionBar?.title = it
        })

        /*
        * 5. The chosen ones - Cargar las shared
        * */
        prefs = this.getSharedPreferences("sharedPrefs", MODE_PRIVATE) ?: error("err")
        getFavoriteMovies()

        getDetails()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detail_menu, menu)
        return true
    }

    private fun getDetails() {
        liveDetail = detailViewModel.details()
    }

    /*
    * 5. The chosen ones - Controlar la pulsación del botón de favorito, guardar el imbdID y
    * los datos necesarios.
    * */
    private fun getFavoriteMovies() {
        movieList = detailViewModel.getListOfFavoriteMovies(prefs)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        for (i in movieList.indices) {
            if (intent?.data?.getQueryParameter("imdbID")!! == movieList[i].imdbID){
                menu!!.getItem(0).setIcon(R.drawable.ic_star_selected)
                menuSelected = false
            }
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.star -> {

                val movieID = intent?.data?.getQueryParameter("imdbID")!!
                var movieDetail = MovieDetail("","","","","","")

                liveDetail.value?.let {
                    movieDetail = it
                }

                if (movieDetail.title.isNotEmpty()) {
                    menuSelected = if (menuSelected) { //Añadir pelicula a favoritos
                        item.setIcon(R.drawable.ic_star_selected)

                        detailViewModel.saveFavoriteMovie(movieDetail, movieID, prefs)

                        false
                    } else { //Borrar pelicula de favoritos
                        item.setIcon(R.drawable.ic_star)

                        detailViewModel.deleteFavoriteMovie(movieDetail, movieID, prefs)

                        true
                    }
                }

                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun getMovieId(): String {
        return intent?.data?.getQueryParameter("imdbID") ?: run {
            throw IllegalStateException("You must provide movie id to display details")
        }
    }

    companion object {
        lateinit var queryProvider: QueryProvider
    }
}
