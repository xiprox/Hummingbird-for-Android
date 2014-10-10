package tr.bcxip.hummingbird.api.objects;

/**
 * Created by Hikari on 10/9/14.
 */
public class Favorite extends Anime {
    int fav_id;
    int fav_rank;

    public int getFavId() {
        return fav_id;
    }

    public int getFavRank() {
        return fav_rank;
    }
}
