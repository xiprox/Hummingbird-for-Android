package tr.bcxip.hummingbird;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.PaletteItem;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.manuelpeinado.fadingactionbar.FadingActionBarHelper;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import tr.bcxip.hummingbird.api.HummingbirdApi;
import tr.bcxip.hummingbird.api.Results;
import tr.bcxip.hummingbird.api.objects.AnimeV2;
import tr.bcxip.hummingbird.api.objects.Favorite;
import tr.bcxip.hummingbird.api.objects.LibraryEntry;
import tr.bcxip.hummingbird.api.objects.Rating;
import tr.bcxip.hummingbird.api.objects.User;
import tr.bcxip.hummingbird.managers.PrefManager;
import uk.me.lewisdeane.ldialogs.CustomDialog;

/**
 * Created by Hikari on 10/8/14.
 */
public class AnimeDetailsActivity extends Activity {

    public static final String ARG_ID = "arg_id";

    final String TAG = "ANIME DETAILS ACTIVITY";

    ActionBar mActionBar;
    FadingActionBarHelper mActionBarHelper;

    HummingbirdApi api;
    PrefManager prefMan;

    Palette mPalette;

    Button mViewTrailer;
    Button mAddToList;
    ImageView mHeaderImage;
    TextView mType;
    TextView mGenre;
    TextView mEpisodeCount;
    TextView mEpisodeLength;
    TextView mAgeRating;
    TextView mAired;
    TextView mCommunityRating;
    TextView mSynopsis;

    String ANIME_ID;

    AnimeV2 anime;

    String newWatchStatus;
    int newEpisodesWatched;
    boolean newIsRewatching;
    int newRewatchedTimes;
    String newRating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActionBar = getActionBar();
        api = new HummingbirdApi(this);
        prefMan = new PrefManager(this);

