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
    boolean following; // Should be ignored according to the API docs
    List<Favorite> favorites;

    public String getName() {
        return name;
    }

    public String getWaifu() {
        return waifu;
    }

    public String getWaifuOrHusbando() {
        return waifu_or_husbando;
    }

    public String getWaifuSlug() {
        return waifu_slug;
    }

    public String getWaifuCharId() {
        return waituf_char_id;
    }

    public String getLocation() {
        return location;
    }

    public String getWebsite() {
        return website;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getCoverImage() {
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

    public int getLifeSpentOnAnime() {
        return life_spent_on_anime;
    }

    public boolean showAdultAdultContent() {
        return show_adult_content;
    }

    public String getTitleLanguagePreference() {
        return title_language_preference;
    }

    public String getLastLibraryUpdate() {
        return last_library_update;
    }

    public boolean isOnline() {
        return online;
    }

    public List<Favorite> getFavorites() {
        return favorites;
    }
}
