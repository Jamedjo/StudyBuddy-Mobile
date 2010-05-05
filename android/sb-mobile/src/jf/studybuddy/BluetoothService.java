package jf.studybuddy;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.database.Cursor;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import jf.studybuddy.model.Note;
import jf.studybuddy.net.FrameHeader;

import java.io.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.UUID;

public class BluetoothService {
    public static final int BLUETOOTH_DISCOVERABLE_DURATION = 120;
    private static final int FRAME_STRING = 0, FRAME_IMG = 1,
            FRAME_START_NOTES = 2, FRAME_END_NOTES = 3;
    public static final int MSG_CONNECTED = 0, MSG_PROGRESS = 4, MSG_RECEIVING = 2, MSG_DONE = 3;
    private static final UUID uuid = UUID.fromString("7e1e6390-5782-11df-9879-0800200c9a66");

    private SBActivity sbActivity;
    private BluetoothAdapter btAdapter;
    private SyncService sync = new SyncService();
    private Handler progressHandler;

    public BluetoothService(SBActivity sb) {
        this.sbActivity = sb;
        this.btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * Will check if the device is connectable & discoverable and request the user enables it if not.
     *
     * @return true if the device is discoverable & connectable.
     */
    public boolean requestEnableBluetooth() {
        if (btAdapter == null) {
            handleBluetoothMsg(sbActivity.getString(R.string.bluetooth_no_adapter), null);
            return false;
        }

        int scanMode = btAdapter.getScanMode();

        if (scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, BLUETOOTH_DISCOVERABLE_DURATION);
            sbActivity.startActivityForResult(discoverableIntent, SBActivity.REQUEST_ENABLE_BLUETOOTH);
        }

        return scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE;
    }

    /**
     * Called when bluetooth is made discoverable by the user.
     * Spawn a new thread and initiate the transfer.
     */
    public void bluetoothDiscoverable() {
        sbActivity.getHandler().post(new Runnable() {
            public void run() {
                sbActivity.showDialog(SBActivity.DIALOG_BLUETOOTH_PROGRESS);
            }
        });
        new Thread(sync).start();
    }

