package tr.bcxip.hummingbird;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Hikari on 10/24/14.
 */
public class SearchFragment extends Fragment {

    private static final String TAG = "SEARCH FRAGMENT";
    public static final String TAG_SEARCH_FRAGMENT = "search_fragment";

    public static final String ARG_SEARCH_TYPE = "search_type";
    public static final String ARG_QUERY = "query";

    public static final String SEARCH_TYPE_GENERAL = "search_type_general";
    public static final String SEARCH_TYPE_LIBRARY = "search_type_library";

    Context context;

    View rootView;

    String SEARCH_TYPE;
    String QUERY;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_search, null);

        Bundle args = getArguments();
        if (args != null) {
            SEARCH_TYPE = args.getString(ARG_SEARCH_TYPE);
            QUERY = args.getString(ARG_QUERY);
        }

        if (SEARCH_TYPE == null || SEARCH_TYPE.equals("") || SEARCH_TYPE.trim().equals("")) {
            Log.e(TAG, "No search type was passed; Nothing to do...");
            return new View(context);
        }

        if (QUERY == null || QUERY.equals("") || QUERY.trim().equals("")) {
            Log.e(TAG, "No search query was passed; Can't do any kind of search");
            return new View(context);
        }

        TextView text = (TextView) rootView.findViewById(R.id.search_query);
        text.setText(QUERY);


        return rootView;
    }


}
