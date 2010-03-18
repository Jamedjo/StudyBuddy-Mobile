package jf.studybuddy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.View;
import jf.studybuddy.model.Note;

import java.io.File;

/**
 * @author alex
 */
public class NoteView extends View {
    //the note we're viewing
    private Note note;
    //and the image of that note
    private Bitmap img;
    //the current zoom level
    private float zoom;

    public NoteView(Context context) {
        this(context, null);
    }

    public NoteView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setNote(Note n) {
        this.note = n;

        //load the image from the file
        File imgFile = new File(Environment.getExternalStorageDirectory(),
                Note.FOLDER_PREFIX + "/" + n.getFileName());
        this.img = BitmapFactory.decodeFile(imgFile.toURI().toString());
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);

        c.drawBitmap(img, 0, 0, null);
    }
}
