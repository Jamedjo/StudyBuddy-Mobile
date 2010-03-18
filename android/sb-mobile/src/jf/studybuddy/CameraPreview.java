package jf.studybuddy;

import android.content.Context;
import android.hardware.Camera;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import jf.studybuddy.model.Note;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Represents the View object for camera previews.
 *
 * @author alex
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback,
        View.OnClickListener, Camera.PictureCallback {
    private SBActivity sb;
    private Camera camera;
    private DbAdaptor db;
    private byte[] lastPic;

    public CameraPreview(Context c, AttributeSet attr) {
        super(c, attr);

        sb = (SBActivity) c;
        db = sb.getDbAdaptor();

        SurfaceHolder surface = getHolder();
        surface.addCallback(this);
        surface.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /**
     * Handle button presses from the four buttons on this screen.
     */
    public void onClick(View view) {
        if (view.getId() == R.id.take) {
            camera.takePicture(null, null, this);
        } else if (view.getId() == R.id.discard) {
            camera.startPreview();
        } else if (view.getId() == R.id.save_untagged || view.getId() == R.id.save_and_tag) {
            sb.showDialog(SBActivity.SAVE_PROGRESS_DIALOG);
            final String imageFileName = String.valueOf(System.currentTimeMillis()) + ".jpg";

            new Thread(new Runnable() {
                public void run() {

                    File root = Environment.getExternalStorageDirectory();

                    //create the note folder if it doesn't exist
                    File folder = new File(root, Note.FOLDER_PREFIX);
                    if (!folder.isDirectory())
                        folder.mkdir();

                    File targetFile = new File(folder, imageFileName);
                    OutputStream out = null;
                    try {
                        out = new FileOutputStream(targetFile);
                        out.write(lastPic);
                        db.addNewImage(imageFileName);
                    } catch (IOException e) {
                        Log.e(SBActivity.TAG, "Error writing image file", e);
                    } finally {
                        try {
                            out.close();
                        } catch (Exception e) {
                            //ignore
                        }
                    }

                    sb.dismissDialog(SBActivity.SAVE_PROGRESS_DIALOG);
                }
            }).start();
            switch (view.getId()) {
                case R.id.save_untagged:
                    camera.startPreview();
                    break;
                case R.id.save_and_tag:
                    toggleButtons();
                    sb.setViewSingleNote(imageFileName);
                    return;
            }
        }
        toggleButtons();
    }

    /**
     * When the user clicks the 'Take' button a preview shows up, at which point the button
     * should be disabled and the other three should be shown.
     */
    public void toggleButtons() {
        //grab references to our buttons
        Button takePic = (Button) sb.findViewById(R.id.take);
        Button saveUntagged = (Button) sb.findViewById(R.id.save_untagged);
        Button saveAndTag = (Button) sb.findViewById(R.id.save_and_tag);
        Button discard = (Button) sb.findViewById(R.id.discard);

        boolean toggle = takePic.isEnabled();
        takePic.setEnabled(!toggle);
        saveUntagged.setEnabled(toggle);
        saveAndTag.setEnabled(toggle);
        discard.setEnabled(toggle);
    }

    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        camera = Camera.open();
    }

    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int w, int h) {
        try {
            camera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            Log.w(SBActivity.TAG, "Error setting preview display", e);
        }
        camera.startPreview();
    }

    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        camera.stopPreview();
        releaseCamera();
    }

    private void releaseCamera() {
        camera.release();
        camera = null;
    }

    public void onPictureTaken(byte[] bytes, Camera camera) {
        lastPic = bytes;
    }
}
