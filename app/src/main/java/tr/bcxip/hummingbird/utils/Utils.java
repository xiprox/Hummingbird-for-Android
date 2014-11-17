package tr.bcxip.hummingbird.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;

import org.joda.time.format.ISODateTimeFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import tr.bcxip.hummingbird.api.objects.Story;
import tr.bcxip.hummingbird.api.objects.Substory;

/**
 * Created by Hikari on 10/19/14.
 */
public class Utils {

    public static long getTimestampFromISO8601(String iso8601) {
        return ISODateTimeFormat.dateTime().parseMillis(iso8601);
    }

    public static long getTimestampFromAirDate(String airDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = sdf.parse(airDate);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static List<Story> sortStoriesByDate(List<Story> list) {
        Collections.sort(list, new Comparator<Story>() {
            public int compare(Story item1, Story item2) {
                return new Date(item2.getUpdatedAt()).compareTo(new Date(item1.getUpdatedAt()));
            }
        });
        return list;
    }

    public static List<Substory> sortSubstoriesByDate(List<Substory> list) {
        Collections.sort(list, new Comparator<Substory>() {
            public int compare(Substory item1, Substory item2) {
                return new Date(item2.getCreatedAt()).compareTo(new Date(item1.getCreatedAt()));
            }
        });
        return list;
    }

    public static int pxToDp(Context context, float px) {
        return (int) (px / context.getResources().getDisplayMetrics().density);
    }

    public static int dpToPx(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    public static void startActivityWithTransition(Context context, Intent intent,
                                                   ActivityOptionsCompat transition) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            context.startActivity(intent, transition.toBundle());
        else
            context.startActivity(intent);
    }
}
