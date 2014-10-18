package tr.bcxip.hummingbird;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Hikari on 10/9/14.
 */
public class MainFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, null);

        final EditText mId = (EditText) rootView.findViewById(R.id.anime_id);
        Button mGo = (Button) rootView.findViewById(R.id.load_by_id);

        mGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AnimeDetailsActivity.class);
                intent.putExtra(AnimeDetailsActivity.ARG_ID, Integer.parseInt(mId.getText().toString()));
                startActivity(intent);
            }
        });

        return rootView;
    }
}
