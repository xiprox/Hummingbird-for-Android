package tr.bcxip.hummingbird;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import retrofit.RetrofitError;
import tr.bcxip.hummingbird.api.HummingbirdApi;
import tr.bcxip.hummingbird.managers.PrefManager;

/**
 * Created by mhca on 10/10/2014.
 */
public class LoginActivity extends ActionBarActivity {

    private static final String TAG = "Login Activity";

    HummingbirdApi api;
    PrefManager prefMan;

    EditText mUsername;
    EditText mPassword;
    TextView mErrorMessage;
    Button mSignIn;
    Button mSignUp;
    Button mHelp;
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        api = new HummingbirdApi(this);
        prefMan = new PrefManager(this);

        mUsername = (EditText) findViewById(R.id.edit_username);
        mPassword = (EditText) findViewById(R.id.edit_password);
        mSignIn = (Button) findViewById(R.id.btn_login);
        mSignUp = (Button) findViewById(R.id.btn_new);
        mHelp = (Button) findViewById(R.id.btn_help);
        mErrorMessage = (TextView) findViewById(R.id.error_text);
        mProgressBar = (ProgressBar) findViewById(R.id.login_progress_bar);

        mSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AuthenticationTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendLink(2);
            }
        });

        mHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendLink(1);
            }
        });

        mUsername.addTextChangedListener(new TextWatcher() {
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
            }
        });
    }

    //1 help, 2 sign up
    public void sendLink(int i) {
        switch (i) {
            case 1:
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://hummingbird.me/users/password/new")));
                break;
            case 2:
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://hummingbird.me/users/sign_up")));
                break;
        }
    }

    public void updateState() {
        if (mUsername.getText().toString().length() != 0) {
            if (mPassword.getText().toString().length() != 0) {
                mSignIn.setEnabled(true);
            } else {
                mSignIn.setEnabled(false);
            }
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
            mErrorMessage.setVisibility(View.GONE);
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
                return e.getMessage() + "";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            mProgressBar.setVisibility(View.INVISIBLE);

            if (result.equals(RESULT_SUCCESS)) {
                /* Store the token for later use */
                prefMan.setAuthToken(authToken);
                prefMan.setUsername(mUsername.getText().toString());

                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            } else if (result.equals(RESULT_UNAUTHORIZED)) {
                mErrorMessage.setText(R.string.error_invalid_credentials);
                mErrorMessage.setVisibility(View.VISIBLE);
            } else if (result.contains(RESULT_UNABLE_TO_RESOLVE_HOST)) {
                mErrorMessage.setText(R.string.error_connection);
                mErrorMessage.setVisibility(View.VISIBLE);
            } else if (result.contains("522")) {
                mErrorMessage.setText(R.string.error_cant_connect_to_server);
                mErrorMessage.setVisibility(View.VISIBLE);
            } else if (result.contains("504")) {
                mErrorMessage.setText(R.string.error_gateway_timeout);
                mErrorMessage.setVisibility(View.VISIBLE);
            } else {
                mErrorMessage.setText(R.string.error);
                mErrorMessage.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            // TODO - Exit Application
        }
        return super.onKeyDown(keyCode, event);
    }
}
