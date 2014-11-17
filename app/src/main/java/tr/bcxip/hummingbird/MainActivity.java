package tr.bcxip.hummingbird;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

import tr.bcxip.hummingbird.managers.PrefManager;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private static final String STATE_ACTIONBAR_TITLE = "action_bar_title";

    PrefManager prefMan;

    private NavigationDrawerFragment mNavigationDrawerFragment;

    public static CharSequence mTitle;

    Toolbar toolbar;

    View mStatusBarBackground;

    Map<Integer, String> tags = new HashMap<Integer, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefMan = new PrefManager(this);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tags.put(1000, ProfileFragment.FRAGMENT_TAG_PROFILE);
        tags.put(1, LibraryFragment.FRAGMENT_TAG_LIBRARY);

        mStatusBarBackground = findViewById(R.id.status_bar_placeholder);

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
                    R.id.navigation_drawer_holder,
                    (DrawerLayout) findViewById(R.id.drawer_layout));
        }

        if (savedInstanceState != null) {
            mTitle = savedInstanceState.getString(STATE_ACTIONBAR_TITLE);
            setTitle(mTitle.toString());
        }
    }

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
                    .replace(R.id.container, fragment,
                            tags.get(position) != null ? tags.get(position) : "")
                    .commit();

        Resources res = getResources();

        if (toolbar != null)
            toolbar.setBackgroundColor(res.getColor(R.color.apptheme_primary));

        setStatusBarBackgroundColor(res.getColor(R.color.apptheme_primary_dark));
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
        return true;
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public void setStatusBarBackgroundColor(int color) {
        if (mNavigationDrawerFragment != null)
            mNavigationDrawerFragment.setStatusBarColor(color);
        if (mStatusBarBackground != null)
            mStatusBarBackground.setBackgroundColor(color);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search_anime:
                startActivity(new Intent(this, AnimeSearchActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_ACTIONBAR_TITLE, mTitle.toString());
    }
}
