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
import tr.bcxip.hummingbird.adapters.LibraryAdapter;
import tr.bcxip.hummingbird.api.HummingbirdApi;
import tr.bcxip.hummingbird.api.Results;
import tr.bcxip.hummingbird.api.objects.LibraryEntry;
import tr.xip.widget.errorview.ErrorView;

/**
 * Created by Hikari on 10/14/14.
 */
public class LibraryTabFragment extends Fragment implements ErrorView.RetryListener {

    private static final String TAG = "LIBRARY TAB FRAGMENT";

    public static final String ARG_LIBRARY_FILTER = "library_filter";
    public static final String ARG_USERNAME = "username";

    Context context;
    HummingbirdApi api;

    List<LibraryEntry> mLibrary;

    LoadTask loadTask;

    GridView mGrid;
    ViewFlipper mFlipper;
    ErrorView mErrorView;

    String USERNAME;
    String FILTER;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        api = new HummingbirdApi(context);

        USERNAME = getArguments().getString(ARG_USERNAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_library_tab, container, false);

        Bundle args = getArguments();

        if (args != null)
            FILTER = args.getString(LibraryTabFragment.ARG_LIBRARY_FILTER);

        mGrid = (GridView) rootView.findViewById(R.id.library_grid);
        mFlipper = (ViewFlipper) rootView.findViewById(R.id.library_flipper);
        mErrorView = (ErrorView) rootView.findViewById(R.id.library_error_view);
        mErrorView.setOnRetryListener(this);

        loadTask = new LoadTask();
        loadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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
                mLibrary = api.getLibrary(USERNAME, FILTER);
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
                mGrid.setAdapter(new LibraryAdapter(context, R.layout.item_library, mLibrary));

                mGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        Intent intent = new Intent(context, AnimeDetailsActivity.class);
                        intent.putExtra(AnimeDetailsActivity.ARG_ID,
                                mLibrary.get(position).getAnime().getId());
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
