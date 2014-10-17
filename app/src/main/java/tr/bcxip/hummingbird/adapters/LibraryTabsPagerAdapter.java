package tr.bcxip.hummingbird.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;

import tr.bcxip.hummingbird.LibraryFragment;
import tr.bcxip.hummingbird.LibraryTabFragment;
import tr.bcxip.hummingbird.R;

/**
 * Created by Hikari on 10/14/14.
 */
public class LibraryTabsPagerAdapter extends FragmentStatePagerAdapter {

    Context context;

    public LibraryTabsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        Bundle args = new Bundle();

        switch (position) {
            case 0:
                break;
            case 1:
                args.putString(LibraryTabFragment.ARG_LIBRARY_FILTER,
                        LibraryFragment.FILTER_CURRENTLY_WATCHING);
                break;
            case 2:
                args.putString(LibraryTabFragment.ARG_LIBRARY_FILTER,
                        LibraryFragment.FILTER_PLAN_TO_WATCH);
                break;
            case 3:
                args.putString(LibraryTabFragment.ARG_LIBRARY_FILTER,
                        LibraryFragment.FILTER_COMPLETED);
                break;
            case 4:
                args.putString(LibraryTabFragment.ARG_LIBRARY_FILTER,
                        LibraryFragment.FILTER_ON_HOLD);
                break;
            case 5:
                args.putString(LibraryTabFragment.ARG_LIBRARY_FILTER,
                        LibraryFragment.FILTER_DROPPED);
                break;
        }

        LibraryTabFragment fragment = new LibraryTabFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getCount() {
        return 6;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return getString(R.string.content_library_all);
            case 1:
                return getString(R.string.content_library_watching);
            case 2:
                return getString(R.string.content_library_planned);
            case 3:
                return getString(R.string.content_library_completed);
            case 4:
                return getString(R.string.content_library_on_hold);
            case 5:
                return getString(R.string.content_library_dropped);
            default:
                return "";
        }
    }

    private String getString(int res) {
        return context.getString(res);
    }
}
