package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** The specific class of objects that represents the current state of StagingArea, which should always be empty after commiting */

public class StagedFile implements Serializable {
    Map<File, String> Addition;
    Set<File> Removal;

    public StagedFile() {
        Addition = new HashMap<>();
        Removal = new HashSet<>();
    }

}
