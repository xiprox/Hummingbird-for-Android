package tr.bcxip.hummingbird;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.List;

import ca.weixiao.widget.InfiniteScrollListView;
import retrofit.RetrofitError;
import tr.bcxip.hummingbird.adapters.FeedTimelineAdapter;
import tr.bcxip.hummingbird.api.HummingbirdApi;
import tr.bcxip.hummingbird.api.Results;
import tr.bcxip.hummingbird.api.objects.Story;
import tr.bcxip.hummingbird.managers.PrefManager;
import tr.xip.widget.errorview.ErrorView;

/**
 * Created by Hikari on 10/11/14.
 */
public class FeedFragment extends Fragment implements ErrorView.RetryListener,
        FeedTimelineAdapter.NewPageListener {

    public static final String ARG_USERNAME = "username";

    private static final int FLIPPER_ITEM_PROGRESS = 0;
    private static final int FLIPPER_ITEM_LIST = 1;
    private static final int FLIPPER_ITEM_ERROR = 2;

    private static final String TAG = "FEED";

    Context context;
    HummingbirdApi api;
    PrefManager prefMan;

    InfiniteScrollListView mList;
    ViewFlipper mFlipper;
    ErrorView mErrorView;

    List<Story> mItems = new ArrayList<Story>();

    FeedTimelineAdapter adapter;

    String username;

    LoadTask loadTask;

    int page = 1;

    ProfileFragment parent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        api = new HummingbirdApi(context);
        prefMan = new PrefManager(context);

        try {
            parent = (ProfileFragment) getFragmentManager()
                    .findFragmentByTag(ProfileFragment.FRAGMENT_TAG_PROFILE);
        } catch (Exception e) {
            /* empty */
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_feed, null);

        LayoutInflater layoutInflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mList = (InfiniteScrollListView) rootView.findViewById(R.id.feed_list);
        mList.setLoadingView(layoutInflater.inflate(R.layout.loading_view, null));
        mList.setLoadingMode(InfiniteScrollListView.LoadingMode.SCROLL_TO_BOTTOM);
        mList.setStopPosition(InfiniteScrollListView.StopPosition.REMAIN_UNCHANGED);

        adapter = new FeedTimelineAdapter(context, mItems, this);
        adapter.setLoadingMode(InfiniteScrollListView.LoadingMode.SCROLL_TO_BOTTOM);
        adapter.setStopPosition(InfiniteScrollListView.StopPosition.REMAIN_UNCHANGED);

        mList.setAdapter(adapter);

        mFlipper = (ViewFlipper) rootView.findViewById(R.id.feed_view_flipper);

        mErrorView = (ErrorView) rootView.findViewById(R.id.feed_error_view);
        mErrorView.setOnRetryListener(this);

        /**
         * Check for any username arguments being passed to the fragment. If one is passed, we will
         * load the feed for that username. If no argument is passed, we load the feed for the
         * currently logged in user.
         */
        if (getArguments() != null && getArguments().getString(ARG_USERNAME) != null) {
            username = getArguments().getString(ARG_USERNAME);
            executeLoadTask();
        } else if (prefMan.getUsername() != null) {
            username = prefMan.getUsername();
            executeLoadTask();
        } else
            Log.e(TAG, "Username not found! Is there a problem with the logged user?");

        return rootView;
    }

    private void executeLoadTask() {
        loadTask = new LoadTask();
        loadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (loadTask != null) loadTask.cancel(true);
    }

    @Override
    public void onRetry() {
        if (loadTask != null && !loadTask.isCancelled())
            loadTask.cancel(false);

        loadTask = new LoadTask();
        loadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onScrollNext() {
        new LoadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class LoadTask extends AsyncTask<Void, Void, Integer> {

        RetrofitError.Kind errorKind;

        boolean reachedEnd;

        List<Story> mTempList = new ArrayList<Story>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (page == 1 || mFlipper.getDisplayedChild() == FLIPPER_ITEM_ERROR)
                mFlipper.setDisplayedChild(FLIPPER_ITEM_PROGRESS);

            if (adapter != null) adapter.lock();
            mList.addLoadingView(mList, mList.getLoadingView());
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                mTempList = api.getFeed(username, page);
                if (mTempList.size() != 0) page++;
                else reachedEnd = true;

                return Results.CODE_OK;
            } catch (RetrofitError e) {
                errorKind = e.getKind();

                if (e.getKind() == RetrofitError.Kind.NETWORK) {
                    Log.e(TAG, e.getMessage());
                    return Results.CODE_NETWORK_ERROR;
                } else if (e.getKind() == RetrofitError.Kind.HTTP) {
                    Log.e(TAG, e.getMessage());
                    return e.getResponse().getStatus();
                } else {
                    Log.e(TAG, e.getMessage());
                    return Results.CODE_UNKNOWN;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return Results.CODE_UNKNOWN;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            if (result == Results.CODE_OK) {
                if (adapter != null)
                    adapter.addEntries(mTempList);

                mFlipper.setDisplayedChild(FLIPPER_ITEM_LIST);
            } else {
                if (errorKind == RetrofitError.Kind.HTTP)
                    mErrorView.setError(result);
                else if (errorKind == RetrofitError.Kind.NETWORK)
                    mErrorView.setErrorDetail(R.string.error_connection);
                else
                    mErrorView.setErrorDetail(R.string.error_unknown);

                mFlipper.setDisplayedChild(FLIPPER_ITEM_ERROR);
            }

            if (adapter != null && !reachedEnd) adapter.unlock();
            mList.removeLoadingView(mList, mList.getLoadingView());
        }
    }
}