package tr.bcxip.hummingbird.api.objects;

import java.util.List;

/**
 * Created by Hikari on 10/9/14.
 */
public class User {
    String name;
    String waifu;
    String waifu_or_husbando;
    String waifu_slug;
    String waituf_char_id;
    String location;
    String website;
    String avatar;
    String cover_image;
    String about;
    String bio;
    int karma;
    int life_spent_on_anime;
    boolean show_adult_content;
    String title_language_preference;
    String last_library_update;
    boolean online;
    boolean following;
    List<Favorite> favorites;

    public String getName() {
        return name;
    }
    /*
    public String getLocation() {
        return location;
    }
    public String getWebsite() {
        return website;
    }
    public String getAvatar() {
        return avatar;
    }
    public String getCover() {
        return cover_image;
    }
    public String getAbout() {
        return about;
    }
    public String getBio() {
        return bio;
    }
    public int getKarma() {
        return karma;
    }
    public int getLifeSpent() {
        return life_spent_on_anime;
    }
    public boolean getAdultConent() {
        return show_adult_content;
    }
    public String getTitleLanguage() {
        return title_language_preference;
    }
    public String getLastLibaryUpdate() {
        return last_library_update;
    }
    public boolean getOnline() {
        return online;
    }
    public boolean getFollowing() {
        return following;
    }
    public List<Favorite> getFavorites() {
        return favorites;
    }
    */
}
