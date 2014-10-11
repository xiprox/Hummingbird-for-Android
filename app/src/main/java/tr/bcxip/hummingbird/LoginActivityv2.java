package tr.bcxip.hummingbird;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.ProgressBar;

import android.widget.Toast;

import retrofit.RetrofitError;
import tr.bcxip.hummingbird.api.HummingbirdApi;
import tr.bcxip.hummingbird.managers.PrefManager;

/**
 * Created by mhca on 10/10/2014.
 * Not Working :D
 */
public class LoginActivityv2 extends Activity {

    HummingbirdApi api;
    PrefManager prefMan;

    ImageView mHeaderBackground;
    ImageView mHeaderLogo;

    EditText mUsername;
    EditText mPassword;

    ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginv2);
        getActionBar().hide();

        api = new HummingbirdApi(this);
        prefMan = new PrefManager(this);

        mHeaderBackground = (ImageView) findViewById(R.id.login_header_bg);
        mHeaderLogo = (ImageView) findViewById(R.id.login_header_logo);
        adjustHeaderSizes();

        mUsername = (EditText) findViewById(R.id.usernamev2);
        mPassword = (EditText) findViewById(R.id.passwordv2);
        mProgressBar = (ProgressBar) findViewById(R.id.login_progress_bar);

        goLogin();
    }

    public void adjustHeaderSizes() {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        mHeaderBackground.getLayoutParams().height = (size.y / 2);
        mHeaderLogo.getLayoutParams().height = mHeaderBackground.getLayoutParams().height / 3;
        mHeaderLogo.getLayoutParams().width = mHeaderLogo.getLayoutParams().height
                + mHeaderLogo.getLayoutParams().height / 6;
    }

    public void goLogin() {
        int red = getResources().getColor(android.R.color.holo_red_dark);

        if (mUsername.getText().toString().length() == 0 ||
                mUsername.getText().toString().trim().length() == 0){

            mUsername.setHint(getResources().getString(R.string.error_username));
            mUsername.setHintTextColor(red);
        }else if (mPassword.getText().toString().length() != 0 &&
                mPassword.getText().toString().trim().length() != 0){

            mUsername.setHint(getResources().getString(R.string.error_password));
            mUsername.setHintTextColor(red);
        }else{
            new AuthenticationTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    protected class AuthenticationTask extends AsyncTask<Void, Void, String> {

        String RESULT_SUCCESS = "success";
        String RESULT_UNAUTHORIZED = "401 Unauthorized";
        String RESULT_UNABLE_TO_RESOLVE_HOST = "Unable to resolve host";

        String authToken;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                authToken = api.authenticate(
                        mUsername.getText().toString(),
                        mPassword.getText().toString()
                );

                return RESULT_SUCCESS;
            } catch (RetrofitError e) {
                Log.e("AUTHENTICATION ERROR", e.getMessage());
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            mProgressBar.setVisibility(View.GONE);

            int red = getResources().getColor(android.R.color.holo_red_dark);

            if (result.equals(RESULT_SUCCESS)) {
                /* Store the token for later use */
                prefMan.setAuthToken(authToken);

                startActivity(new Intent(LoginActivityv2.this, MainActivity.class));
                finish();
            } else if (result.equals(RESULT_UNAUTHORIZED)) {
                mUsername.setText(R.string.error_invalid_credentials);
                mPassword.setText("");
            } else if (result.contains(RESULT_UNABLE_TO_RESOLVE_HOST)) {
                Toast.makeText(getApplicationContext(), "" + R.string.error_connection_error,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
