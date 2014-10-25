package tr.bcxip.hummingbird;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import tr.bcxip.hummingbird.managers.PrefManager;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private static final String STATE_ACTIONBAR_TITLE = "action_bar_title";

    PrefManager prefMan;

    private NavigationDrawerFragment mNavigationDrawerFragment;

    public static CharSequence mTitle;

    Toolbar toolbar;

    SearchFragment searchFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefMan = new PrefManager(this);

        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        /**
         * Check if the user's authenticated. If not, send them to login...
         */
        if (!prefMan.isAuthenticated()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            mNavigationDrawerFragment.setUp(
                    R.id.navigation_drawer,
                    (DrawerLayout) findViewById(R.id.drawer_layout));
        }

        if (savedInstanceState != null) {
            mTitle = savedInstanceState.getString(STATE_ACTIONBAR_TITLE);
            setTitle(mTitle.toString());
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = null;

        switch (position) {
            case 1000:
                fragment = new ProfileFragment();
                mTitle = getString(R.string.title_profile);
                break;
            case 0:
                fragment = new TimelineFragment();
                mTitle = getString(R.string.title_timeline);
                break;
            case 1:
                fragment = new LibraryFragment();
                mTitle = getString(R.string.title_library);
                break;
        }

        if (fragment != null)
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment, ProfileFragment.FRAGMENT_TAG_PROFILE)
                    .commit();

        if (toolbar != null)
            toolbar.setBackgroundColor(getResources().getColor(R.color.apptheme_primary));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(getResources().getColor(R.color.apptheme_primary_dark));
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        restoreActionBar();

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint(getString(R.string.search_hint));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                createSearchFragment(searchFragment, query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (searchFragment == null)
                    searchFragment = createSearchFragment(searchFragment, query);
                else
                    updateSearchFragment(searchFragment, query);

                return false;
            }
        });

        return true;
    }

    private SearchFragment createSearchFragment(SearchFragment fragment, String query) {
        Bundle args = new Bundle();
        args.putString(SearchFragment.ARG_SEARCH_TYPE, SearchFragment.SEARCH_TYPE_GENERAL);
        args.putString(SearchFragment.ARG_QUERY, query);

        fragment = new SearchFragment();
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, SearchFragment.TAG_SEARCH_FRAGMENT)
                .addToBackStack(SearchFragment.TAG_SEARCH_FRAGMENT)
                .commit();

        return fragment;
    }

    private void updateSearchFragment(SearchFragment fragment, String query) {
//        fragment.updateResults(query);
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_ACTIONBAR_TITLE, mTitle.toString());
    }
}
