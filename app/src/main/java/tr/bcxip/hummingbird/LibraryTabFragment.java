package tr.bcxip.hummingbird;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

/**
 * Created by Hikari on 10/14/14.
 */
public class LibraryTabFragment extends Fragment {

    private static final String TAG = "LIBRARY TAB FRAGMENT";

    public static final String ARG_LIBRARY_FILTER = "library_filter";
    public static final String ARG_USERNAME = "username";

    Context context;
    HummingbirdApi api;

    List<LibraryEntry> mLibrary;

    LoadTask loadTask;

    GridView mGrid;
    ViewFlipper mFlipper;

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

        loadTask = new LoadTask();
        loadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (loadTask != null) loadTask.cancel(true);
    }

    private class LoadTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try {
                mLibrary = api.getLibrary(USERNAME, FILTER);
                return Results.RESULT_SUCCESS;
            } catch (RetrofitError e) {
                Log.e(TAG, e.getMessage());
                return e.getMessage();
            } catch (Exception e) {
                e.printStackTrace();
                return Results.RESULT_EXCEPTION;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result.equals(Results.RESULT_SUCCESS)) {
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
                // TODO - Handle failure
            }
        }
    }
}
