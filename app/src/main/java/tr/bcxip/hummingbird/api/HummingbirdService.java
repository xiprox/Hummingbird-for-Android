package tr.bcxip.hummingbird.api;

import java.util.List;
import java.util.Map;

import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.QueryMap;
import tr.bcxip.hummingbird.api.objects.Anime;
import tr.bcxip.hummingbird.api.objects.Favorite;
import tr.bcxip.hummingbird.api.objects.LibraryEntry;
import tr.bcxip.hummingbird.api.objects.Story;
import tr.bcxip.hummingbird.api.objects.User;

/**
 * Created by Hikari on 10/8/14.
 */
public interface HummingbirdService {

    @POST("/users/authenticate")
    public String authenticate(@QueryMap Map<String, String> options);

    @GET("/users/{username}")
    public User getUser(@Path("username") String username);

    @GET("/search/anime")
    public List<Anime> searchAnime(@Query("query") String title);

    @GET("/users/{username}/library")
    public List<LibraryEntry> getLibrary(
            @Path("username") String username,
            @Query("status") String status
    );

    @POST("/libraries/{id}")
    public LibraryEntry addUpdateLibraryEntry(@Path("id") String id,
                                              @QueryMap Map<String, String> params);

    @POST("/libraries/{id}/remove")
    public boolean removeLibraryEntry(
            @Path("id") String id,
            @Query("auth_token") String authToken
    );

    @GET("/users/{username}/feed")
    public List<Story> getFeed(@Path("username") String username);

    @GET("/users/{username}/favorite_anime")
    public List<Favorite> getFavorites(@Path("username") String username);
}
