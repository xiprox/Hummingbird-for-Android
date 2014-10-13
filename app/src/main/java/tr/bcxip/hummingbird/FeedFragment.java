package tr.bcxip.hummingbird;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
public class FeedFragment extends Fragment {

    public static final String ARG_USERNAME = "username";

    final String TAG = "FEED";

    Context context;
    HummingbirdApi api;
    PrefManager prefMan;

    ListView mList;
    ViewFlipper mFlipper;

    List<Story> mItems;

    String username;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        api = new HummingbirdApi(context);
        prefMan = new PrefManager(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_feed, null);

        mList = (ListView) rootView.findViewById(R.id.feed_list);
        mFlipper = (ViewFlipper) rootView.findViewById(R.id.feed_view_flipper);

        /**
         * Check for any username arguments being passed to the fragment. If one is passed, we will
         * load the feed for that username. If no argument is passed, we load the feed for the
         * currently logged in user.
         */
        if (getArguments() != null && getArguments().getString(ARG_USERNAME) != null)
            username = getArguments().getString(ARG_USERNAME);
        else if (prefMan.getUsername() != null) {
            username = prefMan.getUsername();
            new LoadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else
            Log.e(TAG, "Username not found! Is there a problem with the logged user?");

        return rootView;
    }

    protected class LoadTask extends AsyncTask<Void, Void, String> {

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

            if (result.equals(Results.RESULT_SUCCESS)) {
                mList.setAdapter(new FeedAdapter(context, R.layout.item_story_comment, mItems, username));
                if (mFlipper.getDisplayedChild() == 0) mFlipper.showNext();
            }
        }
    }
}
