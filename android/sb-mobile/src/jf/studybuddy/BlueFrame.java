package jf.studybuddy;

import java.io.IOException;
import java.io.OutputStream;

public class BlueFrame {

    private OutputStream outPort;

    public BlueFrame(OutputStream portOut) {
        outPort = portOut;
    }

    //Need to make sure FrameType is valid for command
    public boolean sendString(FrameType type, String text) {
        //send string and add \n at end
        //byte[] message = (text+"\n").getBytes();
        byte[] message = (text).getBytes();
        return writeMessageWithLength(type.val, message);
    }

    public boolean sendCommand(FrameType type) {
        return true;
    }

    public boolean sendImage(FrameType type, String text) {
        return true;
    }

    private boolean writeMessageWithLength(byte typeByte, byte[] message) {
        try {
            outPort.write(typeByte);
            int l = message.length;
            byte[] len = new byte[]{(byte) l, (byte) (l >> 8), (byte) (l >> 16), (byte) (l >>> 24)};//Converts int to byte[]
            outPort.write(len);
            outPort.write(message);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}

enum FrameType {
    Text((byte) 0x00), Image((byte) 0x01), ImagesDone((byte) 0x02), Command((byte) 0x03), SomthingElse((byte) 0x04);
    byte val;
    FrameType(byte value) {
        val = value;
    }
}