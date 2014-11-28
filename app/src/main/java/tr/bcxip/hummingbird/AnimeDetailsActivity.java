package tr.bcxip.hummingbird;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit.RetrofitError;
import tr.bcxip.hummingbird.api.HummingbirdApi;
import tr.bcxip.hummingbird.api.objects.Anime;
import tr.bcxip.hummingbird.api.objects.LibraryEntry;
import tr.bcxip.hummingbird.api.objects.Rating;
import tr.bcxip.hummingbird.managers.PrefManager;
import tr.bcxip.hummingbird.utils.Utils;
import tr.bcxip.hummingbird.widget.ObservableScrollView;
import tr.xip.widget.simpleratingview.SimpleRatingView;
import uk.me.lewisdeane.ldialogs.CustomDialog;

/**
 * Created by Hikari on 10/8/14.
 */
public class AnimeDetailsActivity extends ActionBarActivity implements
        ObservableScrollView.Callbacks {

    private static final String TAG = "ANIME DETAILS ACTIVITY";

    public static final String ARG_ID = "arg_id";
    public static final String ARG_ANIME_OBJ = "arg_anime_obj";

    public static final String STATE_ANIME = "state_anime";
    public static final String STATE_LIBRARY_ENTRY = "state_library_entry";

    private static final int STATE_ONSCREEN = 0;
    private static final int STATE_OFFSCREEN = 1;
    private static final int STATE_RETURNING = 2;

    private int lastDampedScroll;
    private int lastHeaderHeight = -1;
    private boolean firstGlobalLayoutPerformed;
    private boolean lastToolbarVisibility = true;
    private boolean lastHeaderVisibility = true;

    Toolbar toolbar;
    ActionBar mActionBar;

    HummingbirdApi api;
    PrefManager prefMan;

    Palette mPalette;

    FloatingActionButton mActionButton;

    private Toolbar mQuickReturnView;
    private View mPlaceholderView;
    private ObservableScrollView mObservableScrollView;
    private int mMinRawY = 0;
    private int mState = STATE_ONSCREEN;
    private int mQuickReturnHeight;
    private int mMaxScrollY;

    private Drawable mActionBarBackgroundDrawable;

    LinearLayout mContentsHolder;
    FrameLayout mInfoHolder;
    LinearLayout mMoreInfoHolder;
    FrameLayout mLibraryInfoHolder;
    FrameLayout mHeaderHolder;
    ImageView mHeaderImage;
    FrameLayout mCoverHolder;
    ImageView mCoverImage;
    TextView mTitle;
    TextView mType;
    TextView mGenre;
    TextView mEpisodeCount;
    TextView mEpisodeLength;
    TextView mAgeRating;
    TextView mAired;
    TextView mCommunityRating;
    LinearLayout mSynopsisHolder;
    TextView mSynopsis;
    LinearLayout mMoreSimilarAnime;

    MenuItem mRemove;

    ProgressBar mLibraryProgressBar;
    Spinner mStatusSpinner;
    LinearLayout mEpisodesHolder;
    TextView mEpisodes;
    SwitchCompat mRewatching;
    LinearLayout mRewatchedTimesHolder;
    TextView mRewatchedTimes;
    SwitchCompat mPrivate;
    RatingBar mRatingBar;
    SimpleRatingView mSimpleRatingView;

    String ANIME_ID;

    Anime anime;
    LibraryEntry libraryEntry;

    String newWatchStatus;
    int newEpisodesWatched;
    boolean newIsRewatching;
    int newRewatchedTimes;
    boolean newPrivate;
    String newRating;

    Bitmap coverBitmap;

    int darkMutedColor;
    int vibrantColor;
    int darkVibrantColor;

    ObjectAnimator toolbarBgFadeAnim;

    @TargetApi(Build.VERSION_CODES.L)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_details);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mActionBar = getSupportActionBar();
        api = new HummingbirdApi(this);
        prefMan = new PrefManager(this);

        mActionBar.setDisplayHomeAsUpEnabled(true);

        anime = (Anime) getIntent().getSerializableExtra(ARG_ANIME_OBJ);
        ANIME_ID = getIntent().getStringExtra(ARG_ID);

        if (savedInstanceState != null) {
            Anime savedAnime = (Anime) savedInstanceState.getSerializable(STATE_ANIME);
            if (savedAnime != null) anime = savedAnime;

            LibraryEntry savedLibraryEntry =
                    (LibraryEntry) savedInstanceState.getSerializable(STATE_LIBRARY_ENTRY);
            if (savedLibraryEntry != null)
                libraryEntry = savedLibraryEntry;
        }

        mActionBarBackgroundDrawable = new ColorDrawable(darkMutedColor != 0 ? darkMutedColor
                : getResources().getColor(R.color.neutral_darker));
        mActionBarBackgroundDrawable.setAlpha(0);
        toolbar.setBackgroundDrawable(mActionBarBackgroundDrawable);

        mActionButton = (FloatingActionButton) findViewById(R.id.fab);

        mQuickReturnView = toolbar;
        mPlaceholderView = findViewById(R.id.placeholder);
        mObservableScrollView = (ObservableScrollView) findViewById(R.id.anime_details_scroll_view);
        mObservableScrollView.setCallbacks(this);
        mObservableScrollView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        onScrollChanged(mObservableScrollView.getScrollY());
                        mMaxScrollY = mObservableScrollView.computeVerticalScrollRange()
                                - mObservableScrollView.getHeight();
                        mQuickReturnHeight = mQuickReturnView.getHeight();

                        int headerHeight = mHeaderImage.getHeight();
                        if (!firstGlobalLayoutPerformed && headerHeight != 0) {
                            updateHeaderHeight(headerHeight);
                            firstGlobalLayoutPerformed = true;
                        }
                    }
                });

        mContentsHolder = (LinearLayout) findViewById(R.id.anime_details_content_holder);
        mInfoHolder = (FrameLayout) findViewById(R.id.anime_details_info_holder);
        mMoreInfoHolder = (LinearLayout) findViewById(R.id.anime_details_more_info_holder);
        mLibraryInfoHolder = (FrameLayout) findViewById(R.id.anime_details_library_info_holder);
        mHeaderHolder = (FrameLayout) findViewById(R.id.anime_details_header_holder);
        mHeaderImage = (ImageView) findViewById(R.id.anime_details_header);
        mCoverHolder = (FrameLayout) findViewById(R.id.anime_details_cover_image_holder);
        mCoverImage = (ImageView) findViewById(R.id.anime_details_cover_image);
        mTitle = (TextView) findViewById(R.id.anime_details_title);
        mType = (TextView) findViewById(R.id.anime_details_type);
        mGenre = (TextView) findViewById(R.id.anime_details_genres);
        mEpisodeCount = (TextView) findViewById(R.id.anime_details_episode_count);
        mEpisodeLength = (TextView) findViewById(R.id.anime_details_episode_duration);
        mAgeRating = (TextView) findViewById(R.id.anime_details_age_rating);
        mAired = (TextView) findViewById(R.id.anime_details_aired);
        mCommunityRating = (TextView) findViewById(R.id.anime_details_community_rating);
        mSynopsisHolder = (LinearLayout) findViewById(R.id.anime_details_synopsis_holder);
        mSynopsis = (TextView) findViewById(R.id.anime_details_synopsis);
        mMoreSimilarAnime = (LinearLayout) findViewById(R.id.anime_details_more_similar_anime);

        mLibraryProgressBar = (ProgressBar) findViewById(R.id.anime_details_library_progress_bar);
        mStatusSpinner = (Spinner) findViewById(R.id.anime_details_status_spinner);
        mEpisodesHolder = (LinearLayout) findViewById(R.id.anime_details_library_episodes_holder);
        mEpisodes = (TextView) findViewById(R.id.anime_details_library_episodes);
        mRewatching = (SwitchCompat) findViewById(R.id.anime_details_library_rewatching);
        mRewatchedTimesHolder = (LinearLayout) findViewById(R.id.anime_details_library_rewatched_holder);
        mRewatchedTimes = (TextView) findViewById(R.id.anime_details_library_rewatched);
        mPrivate = (SwitchCompat) findViewById(R.id.anime_details_library_private);
        mRatingBar = (RatingBar) findViewById(R.id.anime_details_library_rating_advanced);
        mSimpleRatingView = (SimpleRatingView) findViewById(R.id.anime_details_library_rating_simple);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ViewOutlineProvider infoOutlineProvider = new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRect(
                            0,
                            Utils.dpToPx(AnimeDetailsActivity.this,
                                    getResources().getDimension(R.dimen.offset_details_info_card)),
                            view.getWidth(),
                            view.getHeight()
                    );
                }
            };
            mInfoHolder.setOutlineProvider(infoOutlineProvider);
        }

        ViewCompat.setElevation(mInfoHolder, Utils.dpToPx(this, 2));
        ViewCompat.setElevation(mMoreInfoHolder, Utils.dpToPx(this, 2));
        ViewCompat.setElevation(mLibraryInfoHolder, Utils.dpToPx(this, 2));

        if (anime != null) {
            displayAnimeInfo();

            if (libraryEntry == null)
                new LibraryEntryTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else
                displayLibraryElements();
        } else
            new AnimeInfoTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (anime != null)
            outState.putSerializable(STATE_ANIME, anime);

        if (libraryEntry != null)
            outState.putSerializable(STATE_LIBRARY_ENTRY, libraryEntry);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.anime_details, menu);
        mRemove = menu.findItem(R.id.action_remove);

        // Update library info once again if entry was loaded from saved instance. This is done
        // because when the #displayLibraryElements() method is run, mRemove is null. Thus, it is
        // not made VISIBLE. Updating thins here makes it visible... Nah, you get the point.
        if (libraryEntry != null)
            displayLibraryElements();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_copy_title:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getResources().getString
                        (R.string.toast_copy_title), anime.getTitle());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(
                        AnimeDetailsActivity.this,
                        getResources().getString(R.string.toast_copy_title)
                                + " \"" + anime.getTitle() + "\"",
                        Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_share:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");

                String messageBody = getString(R.string.content_sharing_text);
                messageBody = messageBody
                        .replace("{anime-name}", anime.getTitle())
                        .replace("{anime-url}", "https://hummingbird.me/anime/" + anime.getSlug());

                intent.putExtra(Intent.EXTRA_TEXT, messageBody);

                startActivity(Intent.createChooser(intent,
                        getResources().getString(R.string.action_share)));
                break;
            case R.id.action_remove:
                CustomDialog.Builder builder = new CustomDialog.Builder(
                        AnimeDetailsActivity.this,
                        R.string.title_remove,
                        R.string.yes);
                builder.negativeText(R.string.no);
                builder.positiveColor(vibrantColor);
                String contentText = getString(R.string.content_remove_are_you_sure);
                contentText = contentText.replace("{anime-name}", anime.getTitle());
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
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onScrollChanged(int scrollY) {
        int originalScrollY = scrollY;
        scrollY = Math.min(mMaxScrollY, scrollY);

        int rawY = mPlaceholderView.getTop() - scrollY;
        int translationY = 0;

        switch (mState) {
            case STATE_OFFSCREEN:
                if (rawY <= mMinRawY) {
                    mMinRawY = rawY;
                } else {
                    mState = STATE_RETURNING;
                }
                translationY = rawY;
                break;

            case STATE_ONSCREEN:
                if (rawY < -mQuickReturnHeight) {
                    mState = STATE_OFFSCREEN;
                    mMinRawY = rawY;
                }
                translationY = rawY;
                break;

            case STATE_RETURNING:
                translationY = (rawY - mMinRawY) - mQuickReturnHeight;
                if (translationY > 0) {
                    translationY = 0;
                    mMinRawY = rawY - mQuickReturnHeight;
                }

                if (rawY > 0) {
                    mState = STATE_ONSCREEN;
                    translationY = rawY;
                }

                if (translationY < -mQuickReturnHeight) {
                    mState = STATE_OFFSCREEN;
                    mMinRawY = rawY;
                }
                break;
        }
        mQuickReturnView.animate().cancel();
        mQuickReturnView.setTranslationY(translationY + scrollY);

        /** Header image parallax stuff */
        int currentHeaderHeight = mHeaderImage.getHeight();

        if (currentHeaderHeight != lastHeaderHeight)
            updateHeaderHeight(currentHeaderHeight);

        float damping = 0.5f;
        int dampedScroll = (int) (originalScrollY * damping);
        int offset = lastDampedScroll - dampedScroll;
        mHeaderImage.offsetTopAndBottom(-offset);

        if (firstGlobalLayoutPerformed)
            lastDampedScroll = dampedScroll;

        /** Action bar background stuff */
        boolean toolbarVisibility = isToolbarVisibleOnScreen();
        boolean headerVisibility = isHeaderVisibleOnScreen();

        if (lastToolbarVisibility != toolbarVisibility || lastHeaderVisibility != headerVisibility)
            updateToolbarBackgroundColor(!headerVisibility);

        lastToolbarVisibility = toolbarVisibility;
        lastHeaderVisibility = headerVisibility;
    }

    private void updateHeaderHeight(int headerHeight) {
        lastHeaderHeight = headerHeight;
    }

    private void updateToolbarBackgroundColor(boolean show) {
        if (toolbarBgFadeAnim == null)
            toolbarBgFadeAnim = ObjectAnimator.ofInt(mActionBarBackgroundDrawable, "alpha", 255, 0)
                    .setDuration(400);

        if (show) {
            if (!toolbarBgFadeAnim.isStarted())
                mActionBarBackgroundDrawable.setAlpha(255);
        } else if (((ColorDrawable) mActionBarBackgroundDrawable).getAlpha() != 0)
            toolbarBgFadeAnim.start();
    }

    private boolean isHeaderVisibleOnScreen() {
        Rect headerBounds = new Rect();
        mHeaderHolder.getHitRect(headerBounds);
        return Rect.intersects(getScrollViewBounds(), headerBounds);
    }

    private boolean isToolbarVisibleOnScreen() {
        Rect toolbarBounds = new Rect();
        toolbar.getHitRect(toolbarBounds);
        return Rect.intersects(getScrollViewBounds(), toolbarBounds);
    }

    private Rect getScrollViewBounds() {
        return new Rect(
                mObservableScrollView.getScrollX(),
                mObservableScrollView.getScrollY(),
                mObservableScrollView.getScrollX() + mObservableScrollView.getWidth(),
                mObservableScrollView.getScrollY() + mObservableScrollView.getHeight());
    }

    @Override
    public void onDownMotionEvent() {
        /* empty */
    }

    @Override
    public void onUpOrCancelMotionEvent() {
        /* empty */
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
            enableFAB();
        } else {
            disableFAB();
        }
    }

    private void enableFAB() {
        if (mActionButton != null) {
            mActionButton.setEnabled(true);
            ObjectAnimator anim = ObjectAnimator.ofInt(mActionButton.getDrawable(), "alpha", 255);
            anim.setDuration(200).start();
        }
    }

    private void disableFAB() {
        if (mActionButton != null) {
            mActionButton.setEnabled(false);
            ObjectAnimator anim = ObjectAnimator.ofInt(mActionButton.getDrawable(), "alpha", 100);
            anim.setDuration(200).start();
        }
    }

    /**
     * isFirstLoad - indicates whether it's the first time we are loading the content (not a reload
     * after a removal, for instance). Why? Because, the progress dialog
     * has to be cancelable on first load.
     */
    private class AnimeInfoTask extends AsyncTask<Boolean, Void, Boolean> {

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
                    cancel(false);
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
                displayAnimeInfo();
                new LibraryEntryTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                Toast.makeText(AnimeDetailsActivity.this, R.string.error_cant_load_data, Toast.LENGTH_LONG).show();
                finish();
            }

            dialog.dismiss();
        }
    }

    private class LibraryEntryTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (mLibraryProgressBar != null)
                mLibraryProgressBar.setVisibility(View.VISIBLE);

            if (mLibraryInfoHolder != null)
                mLibraryInfoHolder.setVisibility(View.GONE);

            if (mActionButton != null)
                disableFAB();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                if (anime != null)
                    libraryEntry = api.getLibraryEntryIfAnimeExists(anime.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void voidd) {
            super.onPostExecute(voidd);
            displayLibraryElements();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void displayAnimeInfo() {
        Resources res = getResources();

        final ImageView imageView = new ImageView(AnimeDetailsActivity.this);
        Picasso.with(AnimeDetailsActivity.this)
                .load(anime.getCoverImage()).into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                coverBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            }

            @Override
            public void onError() {

            }
        });

        if (coverBitmap != null) {
            mPalette = Palette.generate(coverBitmap);
            if (mPalette != null) {
                darkMutedColor = mPalette.getDarkMutedColor(res.getColor(R.color.neutral_darker));
                vibrantColor = mPalette.getVibrantColor(res.getColor(R.color.apptheme_primary));
                darkVibrantColor = mPalette.getDarkVibrantColor(res.getColor(R.color.apptheme_primary_dark));
            }
        } else
            darkMutedColor = res.getColor(R.color.neutral_darker);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(darkMutedColor);

        int alpha = mActionBarBackgroundDrawable.getAlpha();
        mActionBarBackgroundDrawable = new ColorDrawable(darkMutedColor);
        mActionBarBackgroundDrawable.setAlpha(alpha);
        if (toolbar != null)
            toolbar.setBackgroundDrawable(mActionBarBackgroundDrawable);

        mActionButton.setOnClickListener(new OnAddToLibraryClickListener());
        mActionButton.setColorNormal(vibrantColor);
        mActionButton.setColorPressed(darkVibrantColor);

        mCoverImage.setImageBitmap(coverBitmap);
        mCoverHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO - Fullscreen
            }
        });

        // TODO - Put something else here
        mHeaderImage.setImageBitmap(coverBitmap);
        mHeaderHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO - Fullscreen
            }
        });

        mTitle.setText(anime.getTitle());

        mType.setText(anime.getShowType());

        /*
            TODO
            For some reason the API omits genres in the Anime object bundled with the Library Entry
            object. Gotta find a solution for this. God! I really don't wanna have to load all data
            just because of a few damn genres!
         */
        if (anime.getGenres() != null) {
            String genres = "";
            for (int i = 0; i < anime.getGenres().size(); i++) {
                if (i == 0)
                    genres = anime.getGenres().get(i).getName();
                else
                    genres += ", " + anime.getGenres().get(i).getName();
            }
            mGenre.setText(genres);
        }

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
        mSynopsisHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO - Show more
            }
        });

        mMoreSimilarAnime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO - More similar activity...
            }
        });
    }

    /* If Anime exist in user library, show library related elements... */
    public void displayLibraryElements() {
        if (libraryEntry != null) {
            if (mRemove != null)
                mRemove.setVisible(true);

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
                mSimpleRatingView.setVisibility(View.GONE);
            } else {
                if (rating.getSimpleRating() != null)
                    mSimpleRatingView.setSelectedRating(
                            Utils.getRatingFromString(rating.getSimpleRating()));
                else
                    mSimpleRatingView.setSelectedRating(SimpleRatingView.Rating.NEUTRAL);

                mRatingBar.setVisibility(View.GONE);
                mSimpleRatingView.setVisibility(View.VISIBLE);
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
                    builder.positiveColor(vibrantColor);

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
                    builder.positiveColor(vibrantColor);

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

            mSimpleRatingView.setOnRatingChangedListener(new SimpleRatingView.OnRatingChangeListener() {
                @Override
                public void onRatingChanged(SimpleRatingView.Rating rating) {
                    switch (rating) {
                        case POSITIVE:
                            newRating = "1";
                            break;
                        case NEUTRAL:
                            newRating = "3";
                            break;
                        case NEGATIVE:
                            newRating = "5";
                            break;
                    }

                    updateUpdateButtonStatus(libraryEntry);
                }
            });

            mActionButton.setImageResource(R.drawable.ic_upload_white_24dp);
            mActionButton.setOnClickListener(new OnLibraryUpdateClickListener());

            mLibraryProgressBar.setVisibility(View.GONE);
            mLibraryInfoHolder.setVisibility(View.VISIBLE);
        } else {
            enableFAB();
            mRemove.setVisible(false);
            mLibraryProgressBar.setVisibility(View.GONE);
            mLibraryInfoHolder.setVisibility(View.GONE);
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
                toastMessage = toastMessage.replace("{anime-name}", anime.getTitle());
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
                new AnimeInfoTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else
                Toast.makeText(AnimeDetailsActivity.this,
                        R.string.error_cant_remove_item, Toast.LENGTH_LONG).show();

            dialog.dismiss();
        }
    }
}
