package tr.bcxip.hummingbird;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
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
import tr.xip.widget.errorview.ErrorView;

/**
 * Created by Hikari on 10/11/14.
 */
public class FeedFragment extends Fragment implements ErrorView.RetryListener {

    public static final String ARG_USERNAME = "username";

    final String TAG = "FEED";

    Context context;
    HummingbirdApi api;
    PrefManager prefMan;

    ListView mList;
    ViewFlipper mFlipper;
    ErrorView mErrorView;

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

        mErrorView = (ErrorView) rootView.findViewById(R.id.feed_error_view);
        mErrorView.setOnRetryListener(this);

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
    }

    @Override
    public void onRetry() {
        if (loadTask != null && !loadTask.isCancelled())
            loadTask.cancel(false);

        loadTask = new LoadTask();
        loadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                mItems = api.getFeed(username);
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
                mList.setAdapter(new FeedAdapter(context, R.layout.item_story_comment, mItems, username));
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
                }
                if (mFlipper.getDisplayedChild() == 1)
                    mFlipper.showNext();
            }
        }
    }
}