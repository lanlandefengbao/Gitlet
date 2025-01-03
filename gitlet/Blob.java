package gitlet;

import java.io.Serializable;
import java.util.*;

/** A Blob object contains byte[], which represents a file's content.
  For any commited file, we represent it by Map<filaname, Blob's file name which is SHA1>. */

public class Blob implements Serializable, Dumpable {

    private byte[] Content;

    public Blob(byte[] CONTENT) {
        Content = CONTENT;
    }

    public Blob() {
    }

    public byte[] getContent() {
        return Content;
    }

    @Override
    public void dump() {
        System.out.printf("Content: %s%n", Arrays.toString(Content));
    }
}