        ANIME_ID = getIntent().getStringExtra(ARG_ID);
        new LoadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.anime_details, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_copy_title) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(getResources().getString
                    (R.string.toast_copy_title), anime.getCanonicalTitle());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(
                    AnimeDetailsActivity.this,
                    getResources().getString(R.string.toast_copy_title)
                            + " \"" + anime.getCanonicalTitle() + "\"",
                    Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.action_share) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String sSlug = anime.getCanonicalTitle().toLowerCase().replaceAll
                    ("[^0-9a-z]", "-").replaceAll("-+", "-");
            String slugCheck = sSlug.substring(sSlug.length() - 1, sSlug.length());
            //check if there is "-" at the end of slug
            if (slugCheck.equals("-")) {
                sSlug = sSlug.substring(0, sSlug.length() - 1);
            }
            intent.putExtra(Intent.EXTRA_TEXT, anime.getCanonicalTitle() + " at hummingbird.me/anime/" + sSlug);
            startActivity(Intent.createChooser(intent,
                    getResources().getString(R.string.share_title)));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigateUp() {
        super.onBackPressed();
        return true;
    }

    public void updateLibraryEntry(LibraryEntry entry) {
        String oldWatchStatus = entry.getStatus();
        int oldEpisodesWatched = entry.getEpisodesWatched();
        int oldRewatchedTimes = entry.getNumberOfRewatches();

        Map map = new HashMap<String, String>();

        String authToken = prefMan.getAuthToken();
        if (authToken == null || authToken.equals("") || authToken.trim().equals("")) {
            Log.e(TAG, "Authentication token was not found. Update request can't be sent!");
            return;
        } else
            map.put("auth_token", authToken);

        if ((newWatchStatus != null && !newWatchStatus.equals("") && !newWatchStatus.trim().equals(""))
                && !newWatchStatus.equals(oldWatchStatus))
            map.put("status", newWatchStatus);

        if (newEpisodesWatched != oldEpisodesWatched)
            map.put("episodes_watched", newEpisodesWatched + "");

        map.put("rewatching", newIsRewatching);

        if (newRewatchedTimes != oldRewatchedTimes)
            map.put("rewatched_times", newRewatchedTimes + "");

        if (!newRating.equals(entry.getRating().getAdvancedRating()))
            map.put("sane_rating_update", newRating);

        new UpdateTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, map);
    }

    public void updateUpdateButtonStatus(LibraryEntry entry) {
        if (!newWatchStatus.equals(entry.getStatus()) ||
                newEpisodesWatched != entry.getEpisodesWatched() ||
                newIsRewatching != entry.isRewatching() ||
                newRewatchedTimes != entry.getNumberOfRewatches() ||
                !newRating.equals(entry.getRating().getAdvancedRating())) {
            mAddToList.setEnabled(true);
        } else
            mAddToList.setEnabled(false);
    }

    protected class LoadTask extends AsyncTask<Void, Void, Boolean> {

        Bitmap coverBitmap = null;
        LibraryEntry libraryEntry;
        User user;

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                if (ANIME_ID != null && !ANIME_ID.equals("") && !ANIME_ID.trim().equals("")) {
                    Log.i(TAG, "Fetching data for Anime with ID " + ANIME_ID);

                    anime = api.getAnime(ANIME_ID);

                    libraryEntry = api.getLibraryEntryIfAnimeExists(ANIME_ID);

                    String username = prefMan.getUsername();
                    if (username != null)
                        user = api.getUser(username);

                    coverBitmap = Picasso.with(AnimeDetailsActivity.this)
                            .load(anime.getCoverImageLink()).get();
                    mPalette = Palette.generate(coverBitmap);
                    return true;
                } else return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            if (success) {
                final PaletteItem vibrantColor = mPalette.getVibrantColor();

                mActionBarHelper = new FadingActionBarHelper()
                        .actionBarBackground(vibrantColor == null ? new ColorDrawable(R.color.neutral)
                                : new ColorDrawable(vibrantColor.getRgb()))
                        .headerLayout(R.layout.header_anime_details)
                        .headerOverlayLayout(R.layout.header_overlay_anime_details)
                        .contentLayout(R.layout.content_anime_details);
                setContentView(mActionBarHelper.createView(AnimeDetailsActivity.this));
                mActionBarHelper.initActionBar(AnimeDetailsActivity.this);

                mViewTrailer = (Button) findViewById(R.id.anime_details_view_trailer_button);
                mAddToList = (Button) findViewById(R.id.anime_details_add_to_list_button);
                mHeaderImage = (ImageView) findViewById(R.id.anime_details_cover_image);
                mType = (TextView) findViewById(R.id.anime_details_type);
                mGenre = (TextView) findViewById(R.id.anime_details_genres);
                mEpisodeCount = (TextView) findViewById(R.id.anime_details_episode_count);
                mEpisodeLength = (TextView) findViewById(R.id.anime_details_episode_duration);
                mAgeRating = (TextView) findViewById(R.id.anime_details_age_rating);
                mAired = (TextView) findViewById(R.id.anime_details_aired);
                mCommunityRating = (TextView) findViewById(R.id.anime_details_community_rating);
                mSynopsis = (TextView) findViewById(R.id.anime_details_synopsis);

                ImageView mRemove = (ImageView) findViewById(R.id.header_anime_details_remove);
                LinearLayout mFavoritedHolder = (LinearLayout) findViewById(R.id.header_anime_details_favorited);

                LinearLayout mLibraryHolder = (LinearLayout) findViewById(R.id.anime_details_library_holder);
                final Spinner mStatusSpinner = (Spinner) findViewById(R.id.anime_details_status_spinner);
                LinearLayout mEpisodesHolder = (LinearLayout) findViewById(R.id.anime_details_library_episodes_holder);
                final TextView mEpisodes = (TextView) findViewById(R.id.anime_details_library_episodes);
                Switch mRewatching = (Switch) findViewById(R.id.anime_details_library_rewatching);
                LinearLayout mRewatchedTimesHolder = (LinearLayout) findViewById(R.id.anime_details_library_rewatched_holder);
                final TextView mRewatchedTimes = (TextView) findViewById(R.id.anime_details_library_rewatched);
                Switch mPrivate = (Switch) findViewById(R.id.anime_details_library_private);
                RatingBar mRatingBar = (RatingBar) findViewById(R.id.anime_details_library_rating);
                TextView mRatingSimple = (TextView) findViewById(R.id.anime_deatails_library_rating_simple);

                mViewTrailer.getBackground().setColorFilter(vibrantColor.getRgb(), PorterDuff.Mode.SRC_ATOP);
                mViewTrailer.setTextColor(vibrantColor.getRgb());
                mAddToList.getBackground().setColorFilter(vibrantColor.getRgb(), PorterDuff.Mode.SRC_ATOP);
                mAddToList.setOnClickListener(new OnAddToListClickListener());

                if (anime.getTrailer() == null || anime.getTrailer().equals(""))
                    mViewTrailer.setVisibility(View.GONE);

                mViewTrailer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://www.youtube.com/watch?v=" + anime.getTrailer())));
                    }
                });

                Point size = new Point();
                getWindowManager().getDefaultDisplay().getSize(size);
                mHeaderImage.getLayoutParams().height = (size.y / 3) * 2;

                mHeaderImage.setImageDrawable(new BitmapDrawable(coverBitmap));

                mActionBar.setTitle(anime.getCanonicalTitle());

                mType.setText(anime.getType());

                String genres = "";
                for (int i = 0; i < anime.getGenres().size(); i++) {
                    if (i == 0)
                        genres = anime.getGenres().get(i);
                    else
                        genres += ", " + anime.getGenres().get(i);
                }
                mGenre.setText(genres);

                mEpisodeCount.setText(anime.getEpisodeCount() + "");

                mEpisodeLength.setText(anime.getEpisodeLength() + " " + getString(R.string.content_minutes));

                mAgeRating.setText(anime.getAgeRating());

                String finishDate = String.valueOf(anime.getAiringFinishedDate());
                if (finishDate.equals("null"))
                    finishDate = getResources().getString(R.string.finish_date);
                mAired.setText(anime.getAiringStartDate() + " - " + finishDate);

                String comRating = String.valueOf(anime.getCommunityRating());
                if (comRating.length() > 3)
                    comRating = comRating.substring(0, 4);
                else if (comRating.equals("0.0"))
                    comRating = getResources().getString(R.string.not_rated);
                mCommunityRating.setText(comRating);

                mSynopsis.setText(anime.getSynopsis());

                /* Anime exist in user library. Show library related elements... */
                if (libraryEntry != null) {
                    mRemove.setVisibility(View.VISIBLE);
                    mRemove.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // TODO - Remove entry
                        }
                    });

                    for (Favorite fav : user.getFavorites())
                        if (fav.getItemId().equals(ANIME_ID))
                            mFavoritedHolder.setVisibility(View.VISIBLE);

                    mLibraryHolder.setVisibility(View.VISIBLE);
                    mLibraryHolder.setBackgroundDrawable(vibrantColor != null ?
                                    new ColorDrawable(vibrantColor.getRgb()) :
                                    new ColorDrawable(getResources().getColor(R.color.neutral))
                    );

                    mEpisodes.setText(libraryEntry.getEpisodesWatched() + "/" + anime.getEpisodeCount());

                    mRewatching.setChecked(libraryEntry.isRewatching());

                    mRewatchedTimes.setText(libraryEntry.getNumberOfRewatches() + "");

                    mPrivate.setChecked(libraryEntry.isPrivate());

                    Rating rating = libraryEntry.getRating();
                    if (rating.isAdvanced() && rating.getAdvancedRating() != null) {
                        mRatingBar.setRating(Float.parseFloat(rating.getAdvancedRating()));
                        mRatingBar.setVisibility(View.VISIBLE);
                        mRatingSimple.setVisibility(View.GONE);
                    } else {
                        mRatingBar.setVisibility(View.GONE);
                        mRatingSimple.setVisibility(View.VISIBLE);
                        mRatingSimple.setText(rating.getSimpleRating());
                    }

                    newWatchStatus = libraryEntry.getStatus();
                    newEpisodesWatched = libraryEntry.getEpisodesWatched();
                    newIsRewatching = libraryEntry.isRewatching();
                    newRewatchedTimes = libraryEntry.getNumberOfRewatches();
                    newRating = libraryEntry.getRating().getAdvancedRating();

                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                            AnimeDetailsActivity.this,
                            R.array.library_watch_status_items,
                            R.layout.item_spinner_library_status);
                    adapter.setDropDownViewResource(R.layout.item_spinner_item_library_status);
                    mStatusSpinner.setAdapter(adapter);

                    String watchStatus = libraryEntry.getStatus();
                    if (watchStatus.equals("currently-watching"))
                        mStatusSpinner.setSelection(0);
                    if (watchStatus.equals("plan-to-watch"))
                        mStatusSpinner.setSelection(1);
                    if (watchStatus.equals("completed"))
                        mStatusSpinner.setSelection(2);
                    if (watchStatus.equals("on-hold"))
                        mStatusSpinner.setSelection(3);
                    if (watchStatus.equals("dropped"))
                        mStatusSpinner.setSelection(4);

                    mEpisodesHolder.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            CustomDialog.Builder builder = new CustomDialog.Builder(
                                    AnimeDetailsActivity.this,
                                    R.string.content_episodes,
                                    android.R.string.ok);
                            builder.negativeText(android.R.string.cancel);
                            builder.positiveColor(getResources().getColor(R.color.apptheme_primary));

                            CustomDialog dialog = builder.build();

                            View dialogView = getLayoutInflater().inflate(R.layout.number_picker, null);
                            final NumberPicker mNumberPicker = (NumberPicker)
                                    dialogView.findViewById(R.id.number_picker);

                            dialog.setCustomView(dialogView);

                            mNumberPicker.setMaxValue(anime.getEpisodeCount());
                            mNumberPicker.setValue(newEpisodesWatched);
                            mNumberPicker.setWrapSelectorWheel(false);

                            dialog.setClickListener(new CustomDialog.ClickListener() {
                                @Override
                                public void onConfirmClick() {
                                    newEpisodesWatched = mNumberPicker.getValue();
                                    mEpisodes.setText(newEpisodesWatched + "/" + anime.getEpisodeCount());

                                    if (newEpisodesWatched == anime.getEpisodeCount())
                                        mStatusSpinner.setSelection(2); // (completed)

                                    updateUpdateButtonStatus(libraryEntry);
                                }

                                @Override
                                public void onCancelClick() {
                                    /* empty */
                                }
                            });

                            dialog.show();
                        }
                    });

                    mRewatching.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                            newIsRewatching = isChecked;
                            updateUpdateButtonStatus(libraryEntry);
                        }
                    });

                    mRewatchedTimesHolder.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            CustomDialog.Builder builder = new CustomDialog.Builder(
                                    AnimeDetailsActivity.this,
                                    R.string.content_rewatched,
                                    android.R.string.ok);
                            builder.negativeText(android.R.string.cancel);
                            builder.positiveColor(getResources().getColor(R.color.apptheme_primary));

                            CustomDialog dialog = builder.build();

                            View dialogView = getLayoutInflater().inflate(R.layout.number_picker, null);
                            final NumberPicker mNumberPicker = (NumberPicker)
                                    dialogView.findViewById(R.id.number_picker);

                            dialog.setCustomView(dialogView);

                            mNumberPicker.setMaxValue(200);
                            mNumberPicker.setValue(newRewatchedTimes);
                            mNumberPicker.setWrapSelectorWheel(false);

                            dialog.setClickListener(new CustomDialog.ClickListener() {
                                @Override
                                public void onConfirmClick() {
                                    newRewatchedTimes = mNumberPicker.getValue();
                                    mRewatchedTimes.setText(newRewatchedTimes + "");
                                    updateUpdateButtonStatus(libraryEntry);
                                }

                                @Override
                                public void onCancelClick() {
                                    /* empty */
                                }
                            });

                            dialog.show();
                        }
                    });

                    mPrivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            /* I couldn't find any parameter in the API for setting privacy.
                            *  Well, changing the privacy value doesn't mean a thing to us as we can
                            *  only fetch the user's library anonymously.
                            *
                            *  Keeping the code here in case we happen to figure a way in the future.
                            * */
                        }
                    });

                    mStatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                            switch (position) {
                                case 0:
                                    newWatchStatus = "currently-watching";
                                    break;
                                case 1:
                                    newWatchStatus = "plan-to-watch";
                                    break;
                                case 2:
                                    newWatchStatus = "completed";
                                    break;
                                case 3:
                                    newWatchStatus = "on-hold";
                                    break;
                                case 4:
                                    newWatchStatus = "dropped";
                                    break;
                            }

                            updateUpdateButtonStatus(libraryEntry);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                    mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                        @Override
                        public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                            newRating = rating + "";
                            updateUpdateButtonStatus(libraryEntry);
                        }
                    });

                    mAddToList.setText(R.string.content_update);
                    mAddToList.setEnabled(false);
                    mAddToList.setOnClickListener(new OnLibraryUpdateClickListener());
                }

            } else {
                Toast.makeText(AnimeDetailsActivity.this, R.string.error_cant_load_data, Toast.LENGTH_LONG).show();
                finish();
            }
        }

        private class OnAddToListClickListener implements View.OnClickListener {

            @Override
            public void onClick(View view) {
                Toast.makeText(
                        AnimeDetailsActivity.this,
                        "Adding " + anime.getCanonicalTitle() + " to list :P",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }

        private class OnLibraryUpdateClickListener implements View.OnClickListener {
            @Override
            public void onClick(View view) {
                updateLibraryEntry(libraryEntry);
                Toast.makeText(AnimeDetailsActivity.this, "Updating :P", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class UpdateTask extends AsyncTask<Map, Void, String> {

        LibraryEntry resultEntry;

        @Override
        protected String doInBackground(Map... maps) {
            try {
                if (maps[0] != null) {
                    resultEntry = api.addUpdateLibraryEntry(ANIME_ID, maps[0]);
                    return Results.RESULT_SUCCESS;
                } else
                    return Results.RESULT_FAILURE;
            } /*catch (RetrofitError e) {
                Log.e(TAG, e.getMessage());

                if (e.getMessage().equals(Results.RESULT_UNAUTHORIZED)) {
                    Log.e(TAG, "Wrong authentication token; request failed!");
                    return Results.RESULT_UNAUTHORIZED;
                }

                return Results.RESULT_EXCEPTION;
            }*/ catch (Exception e) {
                e.printStackTrace();
                return Results.RESULT_EXCEPTION;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result.equals(Results.RESULT_SUCCESS)) {
                // TODO - Do something :P
            }
        }
    }
}
