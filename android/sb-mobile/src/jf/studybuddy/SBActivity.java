package jf.studybuddy;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import jf.studybuddy.model.Note;

/**
 * The main Activity class.
 * Responsible for setting the initial content view and initializing layout elements
 */
public class SBActivity extends Activity {
    //the tag to use when printing log statements
    public static final String TAG = "StudyBuddy";
    //arbitrary ID number to use for the 'saving picture' progress dialog
    public static final int SAVE_PROGRESS_DIALOG = 0;
    private DbAdaptor db = new DbAdaptor(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.save_notes);

        CameraPreview cameraPreview = (CameraPreview) findViewById(R.id.surface);
        //assign the click listener for the two buttons on the camera preview screen
        findViewById(R.id.discard).setOnClickListener(cameraPreview);
        findViewById(R.id.take).setOnClickListener(cameraPreview);
        findViewById(R.id.save_untagged).setOnClickListener(cameraPreview);
        findViewById(R.id.save_and_tag).setOnClickListener(cameraPreview);

        //open up the DbAdaptor
        db.open();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        db.close();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case SAVE_PROGRESS_DIALOG:
                ProgressDialog imageSaving = new ProgressDialog(this);
                imageSaving.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                imageSaving.setMessage("Saving...");
                imageSaving.setCancelable(false);
                return imageSaving;
            default:
                return null;
        }
    }

    public void setViewSingleNote(String fileName) {
        Note n = db.findSingleNoteByFile(fileName);
        setContentView(R.layout.view_note);
        NoteView noteView = (NoteView) findViewById(R.id.noteview);
        noteView.setNote(n);
    }

    public DbAdaptor getDbAdaptor() {
        return db;
    }
}
