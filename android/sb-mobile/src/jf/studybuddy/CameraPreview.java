package jf.studybuddy;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.IOException;

/**
 * Represents the View object for camera previews.
 *
 * @author alex
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback,
        View.OnClickListener, Camera.PictureCallback, Camera.AutoFocusCallback {
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
    public void onClick(final View view) {
        if (view.getId() == R.id.take) {
            camera.autoFocus(this);
            sb.findViewById(R.id.take).setEnabled(false);
        } else if (view.getId() == R.id.discard) {
            discardPicture();
            setTakePictureButtons(true);
        } else if (R.id.save_untagged == view.getId() || view.getId() == R.id.save_and_tag) {
            sb.showDialog(SBActivity.DIALOG_SAVE_PROGRESS);

            new Thread(new Runnable() {
                public void run() {
                    final String fileName = sb.saveImage(lastPic);
                    sb.getHandler().post(new Runnable() {
                        public void run() {
                            if (view.getId() == R.id.save_untagged) {
                                camera.startPreview();
                                setTakePictureButtons(true);
                            } else {
                                setTakePictureButtons(true);
                                sb.setViewSingleNote(fileName);
                            }
                            sb.dismissDialog(SBActivity.DIALOG_SAVE_PROGRESS);
                        }
                    });
                }
            }).start();

        }
    }

    public void setTakePictureButtons(boolean takePic) {
        sb.findViewById(R.id.take).setEnabled(takePic);
        sb.findViewById(R.id.save_and_tag).setEnabled(!takePic);
        sb.findViewById(R.id.save_untagged).setEnabled(!takePic);
        sb.findViewById(R.id.discard).setEnabled(!takePic);
    }

    public void discardPicture() {
        camera.startPreview();
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
        setTakePictureButtons(false);
    }

    public void onAutoFocus(boolean b, Camera camera) {
        camera.takePicture(null, null, this);
    }
}
