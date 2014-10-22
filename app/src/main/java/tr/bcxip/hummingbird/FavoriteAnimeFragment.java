package tr.bcxip.hummingbird;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ViewFlipper;

import java.util.List;

import retrofit.RetrofitError;
import tr.bcxip.hummingbird.adapters.FavoriteAnimeAdapter;
import tr.bcxip.hummingbird.api.HummingbirdApi;
import tr.bcxip.hummingbird.api.Results;
import tr.bcxip.hummingbird.api.objects.FavoriteAnime;
import tr.bcxip.hummingbird.managers.PrefManager;
import tr.bcxip.hummingbird.widget.ExpandableHeightGridView;
import tr.xip.widget.errorview.ErrorView;

/**
 * Created by Hikari on 10/22/14.
 */
public class FavoriteAnimeFragment extends Fragment implements ErrorView.RetryListener {

    public static final String ARG_USERNAME = "username";

    private static final String TAG = "FAVORITE ANIME FRAGMENT";

    Context context;
    HummingbirdApi api;
    PrefManager prefMan;

    View rootView;

    String username;

    List<FavoriteAnime> favsList;

    GridView mFavorites;
    ViewFlipper mFlipper;
    ErrorView mErrorView;

    LoadTask loadTask;

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
        rootView = inflater.inflate(R.layout.fragment_favorite_anime, null);

        /*
        * Check if any username argument is passed. If one is passed, load the data for it;
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

        mFavorites = (GridView) rootView.findViewById(R.id.favorite_anime_grid);
        mFlipper = (ViewFlipper) rootView.findViewById(R.id.favorite_anime_flipper);
        mErrorView = (ErrorView) rootView.findViewById(R.id.favorite_anime_error_view);
        mErrorView.setOnRetryListener(this);

        executeLoadTask();

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (loadTask != null)
            loadTask.cancel(true);
    }

    @Override
    public void onRetry() {
        executeLoadTask();
    }

    private void executeLoadTask() {
        if (loadTask != null && !loadTask.isCancelled())
            loadTask.cancel(true);

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
                favsList = api.getFavoriteAnime(username);
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
                if (favsList != null && favsList.size() != 0)
                    mFavorites.setAdapter(new FavoriteAnimeAdapter(context, R.layout.item_favorite_grid, favsList));

                mFavorites.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        Intent intent = new Intent(context, AnimeDetailsActivity.class);
                        intent.putExtra(AnimeDetailsActivity.ARG_ID, favsList.get(position).getId());
                        context.startActivity(intent);
                    }
                });

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
