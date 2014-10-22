package tr.bcxip.hummingbird.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import tr.bcxip.hummingbird.FavoriteAnimeFragment;
import tr.bcxip.hummingbird.FeedFragment;
import tr.bcxip.hummingbird.ProfileFragment;
import tr.bcxip.hummingbird.R;
import tr.bcxip.hummingbird.UserInfoFragment;

/**
 * Created by Hikari on 10/22/14.
 */
public class ProfileTabsPagerAdapter extends FragmentStatePagerAdapter {

    Context context;

    String username;

    public ProfileTabsPagerAdapter(Context context, FragmentManager fm, String username) {
        super(fm);
        this.context = context;
        this.username = username;
    }

    @Override
    public Fragment getItem(int position) {
        Bundle args = new Bundle();
        Fragment fragment = null;

        switch (position) {
            case 0:
                fragment = new UserInfoFragment();
                break;
            case 1:
                fragment = new FeedFragment();
                break;
            case 2:
                fragment = new FavoriteAnimeFragment();
        }

        args.putString(ProfileFragment.ARG_USERNAME, username);

        if (fragment != null)
            fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return getString(R.string.title_info);
            case 1:
                return getString(R.string.title_feed);
            case 2:
                return getString(R.string.content_favorite_anime);
            default:
                return "";
        }
    }

    private String getString(int res) {
        return context.getString(res);
    }
}
