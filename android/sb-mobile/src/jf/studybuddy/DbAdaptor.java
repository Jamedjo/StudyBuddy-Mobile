package jf.studybuddy;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import jf.studybuddy.model.Note;

/**
 * TODO: Add convenience methods for modifying stuff in the DB.
 *
 * @author alex
 */
public class DbAdaptor {
    private static final String TAG = "DbAdaptor";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME = "sbdata";
    private static final int DATABASE_VERSION = 1;

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
        mDb.execSQL("INSERT INTO imgs (file, time) VALUES " +
                "('" + fname + "', " + System.currentTimeMillis() + ");");
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
                    "value text not null);",
            "CREATE TABLE imgs (_id integer primary key autoincrement, " +
                    "file text not null, time integer not null);",
            "CREATE TABLE meta_tags (tag1_id integer not null, tag2_id integer not null);",
            "CREATE TABLE note_tags (tag_id integer not null, note_id integer not null);"
    };

    private static final String[] TABLES = new String[]{
            "tags", "imgs", "meta_tags", "note_tags"
    };
}
