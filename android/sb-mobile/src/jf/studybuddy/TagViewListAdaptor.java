package jf.studybuddy;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 */
public class TagViewListAdaptor extends CursorAdapter {
    private LayoutInflater inflater;

    public TagViewListAdaptor(Context context, Cursor c) {
        super(context, c);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View v = inflater.inflate(R.layout.view_tag_item, viewGroup, false);
        bindView(v, context, cursor);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        View v = view.findViewById(R.id.tag_subtags);
        v.setVisibility(View.GONE);
        TextView tagName = (TextView) view.findViewById(R.id.tag_name);
        tagName.setText(cursor.getString(1));
        tagName.setTag(cursor.getLong(0));
        TextView imgCount = (TextView) view.findViewById(R.id.tag_count);
        imgCount.setText("("+cursor.getInt(2)+" notes)");
    }

    /**
     * Re-queries the cursor and refreshes the data in the list.
     */
    public void refreshData() {
        getCursor().requery();
        notifyDataSetChanged();
    }
}
