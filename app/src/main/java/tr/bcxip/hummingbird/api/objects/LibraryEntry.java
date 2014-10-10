package tr.bcxip.hummingbird.api.objects;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Hikari on 10/8/14.
 */
public class LibraryEntry {
    String id; // Should be ignored according to the API docs
    int episodes_watched;
    String last_watched;
    int rewatched_times;
    String notes;
    boolean notes_present;
    String status;

    @SerializedName("private")
    boolean isPrivate;

    boolean rewatching;
    Anime anime;
    Rating rating;

    public int getEpisodesWatched() {
        return episodes_watched;
    }

    public String getLastWatched() {
        return last_watched; // TODO - Properly parse into a timestamp
    }

    public int getNumberOfRewatches() {
        return rewatched_times;
    }

    public String getNotes() {
        return notes;
    }

    public boolean doNotesExist() {
        return notes_present;
    }

    public String getStatus() {
        return status;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public boolean isRewatching() {
        return rewatching;
    }

    public Anime getAnime() {
        return anime;
    }

    public Rating getRating() {
        return rating;
    }
}
