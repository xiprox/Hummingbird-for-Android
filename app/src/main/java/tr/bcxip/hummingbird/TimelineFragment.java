package tr.bcxip.hummingbird;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;
import tr.bcxip.hummingbird.adapters.FeedAdapter;
import tr.bcxip.hummingbird.api.HummingbirdApi;
import tr.bcxip.hummingbird.api.Results;
import tr.bcxip.hummingbird.api.objects.Story;
import tr.bcxip.hummingbird.managers.PrefManager;
import tr.xip.widget.errorview.ErrorView;

/**
 * Created by Hikari on 10/22/14.
 */
public class TimelineFragment extends Fragment implements ErrorView.RetryListener {

    private static final String TAG = "TIMELINE";

    Context context;
    HummingbirdApi api;
    PrefManager prefMan;

    View rootView;

    ListView mList;
    ViewFlipper mFlipper;
    ErrorView mErrorView;

    List<Story> mItems = new ArrayList<Story>();

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
        rootView = inflater.inflate(R.layout.fragment_timeline, null);

        mList = (ListView) rootView.findViewById(R.id.timeline_list);
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

    private class LoadTask extends AsyncTask<Void, Void, Integer> {

        RetrofitError.Kind errorKind;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mFlipper.getDisplayedChild() == 1) mFlipper.showPrevious();
            if (mFlipper.getDisplayedChild() == 2) {
                mFlipper.showPrevious();
                mFlipper.showPrevious();
            }
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                List<Story> mTempList;

                int page = 1;
                mTempList = api.getTimeline(prefMan.getAuthToken(), page);
                while (mTempList.size() != 0) {
                    mItems.addAll(mTempList);
                    page++;
                    mTempList = api.getTimeline(prefMan.getAuthToken(), page);
                    Log.d("", "ADDED MORE");
                }

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
                FeedAdapter adapter = new FeedAdapter(context, R.layout.item_story_comment, mItems);
                mList.setAdapter(adapter);
                if (mFlipper.getDisplayedChild() == 0) mFlipper.showNext();
            } else {
                if (errorKind == RetrofitError.Kind.HTTP)
                    mErrorView.setError(result);
                else if (errorKind == RetrofitError.Kind.NETWORK)
                    mErrorView.setErrorDetail(R.string.error_connection);
                else
                    mErrorView.setErrorDetail(R.string.error_unknown);

                if (mFlipper.getDisplayedChild() == 0) {
                    mFlipper.showNext();
                    mFlipper.showNext();
                } else if (mFlipper.getDisplayedChild() == 1)
                    mFlipper.showNext();
            }
        }
    }
}
