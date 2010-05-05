package jf.studybuddy.model;

/**
 * Represents a note object stored in the db. Just for convenience.
 *
 * @author alex
 */
public class Note {
    public static final String FOLDER_PREFIX = "sb-notes";
    private long id;
    private String fileName;
    private long saveTime;

    public Note(long i, String s, long l) {
        this.id = i;
        this.fileName = s;
        this.saveTime = l;
    }

    public long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public long getSaveTime() {
        return saveTime;
    }

    public String toString() {
        return "[Note:id="+id+";fileName="+fileName+";saveTime="+saveTime+"]";
    }
}
