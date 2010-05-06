package jf.studybuddy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.*;
import android.widget.*;
import jf.studybuddy.model.Note;
import jf.studybuddy.model.NoteGalleryImageAdaptor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Stack;

/**
 * The main Activity class.
 * Responsible for setting the initial content view and initializing layout elements
 * <p/>
 * TODO: Mobile user guide.
 * TODO: add an untagged category for images.
 * TODO: Add context-aware back button handling
 */
public class SBActivity extends Activity {
    //the tag to use when printing log statements
    public static final String TAG = "StudyBuddy";
    //our adaptor for the database
    private DbAdaptor db = new DbAdaptor(this);
    //our handler for callbacks to the UI thread
    private Handler handler = new Handler();
    //our View objects
    private View viewCaptureNote, viewNote, viewTagList, viewNoteGallery;
    //the currently active view
    private View activeView;
    //the previous views
    private Stack<View> previousViews = new Stack<View>();
    //bluetooth
    private BluetoothService bService = new BluetoothService(this);
    //activity request id
    public static final int REQUEST_ENABLE_BLUETOOTH = 0;
    //context menu ids
    private static final int CONTEXT_DELETE_TAG = 0;
    private static final int CONTEXT_META_TAGS = 1;
    private static final int CONTEXT_VIEW_NOTES = 2;
    private ProgressDialog btProgressDialog;
    //dialog ids
    public static final int DIALOG_SAVE_PROGRESS = 0;
    public static final int DIALOG_BLUETOOTH_PROGRESS = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //open up the DbAdaptor
        db.open();

        //gives us more screen space
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //need to do this because the camera doesn't do portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //initialize all our views.
        LayoutInflater inflater = getLayoutInflater();
        viewCaptureNote = inflater.inflate(R.layout.view_capture_note, null);
        viewNote = inflater.inflate(R.layout.view_note, null);
        viewTagList = inflater.inflate(R.layout.view_tag_list, null);
        viewNoteGallery = inflater.inflate(R.layout.view_note_gallery, null);

        //set the content view initially to save notes
        setContentView(viewCaptureNote);

        //set up the click listeners for the camera preview screen
        CameraPreview cameraPreview = (CameraPreview) findViewById(R.id.surface);
        findViewById(R.id.discard).setOnClickListener(cameraPreview);
        findViewById(R.id.take).setOnClickListener(cameraPreview);
        findViewById(R.id.save_untagged).setOnClickListener(cameraPreview);
        findViewById(R.id.save_and_tag).setOnClickListener(cameraPreview);

