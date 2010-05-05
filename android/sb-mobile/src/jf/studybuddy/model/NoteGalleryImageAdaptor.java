package jf.studybuddy.model;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import jf.studybuddy.SBActivity;

import java.io.File;

public class NoteGalleryImageAdaptor extends BaseAdapter {
    public static final int ITEM_HEIGHT = 360, ITEM_WIDTH = 420;
    private Context ctx;
    private ImageView[] loadedImages;
    private Cursor notes;

    public NoteGalleryImageAdaptor(Context ctx, Cursor c) {
        this.ctx = ctx;
        this.notes = c;
        this.loadedImages = new ImageView[getCount()];
    }

    @Override
    public int getCount() {
        return notes.getCount();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        notes.moveToPosition(i);
        return notes.getLong(0);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ImageView img;
        if (loadedImages[i] != null) {
            img = loadedImages[i];
        } else {
            notes.moveToPosition(i);

            File imgFile = new File(Environment.getExternalStorageDirectory(),
                    Note.FOLDER_PREFIX + "/" + notes.getString(1));
            img = new ImageView(ctx);
            img.setScaleType(ImageView.ScaleType.FIT_CENTER);
            img.setLayoutParams(new Gallery.LayoutParams(ITEM_WIDTH, ITEM_HEIGHT));
            BitmapFactory.Options loadOpts = new BitmapFactory.Options();
            loadOpts.outWidth = ITEM_WIDTH;
            loadOpts.outHeight = ITEM_HEIGHT;
            loadOpts.inSampleSize = 4;
            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.toString(), loadOpts);
            img.setImageBitmap(bitmap);

            //tag the view object with the note object associated with this note
            img.setTag(new Note(notes.getLong(0), notes.getString(1), notes.getLong(2)));
            
            loadedImages[i] = img;
        }
        return img;
    }
}
