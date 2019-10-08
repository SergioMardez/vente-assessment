package com.vp.list;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.vp.list.viewmodel.SearchResult;
import com.vp.list.viewmodel.ListViewModel;

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

import static android.widget.Toast.LENGTH_SHORT;

public class ListFragment extends Fragment implements GridPagingScrollListener.LoadMoreItemsListener, ListAdapter.OnItemClickListener {
    public static final String TAG = "ListFragment";
    private static final String CURRENT_QUERY = "current_query";

    @Inject
    ViewModelProvider.Factory factory;

    private ListViewModel listViewModel;
    private GridPagingScrollListener gridPagingScrollListener;
    private ListAdapter listAdapter;
    private ViewAnimator viewAnimator;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView errorTextView;
    private String currentQuery = "Interview";

    /*
     * 4. Some refreshments - Añadido un botón que sólo aparece cuando hay un error y desaparece
     * al cargarse la lista
     */
    private Button reloadData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSupportInjection.inject(this);
        listViewModel = ViewModelProviders.of(this, factory).get(ListViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerView);
        viewAnimator = view.findViewById(R.id.viewAnimator);
        progressBar = view.findViewById(R.id.progressBar);
        errorTextView = view.findViewById(R.id.errorText);

        /*
         * 4. Some refreshments - Añadido un botón que sólo aparece cuando hay un error y desaparece
         * al cargarse la lista
         */
        reloadData = view.findViewById(R.id.reloadButton);

        if (savedInstanceState != null) {
            currentQuery = savedInstanceState.getString(CURRENT_QUERY);
        }

        initBottomNavigation(view);
        initList();
        listViewModel.observeMovies().observe(this, searchResult -> {
            if (searchResult != null) {
                handleResult(listAdapter, searchResult);
            }
        });
        listViewModel.searchMoviesByTitle(currentQuery, 1);
        showProgressBar();

        /*
         * 4. Some refreshments - El listener del boton, hace una nueva llamada al API
         */
        reloadData.setOnClickListener(v -> listViewModel.searchMoviesByTitle(currentQuery, 1));
    }

    private void initBottomNavigation(@NonNull View view) {
        BottomNavigationView bottomNavigationView = view.findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.favorites) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("app://movies/favorites"));
                intent.setPackage(requireContext().getPackageName());
                startActivity(intent);
            }
            return true;
        });
    }

    private void initList() {
        listAdapter = new ListAdapter();
        listAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(),
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 3);
        recyclerView.setLayoutManager(layoutManager);

        // Pagination
        gridPagingScrollListener = new GridPagingScrollListener(layoutManager);
        gridPagingScrollListener.setLoadMoreItemsListener(this);
        recyclerView.addOnScrollListener(gridPagingScrollListener);
    }

    private void showProgressBar() {
        viewAnimator.setDisplayedChild(viewAnimator.indexOfChild(progressBar));
    }

    private void showList() {
        /*
         * 4. Some refreshments - Se oculta si está visible
         */
        if (reloadData.getVisibility() == View.VISIBLE) {
            reloadData.setVisibility(View.GONE);
        }

        viewAnimator.setDisplayedChild(viewAnimator.indexOfChild(recyclerView));
    }

    private void showError() {
        viewAnimator.setDisplayedChild(viewAnimator.indexOfChild(errorTextView));

        /*
         * 4. Some refreshments - Aparece al recibir un error
         */
        reloadData.setVisibility(View.VISIBLE);
    }

    private void handleResult(@NonNull ListAdapter listAdapter, @NonNull SearchResult searchResult) {
        switch (searchResult.getListState()) {
            case LOADED: {
                setItemsData(listAdapter, searchResult);
                showList();
                break;
            }
            case IN_PROGRESS: {
                showProgressBar();
                break;
            }
            default: {
                showError();
            }
        }
        gridPagingScrollListener.markLoading(false);
    }

    private void setItemsData(@NonNull ListAdapter listAdapter, @NonNull SearchResult searchResult) {
        listAdapter.setItems(searchResult.getItems());

        if (searchResult.getTotalResult() <= listAdapter.getItemCount()) {
            gridPagingScrollListener.markLastPage(true);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CURRENT_QUERY, currentQuery);
    }

    @Override
    public void loadMoreItems(int page) {
        gridPagingScrollListener.markLoading(true);
        listViewModel.searchMoviesByTitle(currentQuery, page);
    }

    public void submitSearchQuery(@NonNull final String query) {
        currentQuery = query;
        listAdapter.clearItems();
        listViewModel.searchMoviesByTitle(query, 1);
        showProgressBar();
    }

    @Override
    public void onItemClick(String imdbID) {
        /*
         * 2. The Lost Event - Redirigir a la página de detalle con el id correcto
         */
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("app://movies/detail?imdbID=" + imdbID));
        intent.setPackage(requireContext().getPackageName());
        startActivity(intent);
    }
}
