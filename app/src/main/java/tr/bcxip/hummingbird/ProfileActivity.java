package tr.bcxip.hummingbird;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;

/**
 * Created by Hikari on 10/24/14.
 */
public class ProfileActivity extends ActionBarActivity {

    public static final String ARG_USERNAME = "username";

    private static final String TAG = "PROFILE ACTIVITY";

    String USERNAME;

    View mStatusBarBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mStatusBarBackground = findViewById(R.id.profile_status_bar_placeholder);

        ProfileFragment fragment = new ProfileFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();

        USERNAME = getIntent().getStringExtra(ARG_USERNAME);
        if (USERNAME == null || USERNAME.equals("") || USERNAME.trim().equals("")) {
            Log.e(TAG, "No username argument was passed; Exiting...");
            finish();
        } else {
            Bundle args = new Bundle();
            args.putString(ProfileFragment.ARG_USERNAME, USERNAME);
            fragment.setArguments(args);
            fragmentManager.beginTransaction().replace(
                    R.id.container,
                    fragment,
                    ProfileFragment.FRAGMENT_TAG_PROFILE
            ).commit();
        }
    }

    public void setStatusBarBackgroundColor(int color) {
        if (mStatusBarBackground != null)
            mStatusBarBackground.setBackgroundColor(color);
    }
}
