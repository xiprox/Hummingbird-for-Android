package tr.bcxip.hummingbird;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit.RetrofitError;
import tr.bcxip.hummingbird.adapters.FavoriteAnimeAdapter;
import tr.bcxip.hummingbird.api.HummingbirdApi;
import tr.bcxip.hummingbird.api.Results;
import tr.bcxip.hummingbird.api.objects.FavoriteAnime;
import tr.bcxip.hummingbird.api.objects.User;
import tr.bcxip.hummingbird.managers.PrefManager;
import tr.bcxip.hummingbird.utils.CircleTransformation;
import tr.bcxip.hummingbird.widget.ExpandableHeightGridView;
import tr.xip.widget.errorview.ErrorView;

/**
 * Created by Hikari on 10/12/14.
 */
public class UserInfoFragment extends Fragment implements ErrorView.RetryListener {

    public static final String ARG_USERNAME = "username";

    final String TAG = "USER INFO FRAGMENT";

    Context context;
    HummingbirdApi api;
    PrefManager prefMan;

    String username;

    User user;

    ImageView mCover;
    ImageView mAvatar;
    TextView mUsername;
    TextView mBio;
    LinearLayout mOnline;
    TextView mWaifu;
    LinearLayout mWaifuHolder;
    TextView mLocation;
    LinearLayout mLocationHolder;
    TextView mWebsite;
    LinearLayout mWebsiteHolder;
    TextView mTimeWatched;

    ViewFlipper mFlipper;
    ErrorView mErrorView;

    int darkMutedColor;

    LoadTask loadTask;

    ProfileFragment parent;

    CoverColorListener mColorListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        api = new HummingbirdApi(context);
        prefMan = new PrefManager(context);

        try {
            parent = (ProfileFragment) getFragmentManager()
                    .findFragmentByTag(ProfileFragment.FRAGMENT_TAG_PROFILE);

            mColorListener = parent;
        } catch (Exception e) {
            /* empty */
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_info, null);

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

        mCover = (ImageView) rootView.findViewById(R.id.user_info_cover);
        mAvatar = (ImageView) rootView.findViewById(R.id.user_info_avatar);
        mUsername = (TextView) rootView.findViewById(R.id.user_info_username);
        mBio = (TextView) rootView.findViewById(R.id.user_info_bio);
        mOnline = (LinearLayout) rootView.findViewById(R.id.user_info_online);
        mWaifu = (TextView) rootView.findViewById(R.id.user_info_waifu);
        mWaifuHolder = (LinearLayout) rootView.findViewById(R.id.user_info_waifu_holder);
        mLocation = (TextView) rootView.findViewById(R.id.user_info_location);
        mLocationHolder = (LinearLayout) rootView.findViewById(R.id.user_info_location_holder);
        mWebsite = (TextView) rootView.findViewById(R.id.user_info_website);
        mWebsiteHolder = (LinearLayout) rootView.findViewById(R.id.user_info_website_holder);
        mTimeWatched = (TextView) rootView.findViewById(R.id.user_info_watched_time);

        mFlipper = (ViewFlipper) rootView.findViewById(R.id.user_info_view_flipper);
        mErrorView = (ErrorView) rootView.findViewById(R.id.user_info_error_view);
        mErrorView.setOnRetryListener(this);

        loadTask = new LoadTask();
        loadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (loadTask != null) loadTask.cancel(true);
    }

    @Override
    public void onRetry() {
        if (loadTask != null && !loadTask.isCancelled())
            loadTask.cancel(false);

        loadTask = new LoadTask();
        loadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    protected class LoadTask extends AsyncTask<Void, Void, Integer> {

        Bitmap coverBitmap;

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
                user = api.getUser(username);
                coverBitmap = Picasso.with(context)
                        .load(user.getCoverImage())
                        .get();

                darkMutedColor = Palette.generate(coverBitmap)
                        .getDarkMutedColor(R.color.apptheme_primary);

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
                if (mColorListener != null)
                    mColorListener.onColorObtained(darkMutedColor);

                mCover.setImageBitmap(coverBitmap);

                Picasso.with(context)
                        .load(user.getAvatar())
                        .transform(new CircleTransformation())
                        .into(mAvatar);

                mUsername.setText(user.getName());
                mUsername.setTextColor(darkMutedColor);

                String bio = user.getBio();
                if (bio != null && !bio.equals("") && !bio.trim().equals(""))
                    mBio.setText(bio);
                else
                    mBio.setVisibility(View.GONE);

                if (user.isOnline())
                    mOnline.setVisibility(View.VISIBLE);

                String waifu = user.getWaifu();
                if (waifu != null && !waifu.equals("") && !waifu.trim().equals("")) {
                    mWaifu.setText(user.getWaifu());
                    mWaifu.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(context, AnimeDetailsActivity.class);
                            intent.putExtra(AnimeDetailsActivity.ARG_ID, user.getWaifuSlug());
                            context.startActivity(intent);
                        }
                    });
                } else
                    mWaifuHolder.setVisibility(View.GONE);

                String location = user.getLocation();
                if (location != null && !location.equals("") && !location.trim().equals(""))
                    mLocation.setText(location);
                else
                    mLocationHolder.setVisibility(View.GONE);

                String website = user.getWebsite();
                if (website != null && !website.equals("") && !website.trim().equals("")) {
                    if (website.contains("http://")) website = website.replace("http://", "");
                    if (website.contains("https://")) website = website.replace("https://", "");
                    mWebsite.setText(website);
                } else
                    mWebsiteHolder.setVisibility(View.GONE);

                int timeWatched = user.getLifeSpentOnAnime(); // TODO - Parse into readable language
                mTimeWatched.setText(timeWatched + "");

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

    public interface CoverColorListener {
        public void onColorObtained(int color);
    }
}
