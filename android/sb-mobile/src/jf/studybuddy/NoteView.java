package jf.studybuddy;

import android.content.Context;
import android.graphics.*;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ZoomButtonsController;
import jf.studybuddy.model.Note;

import java.io.File;

/**
 * @author alex
 */
public class NoteView extends View implements ZoomButtonsController.OnZoomListener {
    //our zoom buttons!
    private ZoomButtonsController zoomButtons;
    //the note we're viewing
    private Note note;
    //and the image of that note
    private Bitmap img;
    //the minimum zoom level
    private double fitZoom = -1;
    //minimum acceptable zoom level
    private double minimumZoom;
    //our canvas
    private RectF canvasRect = null;
    //our zoomindex
    private int zoomIndex = 0;
    private final double[] zooms = {0, 0.25, 0.6, 1.2};
    //movement offset
    private double x = 0, y = 0;
    //temp variables for touch sensing
    private double tX, tY;
    //move a little quicker than normal
    private static final double MOVE_MULTIPLIER = 3;

    public NoteView(Context context) {
        this(context, null);
    }

    public NoteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        zoomButtons = new ZoomButtonsController(this);
        zoomButtons.setOnZoomListener(this);
        zoomButtons.setZoomOutEnabled(false);
    }

    public void setNote(Note n) {
        this.note = n;

        //load the image from the file
        File imgFile = new File(Environment.getExternalStorageDirectory(),
                Note.FOLDER_PREFIX + "/" + n.getFileName());
        this.img = BitmapFactory.decodeFile(imgFile.toString());
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        //just used for sanity checking some stuff.
        Log.v(SBActivity.TAG, "SizeChanged: [" + oldw + "," + oldh + "] -> [" + w + "," + h + "]");
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        super.onTouchEvent(me);
        zoomButtons.setVisible(true);
        int code = me.getAction() & MotionEvent.ACTION_MASK;
        if (code == MotionEvent.ACTION_DOWN) {
            tX = me.getX();
            tY = me.getY();
        } else if (code == MotionEvent.ACTION_MOVE) {
            x += (me.getX() - tX) * MOVE_MULTIPLIER;
            y += (me.getY() - tY) * MOVE_MULTIPLIER;
            tX = me.getX();
            tY = me.getY();
        }
        invalidate();
        return true;
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);

        if (fitZoom == -1) {
            double zoomX = (double) c.getWidth() / (double) img.getWidth(),
                    zoomY = (double) c.getHeight() / (double) img.getHeight();
            fitZoom = Math.min(zoomX, zoomY);
        }

        //it works, don't touch it
        int w = (int) ((fitZoom + (zooms[zoomIndex])) * img.getWidth()),
                h = (int) ((fitZoom + (zooms[zoomIndex])) * img.getHeight());
        int offX = c.getWidth() - w, offY = c.getHeight() - h;
        int tempX = (int) (x * fitZoom + (zooms[zoomIndex])),
                tempY = (int) (y * fitZoom + (zooms[zoomIndex]));
        canvasRect = new RectF(tempX + (offX / 2), tempY + (offY / 2),
                tempX + (c.getWidth() - (offX / 2)), tempY + (c.getHeight() - (offY / 2)));

        if (!img.isRecycled())
            c.drawBitmap(img, new Rect(0, 0, img.getWidth(), img.getHeight()),
                    canvasRect, null);
    }

    @Override
    public void onDetachedFromWindow() {
        //if we lost visibility, discard the bitmap
        Log.i(SBActivity.TAG, "NoteView lost visibility, discarding bitmap");
        img.recycle();

        //also get rid of the zoom buttons since we're not the main window anymore
        zoomButtons.setVisible(false);
    }

    @Override
    public void onVisibilityChanged(boolean b) {
    }

    @Override
    public void onZoom(boolean zoomIn) {
        zoomIndex += zoomIn ? 1 : -1;
        invalidate();
        zoomButtons.setZoomOutEnabled(zoomIndex > 0);
        zoomButtons.setZoomInEnabled(zoomIndex < zooms.length - 1);

        if (zoomIndex == 0)
            x = y = 0;
    }

    public Note getNote() {
        return note;
    }
}