        //set up the 'view untagged' option in the list menu
        View someView = viewTagList.findViewById(R.id.tag_list_untagged);
        someView.findViewById(R.id.tag_subtags).setVisibility(View.GONE);
        ((TextView) someView.findViewById(R.id.tag_name)).setText("Click to view untagged notes");
        someView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Gallery g = (Gallery) viewNoteGallery.findViewById(R.id.note_gallery);
                g.setAdapter(new NoteGalleryImageAdaptor(SBActivity.this, db.getUntaggedNotes()));
                setContentView(viewNoteGallery);
            }
        });

        //set the list adaptor for our tag list
        final ListView lv = (ListView) viewTagList.findViewById(R.id.item_list);
        lv.setAdapter(new TagViewListAdaptor(this, db.getTagList()));
        registerForContextMenu(lv);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                TextView v = (TextView) view.findViewById(R.id.tag_subtags);
                v.setVisibility(v.getVisibility() == View.VISIBLE ?
                        View.GONE : View.VISIBLE);
                long tagId = (Long) view.findViewById(R.id.tag_name).getTag();
                if (v.getVisibility() == View.VISIBLE) {
                    int subtagCount = 0;
                    StringBuilder subTags = new StringBuilder("Sub-tags: ");
                    Cursor c = db.getSubTags(tagId);
                    while (c.moveToNext()) {
                        subTags.append(c.getString(1) + " ");
                        subtagCount++;
                    }
                    if (subtagCount > 0)
                        v.setText(subTags);
                    else v.setText("No sub-tags.");
                }
            }
        });
        lv.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                menu.setHeaderIcon(android.R.drawable.ic_menu_info_details);
                ListView lv = (ListView) viewTagList.findViewById(R.id.item_list);
                Cursor listItem = (Cursor) lv.getItemAtPosition(((AdapterView.AdapterContextMenuInfo) menuInfo).position);
                String tagName = listItem.getString(1);
                menu.setHeaderTitle("Tag '" + tagName + "'");
                menu.add(0, CONTEXT_VIEW_NOTES, 0, "View notes tagged with '" + tagName + "'");
                menu.add(0, CONTEXT_DELETE_TAG, 0, "Delete tag");
            }
        });

        //set up click listeners for add tag
        viewTagList.findViewById(R.id.save_tag).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                EditText editText = (EditText) viewTagList.findViewById(R.id.tag_id);
                if (editText.getText().length() == 0)
                    return;
                db.addNewTag(editText.getText());
                ((TagViewListAdaptor) lv.getAdapter()).refreshData();
                Toast.makeText(SBActivity.this, "Tag '" + editText.getText() +
                        "' added.", Toast.LENGTH_SHORT).show();
                editText.setText("");
            }
        });
        viewTagList.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                Log.i(TAG, "Focus changed innit: " + b);
                if (b)
                    ((TagViewListAdaptor) lv.getAdapter()).refreshData();
            }
        });

        //set up our note viewing gallery
        final Gallery g = (Gallery) viewNoteGallery.findViewById(R.id.note_gallery);
        g.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //set the note number i.e. 1/3
                ((TextView) viewNoteGallery.findViewById(R.id.gallery_note_number)).
                        setText((i + 1) + " / " + g.getCount());

                //set the date field
                Date d = new Date(((Note) view.getTag()).getSaveTime());
                ((TextView) viewNoteGallery.findViewById(R.id.gallery_note_date)).
                        setText(DateFormat.getDateTimeInstance().format(d));

                //set the tag field
                Cursor c = db.getTagListForImg(l);
                StringBuilder sb = new StringBuilder();
                while (c.moveToNext()) {
                    if (c.getInt(2) == 0)
                        continue;
                    sb.append(c.getString(1));
                    sb.append(", ");
                }
                //delete the last 2 chars: ", "
                if (sb.length() > 0)
                    sb.delete(sb.length() - 2, sb.length());
                else sb.append("none");
                ((TextView) viewNoteGallery.findViewById(R.id.gallery_note_tags)).
                        setText(sb.toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        g.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                return true;
            }
        });
        viewNoteGallery.findViewById(R.id.note_gallery_examine).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Note n = (Note)((View)g.getSelectedView()).getTag();
                setViewSingleNote(n.getFileName());
            }
        });
        

        Handler btProgressHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (btProgressDialog == null)
                    return;
                switch (msg.what) {
                    case BluetoothService.MSG_CONNECTED:
                        btProgressDialog.setMessage("Bluetooth connected! Sending updates...");
                        break;
                    case BluetoothService.MSG_RECEIVING:
                        btProgressDialog.setMessage("Receiving updates from computer..");
                        break;
                    case BluetoothService.MSG_PROGRESS:
                        int cur = msg.arg1;
                        int max = msg.arg2;
                        btProgressDialog.setProgress(cur);
                        btProgressDialog.setMax(max);
                        break;
                    case BluetoothService.MSG_DONE:
                        btProgressDialog.dismiss();
                        break;
                }
            }
        };
        bService.setProgressHandler(btProgressHandler);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                if (resultCode == BluetoothService.BLUETOOTH_DISCOVERABLE_DURATION) {
                    bService.bluetoothDiscoverable();
                } else {
                    // User did not enable Bluetooth or an error occured
                }
                break;
        }
    }


    @Override
    public void setContentView(View v) {
        previousViews.push(activeView);
        super.setContentView(v);
        activeView = v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bService.cancel();
        db.close();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_SAVE_PROGRESS:
                ProgressDialog imageSaving = new ProgressDialog(this);
                imageSaving.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                imageSaving.setMessage("Saving...");
                imageSaving.setCancelable(false);
                return imageSaving;
            case DIALOG_BLUETOOTH_PROGRESS:
                btProgressDialog = new ProgressDialog(this);
                btProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                btProgressDialog.setMessage("");
                return btProgressDialog;
            default:
                return null;
        }
    }

    @Override
    protected void onPrepareDialog(int id, Dialog d) {
        switch (id) {
            case DIALOG_BLUETOOTH_PROGRESS:
                btProgressDialog.setMessage("Awaiting connection..");
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case CONTEXT_DELETE_TAG:
                AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                db.deleteTag(menuInfo.id);
                ListView lv = (ListView) viewTagList.findViewById(R.id.item_list);
                Cursor listItem = (Cursor) lv.getItemAtPosition(menuInfo.position);
                String tagName = listItem.getString(1);
                ((TagViewListAdaptor) lv.getAdapter()).refreshData();
                Toast.makeText(SBActivity.this, "Tag '" + tagName +
                        "' deleted.", Toast.LENGTH_SHORT).show();
                return true;
            case CONTEXT_VIEW_NOTES:
                AdapterView.AdapterContextMenuInfo menuInfo2 = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                Gallery g = (Gallery) viewNoteGallery.findViewById(R.id.note_gallery);
                g.setAdapter(new NoteGalleryImageAdaptor(SBActivity.this, db.findNotesByTag(menuInfo2.id)));
                setContentView(viewNoteGallery);
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int key, KeyEvent ke) {
        boolean handled = false;
        if (key == KeyEvent.KEYCODE_BACK) {
            handled = goBack();
        }
        return handled ? true : super.onKeyDown(key, ke);
    }

    private boolean goBack() {
        boolean back = false;
            View previous = null;
            if (!previousViews.isEmpty() && (previous = previousViews.pop()) != null) {
                setContentView(previous);
                previousViews.pop();
                back = true;
            }
        return back;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        super.onCreateOptionsMenu(m);
        getMenuInflater().inflate(R.menu.main_menu, m);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu m) {
        super.onPrepareOptionsMenu(m);
        boolean showMenu = false;
        if (activeView == viewCaptureNote || activeView == viewTagList
                || activeView == viewNoteGallery) {
            m.findItem(R.id.menu_tag_note).setVisible(false);
            m.findItem(R.id.menu_delete_note).setVisible(false);
            showMenu = true;
        } else if (activeView == viewNote) {
            m.findItem(R.id.menu_tag_note).setVisible(true);
            m.findItem(R.id.menu_delete_note).setVisible(true);
            showMenu = true;
        }
        return showMenu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_view_tags:
                //little hack to refresh the data in our tag list view when we open it
                ListView lv = (ListView) viewTagList.findViewById(R.id.item_list);
                ((TagViewListAdaptor) lv.getAdapter()).refreshData();
                setContentView(viewTagList);
                return true;
            case R.id.menu_delete_note:
                Note n = ((NoteView)viewNote).getNote();
                db.deleteTag(n.getId());
                Toast.makeText(SBActivity.this, "Note " + n.getId() +
                        " deleted.", Toast.LENGTH_SHORT).show();
                goBack();
                return true;
            case R.id.menu_save_note:
                setContentView(viewCaptureNote);
                return true;
            case R.id.menu_tag_note:
                final Note curNote = ((NoteView) viewNote).getNote();
                final Cursor c = db.getTagListForImg(curNote.getId());
                CharSequence[] items = new CharSequence[c.getCount()];
                for (int i = 0; i < items.length; i++) {
                    c.moveToNext();
                    items[i] = c.getString(1);
                }

                final boolean[] checked = new boolean[c.getCount()];
                c.moveToFirst();
                for (int i = 0; i < items.length; i++) {
                    checked[i] = c.getInt(2) == 1;
                    c.moveToNext();
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Set tags for note " + curNote.getId())
                        .setIcon(android.R.drawable.ic_menu_edit)
                        .setMultiChoiceItems(items, checked, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                c.moveToPosition(i);
                                db.setTagForNote(curNote.getId(), c.getLong(0), b);
                            }
                        });
                builder.show();
                return true;
            case R.id.menu_bluetooth_sync:
                Cursor c1 = getDbAdaptor().getUpdateList(null);
                int idxId = c1.getColumnIndex("_id");
                int idxTable = c1.getColumnIndex("table_name");
                int idxTarget = c1.getColumnIndex("target_id");
                int idxTarget2 = c1.getColumnIndex("target2_id");
                int idxType = c1.getColumnIndex("update_type");
                int idxTime = c1.getColumnIndex("update_time");
                Log.i(getClass().getName(), "Update list:");
                while (c1.moveToNext()) {
                    Log.i(getClass().getName(), " [_id=" + c1.getInt(idxId) + "] " +
                            (c1.getInt(idxType) == DbAdaptor.UPDATE_TYPE_ADD ? "Added" : "Deleted") + " " +
                            "id " + c1.getInt(idxTarget) + (!c1.isNull(idxTarget2) ? " (id2 " + c1.getInt(idxTarget2) + ")" : "") +
                            " in table " + c1.getString(idxTable) + " (@ " +
                            new Date(c1.getInt(idxTime)) + ")");
                }

                if (bService.requestEnableBluetooth())
                    bService.bluetoothDiscoverable();
                return true;
        }
        return false;
    }

    /**
     * Saves the image on the SD card and inserts a record into the DB.
     *
     * @param lastPic the image to save
     * @return the filename of the saved image
     */
    public String saveImage(byte[] lastPic) {
        final String imageFileName = String.valueOf(System.currentTimeMillis()) + ".jpg";

        File root = Environment.getExternalStorageDirectory();

        //create the note folder if it doesn't exist
        File folder = new File(root, Note.FOLDER_PREFIX);
        if (!folder.isDirectory())
            folder.mkdir();

        //decode the image into a Bitmap
        Bitmap bmp = BitmapFactory.decodeByteArray(lastPic, 0, lastPic.length);

        File targetFile = new File(folder, imageFileName);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(targetFile);
            if (!bmp.compress(Bitmap.CompressFormat.JPEG, 90, out))
                throw new IOException("Error writing bmp to stream!");
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

        return imageFileName;
    }

    public void setViewSingleNote(String fileName) {
        Note n = db.findSingleNoteByFile(fileName);
        Log.i(TAG, fileName);
        setContentView(viewNote);
        NoteView noteView = (NoteView) findViewById(R.id.noteview);
        noteView.setNote(n);
    }

    public DbAdaptor getDbAdaptor() {
        return db;
    }

    public Handler getHandler() {
        return handler;
    }
}
