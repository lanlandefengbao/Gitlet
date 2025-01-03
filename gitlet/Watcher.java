package gitlet;

import java.io.File;
import java.util.*;


/** The specific class of objects through which we can capture project files of different states in the current commit, including "staged for addition/removal", "untracked" and "changed but not staged".
 * For each state, we collect its files of all cases into one instance variable */

public class Watcher {
    /**
     * files in CWD that neither _tracked_ in current commit nor staged for addition.
     */
    private final Map<File, String> untracked1 = new HashMap<>();
    /**
     * files that have been staged for removal, but then re-created in CWD
     */
    private final Set<File> untracked21 = new HashSet<>();
    private final Map<File, String> untracked22 = new HashMap<>();
    /**
     * Files tracked in current commit, with content changed in CWD but not staged for addition;
     */
    private final Map<File, String> changed1 = new HashMap<>();
    /**
     * Files staged for addition, changed in CWD, but (this change) not staged.
     */
    private final Set<File> changed21 = new HashSet<>();
    private final Map<File, String> changed22 = new HashMap<>();
    /**
     * Files staged for addition, but deleted in the working directory.
     */
    private final Set<File> changed31 = new HashSet<>();
    private final Set<File> changed32 = new HashSet<>();
    /**
     * Files tracked in current commit, deleted in CWD, but not staged for removal.
     */
    private final Set<File> changed4 = new HashSet<>();

    private final StagedFile staged;
    private final List<File> cwdFiles;
    private final Map<File, String> commitedFile = Commit.getHeadCommit().Blobs;

    private final static File CWD = new File(System.getProperty("user.dir"));
    private final static Repository repo = new Repository();

    public Watcher() {
        staged = Utils.readObject(repo.STAGING_FILE, StagedFile.class);
        cwdFiles = getAbsolutePaths(repo.PROJECT_FOLDER, new ArrayList<>());
    }

    private List<File> getAbsolutePaths(File CURRENT_PATH, List<File> files) {
        if (CURRENT_PATH.isFile()) {
            files.add(CURRENT_PATH);
        }
        else {
            for (File f : CURRENT_PATH.listFiles()) {
                if(f.getName().equals(".gitlet")) {
                    continue;
                }
                getAbsolutePaths(f, files);
            }
        }
        return files;
    }

    /**
     * Tracing untracked files (untracked1, untracked2 of this object)
     */
    public Boolean getUntrackedFile() {
        for (File f : cwdFiles) {
            if (!staged.Addition.containsKey(f)) {
                String contentHash = Utils.sha1((Object) Utils.readContents(f));
                if (!commitedFile.containsKey(f)) {
                    untracked1.put(f, contentHash);
                }
                if (staged.Removal.contains(f)) {
                    if (contentHash.equals(staged.Addition.get(f))) {
                        untracked21.add(f);
                    } else {
                        untracked22.put(f, contentHash);
                    }
                }
            }
        }
        return !untracked1.isEmpty() || !untracked21.isEmpty() || !untracked22.isEmpty();
    }

    /**
     * Tracing changed but not staged files (changed1-4)
     */
    public Boolean getChangedFile() {
        for (File f : staged.Addition.keySet()) {
            if (commitedFile.containsKey(f)) {
                if (!cwdFiles.contains(f)) {
                    changed31.add(f);
                } else {
                    String contentHashCWD = Utils.sha1((Object) Utils.readContents(f));
                    String contentHashStaged = staged.Addition.get(f);
                    if (!contentHashCWD.equals(contentHashStaged)) {
                        if (contentHashCWD.equals(commitedFile.get(f))) {
                            changed21.add(f);
                        } else {
                            changed22.put(f, contentHashCWD);
                        }
                    }

                }
            }
            else {
                if (!cwdFiles.contains(f)) {
                    changed32.add(f);
                } else {
                    String contentHashCWD = Utils.sha1((Object) Utils.readContents(f));
                    String contentHashStaged = staged.Addition.get(f);
                    if (!contentHashStaged.equals(contentHashCWD)) {
                        changed22.put(f, contentHashCWD);
                    }
                }
            }
        }
        for (File f : commitedFile.keySet()) {
            if (!cwdFiles.contains(f)) {
                if (!staged.Removal.contains(f)) {
                    changed4.add(f);
                }
            }
            else {
                String contentHash = Utils.sha1((Object) Utils.readContents(f));
                if (!contentHash.equals(commitedFile.get(f)) && !staged.Addition.containsKey(f)) {
                    changed1.put(f, contentHash);
                }
            }
        }
        return !changed1.isEmpty() || !changed21.isEmpty() || !changed22.isEmpty() || !changed31.isEmpty() || !changed32.isEmpty() || !changed4.isEmpty();
    }

