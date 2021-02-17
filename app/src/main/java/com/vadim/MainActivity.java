package com.vadim;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;
import com.vadim.fragment.FavouriteQrDataListFragment;
import com.vadim.fragment.PermissionFragment;
import com.vadim.fragment.SearcherQrDataListFragment;
import com.vadim.presenter.json.JsonUtil;
import com.vadim.qr_code.R;
import com.vadim.ui.UtilsUI;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int STORAGE_PERMISSION_ALL = 1;
    private static final int FIRST_SELECTED_ELEMENT_OF_NAVIGATION_VIEW = 0;

    private ActionBar actionBar;
    private ActionBarDrawerToggle drawerToggle;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.navigation_view)
    NavigationView navigationView;

    private UtilsUI.UtilsFragment searcherFragment;
    private Bundle savedInstanceState;
    private SearchView searchView;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        this.savedInstanceState = savedInstanceState;
        setActionBar();
        if (savedInstanceState == null) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE )
                    != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE },
                        STORAGE_PERMISSION_ALL);
                actionBar.hide();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.qrDataListFragment, new PermissionFragment())
                        .commit();
            } else {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.qrDataListFragment, new SearcherQrDataListFragment())
                        .commit();
            }
        }
        navigationView.setNavigationItemSelectedListener(this);
        initSelectedElementOfNavigationView();
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                null, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_ALL &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            navigationView.setNavigationItemSelectedListener(this);
            initSelectedElementOfNavigationView();
            JsonUtil.createJsonFile(this, JsonUtil.NAME_OF_FAVOURITE_FILE);
            JsonUtil.createJsonFile(this, JsonUtil.NAME_OF_INDEX_FILE);
            JsonUtil.createJsonFile(this, JsonUtil.NAME_OF_QR_DATA_FILE);
            setActionBar();
            drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                    null, R.string.drawer_open,
                    R.string.drawer_close);
            drawerLayout.addDrawerListener(drawerToggle);
            actionBar.show();
            getSupportFragmentManager()
                    .beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.qrDataListFragment, new SearcherQrDataListFragment())
                    .commit();
        }
    }

    private void initSelectedElementOfNavigationView() {
        Menu menu = navigationView.getMenu();
        MenuItem menuItem = menu.getItem(FIRST_SELECTED_ELEMENT_OF_NAVIGATION_VIEW);
        menuItem.setChecked(true);
    }

    private void setActionBar() {
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.app_name);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "Start Create Options Menu");
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.searchQrData);
        searchView = (SearchView) menuItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        if (savedInstanceState != null) {
            Boolean focusable = (Boolean) savedInstanceState.get("focusable");
            Object searchViewText = savedInstanceState.get("searchViewText");
            if (focusable != null && searchViewText != null && focusable) {
                searchView.setQuery(String.valueOf(searchViewText), true);
                searchView.setIconified(false);
            }
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searcherFragment = getSearcherFragment();
                Filter.FilterListener filterListener = getFilterListener(searcherFragment);
                searcherFragment.filter(query, filterListener);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searcherFragment = getSearcherFragment();
                Filter.FilterListener filterListener = getFilterListener(searcherFragment);
                searcherFragment.filter(newText, filterListener);
                return false;
            }
        });
        return true;
    }

    private Filter.FilterListener getFilterListener(UtilsUI.UtilsFragment searcherFragment) {
        return count -> {
            if (count == 0) {
                searcherFragment.displayEmptyListView();
            } else {
                searcherFragment.displayListView();
            }
        };
    }

    private UtilsUI.UtilsFragment getSearcherFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        Fragment searcherFragment = fragments.get(0);
        if (searcherFragment instanceof PermissionFragment)
            return new UtilsUI.UtilsFragment() {
                @Override
                public void filter(String text, Filter.FilterListener filterListener) { }

                @Override
                public void displayEmptyListView() { }

                @Override
                public void displayListView() { }
            };
        return (UtilsUI.UtilsFragment) searcherFragment;
    }

    @Override
    public void onBackPressed() {
        if (searchView != null && !searchView.isIconified()) {
            searchView.setIconified(true);
        }
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        super.onBackPressed();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (searchView != null) {
            outState.putBoolean("focusable", searchView.requestFocus());
            outState.putString("searchViewText", String.valueOf(searchView.getQuery()));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView);
            } else {
                drawerLayout.openDrawer(navigationView);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.main_page) {
            actionBar.setTitle(R.string.app_name);
            startQrDataListFragment(new SearcherQrDataListFragment());
        } else if (itemId == R.id.favorites) {
            actionBar.setTitle(R.string.name_of_favourites_page);
            startQrDataListFragment(new FavouriteQrDataListFragment());
        } else if (itemId == R.id.settings) {
            item.setCheckable(false);
            startSettingsActivity();
        } else if (itemId == R.id.share) {
            item.setCheckable(false);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startQrDataListFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.qrDataListFragment, fragment)
                .commit();
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

}