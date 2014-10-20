package tr.bcxip.hummingbird;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ViewFlipper;

import java.util.List;

import retrofit.RetrofitError;
import tr.bcxip.hummingbird.adapters.FeedAdapter;
import tr.bcxip.hummingbird.api.HummingbirdApi;
import tr.bcxip.hummingbird.api.Results;
import tr.bcxip.hummingbird.api.objects.Story;
import tr.bcxip.hummingbird.managers.PrefManager;

/**
 * Created by Hikari on 10/11/14.
 */
public class FeedFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String ARG_USERNAME = "username";

    final String TAG = "FEED";

    Context context;
    HummingbirdApi api;
    PrefManager prefMan;

    ListView mList;
    ViewFlipper mFlipper;
    SwipeRefreshLayout mSwipeRefresh;

    List<Story> mItems;

    String username;

    LoadTask loadTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        api = new HummingbirdApi(context);
        prefMan = new PrefManager(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_feed, null);

        mList = (ListView) rootView.findViewById(R.id.feed_list);
        mFlipper = (ViewFlipper) rootView.findViewById(R.id.feed_view_flipper);

        mSwipeRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.feed_swipe_refresh);
        mSwipeRefresh.setOnRefreshListener(this);

        Resources res = getResources();
        mSwipeRefresh.setColorSchemeColors(res.getColor(R.color.apptheme_primary),
                res.getColor(R.color.apptheme_primary_dark));

        /**
         * Check for any username arguments being passed to the fragment. If one is passed, we will
         * load the feed for that username. If no argument is passed, we load the feed for the
         * currently logged in user.
         */
        if (getArguments() != null && getArguments().getString(ARG_USERNAME) != null)
            username = getArguments().getString(ARG_USERNAME);
        else if (prefMan.getUsername() != null) {
            username = prefMan.getUsername();
            loadTask = new LoadTask();
            loadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else
            Log.e(TAG, "Username not found! Is there a problem with the logged user?");

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (loadTask != null) loadTask.cancel(true);
        if (mSwipeRefresh != null && mSwipeRefresh.isRefreshing())
            mSwipeRefresh.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        if (loadTask != null && !loadTask.isCancelled())
            loadTask.cancel(false);

        loadTask = new LoadTask();
        loadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class LoadTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mSwipeRefresh != null) mSwipeRefresh.setRefreshing(true);
            if (mFlipper.getDisplayedChild() == 1) mFlipper.showPrevious();
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                mItems = api.getFeed(username);
                return Results.RESULT_SUCCESS;
            } catch (RetrofitError e) {
                Log.e(TAG, e.getMessage());
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result.equals(Results.RESULT_SUCCESS))
                mList.setAdapter(new FeedAdapter(context, R.layout.item_story_comment, mItems, username));

            if (mFlipper.getDisplayedChild() == 0) mFlipper.showNext();
            if (mSwipeRefresh != null) mSwipeRefresh.setRefreshing(false);
        }
    }
}