    /** Take in an absolute path, returns the relative path based on CWD. */
    private String getRelativePath(File f) {
        String absolutePath = f.getAbsolutePath();
        String cwdPath = CWD.getAbsolutePath();
        String rawPath = absolutePath.substring(cwdPath.length() + 1);
        return rawPath.replace(File.separator, "/");
    }

    /** Displays Branches(with current branch marked by *), Staged files, Removed files, Changes not staged, and Untracked files. */
    public void getStatus() {
        getUntrackedFile();
        getChangedFile();
        System.out.println("=== Branches ===");
        if (Commit.isDetached()) {
            String SHA1 = Utils.readContentsAsString(repo.HEAD);
            System.out.println("*" + "(HEAD detached at " + SHA1.substring(0,7) + ")");
            for (File f : repo.LOCAL_BRANCH_FOLDER.listFiles()) {
                System.out.println(f.getName());
            }
        } else {
            File HEAD = new File(Utils.readContentsAsString(repo.HEAD).substring(5));
            for (File f : repo.LOCAL_BRANCH_FOLDER.listFiles()) {
                if (f.equals(HEAD)) {
                    System.out.println("*" + f.getName());
                } else {
                    System.out.println(f.getName());
                }
            }
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        for (File f : staged.Addition.keySet()) {
            System.out.println(getRelativePath(f));
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (File f : staged.Removal) {
            System.out.println(getRelativePath(f));
        };
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (File f : changed1.keySet()) {
            System.out.println(getRelativePath(f) + " (modified)");
        }
        for (File f : changed21) {
            System.out.println(getRelativePath(f) + " (modified)");
        }
        for (File f : changed22.keySet()) {
            System.out.println(getRelativePath(f) + " (modified)");
        }
        for (File f : changed31) {
            System.out.println(getRelativePath(f) + " (deleted)");
        }
        for (File f : changed32) {
            System.out.println(getRelativePath(f) + " (deleted)");
        }
        for (File f : changed4) {
            System.out.println(getRelativePath(f) + " (deleted)");
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        for (File f : untracked1.keySet()) {
            System.out.println(getRelativePath(f));
        }
        for (File f : untracked21) {
            System.out.println(getRelativePath(f));
        }
        for (File f : untracked22.keySet()) {
            System.out.println(getRelativePath(f));
        }
        System.out.println();
    }

    /** Adding a file for addition. Not like real Git, where "add" also responsible for adding a file for removal.
     * And this method only takes in the file's ABSOLUTE path */
    public void addOne(File f) {
        byte[] content = Utils.readContents(f);
        String contentHash = Utils.sha1((Object) content);
//        update the stagingArea
        if (!staged.Addition.containsKey(f)) {
            if (staged.Removal.contains(f)) {
                staged.Removal.remove(f); //1.2
                if (!contentHash.equals(commitedFile.get(f))) {
                    staged.Addition.put(f, contentHash); //1.2.2
                }
            } else {
                if (!commitedFile.containsKey(f)) {
                    staged.Addition.put(f, contentHash); //1.1
                } else {
                    if (!contentHash.equals(commitedFile.get(f))) {
                        staged.Addition.put(f, contentHash); //2.1
                    }
                }
            }
        } else {
            if (!staged.Addition.get(f).equals(contentHash)) {
                if (commitedFile.containsKey(f) && commitedFile.get(f).equals(contentHash)) {
                    staged.Addition.remove(f); //2.2.1
                } else {
                    staged.Addition.put(f, contentHash); //2.2.2
                }
            }
        }

//        store the staged file with new version of contents into .gitlet/objects folder, so we should have the right contents when commiting even though the file was deleted/modified in CWD. */
        Blob blob = new Blob(content);
        File thisBlobFolder = Utils.join(repo.OBJECT_FOLDER, contentHash.substring(0,2));
        File thisBlob = Utils.join(thisBlobFolder, contentHash.substring(2));
        if (!thisBlob.exists()) {
            thisBlobFolder.mkdir();
            Utils.writeObject(thisBlob, blob);
        }
//        update the staging file locally
        Utils.writeObject(repo.STAGING_FILE, staged);

    }

    /** Adding a file for removal(the missing part of real Git's "add" function from our "addOne" method).
     * Furthermore, it deletes the file from CWD if one haven't done so (so we can delete and stage a file for removal directly through Gitlet in one step).
     * And this method only takes in the file's ABSOLUTE path */
    public void removeOne(File f) {
        if (staged.Addition.containsKey(f)) {
            staged.Addition.remove(f);
            if (commitedFile.containsKey(f)) {
                staged.Removal.add(f);
                cwdFiles.remove(f);
                Utils.restrictedDelete(f);
            }
        } else {
            if (commitedFile.containsKey(f)) {
                staged.Removal.add(f);
                cwdFiles.remove(f);
                Utils.restrictedDelete(f);
            } else {
                System.out.println("No reason to remove this file. ");
            }
        }

        Utils.writeObject(repo.STAGING_FILE, staged);
    }

    /** Update the stagingArea once for all */
    public void updateAll() {
        getUntrackedFile();
        getChangedFile();
        for (File f : untracked1.keySet()) {
            staged.Addition.put(f, untracked1.get(f));
        }
        for (File f : untracked21) {
            staged.Removal.remove(f);
        }
        for (File f : untracked22.keySet()) {
            staged.Removal.remove(f);
            staged.Addition.put(f, untracked22.get(f));
        }
        for (File f : changed1.keySet()) {
            staged.Addition.put(f, changed1.get(f));
        }
        for (File f : changed21) {
            staged.Addition.remove(f);
        }
        for (File f : changed22.keySet()) {
            staged.Addition.put(f, changed22.get(f));
        }
        for (File f : changed31) {
            staged.Addition.remove(f);
            staged.Removal.add(f);
        }
        for (File f : changed32) {
            staged.Addition.remove(f);
        }
        for (File f : changed4) {
            staged.Removal.add(f);
        }
//      update stagingArea locally
        Utils.writeObject(repo.STAGING_FILE, staged);
//      write NEW blob objects into .gitlet/objects folder
        for (File f : staged.Addition.keySet()) {
            String contentHash = staged.Addition.get(f);
            File BLOB_FOLDER = Utils.join(repo.OBJECT_FOLDER, contentHash.substring(0,2));
            if (!BLOB_FOLDER.exists()) {
                BLOB_FOLDER.mkdirs();
                Blob blob = new Blob(Utils.readContents(f));
                Utils.writeObject(Utils.join(BLOB_FOLDER, contentHash.substring(2)), blob);
            }
            else {
                for (String fileName : Utils.plainFilenamesIn(BLOB_FOLDER)) {
                    if (fileName.equals(contentHash.substring(2))) {
                        break;
                    }
                    Blob blob = new Blob(Utils.readContents(f));
                    Utils.writeObject(Utils.join(BLOB_FOLDER, contentHash.substring(2)), blob);
                }
            }
        }
    }

    public List<File> getCWDFiles() {
        return cwdFiles;
    }

    StagedFile getStaged() {
        return staged;
    }

    public boolean isStagedEmpty() {
        return staged.Addition.isEmpty() && staged.Removal.isEmpty();
    }
}

