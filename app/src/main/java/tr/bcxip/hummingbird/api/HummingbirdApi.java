package tr.bcxip.hummingbird.api;

import android.content.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.RestAdapter;
import tr.bcxip.hummingbird.api.objects.Anime;
import tr.bcxip.hummingbird.api.objects.FavoriteAnime;
import tr.bcxip.hummingbird.api.objects.LibraryEntry;
import tr.bcxip.hummingbird.api.objects.Story;
import tr.bcxip.hummingbird.api.objects.User;
import tr.bcxip.hummingbird.managers.PrefManager;

/**
 * Created by Hikari on 10/8/14.
 */
public class HummingbirdApi {
    private static final String API_HOST_v1 = "https://hummingbird.me/api/v1";
    private static final String API_HOST_v2 = "https://hummingbird.me/api/v2";

    Context context;
    HummingbirdService service;
    PrefManager prefMan;

    public HummingbirdApi(Context context) {
        this.context = context;
        prefMan = new PrefManager(context);
        setupServices();
    }

    public String authenticate(String nameOrEmail, String password) {
        Map<String, String> params = new HashMap<String, String>();

        // TODO - Silcen Mi ? :D
        if (nameOrEmail.contains("@"))
            params.put("email", nameOrEmail);
        else
            params.put("username", nameOrEmail);

        params.put("password", password);

        return service.authenticate(params);
    }

    private void setupServices() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(API_HOST_v1)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        service = restAdapter.create(HummingbirdService.class);
    }

    public List<Anime> searchAnime(String query) {
        return service.searchAnime(query);
    }

    public User getUser(String username) {
        return service.getUser(username);
    }

    public Anime getAnime(String idOrSlug) {
        return service.getAnime(idOrSlug);
    }

    public List<Story> getFeed(String username, int page) {
        return service.getFeed(username, page);
    }

    public List<Story> getTimeline(String token, int page) {
        return service.getTimeline(token, page);
    }

    public List<LibraryEntry> getLibrary(String username, Map<String, String> params) {
        return service.getLibrary(username, params != null ? params : new HashMap<String, String>());
    }

    public LibraryEntry getLibraryEntryIfAnimeExists(String animeId) {
        Map<String, String> params = new HashMap<String, String>();

        if (prefMan.getAuthToken() != null)
            params.put("auth_token", prefMan.getAuthToken());

        LibraryEntry libEntry = null;
        for (LibraryEntry entry : getLibrary(prefMan.getUsername(), params)) {
            if (entry.getAnime().getId().equals(animeId))
                libEntry = entry;
        }

        return libEntry;
    }

    public LibraryEntry addUpdateLibraryEntry(String id, Map params) {
        return service.addUpdateLibraryEntry(id, params);
    }

    public boolean removeLibraryEntry(String id, String authToken) {
        return service.removeLibraryEntry(id, authToken);
    }

    public List<FavoriteAnime> getFavoriteAnime(String username) {
        return service.getFavoriteAnime(username);
    }
}
