package tr.bcxip.hummingbird.api;

import retrofit.http.GET;
import retrofit.http.Path;
import tr.bcxip.hummingbird.api.objects.Anime;

/**
 * Created by Hikari on 10/8/14.
 */
public interface HummingbirdServiceV2 {

    @GET("/anime/{id}")
    Anime getAnime(@Path("id") int id);

}
