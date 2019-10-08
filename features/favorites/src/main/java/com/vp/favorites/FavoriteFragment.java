package com.vp.favorites;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vp.favorites.model.MovieList;
import com.vp.favorites.viewmodel.FavoriteViewModel;

import java.util.List;

/*
 * 5. The chosen ones - Este fragmento funciona de forma similar a la lista. Recoge los favoritos
 * de las shared y los muestra en una lista igual que en la busqueda.
 * */

public class FavoriteFragment extends Fragment implements FavoriteAdapter.OnItemClickListener {
    public static final String TAG = "FavoriteFragment";
    private SharedPreferences preferences;

    private FavoriteViewModel viewModel;
    private RecyclerView recyclerView;
    private FavoriteAdapter listAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(FavoriteViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        preferences = this.getActivity().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);

        return inflater.inflate(R.layout.activity_favorite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerViewFavorite);

        initList();

        loadList();
    }

    @Override
    public void onResume() {
        super.onResume();

        loadList(); //Recargar la lista despúes de ir a la vista de detalle
    }

    private void initList() {
        listAdapter = new FavoriteAdapter();
        listAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(),
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 3);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void loadList() {
        List<MovieList> list = viewModel.getListOfFavoriteMovies(preferences);

        listAdapter.clearItems();
        listAdapter.setItems(list);
    }

    @Override
    public void onItemClick(String imdbID) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("app://movies/detail?imdbID=" + imdbID));
        intent.setPackage(requireContext().getPackageName());
        startActivity(intent);
    }
}
