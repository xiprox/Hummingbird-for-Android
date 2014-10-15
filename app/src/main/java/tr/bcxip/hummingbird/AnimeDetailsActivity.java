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
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.manuelpeinado.fadingactionbar.FadingActionBarHelper;
import com.squareup.picasso.Picasso;

import tr.bcxip.hummingbird.api.HummingbirdApi;
import tr.bcxip.hummingbird.api.objects.AnimeV2;
import tr.bcxip.hummingbird.api.objects.Favorite;
import tr.bcxip.hummingbird.api.objects.LibraryEntry;
import tr.bcxip.hummingbird.api.objects.User;
import tr.bcxip.hummingbird.managers.PrefManager;

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
                PaletteItem vibrantColor = mPalette.getVibrantColor();

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
                Spinner mStatusSpinner = (Spinner) findViewById(R.id.anime_details_status_spinner);
                LinearLayout mEpisodesHolder = (LinearLayout) findViewById(R.id.anime_details_library_episodes_holder);
                TextView mEpisodes = (TextView) findViewById(R.id.anime_details_library_episodes);
                Switch mRewatching = (Switch) findViewById(R.id.anime_details_library_rewatching);
                LinearLayout mRewatchedTimesHolder = (LinearLayout) findViewById(R.id.anime_details_library_rewatched_holder);
                TextView mRewatchedTimes = (TextView) findViewById(R.id.anime_details_library_rewatched);
                Switch mPrivate = (Switch) findViewById(R.id.anime_details_library_private);

                mViewTrailer.getBackground().setColorFilter(vibrantColor.getRgb(), PorterDuff.Mode.SRC_ATOP);
                mViewTrailer.setTextColor(vibrantColor.getRgb());
                mAddToList.getBackground().setColorFilter(vibrantColor.getRgb(), PorterDuff.Mode.SRC_ATOP);

                if (anime.getTrailer() == null || anime.getTrailer().equals(""))
                    mViewTrailer.setVisibility(View.GONE);

                mViewTrailer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://www.youtube.com/watch?v=" + anime.getTrailer())));
                    }
                });

                mAddToList.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(
                                AnimeDetailsActivity.this,
                                "Adding " + anime.getCanonicalTitle() + " to list :P",
                                Toast.LENGTH_SHORT
                        ).show();
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

                    if (libraryEntry.isRewatching())
                        mRewatching.setChecked(true);
                    else
                        mRewatching.setChecked(false);

                    mRewatchedTimes.setText(libraryEntry.getNumberOfRewatches() + "");

                    if (libraryEntry.isPrivate())
                        mPrivate.setChecked(true);
                    else
                        mPrivate.setChecked(false);

                    mEpisodesHolder.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // TODO - Open episode dialog
                            // TODO - update change indicator.........
                        }
                    });

                    mRewatching.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            // TODO - update change indicator......
                        }
                    });

                    mRewatchedTimesHolder.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // TODO - Open dialog
                            // TODO - update change indicator......
                        }
                    });

                    mPrivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            // TODO - update change indicator...
                        }
                    });

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

                    mStatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                            Log.d("POS", position + "");
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                    mAddToList.setText(R.string.content_update);
                    mAddToList.setEnabled(false);
                }

            } else {
                Toast.makeText(AnimeDetailsActivity.this, R.string.error_cant_load_data, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
