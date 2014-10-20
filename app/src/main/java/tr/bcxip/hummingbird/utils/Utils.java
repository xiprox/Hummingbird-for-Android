package tr.bcxip.hummingbird.utils;

import org.joda.time.format.ISODateTimeFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

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

    public static List<Substory> sortSubstoriesByDate(List<Substory> list) {
        Collections.sort(list, new Comparator<Substory>() {
            public int compare(Substory item1, Substory item2) {
                return new Date(item2.getCreatedAt()).compareTo(new Date(item1.getCreatedAt()));
            }
        });
        return list;
    }
}
