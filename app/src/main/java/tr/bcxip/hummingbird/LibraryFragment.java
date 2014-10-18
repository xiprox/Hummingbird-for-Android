package tr.bcxip.hummingbird;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;

import tr.bcxip.hummingbird.adapters.LibraryTabsPagerAdapter;
import tr.bcxip.hummingbird.managers.PrefManager;

/**
 * Created by Hikari on 10/14/14.
 */
public class LibraryFragment extends Fragment {

    /* DON'T MODIFY!
     * Should be same as the Hummingbird API...
     * */
    public static final String FILTER_CURRENTLY_WATCHING = "currently-watching";
    public static final String FILTER_PLAN_TO_WATCH = "plan-to-watch";
    public static final String FILTER_COMPLETED = "completed";
    public static final String FILTER_ON_HOLD = "on-hold";
    public static final String FILTER_DROPPED = "dropped";

    public static final String ARG_USERNAME = "username";

    Context context;
    PrefManager prefMan;

    ViewPager mViewPager;
    PagerSlidingTabStrip mTabs;

    String username;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        prefMan = new PrefManager(context);

        /* Check if any username argument is passed. If one is passed, load the library for it;
        * if not, load for the currently logged in user.
        * */
        Bundle args = getArguments();
        String argUsername = args != null ? args.getString(ARG_USERNAME) : null;
        username = argUsername != null ? argUsername : prefMan.getUsername();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_library, null);

        mViewPager = (ViewPager) rootView.findViewById(R.id.library_view_pager);
        mViewPager.setOffscreenPageLimit(5);
        mViewPager.setAdapter(new LibraryTabsPagerAdapter(context, getFragmentManager(), username));

        mTabs = (PagerSlidingTabStrip) rootView.findViewById(R.id.library_tabs);
        mTabs.setViewPager(mViewPager);

        /* Select default item (Currently Watching) */
        mViewPager.setCurrentItem(1);

        return rootView;
    }
}