    /**
     * Convenience method to handle bluetooth problems/issues. Displays a toast to the user and prints the exception
     * (if there was one) to the log.
     *
     * @param s the string to display to the user
     * @param e the exception or null if there wasn't one
     */
    private void handleBluetoothMsg(final String s, Exception e) {
        //has to be posted through the handler, otherwise it's not happy.
        sbActivity.getHandler().post(new Runnable() {
            public void run() {
                //display a toast
                Toast toast = Toast.makeText(sbActivity, "Bluetooth: " + s, Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        //print the error if there was one
        if (e != null)
            Log.e(SBActivity.TAG, "[Bluetooth Error] " + s, e);
    }

    public void setProgressHandler(Handler progressHandler) {
        this.progressHandler = progressHandler;
    }

    /**
     * Sends a message to the progress handler informing it of some progress made.
     *
     * @param cur the current progress tick
     * @param max the maximum progress tick
     */
    private void updateProgress(int cur, int max) {
        Message m = Message.obtain();
        m.what = MSG_PROGRESS;
        m.arg1 = cur;
        m.arg2 = max;
        progressHandler.sendMessage(m);
    }

    public void cancel() {
        sync.cancel();
    }

    /**
     * Represents a sync taking place.
     */
    private class SyncService implements Runnable {
        //we prepend every line we send with a number based on what it is.
        private static final int SYNC_TYPE_IMG = 1,
                SYNC_TYPE_TAG = 2;
        private boolean running = false;
        private BluetoothSocket socket;

        public void run() {
            running = true;
            BluetoothServerSocket tmp = null;
            BluetoothSocket socket = null;
            try {
                tmp = btAdapter.listenUsingRfcommWithServiceRecord(SBActivity.TAG, uuid);
                socket = tmp.accept();

                Message m = Message.obtain();
                m.what = MSG_CONNECTED;
                progressHandler.sendMessage(m);

                //once we open our socket, close the server socket
                tmp.close();
            } catch (IOException e) {
                handleBluetoothMsg(sbActivity.getString(R.string.bluetooth_connect_failed), e);
                cancel();
                //connection failed, no point hanging around
                return;
            }

            /**
             * From here on in, initiate the sync.
             */
            DbAdaptor db = sbActivity.getDbAdaptor();

            /**
             * Firstly, build a list of new image filenames and new tag names to send over
             */
            HashSet<Integer> updatedImgIds = getUpdatedIds("imgs");
            StringBuilder newImgs = new StringBuilder();
            for (Cursor allImgs = db.getImgList(); allImgs.moveToNext();) {
                if (updatedImgIds.contains(allImgs.getInt(0)))
                    newImgs.append(SYNC_TYPE_IMG).append(",").append(allImgs.getString(1)).append("\n");
            }
            Log.i(SBActivity.TAG, newImgs.toString());

            HashSet<Integer> updatedTagIds = getUpdatedIds("tags");
            StringBuilder newTags = new StringBuilder();
            for (Cursor allTags = db.getTagList(); allTags.moveToNext();) {
                if (updatedTagIds.contains(allTags.getInt(0)))
                    newTags.append(SYNC_TYPE_TAG).append(",").append(allTags.getString(1)).append("\n");
            }

            try {

                //send the strings built
                FrameHeader.writeToStream(socket.getOutputStream(), FrameHeader.TYPE_NEWDBVALUES, newTags.length() + newImgs.length());
                socket.getOutputStream().write(newImgs.toString().getBytes());
                socket.getOutputStream().write(newTags.toString().getBytes());
                FrameHeader.writeToStream(socket.getOutputStream(), FrameHeader.TYPE_FINISHED_SENDING);

                /**
                 * Next, we get ready to recieve a list of what we send over but with the computer_id value appended.
                 * We then insert this value into our database.
                 */
                FrameHeader fh = FrameHeader.readFromStream(socket.getInputStream());
                Log.i(getClass().getName(), "frame.type=" + fh.getType() + ", length=" + fh.getLength());

                byte[] buf = new byte[fh.getLength()];
                new DataInputStream(socket.getInputStream()).readFully(buf);
                BufferedReader brIn = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buf)));
                String inStr;
                while ((inStr = brIn.readLine()) != null) {
                    String[] strArgs = inStr.split(",");
                    int syncType = Integer.parseInt(strArgs[0]);
                    String value = strArgs[1];
                    int computerId = Integer.parseInt(strArgs[2]);
                    Log.i(getClass().getName(), "syncType=" + syncType + ",value=" + value + ",computerId=" + computerId);
                    db.updateComputerId(syncType == SYNC_TYPE_IMG ? "imgs" : "tags",
                            syncType == SYNC_TYPE_IMG ? "file" : "value", value, computerId);
                }

                FrameHeader fh2 = FrameHeader.readFromStream(socket.getInputStream());
                Log.i(getClass().getName(), "Recieved frame type " + fh.getType());

                /**
                 * Now we send all our updated note_tag records to the computer, with the computer's own ids.
                 */
                Cursor updates = db.getUpdateList("note_tags");
                StringBuilder sb = new StringBuilder();
                while (updates.moveToNext()) {
                    int tagId = db.findComputerId("tags", updates.getInt(2));
                    int imgId = db.findComputerId("imgs", updates.getInt(3));
                    sb.append("3," + (updates.getInt(4) == DbAdaptor.UPDATE_TYPE_ADD ? "Add" : "Delete") +
                            "," + imgId + "," + tagId + "\n");
                }
                updates.close();
                FrameHeader.writeToStream(socket.getOutputStream(), FrameHeader.TYPE_SYNC, sb.length());
                socket.getOutputStream().write(sb.toString().getBytes());

                /**
                 * Start sending images to the computer.
                 */
                FrameHeader.writeToStream(socket.getOutputStream(), FrameHeader.TYPE_IMGS_START);

                int totalSize = 0;
                LinkedList<File> filesToSend = new LinkedList<File>();
                // write all the images
                for (Cursor allImgs = db.getImgList(); allImgs.moveToNext();) {
                    if (updatedImgIds.contains(allImgs.getInt(0))) {
                        File f = new File(Environment.getExternalStorageDirectory(),
                                Note.FOLDER_PREFIX + "/" + allImgs.getString(1));
                        totalSize += f.length();
                        filesToSend.add(f);
                    }
                }

                byte[] buf1 = new byte[10240];
                int read = -1;
                int progress = 0;
                for (File f : filesToSend) {
                    Log.i(getClass().getName(), "Sending image " + f);
                    String fileName = f.getName();
                    FrameHeader.writeToStream(socket.getOutputStream(), FrameHeader.TYPE_IMGS_FILENAME,
                            fileName.length());
                    socket.getOutputStream().write(fileName.getBytes());
                    FrameHeader.writeToStream(socket.getOutputStream(), FrameHeader.TYPE_BINARY,
                            (int) f.length());
                    FileInputStream fis = new FileInputStream(f);
                    while ((read = fis.read(buf1)) != -1) {
                        progress += read;
                        socket.getOutputStream().write(buf1, 0, read);
                        updateProgress(progress, totalSize);
                    }
                    fis.close();
                }

                FrameHeader.writeToStream(socket.getOutputStream(), FrameHeader.TYPE_IMGS_STOP);

                FrameHeader.writeToStream(socket.getOutputStream(), FrameHeader.TYPE_FINISHED_SENDING);

                db.clearUpdateTable();

                Message m = Message.obtain();
                m.what = MSG_RECEIVING;
                progressHandler.sendMessage(m);

                fh = FrameHeader.readFromStream(socket.getInputStream());
                buf = new byte[fh.getLength()];
                new DataInputStream(socket.getInputStream()).readFully(buf);
                brIn = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buf)));
                while ((inStr = brIn.readLine()) != null) {
                    String[] strArgs = inStr.split(",");
                    int syncType = Integer.parseInt(strArgs[0]);
                    boolean add = strArgs[1].equals("Add");
                    switch (syncType) {
                        case 1: //image change
                            int compImgId = Integer.parseInt(strArgs[2]);
                            String imgFileName = strArgs[3];
                            db.addImgFromComputer(compImgId, imgFileName, add);
                            break;
                        case 2: //tag change
                            int compTagId = Integer.parseInt(strArgs[2]);
                            String tagTitle = strArgs[3];
                            db.addTagFromComputer(tagTitle, compTagId, add);
                            break;
                        case 3: //note_tag
                            int compImgId1 = Integer.parseInt(strArgs[2]);
                            int compTagId1 = Integer.parseInt(strArgs[3]);
                            db.addNoteTagFromComputer(compImgId1, compTagId1, add);
                            break;
                        case 4: //tag_tag
                            //ignore
                            break;
                    }
                }

                //comp sending type_imgs_start
                FrameHeader imgsStart = FrameHeader.readFromStream(socket.getInputStream());
                Log.i(SBActivity.TAG, "imgs_start="+imgsStart.getType());

                while (true) {
                    fh = FrameHeader.readFromStream(socket.getInputStream());

                    Log.i(SBActivity.TAG, "type: "+fh.getType());
                    if (fh.getType() == FrameHeader.TYPE_IMGS_STOP)
                        break;

                    //comp sending a string
                    Log.i(SBActivity.TAG, "filename header type: "+fh.getType());
                    buf = new byte[fh.getLength()];
                    new DataInputStream(socket.getInputStream()).readFully(buf);
                    String filename = new String(buf);
                    Log.i(SBActivity.TAG, "filename: '"+filename+"'");

                    FrameHeader imgfileHeader = FrameHeader.readFromStream(socket.getInputStream());
                    Log.i(SBActivity.TAG, "receiving image length "+imgfileHeader.getLength());
                    FileOutputStream fos = new FileOutputStream(
                            new File(Environment.getExternalStorageDirectory(),
                                    Note.FOLDER_PREFIX + "/" + filename));
                    Log.i(SBActivity.TAG, "saving to "+fos.getFD().toString());
                    /*buf = new byte[10240];
                    progress = 0;
                    int remaining = imgfileHeader.getLength();
                    while ((read = socket.getInputStream().read(buf, 0, (remaining>buf.length) ? buf.length : remaining)) != -1) {
                        fos.write(buf, 0, read);
                        remaining -= read;
                        progress += read;
                        updateProgress(progress, imgfileHeader.getLength());
                    }*/
                    for(int i=0; i<imgfileHeader.getLength(); i++)
                        fos.write(socket.getInputStream().read());
                    Log.i(SBActivity.TAG, "done saving image!");
                }

                //comp letting us know it's finished sending
                FrameHeader.readFromStream(socket.getInputStream());
                Log.i(SBActivity.TAG, "received stop_img");

                FrameHeader.writeToStream(socket.getOutputStream(), FrameHeader.TYPE_DONE_AND_DONE);

                m = Message.obtain();
                m.what = MSG_DONE;
                progressHandler.sendMessage(m);

                handleBluetoothMsg(sbActivity.getString(R.string.bluetooth_done), null);
            } catch (IOException ioe) {
                handleBluetoothMsg(sbActivity.getString(R.string.bluetooth_error_sending), ioe);
            } finally {
                cancel();
            }
        }

        private final HashSet<Integer> getUpdatedIds(String table) {
            HashSet<Integer> updatedIds = new HashSet<Integer>();
            for (Cursor updatedTags = sbActivity.getDbAdaptor().getUpdateList(table); updatedTags.moveToNext();) {
                //add the updated target ids (so, all the new target ids) to the set
                updatedIds.add(updatedTags.getInt(2));
            }
            return updatedIds;
        }

        /**
         * Closes the socket if open, sets running to false.
         */
        public void cancel() {
            running = false;
            updateProgress(0, 0);
            try {
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                //ignore, don't care
            }
        }
    }
}
