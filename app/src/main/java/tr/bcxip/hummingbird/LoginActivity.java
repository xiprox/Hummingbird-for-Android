package tr.bcxip.hummingbird;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import retrofit.RetrofitError;
import tr.bcxip.hummingbird.api.HummingbirdApi;
import tr.bcxip.hummingbird.api.Results;
import tr.bcxip.hummingbird.managers.PrefManager;

/**
 * Created by mhca on 10/10/2014.
 */
public class LoginActivity extends Activity {

    HummingbirdApi api;
    PrefManager prefMan;

    ImageView mHeaderBackground;
    ImageView mHeaderLogo;

    EditText mUsernameOrEmail;
    TextView mUsernameOrEmailHint;
    EditText mPassword;
    TextView mPasswordHint;

    LinearLayout mMessageHolder;
    ImageView mMessageIndicator;
    TextView mMessageText;

    ProgressBar mProgressBar;

    Button mSignUp;
    Button mSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        api = new HummingbirdApi(this);
        prefMan = new PrefManager(this);

        mHeaderBackground = (ImageView) findViewById(R.id.login_header_bg);
        mHeaderLogo = (ImageView) findViewById(R.id.login_header_logo);
        adjustHeaderSizes();

        mUsernameOrEmail = (EditText) findViewById(R.id.login_username_or_email);
        mUsernameOrEmailHint = (TextView) findViewById(R.id.login_username_or_email_hint);
        mPassword = (EditText) findViewById(R.id.login_password);
        mPasswordHint = (TextView) findViewById(R.id.login_password_hint);

        mMessageHolder = (LinearLayout) findViewById(R.id.login_message_holder);
        mMessageIndicator = (ImageView) findViewById(R.id.login_message_indicator);
        mMessageText = (TextView) findViewById(R.id.login_message_text);

        mProgressBar = (ProgressBar) findViewById(R.id.login_progress_bar);

        mSignUp = (Button) findViewById(R.id.login_sign_up);
        mSignIn = (Button) findViewById(R.id.login_sign_in);

        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO - Send to a sign up
            }
        });

        mSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AuthenticationTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        mUsernameOrEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                /* empty */
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                /* empty */
            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateState();

                if (mUsernameOrEmail.getText().length() != 0)
                    mUsernameOrEmailHint.setVisibility(View.VISIBLE);
                else
                    mUsernameOrEmailHint.setVisibility(View.INVISIBLE);
            }
        });

        mPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                /* empty */
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                /* empty */
            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateState();

                if (mPassword.getText().length() != 0)
                    mPasswordHint.setVisibility(View.VISIBLE);
                else
                    mPasswordHint.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void adjustHeaderSizes() {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        mHeaderBackground.getLayoutParams().height = (size.y / 3);
        mHeaderLogo.getLayoutParams().height = mHeaderBackground.getLayoutParams().height / 3;
        mHeaderLogo.getLayoutParams().width = mHeaderLogo.getLayoutParams().height
                + mHeaderLogo.getLayoutParams().height / 6;
    }

    public void updateState() {
        int red = getResources().getColor(android.R.color.holo_red_dark);

        if ((mUsernameOrEmail.getText().toString().length() == 0 ||
                mUsernameOrEmail.getText().toString().trim().length() == 0)
                &&
                (mPassword.getText().toString().length() != 0 &&
                        mPassword.getText().toString().trim().length() != 0)) {

            mMessageText.setText(R.string.error_username_or_email_cant_be_empty);
            mMessageText.setTextColor(red);
            mMessageHolder.setVisibility(View.VISIBLE);

            mSignIn.setEnabled(false);
        }

        if ((mPassword.getText().toString().length() == 0 ||
                mPassword.getText().toString().trim().length() == 0)
                &&
                (mUsernameOrEmail.getText().toString().length() != 0 &&
                        mUsernameOrEmail.getText().toString().trim().length() != 0)) {

            mMessageIndicator.setColorFilter(red, PorterDuff.Mode.SRC_ATOP);
            mMessageText.setText(R.string.error_password_cant_be_empty);
            mMessageText.setTextColor(red);
            mMessageHolder.setVisibility(View.VISIBLE);

            mSignIn.setEnabled(false);
        }

        if ((mPassword.getText().toString().length() == 0 ||
                mPassword.getText().toString().trim().length() == 0)
                &&
                (mUsernameOrEmail.getText().toString().length() == 0 ||
                        mUsernameOrEmail.getText().toString().trim().length() == 0)) {

            mMessageIndicator.setColorFilter(red, PorterDuff.Mode.SRC_ATOP);
            mMessageText.setText(R.string.error_login_fields_cant_be_empty);
            mMessageText.setTextColor(red);
            mMessageHolder.setVisibility(View.VISIBLE);

            mSignIn.setEnabled(false);
        }

        if ((mPassword.getText().toString().length() != 0 &&
                mPassword.getText().toString().trim().length() != 0)
                &&
                (mUsernameOrEmail.getText().toString().length() != 0 &&
                        mUsernameOrEmail.getText().toString().trim().length() != 0)) {
            mMessageHolder.setVisibility(View.GONE);

            mSignIn.setEnabled(true);
        }


    }

    protected class AuthenticationTask extends AsyncTask<Void, Void, String> {

        String authToken;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
            mMessageHolder.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                authToken = api.authenticate(
                        mUsernameOrEmail.getText().toString(),
                        mPassword.getText().toString()
                );

                return Results.RESULT_SUCCESS;
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

            if (result.equals(Results.RESULT_SUCCESS)) {
                /* Store the token for later use */
                prefMan.setAuthToken(authToken);

                /* Store the username as well */
                // TODO - Will have to disable email login...
                if (!mUsernameOrEmail.getText().toString().contains("@"))
                    prefMan.setUsername(mUsernameOrEmail.getText().toString());

                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            } else if (result.equals(Results.RESULT_UNAUTHORIZED)) {
                mMessageText.setText(R.string.error_invalid_credentials);
                mMessageIndicator.setColorFilter(red, PorterDuff.Mode.SRC_ATOP);
                mMessageHolder.setVisibility(View.VISIBLE);
            } else if (result.contains(Results.RESULT_UNABLE_TO_RESOLVE_HOST)) {
                mMessageText.setText(R.string.error_connection_error);
                mMessageIndicator.setColorFilter(red, PorterDuff.Mode.SRC_ATOP);
                mMessageHolder.setVisibility(View.VISIBLE);
            }
        }
    }
}
