package tr.bcxip.hummingbird;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.SwitchCompat;
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
import android.widget.TextView;
import android.widget.Toast;

import com.manuelpeinado.fadingactionbar.extras.actionbarcompat.FadingActionBarHelper;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit.RetrofitError;
import tr.bcxip.hummingbird.api.HummingbirdApi;
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
public class AnimeDetailsActivity extends ActionBarActivity {

    public static final String ARG_ID = "arg_id";

    final String TAG = "ANIME DETAILS ACTIVITY";

    ActionBar mActionBar;
    FadingActionBarHelper mActionBarHelper;

    HummingbirdApi api;
    PrefManager prefMan;

    Palette mPalette;

    LinearLayout mButtonsHolder;
    Button mViewTrailer;
    Button mAddToLibrary;
    ImageView mHeaderImage;
    TextView mType;
    TextView mGenre;
    TextView mEpisodeCount;
    TextView mEpisodeLength;
    TextView mAgeRating;
    TextView mAired;
    TextView mCommunityRating;
    TextView mSynopsis;

    ImageView mRemove;
    LinearLayout mFavoritedHolder;

    LinearLayout mLibraryHolder;
    Spinner mStatusSpinner;
    LinearLayout mEpisodesHolder;
    TextView mEpisodes;
    SwitchCompat mRewatching;
    LinearLayout mRewatchedTimesHolder;
    TextView mRewatchedTimes;
    SwitchCompat mPrivate;
    RatingBar mRatingBar;
    TextView mRatingSimple;

    String ANIME_ID;

    AnimeV2 anime;
    LibraryEntry libraryEntry;
    User user;

    String newWatchStatus;
    int newEpisodesWatched;
    boolean newIsRewatching;
    int newRewatchedTimes;
    boolean newPrivate;
    String newRating;

    int darkMutedColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActionBar = getSupportActionBar();
        api = new HummingbirdApi(this);
        prefMan = new PrefManager(this);

        mActionBar.setDisplayHomeAsUpEnabled(true);

