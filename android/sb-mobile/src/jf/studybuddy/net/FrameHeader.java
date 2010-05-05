package jf.studybuddy.net;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;

/**
 * Represents a frame header. Holds a type and an optional length - does nothing with the body of the frame (if
 * the frame has a body).
 *
 * @author alex
 */
public class FrameHeader {
    /**
     * List of the types of frame we can send/recieve.
     */
    public static final int TYPE_STRING = 0,
            TYPE_BINARY = 1,
            TYPE_IMGS_START = 2,
            TYPE_IMGS_STOP = 3,
            TYPE_IMGS_FILENAME = 4,
            //sent to cue the computer to send data to us
            TYPE_FINISHED_SENDING = 5,
            //first frame sent to the computer, all the new tags/images in the db
            TYPE_NEWDBVALUES = 6,
            TYPE_SYNC = 7,
            TYPE_DONE_AND_DONE = 0xEE;
    /**
     * Command frames are special - if we receive a command frame we don't care about the frame
     * length or frame data. Command frames are set in the static initializer - important!
     */
    private static final HashSet<Integer> commandFrames = new HashSet<Integer>();
    private int type;
    private int length;
    boolean commandFrame = false;

    protected FrameHeader() {

    }

    public int getType() {
        return type;
    }

    public int getLength() {
        if (isCommandFrame())
            throw new IllegalStateException("Command Frame has no associated length");
        return length;
    }

    public boolean isCommandFrame() {
        return commandFrame;
    }

    /**
     * Read a frame header from the input stream.
     *
     * @param in the input stream to read from
     * @return a new FrameHeader object.
     * @throws IOException if the underlying input stream produces an exception.
     */
    public static FrameHeader readFromStream(InputStream in) throws IOException {
        FrameHeader f = new FrameHeader();
        f.type = in.read() & 0xff;
        f.commandFrame = commandFrames.contains(f.type);
        if (!f.commandFrame) {
            byte[] b = new byte[4];
            in.read(b);
            f.length = (b[0] & 0xff) + ((b[1] & 0xff) << 8) + ((b[2] & 0xff) << 16) + ((b[3] & 0xff) << 24);
        }
        return f;
    }

    public static void writeToStream(OutputStream out, int type) throws IOException {
        writeToStream(out, type, -1);
    }

    /**
     * Write a frame header to the output stream.
     *
     * @param out  outputstream to write to
     * @param type frame type (or id)
     * @param len  frame length
     */
    public static void writeToStream(OutputStream out, int type, int len) throws IOException {
        out.write((byte) type);
        if (len >= 0) {
            byte[] outLen = new byte[]{(byte) len, (byte) (len >> 8), (byte) (len >> 16), (byte) (len >> 24)};
            out.write(outLen);
        }
    }

    static {
        commandFrames.add(TYPE_IMGS_START);
        commandFrames.add(TYPE_IMGS_STOP);
        commandFrames.add(TYPE_FINISHED_SENDING);
    }
}
