package tr.bcxip.hummingbird;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
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
public class AnimeDetailsActivity extends Activity {

    public static final String ARG_ID = "arg_id";

    final String TAG = "ANIME DETAILS ACTIVITY";

    ActionBar mActionBar;
    FadingActionBarHelper mActionBarHelper;

    HummingbirdApi api;
    PrefManager prefMan;

    Palette mPalette;

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
    View mLibraryHolderShadow;
    Spinner mStatusSpinner;
    LinearLayout mEpisodesHolder;
    TextView mEpisodes;
    Switch mRewatching;
    LinearLayout mRewatchedTimesHolder;
    TextView mRewatchedTimes;
    Switch mPrivate;
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
    String newRating;

    PaletteItem vibrantColor;

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
        boolean oldIsRewatching = entry.isRewatching();

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

        if (!newRating.equals(entry.getRating().getAdvancedRating()))
            map.put("sane_rating_update", newRating);

        if (map.size() != 0)
            new UpdateTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, map);
    }

    public void updateUpdateButtonStatus(LibraryEntry entry) {
        if (!newWatchStatus.equals(entry.getStatus()) ||
                newEpisodesWatched != entry.getEpisodesWatched() ||
                newIsRewatching != entry.isRewatching() ||
                newRewatchedTimes != entry.getNumberOfRewatches() ||
                !newRating.equals(entry.getRating().getAdvancedRating() != null ?
                        entry.getRating().getAdvancedRating() : "0")) {
            mAddToLibrary.setEnabled(true);
        } else
            mAddToLibrary.setEnabled(false);
    }

    protected class LoadTask extends AsyncTask<Void, Void, Boolean> {

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
        }

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
                vibrantColor = mPalette.getVibrantColor();

                mActionBarHelper = new FadingActionBarHelper()
                        .actionBarBackground(vibrantColor == null ? new ColorDrawable(R.color.neutral)
                                : new ColorDrawable(vibrantColor.getRgb()))
                        .headerLayout(R.layout.header_anime_details)
                        .headerOverlayLayout(R.layout.header_overlay_anime_details)
                        .contentLayout(R.layout.content_anime_details);
                setContentView(mActionBarHelper.createView(AnimeDetailsActivity.this));
                mActionBarHelper.initActionBar(AnimeDetailsActivity.this);

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
                mLibraryHolderShadow = findViewById(R.id.anime_details_library_holder_shadow);
                mStatusSpinner = (Spinner) findViewById(R.id.anime_details_status_spinner);
                mEpisodesHolder = (LinearLayout) findViewById(R.id.anime_details_library_episodes_holder);
                mEpisodes = (TextView) findViewById(R.id.anime_details_library_episodes);
                mRewatching = (Switch) findViewById(R.id.anime_details_library_rewatching);
                mRewatchedTimesHolder = (LinearLayout) findViewById(R.id.anime_details_library_rewatched_holder);
                mRewatchedTimes = (TextView) findViewById(R.id.anime_details_library_rewatched);
                mPrivate = (Switch) findViewById(R.id.anime_details_library_private);
                mRatingBar = (RatingBar) findViewById(R.id.anime_details_library_rating);
                mRatingSimple = (TextView) findViewById(R.id.anime_deatails_library_rating_simple);

                mViewTrailer.getBackground().setColorFilter(vibrantColor.getRgb(), PorterDuff.Mode.SRC_ATOP);
                mViewTrailer.setTextColor(vibrantColor.getRgb());
                mAddToLibrary.getBackground().setColorFilter(vibrantColor.getRgb(), PorterDuff.Mode.SRC_ATOP);
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

            mLibraryHolderShadow.setVisibility(View.VISIBLE);
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
            mLibraryHolderShadow.setVisibility(View.GONE);
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
