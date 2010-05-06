package jf.studybuddy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import jf.studybuddy.model.Note;

/**
 * @author tim
 */
public class DbAdaptor {
    /**
     * Constants for the update_type field in the update table. SQLIte doesn't support enums, sad times.
     * ADD works for adding a single image or tag, or for adding a record to note_tags or meta_tags with
     * two IDs. Same with DEL.
     */
    public static final int UPDATE_TYPE_ADD = 0,
            UPDATE_TYPE_DEL = 1;

    private static final String TAG = "DbAdaptor";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME = "sbdata";
    private static final int DATABASE_VERSION = 4;

    private final Context ctx;

    public DbAdaptor(Context c) {
        this.ctx = c;
    }

    public DbAdaptor open() throws SQLException {
        mDbHelper = new DatabaseHelper(ctx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    public void addNewImage(String fname) {
        ContentValues vals = new ContentValues(2);
        vals.put("file", fname);
        vals.put("time", System.currentTimeMillis());
        long newImgId = mDb.insert("imgs", null, vals);

        addUpdateRecord("imgs", newImgId, UPDATE_TYPE_ADD);
    }

    public long addNewTag(CharSequence text) {
        ContentValues content = new ContentValues(1);
        content.put("value", text.toString());
        long newId = mDb.insert("tags", null, content);

        addUpdateRecord("tags", newId, UPDATE_TYPE_ADD);
        return newId;
    }

    public void deleteTag(long id) {
        String idString = String.valueOf(id);
        mDb.delete("tags", "_id = ?", new String[]{idString});
        mDb.delete("meta_tags", "tag1_id = ?", new String[]{idString});
        mDb.delete("meta_tags", "tag2_id = ?", new String[]{idString});
        mDb.delete("note_tags", "tag_id = ?", new String[]{idString});

        addUpdateRecord("tags", id, UPDATE_TYPE_DEL);
    }

    private void addUpdateRecord(String table, long id, int updateType) {
        addUpdateRecord(table, id, -1, updateType);
    }

    private void addUpdateRecord(String table, long id, long id2, int updateType) {
        if (updateType == UPDATE_TYPE_DEL) {
            /**
             * if we're deleting a record, first check if it was added in the update table. If it was, we delete the
             * ADD record from the table and return so we don't add a DEL record.
             */
            Cursor c = mDb.query("updates", new String[]{"_id"}, "table_name = ? AND update_type = ? " +
                    "AND target_id = ? AND (target2_id = ? OR target2_id = null)",
                    new String[]{table, String.valueOf(UPDATE_TYPE_ADD),
                            String.valueOf(id), String.valueOf(id2)}, null, null, null);
            if (c.moveToFirst()) {
                //at this point we know there was a relevant add record, so delete it!
                mDb.delete("updates", "_id = ?", new String[]{String.valueOf(c.getInt(0))});
                //and then return, so we don't add a DEL record to the updates table.
                return;
            }
        }
        ContentValues vals = new ContentValues();
        vals.put("table_name", table);
        vals.put("target_id", id);
        vals.put("target2_id", id2 == -1 ? null : id2);
        vals.put("update_type", updateType);
        vals.put("update_time", System.currentTimeMillis());
        mDb.insert("updates", null, vals);
    }

    /**
     * Toggles a particular tag for the given note id.
     *
     * @param noteId the note id
     * @param tagId  the tag id
     * @param tagged whether to tag or untag
     */
    public void setTagForNote(long noteId, long tagId, boolean tagged) {
        if (tagged) {
            ContentValues vals = new ContentValues();
            vals.put("tag_id", tagId);
            vals.put("note_id", noteId);
            mDb.insert("note_tags", null, vals);
        } else {
            mDb.delete("note_tags", "note_id = ? AND tag_id = ?",
                    new String[]{String.valueOf(noteId), String.valueOf(tagId)});
        }

        addUpdateRecord("note_tags", tagId, noteId, tagged ? UPDATE_TYPE_ADD : UPDATE_TYPE_DEL);
    }

    public Note findSingleNoteByFile(String fname) {
        Cursor c = mDb.query("imgs", new String[]{"_id", "file", "time"},
                "file = ?", new String[]{fname}, null, null, null);
        Note n = null;
        //check the cursor isn't empty
        if (c.moveToFirst())
            //it's not empty, there should only be one row anyway
            n = new Note(c.getInt(0), c.getString(1), c.getLong(2));
        return n;
    }

    public Cursor findNotesByTag(long tagId) {
        return mDb.rawQuery("SELECT _id, file, time " +
                "FROM imgs, note_tags " +
                "WHERE tag_id = ? AND _id = note_id",
                new String[]{String.valueOf(tagId)});
    }

    public void addTagFromComputer(String tag, int compId, boolean add) {
        if (add) {
            ContentValues vals = new ContentValues();
            vals.put("computer_id", compId);
            vals.put("value", tag);

            mDb.insert("tags", null, vals);
        } else {
            mDb.delete("tags", "computer_id = ?", new String[]{String.valueOf(compId)});
        }
    }

    public void addNoteTagFromComputer(int imgId, int tagId, boolean add) {
        int mobImgId = findId("imgs", imgId);
        int mobTagId = findId("tags", tagId);
        if (add) {
            ContentValues vals = new ContentValues();
            vals.put("tag_id", mobTagId);
            vals.put("note_id", mobImgId);
            mDb.insert("note_tags", null, vals);
        } else {
            mDb.delete("note_tags", "tag_id = ? AND note_id = ?",
                    new String[]{String.valueOf(mobTagId), String.valueOf(mobImgId)});
        }
    }

    public void addImgFromComputer(int compImgId, String imgFileName, boolean add) {
        if (add) {
            ContentValues vals = new ContentValues(3);
            vals.put("file", imgFileName);
            vals.put("time", System.currentTimeMillis());
            vals.put("computer_id", compImgId);
            mDb.insert("imgs", null, vals);
        } else {
            mDb.delete("imgs", "computer_id = ?", new String[] {String.valueOf(compImgId)});
        }
    }

    public int findId(String table, long id) {
        Cursor c = mDb.query(table, new String[]{"_id"}, "computer_id = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        c.moveToFirst();
        return c.getInt(0);
    }

    public int findComputerId(String table, long id) {
        Cursor c = mDb.query(table, new String[]{"computer_id"}, "_id = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        c.moveToFirst();
        return c.getInt(0);
    }

    /**
     * Get a list of updates since the last sync. Columns: _id, table_name, target_id, target2_id, update_type
     *
     * @param table the table name to examine, or null to select data for all tables
     * @return a cursor holding the data
     */
    public Cursor getUpdateList(String table) {
        return mDb.query("updates",
                new String[]{"_id", "table_name", "target_id", "target2_id", "update_type", "update_time"},
                table == null ? null : "table_name = ?", table == null ? null : new String[]{table}
                , null, null, null);
    }

    /**
     * Gets a list of images from the db. Columns are _id, file, time.
     */
    public Cursor getImgList() {
        return mDb.query("imgs", new String[]{"_id", "file", "time"}, null, null, null, null, null);
    }

    /**
     * Gets a list of tags from the database. Columns are _id, value, and the number of notes tagged with that
     * particular tag.
     *
     * @return a cursor representing the data set.
     */
    public Cursor getTagList() {
        return mDb.rawQuery("SELECT _id, value, COUNT(*) " +
                "FROM tags, note_tags " +
                "WHERE _id = tag_id " +
                "GROUP BY _id, value " +
                "UNION ALL " +
                "SELECT _id, value, 0 " +
                "FROM tags " +
                "WHERE NOT EXISTS (" +
                "   SELECT tag_id FROM note_tags" +
                "   WHERE tag_id = _id" +
                ")", null);
    }

    public Cursor getTagListForImg(long imgId) {
        String imgIdStr = String.valueOf(imgId);
        return mDb.rawQuery("SELECT _id, value, COUNT(*) " +
                "FROM tags, note_tags " +
                "WHERE _id = tag_id AND note_id = ?" +
                "GROUP BY _id, value " +
                "UNION ALL " +
                "SELECT _id, value, 0 " +
                "FROM tags " +
                "WHERE NOT EXISTS (" +
                "   SELECT tag_id FROM note_tags" +
                "   WHERE tag_id = _id AND note_id = ?" +
                ")", new String[]{imgIdStr, imgIdStr});
    }

    public Cursor getSubTags(long tagId) {
        return mDb.rawQuery("SELECT _id, value " +
                "FROM tags, meta_tags " +
                "WHERE tag1_id = ?",
                new String[]{String.valueOf(tagId)});
    }

    /**
     * Called by the bluetooth sync service. Signifies that we've recieved a computer_id from the computer and we
     * need to put it in a table.
     *
     * @param table      the table to update (imgs/tags)
     * @param col        the column to search for value in
     * @param value      the filename/tagname to update
     * @param computerId the new computer id
     */
    public void updateComputerId(String table, String col, String value, int computerId) {
        ContentValues vals = new ContentValues();
        vals.put("computer_id", computerId);
        mDb.update(table, vals, col + " = ?",
                new String[]{value});
    }

    public void clearUpdateTable() {
        mDb.delete("updates", null, null);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            for (String s : CREATE_DB)
                db.execSQL(s);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            for (String s : TABLES)
                db.execSQL("DROP TABLE IF EXISTS " + s);
            onCreate(db);
        }
    }

    private static final String[] CREATE_DB = new String[]{
            "CREATE TABLE tags (_id integer primary key autoincrement, " +
                    "computer_id integer default null, " +
                    "value text unique not null);",
            "CREATE TABLE imgs (_id integer primary key autoincrement, " +
                    "computer_id integer default null, " +
                    "file text unique not null, " +
                    "time integer not null);",
            "CREATE TABLE meta_tags (tag1_id integer not null, " +
                    "tag2_id integer not null, " +
                    "PRIMARY KEY (tag1_id, tag2_id));",
            "CREATE TABLE note_tagsHEAD (tag_id integer not null, " +
                    "note_id integer not null, " +
                    "PRIMARY KEY (tag_id, note_id));",
            /**
             * Update table:
             * _id represents the id of the update
             * table_name is the table that was updated
             * target_id is the id of the thing that was changed
             * target2_id is the id of the second thing that was changed (or null)
             * update_type is the type of update happened (add/delete/rename)
             * update_time the timestamp of when it happened
             *  of type UPDATE_TYPE_DEL, UPDATE_TYPE_ADD
             */
            "CREATE TABLE updates (_id integer not null, " +
                    "table_name text not null, " +
                    "target_id integer not null, " +
                    "target2_id integer default null, " +
                    "update_type integer not null, " +
                    "update_time integer not null, " +
                    "PRIMARY KEY (_id));"
    };

    private static final String[] TABLES = new String[]{
            "tags", "imgs", "meta_tags", "note_tags"
    };
}