        ANIME_ID = getIntent().getStringExtra(ARG_ID);
        new LoadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.anime_details, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_copy_title:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getResources().getString
                        (R.string.toast_copy_title), anime.getCanonicalTitle());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(
                        AnimeDetailsActivity.this,
                        getResources().getString(R.string.toast_copy_title)
                                + " \"" + anime.getCanonicalTitle() + "\"",
                        Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_share:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");

                String messageBody = getString(R.string.content_sharing_text);
                messageBody = messageBody
                        .replace("{anime-name}", anime.getCanonicalTitle())
                        .replace("{anime-url}", "https://hummingbird.me/anime/" + anime.getSlug());

                intent.putExtra(Intent.EXTRA_TEXT, messageBody);

                startActivity(Intent.createChooser(intent,
                        getResources().getString(R.string.action_share)));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        return true;
    }

    public void updateLibraryEntry(LibraryEntry entry) {
        String oldWatchStatus = entry.getStatus();
        int oldEpisodesWatched = entry.getEpisodesWatched();
        boolean oldIsRewatching = entry.isRewatching();
        int oldRewatchedTimes = entry.getNumberOfRewatches();
        boolean oldPrivate = entry.isPrivate();

        Map map = new HashMap<String, String>();

        String authToken = prefMan.getAuthToken();
        if (authToken == null || authToken.equals("") || authToken.trim().equals("")) {
            Log.e(TAG, "Authentication token was not found. Update request can't be made!");
            return;
        } else
            map.put("auth_token", authToken);

        if ((newWatchStatus != null && !newWatchStatus.equals("") && !newWatchStatus.trim().equals(""))
                && !newWatchStatus.equals(oldWatchStatus))
            map.put("status", newWatchStatus);

        if (newEpisodesWatched != oldEpisodesWatched)
            map.put("episodes_watched", newEpisodesWatched + "");

        if (newIsRewatching != oldIsRewatching)
            map.put("rewatching", newIsRewatching);

        if (newRewatchedTimes != oldRewatchedTimes)
            map.put("rewatched_times", newRewatchedTimes + "");

        if (newPrivate != oldPrivate)
            map.put("privacy", newPrivate ? "private" : "public");

        if (!newRating.equals(entry.getRating().getAdvancedRating()))
            map.put("sane_rating_update", newRating);

        if (map.size() != 0)
            new UpdateTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, map);
    }

    public void updateUpdateButtonStatus(LibraryEntry entry) {
        if (newRating == null)
            newRating = "0";

        if (!newWatchStatus.equals(entry.getStatus()) ||
                newEpisodesWatched != entry.getEpisodesWatched() ||
                newIsRewatching != entry.isRewatching() ||
                newRewatchedTimes != entry.getNumberOfRewatches() ||
                newPrivate != entry.isPrivate() ||
                !newRating.equals(entry.getRating().getAdvancedRating() != null ?
                        entry.getRating().getAdvancedRating() : "0")) {
            mAddToLibrary.setEnabled(true);
        } else
            mAddToLibrary.setEnabled(false);
    }

    /**
     * @param #0 Boolean isFirstLoad - indicates whether it's the first time we are loading the content
     *           (not a reload after a removal, for instance). Why? Because, the progress dialog
     *           has to be cancelable on first load.
     */
    protected class LoadTask extends AsyncTask<Boolean, Void, Boolean> {

        Bitmap coverBitmap = null;

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(AnimeDetailsActivity.this,
                    getString(R.string.loading),
                    getString(R.string.please_wait___),
                    true
            );

            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    LoadTask.this.cancel(false);
                    finish();
                }
            });
        }

        @Override
        protected Boolean doInBackground(Boolean... bools) {

            /* First time, cancelable dialog */
            if (bools.length != 0) {
                boolean isFirstLoad = bools[0];
                if (isFirstLoad) dialog.setCancelable(true);
            }

            try {
                if (ANIME_ID != null && !ANIME_ID.equals("") && !ANIME_ID.trim().equals("")) {
                    Log.i(TAG, "Fetching data for Anime with ID " + ANIME_ID);

                    anime = api.getAnime(ANIME_ID);

                    libraryEntry = api.getLibraryEntryIfAnimeExists(anime.getId());

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
                Resources res = getResources();
                darkMutedColor = mPalette.getDarkMutedColor(res.getColor(R.color.neutral_darker));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    getWindow().setStatusBarColor(darkMutedColor);

                mActionBarHelper = new FadingActionBarHelper()
                        .actionBarBackground(new ColorDrawable(darkMutedColor))
                        .headerLayout(R.layout.header_anime_details)
                        .headerOverlayLayout(R.layout.header_overlay_anime_details)
                        .contentLayout(R.layout.content_anime_details);
                setContentView(mActionBarHelper.createView(AnimeDetailsActivity.this));
                mActionBarHelper.initActionBar(AnimeDetailsActivity.this);

                mButtonsHolder = (LinearLayout) findViewById(R.id.anime_details_buttons_holder);
                mViewTrailer = (Button) findViewById(R.id.anime_details_view_trailer_button);
                mAddToLibrary = (Button) findViewById(R.id.anime_details_add_to_list_button);
                mHeaderImage = (ImageView) findViewById(R.id.anime_details_cover_image);
                mType = (TextView) findViewById(R.id.anime_details_type);
                mGenre = (TextView) findViewById(R.id.anime_details_genres);
                mEpisodeCount = (TextView) findViewById(R.id.anime_details_episode_count);
                mEpisodeLength = (TextView) findViewById(R.id.anime_details_episode_duration);
                mAgeRating = (TextView) findViewById(R.id.anime_details_age_rating);
                mAired = (TextView) findViewById(R.id.anime_details_aired);
                mCommunityRating = (TextView) findViewById(R.id.anime_details_community_rating);
                mSynopsis = (TextView) findViewById(R.id.anime_details_synopsis);

                mRemove = (ImageView) findViewById(R.id.header_anime_details_remove);
                mFavoritedHolder = (LinearLayout) findViewById(R.id.header_anime_details_favorited);

                mLibraryHolder = (LinearLayout) findViewById(R.id.anime_details_library_holder);
                mStatusSpinner = (Spinner) findViewById(R.id.anime_details_status_spinner);
                mEpisodesHolder = (LinearLayout) findViewById(R.id.anime_details_library_episodes_holder);
                mEpisodes = (TextView) findViewById(R.id.anime_details_library_episodes);
                mRewatching = (SwitchCompat) findViewById(R.id.anime_details_library_rewatching);
                mRewatchedTimesHolder = (LinearLayout) findViewById(R.id.anime_details_library_rewatched_holder);
                mRewatchedTimes = (TextView) findViewById(R.id.anime_details_library_rewatched);
                mPrivate = (SwitchCompat) findViewById(R.id.anime_details_library_private);
                mRatingBar = (RatingBar) findViewById(R.id.anime_details_library_rating);
                mRatingSimple = (TextView) findViewById(R.id.anime_deatails_library_rating_simple);

                mButtonsHolder.setBackgroundDrawable(new ColorDrawable(darkMutedColor));
                mAddToLibrary.setTextColor(darkMutedColor);
                mAddToLibrary.setOnClickListener(new OnAddToLibraryClickListener());

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

                int episodeCount = anime.getEpisodeCount();
                mEpisodeCount.setText(episodeCount != 0 ?
                        episodeCount + "" :
                        getString(R.string.content_unknown));

                int episodeLength = anime.getEpisodeLength();
                mEpisodeLength.setText(episodeLength != 0 ?
                        episodeLength + " " + getString(R.string.content_minutes).toLowerCase() :
                        getString(R.string.content_unknown));

                mAgeRating.setText(anime.getAgeRating());

                SimpleDateFormat airDateFormat = new SimpleDateFormat("d MMMM yyyy");

                long airStart = anime.getAiringStartDate();
                long airEnd = anime.getAiringFinishedDate();

                Date airStartDate = new Date(airStart);
                Date airEndDate = new Date(airEnd);

                Calendar todayCal = Calendar.getInstance();

                Calendar airStartCal = Calendar.getInstance();
                airStartCal.setTime(airStartDate);

                if (airStart == 0 && airEnd == 0)
                    mAired.setText(R.string.content_not_yet_aired);

                if (airStart == 0 && airEnd != 0)
                    mAired.setText(getString(R.string.content_unknown) + " " + getString(R.string.to)
                            + " " + airDateFormat.format(airEnd));

                if (airStart != 0 && airEnd == 0) {
                    if (anime.getEpisodeCount() == 1)
                        mAired.setText(airDateFormat.format(airStart));
                    else
                        mAired.setText(getString(R.string.content_airing_since) + " "
                                + airDateFormat.format(airStart));
                }

                if (airStart != 0 && airEnd != 0)
                    mAired.setText(airDateFormat.format(airStart) + " " + getString(R.string.to)
                            + " " + airDateFormat.format(airEnd));

                if (airStartCal.get(Calendar.YEAR) > todayCal.get(Calendar.YEAR)) {
                    if (anime.getEpisodeCount() == 1)
                        mAired.setText(getString(R.string.content_will_air_on) + " "
                                + airDateFormat.format(airStart));
                    else
                        mAired.setText(getString(R.string.content_will_start_airing_on) + " "
                                + airDateFormat.format(airStart));
                }

                String comRating = String.valueOf(anime.getCommunityRating());
                if (comRating.length() > 3)
                    comRating = comRating.substring(0, 4);
                else if (comRating.equals("0.0"))
                    comRating = getResources().getString(R.string.content_not_yet_rated);
                mCommunityRating.setText(comRating);

                mSynopsis.setText(anime.getSynopsis());

                displayLibraryElements();

            } else {
                Toast.makeText(AnimeDetailsActivity.this, R.string.error_cant_load_data, Toast.LENGTH_LONG).show();
                finish();
            }

            dialog.dismiss();
        }
    }

    /* If Anime exist in user library, show library related elements... */
    public void displayLibraryElements() {
        if (libraryEntry != null) {
            mRemove.setVisibility(View.VISIBLE);
            mRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CustomDialog.Builder builder = new CustomDialog.Builder(
                            AnimeDetailsActivity.this,
                            R.string.title_remove,
                            R.string.yes);
                    builder.negativeText(R.string.no);
                    builder.positiveColor(getResources().getColor(R.color.apptheme_primary));
                    String contentText = getString(R.string.content_remove_are_you_sure);
                    contentText = contentText.replace("{anime-name}", anime.getCanonicalTitle());
                    builder.content(contentText);

                    CustomDialog dialog = builder.build();

                    dialog.setClickListener(new CustomDialog.ClickListener() {
                        @Override
                        public void onConfirmClick() {
                            new RemoveTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }

                        @Override
                        public void onCancelClick() {
                                    /* empty */
                        }
                    });

                    dialog.show();
                }
            });

            for (Favorite fav : user.getFavorites())
                if (fav.getItemId().equals(ANIME_ID))
                    mFavoritedHolder.setVisibility(View.VISIBLE);

            mLibraryHolder.setVisibility(View.VISIBLE);
            mLibraryHolder.setBackgroundDrawable(new ColorDrawable(darkMutedColor));

            final String animeEpisodeCount = anime.getEpisodeCount() != 0 ? anime.getEpisodeCount() + "" : "?";

            mEpisodes.setText(libraryEntry.getEpisodesWatched() + "/" + animeEpisodeCount);

            mRewatching.setChecked(libraryEntry.isRewatching());

            mRewatchedTimes.setText(libraryEntry.getNumberOfRewatches() + "");

            mPrivate.setChecked(libraryEntry.isPrivate());

            Rating rating = libraryEntry.getRating();
            if (rating.isAdvanced()) {
                if (rating.getAdvancedRating() != null)
                    mRatingBar.setRating(Float.parseFloat(rating.getAdvancedRating()));
                else
                    mRatingBar.setRating(0);

                mRatingBar.setVisibility(View.VISIBLE);
                mRatingSimple.setVisibility(View.GONE);
            } else {
                if (rating.getSimpleRating() != null)
                    mRatingSimple.setText(rating.getSimpleRating());
                else
                    mRatingSimple.setText(Rating.RATING_SIMPLE_NEUTRAL);

                mRatingBar.setVisibility(View.GONE);
                mRatingSimple.setVisibility(View.VISIBLE);
            }

            newWatchStatus = libraryEntry.getStatus();
            newEpisodesWatched = libraryEntry.getEpisodesWatched();
            newIsRewatching = libraryEntry.isRewatching();
            newRewatchedTimes = libraryEntry.getNumberOfRewatches();
            newPrivate = libraryEntry.isPrivate();
            newRating = libraryEntry.getRating().getAdvancedRating() != null ?
                    libraryEntry.getRating().getAdvancedRating() : "0";

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
                            R.string.ok);
                    builder.negativeText(R.string.cancel);
                    builder.positiveColor(getResources().getColor(R.color.apptheme_primary));

                    CustomDialog dialog = builder.build();

                    View dialogView = getLayoutInflater().inflate(R.layout.number_picker, null);
                    final NumberPicker mNumberPicker = (NumberPicker)
                            dialogView.findViewById(R.id.number_picker);

                    dialog.setCustomView(dialogView);

                    mNumberPicker.setMaxValue(anime.getEpisodeCount() != 0 ?
                            anime.getEpisodeCount() : 1000);
                    mNumberPicker.setValue(newEpisodesWatched);
                    mNumberPicker.setWrapSelectorWheel(false);

                    dialog.setClickListener(new CustomDialog.ClickListener() {
                        @Override
                        public void onConfirmClick() {
                            newEpisodesWatched = mNumberPicker.getValue();
                            mEpisodes.setText(newEpisodesWatched + "/" + animeEpisodeCount);

                            if ((newEpisodesWatched + "").equals(animeEpisodeCount))
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
                            R.string.ok);
                    builder.negativeText(R.string.cancel);
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
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    newPrivate = isChecked;
                    updateUpdateButtonStatus(libraryEntry);
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

            mRatingSimple.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String rating = mRatingSimple.getText().toString();
                    if (rating.equals(Rating.RATING_SIMPLE_POSITIVE)) {
                        mRatingSimple.setText(Rating.RATING_SIMPLE_NEGATIVE);
                        newRating = "1";
                    }
                    if (rating.equals(Rating.RATING_SIMPLE_NEGATIVE)) {
                        mRatingSimple.setText(Rating.RATING_SIMPLE_NEUTRAL);
                        newRating = "3";
                    }
                    if (rating.equals(Rating.RATING_SIMPLE_NEUTRAL)) {
                        mRatingSimple.setText(Rating.RATING_SIMPLE_POSITIVE);
                        newRating = "5";
                    }

                    updateUpdateButtonStatus(libraryEntry);
                }
            });

            mAddToLibrary.setText(R.string.content_update);
            mAddToLibrary.setEnabled(false);
            mAddToLibrary.setOnClickListener(new OnLibraryUpdateClickListener());
        } else {
            mRemove.setVisibility(View.GONE);
            mLibraryHolder.setVisibility(View.GONE);
        }
    }

    private class OnAddToLibraryClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            new AddTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private class OnLibraryUpdateClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            updateLibraryEntry(libraryEntry);
        }
    }

    private class AddTask extends AsyncTask<Void, Void, LibraryEntry> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(AnimeDetailsActivity.this,
                    getString(R.string.adding),
                    getString(R.string.please_wait___),
                    true
            );
        }

        @Override
        protected LibraryEntry doInBackground(Void... voids) {
            Map<String, String> map = new HashMap<String, String>();
            String authToken = prefMan.getAuthToken();
            if (authToken == null || authToken.equals("") || authToken.trim().equals(""))
                return null;
            else
                map.put("auth_token", authToken);

            map.put("status", "plan-to-watch");

            try {
                return api.addUpdateLibraryEntry(ANIME_ID, map);
            } catch (RetrofitError e) {
                Log.e(TAG, e.getMessage());
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(LibraryEntry result) {
            super.onPostExecute(result);

            if (result != null) {
                libraryEntry = result;
                displayLibraryElements();
            } else {
                Toast.makeText(AnimeDetailsActivity.this,
                        R.string.error_couldnt_add_item,
                        Toast.LENGTH_LONG)
                        .show();
            }

            dialog.dismiss();
        }
    }

    private class UpdateTask extends AsyncTask<Map, Void, LibraryEntry> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(AnimeDetailsActivity.this,
                    getString(R.string.updating),
                    getString(R.string.please_wait___),
                    true
            );
        }

        @Override
        protected LibraryEntry doInBackground(Map... maps) {
            try {
                if (maps[0] != null) {
                    return api.addUpdateLibraryEntry(ANIME_ID, maps[0]);
                } else
                    return null;
            } catch (RetrofitError e) {
                Log.e(TAG, e.getMessage());
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(LibraryEntry result) {
            super.onPostExecute(result);

            if (result != null) {
                libraryEntry = result;
                newRating = result.getRating().getAdvancedRating();
                newRewatchedTimes = result.getNumberOfRewatches();
                newEpisodesWatched = result.getEpisodesWatched();
                newIsRewatching = result.isRewatching();
                newWatchStatus = result.getStatus();
                updateUpdateButtonStatus(result);

                String toastMessage = getString(R.string.info_successfully_updated);
                toastMessage = toastMessage.replace("{anime-name}", anime.getCanonicalTitle());
                Toast.makeText(AnimeDetailsActivity.this,
                        toastMessage,
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(AnimeDetailsActivity.this,
                        R.string.error_couldnt_update_item,
                        Toast.LENGTH_LONG).show();
            }

            dialog.dismiss();
        }
    }

    private class RemoveTask extends AsyncTask<Void, Void, Boolean> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(AnimeDetailsActivity.this,
                    getString(R.string.removing),
                    getString(R.string.please_wait___),
                    true
            );
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            String authToken = prefMan.getAuthToken();
            if (authToken == null || authToken.equals("") || authToken.trim().equals("")) {
                Log.e(TAG, "Authentication token not found. Can't remove library item!");
                return false;
            }
            try {
                return api.removeLibraryEntry(ANIME_ID, prefMan.getAuthToken());
            } catch (RetrofitError e) {
                Log.e(TAG, e.getMessage());
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean removed) {
            super.onPostExecute(removed);

            if (removed)
                new LoadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else
                Toast.makeText(AnimeDetailsActivity.this,
                        R.string.error_cant_remove_item, Toast.LENGTH_LONG).show();

            dialog.dismiss();
        }
    }
}
