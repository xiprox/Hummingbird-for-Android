package tr.bcxip.hummingbird.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Hikari on 10/10/14.
 */
public class PrefManager {

    public static final String PREF_AUTH_TOKEN = "pref_auth_token";
    public static final String PREF_USERNAME = "pref_username";

    private static Context context;
    private static SharedPreferences prefs;
    private static SharedPreferences.Editor prefeditor;

    public PrefManager(Context context) {
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefeditor = prefs.edit();
    }

    public boolean isAuthenticated() {
        return getAuthToken() != null;
    }

    public void setAuthToken(String token) {
        prefeditor.putString(PREF_AUTH_TOKEN, token).commit();
    }

    public String getAuthToken() {
        return prefs.getString(PREF_AUTH_TOKEN, null);
    }

    public void setUsername(String username) {
        prefeditor.putString(PREF_USERNAME, username).commit();
    }

    public String getUsername() {
        return prefs.getString(PREF_USERNAME, null);
    }
}
