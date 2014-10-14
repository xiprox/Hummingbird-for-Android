package tr.bcxip.hummingbird.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import tr.bcxip.hummingbird.LibraryFragment;
import tr.bcxip.hummingbird.R;
import tr.bcxip.hummingbird.api.objects.LibraryEntry;

/**
 * Created by Hikari on 10/14/14.
 */
public class LibraryAdapter extends ArrayAdapter<LibraryEntry> {

    Context context;

    List<LibraryEntry> mItems;

    public LibraryAdapter(Context context, int resource, List<LibraryEntry> items) {
        super(context, resource, items);
        this.context = context;
        mItems = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_library, parent, false);

            holder = new ViewHolder();
            holder.cover = (ImageView) convertView.findViewById(R.id.item_library_cover);
            holder.infoHolder = (LinearLayout) convertView.findViewById(R.id.item_library_info_holder);
            holder.title = (TextView) convertView.findViewById(R.id.item_library_title);
            holder.desc = (TextView) convertView.findViewById(R.id.item_library_desc);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LibraryEntry item = mItems.get(position);

        Log.d("TAG", item.getAnime().getCoverImage());

        Picasso.with(context)
                .load(item.getAnime().getCoverImage())
                .into(holder.cover, new Callback() {
                    @Override
                    public void onSuccess() {
                        try {
                            Bitmap bitmap = ((BitmapDrawable) holder.cover.getDrawable()).getBitmap();
                            int color = Palette.generate(bitmap)
                                    .getDarkMutedColor().getRgb();
                            ColorDrawable background = new ColorDrawable(color);
                            holder.infoHolder.setBackgroundDrawable(background);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError() {

                    }
                });

        holder.title.setText(item.getAnime().getTitle());

        String status = item.getStatus();
        if (status.equals(LibraryFragment.FILTER_CURRENTLY_WATCHING))
            holder.desc.setText(getString(R.string.content_library_watching_at)
                    + " " + item.getEpisodesWatched()
                    + "/" + item.getAnime().getEpisodeCount());
        if (status.equals(LibraryFragment.FILTER_PLAN_TO_WATCH))
            holder.desc.setText(getString(R.string.content_library_planning_to_watch));
        if (status.equals(LibraryFragment.FILTER_COMPLETED))
            holder.desc.setText(getString(R.string.content_library_completed));
        if (status.equals(LibraryFragment.FILTER_ON_HOLD))
            holder.desc.setText(getString(R.string.content_library_on_hold));
        if (status.equals(LibraryFragment.FILTER_DROPPED))
            holder.desc.setText(getString(R.string.content_library_dropped));

        return convertView;
    }

    static class ViewHolder {
        ImageView cover;
        LinearLayout infoHolder;
        TextView title;
        TextView desc;
    }

    private String getString(int res) {
        return context.getString(res);
    }
}
