package tr.bcxip.hummingbird;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;

import tr.bcxip.hummingbird.adapters.ProfileTabsPagerAdapter;
import tr.bcxip.hummingbird.managers.PrefManager;

/**
 * Created by Hikari on 10/22/14.
 */
public class ProfileFragment extends Fragment implements UserInfoFragment.CoverColorListener {

    public static final String FRAGMENT_TAG_PROFILE = "profile_fragment";

    public static final String ARG_USERNAME = "username";

    Context context;
    PrefManager prefMan;

    ViewPager mViewPager;
    PagerSlidingTabStrip mTabs;

    String username;
    int color;

    FeedFragment mFeedFragment;
    UserInfoFragment mUserInfoFragment;
    FavoriteAnimeFragment mFavoriteAnimeFragment;

    boolean shouldColorBars = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        prefMan = new PrefManager(context);

        /*
         * Check if any username is passed to the fragment. If passed, load data for that user;
         * if not, load for the currently logged in user.
         */
        if (getArguments() != null) {
            String receivedUsername = getArguments().getString(ARG_USERNAME);
            if (receivedUsername != null && !receivedUsername.equals("") && !receivedUsername.trim().equals(""))
                username = receivedUsername;
            else
                username = prefMan.getUsername();
        } else
            username = prefMan.getUsername();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, null);

        mViewPager = (ViewPager) rootView.findViewById(R.id.profile_view_pager);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(new ProfileTabsPagerAdapter(context, getFragmentManager(), username));

        ProfileTabsPagerAdapter pagerAdapter = (ProfileTabsPagerAdapter) mViewPager.getAdapter();
        mUserInfoFragment = (UserInfoFragment) pagerAdapter.getItem(0);
        mFeedFragment = (FeedFragment) pagerAdapter.getItem(1);
        mFavoriteAnimeFragment = (FavoriteAnimeFragment) pagerAdapter.getItem(2);

        mTabs = (PagerSlidingTabStrip) rootView.findViewById(R.id.profile_tabs);
        mTabs.setViewPager(mViewPager);

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        shouldColorBars = false;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onColorObtained(int color) {
        this.color = color;
        mTabs.setBackgroundColor(color);

        if (shouldColorBars) {
            try {
                ((ActionBarActivity) context).getSupportActionBar()
                        .setBackgroundDrawable(new ColorDrawable(color));
            } catch (Exception e) {
                /* empty */
            }

            if (context instanceof ProfileActivity)
                ((ProfileActivity) context).setStatusBarBackgroundColor(color);
            else if (context instanceof MainActivity)
                ((MainActivity) context).setStatusBarBackgroundColor(color);
        }
    }
}
