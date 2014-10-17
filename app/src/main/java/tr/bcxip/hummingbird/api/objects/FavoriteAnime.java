package tr.bcxip.hummingbird.api.objects;

/**
 * Created by Hikari on 10/17/14.
 */
public class FavoriteAnime extends Anime {
    int fav_rank;
    String fav_id;

    public int getFavRank() {
        return fav_rank;
    }

    public String getFavId() {
        return fav_id;
    }
}
