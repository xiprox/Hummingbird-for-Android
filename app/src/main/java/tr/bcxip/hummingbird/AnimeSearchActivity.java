package tr.bcxip.hummingbird;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;
import tr.bcxip.hummingbird.adapters.AnimeSearchAdapter;
import tr.bcxip.hummingbird.api.HummingbirdApi;
import tr.bcxip.hummingbird.api.Results;
import tr.bcxip.hummingbird.api.objects.Anime;

/**
 * Created by ix on 11/7/14.
 */
public class AnimeSearchActivity extends ActionBarActivity {

    private static final String TAG = "ANIME SEARCH";

    private static final int FLIPPER_ITEM_INFO = 0;
    private static final int FLIPPER_ITEM_PROGRESS_BAR = 1;
    private static final int FLIPPER_ITEM_RESULTS = 2;
    private static final int FLIPPER_ITEM_ERROR = 3;

    private HummingbirdApi api;

    private Toolbar toolbar;

    private RecyclerView mResultsRecycler;
    private RecyclerView.LayoutManager mLayoutManager;
    private AnimeSearchAdapter mAdapter;

    private TextView mErrorText;
    private ViewFlipper mFlipper;
    private ProgressBar mProgressBar;

    private List<Anime> mResults = new ArrayList<Anime>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in_quick, R.anim.nothing);

        setContentView(R.layout.activity_anime_search);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        api = new HummingbirdApi(this);

        mResultsRecycler = (RecyclerView) findViewById(R.id.search_results);
        mErrorText = (TextView) findViewById(R.id.search_error_text);
        mFlipper = (ViewFlipper) findViewById(R.id.search_flipper);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        mLayoutManager = new LinearLayoutManager(this);
        mResultsRecycler.setLayoutManager(mLayoutManager);
        mResultsRecycler.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));

        mResultsRecycler.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {

            }
        });

        SearchView searchView = (SearchView) findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchFromScratch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                searchInCurrentDataSet(query);
                return false;
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.nothing, R.anim.fade_out_quick);
    }

    private void searchInCurrentDataSet(String query) {
        if (mResults.size() != 0) {
            List<Anime> tempResults = new ArrayList<Anime>();

            for (Anime anime : mResults) {
                boolean titleContainsQuery = StringUtils.containsIgnoreCase(anime.getTitle(), query);
                boolean synopsisContainsQuery = StringUtils.containsIgnoreCase(anime.getSynopsis(), query);
                boolean slugContainsQuery = StringUtils.containsIgnoreCase(anime.getSlug(), query);

                if (titleContainsQuery || synopsisContainsQuery || slugContainsQuery)
                    tempResults.add(anime);
            }

            if (tempResults.size() == 0) {
                String errorText = getString(R.string.error_no_results_found_for);
                errorText = errorText.replace("{query}", query);
                mErrorText.setText(errorText);

                mFlipper.setDisplayedChild(FLIPPER_ITEM_ERROR);
            } else {
                mAdapter = new AnimeSearchAdapter(
                        AnimeSearchActivity.this,
                        mResultsRecycler,
                        tempResults
                );
                mResultsRecycler.setAdapter(mAdapter);
                mFlipper.setDisplayedChild(FLIPPER_ITEM_RESULTS);
            }
        }
    }

    private void searchFromScratch(String query) {
        new SearchTask().execute(query);
    }

    private class SearchTask extends AsyncTask<String, Void, Integer> {

        String query;

        RetrofitError.Kind errorKind;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mFlipper.setDisplayedChild(FLIPPER_ITEM_PROGRESS_BAR);
        }

        @Override
        protected Integer doInBackground(String... strings) {
            query = strings[0];

            if (query != null) {
                try {
                    mResults = api.searchAnime(query);
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
            } else {
                this.cancel(true);
                return 0;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            if (result == Results.CODE_OK) {
                if (mResults.size() == 0) {
                    String errorText = getString(R.string.error_no_results_found_for);
                    errorText = errorText.replace("{query}", query);
                    mErrorText.setText(errorText);

                    mFlipper.setDisplayedChild(FLIPPER_ITEM_ERROR);
                } else {
                    mAdapter = new AnimeSearchAdapter(
                            AnimeSearchActivity.this,
                            mResultsRecycler,
                            mResults
                    );
                    mResultsRecycler.setAdapter(mAdapter);
                    mFlipper.setDisplayedChild(FLIPPER_ITEM_RESULTS);
                }
            } else {
                if (errorKind == RetrofitError.Kind.HTTP)
                    mErrorText.setError(result + "");
                else if (errorKind == RetrofitError.Kind.NETWORK)
                    mErrorText.setError(getString(R.string.error_connection));
                else
                    mErrorText.setError(getString(R.string.error_unknown));

                mFlipper.setDisplayedChild(FLIPPER_ITEM_ERROR);
            }
        }
    }
}
