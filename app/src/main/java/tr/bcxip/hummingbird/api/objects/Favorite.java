package tr.bcxip.hummingbird.api.objects;

/**
 * Created by Hikari on 10/9/14.
 */
public class Favorite {
    int id;
    int user_id;
    int item_id;
    String item_name;
    String created_at;
    String updated_at;
    int fav_rank;

    public int getId() {
        return id;
    }

    public int getUserId() {
        return user_id;
    }

    public int getItemId() {
        return item_id;
    }

    public String getItemName() {
        return item_name;
    }

    public String getCreatedAt() {
        return created_at; // TODO - Parsing
    }

    public String getUpdatedAt() {
        return updated_at; // TODO - Parsing
    }

    public int getFavRank() {
        return fav_rank;
    }
}
