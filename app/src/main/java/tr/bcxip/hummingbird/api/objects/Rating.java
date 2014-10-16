package tr.bcxip.hummingbird.api.objects;

/**
 * Created by Hikari on 10/9/14.
 */
public class Rating {
    String type;
    String value;

    final String TYPE_SIMPLE = "simple";
    final String TYPE_ADVANCED = "advanced";

    final String SIMPLE_NEGATIVE = "negative";
    final String SIMPLE_NEUTRAL = "neutral";
    final String SIMPLE_POSITIVE = "positive";

    public String getSimpleRating() {
        if (type.equals(TYPE_SIMPLE)) {
            Double dvalue = Double.parseDouble(value);
            if (dvalue > 0 && dvalue <= 2.4)
                return SIMPLE_NEGATIVE;
            if (dvalue > 2.4 && dvalue <= 3.6)
                return SIMPLE_NEUTRAL;
            if (dvalue > 3.6 && dvalue <= 5)
                return SIMPLE_POSITIVE;
            return "";
        } else {
            return null;
        }
    }

    public String getAdvancedRating() {
        return value;
    }

    public boolean isAdvanced() {
        return type.equals(TYPE_ADVANCED);
    }

    public boolean isSimple() {
        return type.equals(TYPE_SIMPLE);
    }
}
