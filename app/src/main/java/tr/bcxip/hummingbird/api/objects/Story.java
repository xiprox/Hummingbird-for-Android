package tr.bcxip.hummingbird.api.objects;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Hikari on 10/10/14.
 */
public class Story {
    String id;
    String story_type;
    UserMini user;
    String updated_at;
    boolean self_post;
    UserMini poster;
    Anime media;
    int substories_count;
    List<Substory> substories;

    public static final String STORY_TYPE_COMMENT = "comment";
    public static final String STORY_TYPE_MEDIA = "media_story";

    public String getId() {
        return id;
    }

    public String getStoryType() {
        return story_type;
    }

    public UserMini getUser() {
        return user;
    }

    public String getUpdatedAt() {
        return updated_at;
    }

    public boolean getSelfPost() {
        return self_post;
    }

    public UserMini getPoster() {
        return poster;
    }

    public Anime getMedia() {
        return media;
    }

    public int getSubstoriesCount() {
        return substories_count;
    }

    public List<Substory> getSubstories() {
        return substories;
    }

}
