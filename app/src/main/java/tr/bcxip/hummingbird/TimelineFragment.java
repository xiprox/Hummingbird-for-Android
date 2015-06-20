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
import tr.xip.errorview.ErrorView;

/**
 * Created by Hikari on 10/22/14.
 */
public class TimelineFragment extends Fragment implements ErrorView.RetryListener,
        FeedTimelineAdapter.NewPageListener {

    private static final String TAG = "TIMELINE";

    private static final int FLIPPER_ITEM_PROGRESS = 0;
    private static final int FLIPPER_ITEM_LIST = 1;
    private static final int FLIPPER_ITEM_ERROR = 2;

    Context context;
    HummingbirdApi api;
    PrefManager prefMan;

    View rootView;

    InfiniteScrollListView mList;
    ViewFlipper mFlipper;
    ErrorView mErrorView;

    List<Story> mItems = new ArrayList<Story>();

    FeedTimelineAdapter adapter;

    LoadTask loadTask;

    int page = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        api = new HummingbirdApi(context);
        prefMan = new PrefManager(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_timeline, null);

        LayoutInflater layoutInflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mList = (InfiniteScrollListView) rootView.findViewById(R.id.timeline_list);
        mList.setLoadingView(layoutInflater.inflate(R.layout.loading_view, null));
        mList.setLoadingMode(InfiniteScrollListView.LoadingMode.SCROLL_TO_BOTTOM);
        mList.setStopPosition(InfiniteScrollListView.StopPosition.REMAIN_UNCHANGED);

        adapter = new FeedTimelineAdapter(context, mItems, this);
        adapter.setLoadingMode(InfiniteScrollListView.LoadingMode.SCROLL_TO_BOTTOM);
        adapter.setStopPosition(InfiniteScrollListView.StopPosition.REMAIN_UNCHANGED);

        mList.setAdapter(adapter);

        mFlipper = (ViewFlipper) rootView.findViewById(R.id.timeline_view_flipper);

        mErrorView = (ErrorView) rootView.findViewById(R.id.timeline_error_view);
        mErrorView.setOnRetryListener(this);

        exceuteLoadTask();

        return rootView;
    }

    private void exceuteLoadTask() {
        loadTask = new LoadTask();
        loadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onRetry() {
        if (loadTask != null && !loadTask.isCancelled())
            loadTask.cancel(false);
        exceuteLoadTask();
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
                    mTempList = api.getTimeline(prefMan.getAuthToken(), page);
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
                    mErrorView.setSubtitle(R.string.error_connection);
                else
                    mErrorView.setSubtitle(R.string.error_unknown);

                mFlipper.setDisplayedChild(FLIPPER_ITEM_ERROR);
            }

            if (adapter != null && !reachedEnd) adapter.unlock();
            mList.removeLoadingView(mList, mList.getLoadingView());
        }
    }
}
