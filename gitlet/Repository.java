package gitlet;

import java.io.File;
import java.util.Date;

import static gitlet.Utils.*;

/** An object of this class represents a repository with Gitlet system.
 *  The instance variables necessary files for a Gitlet system.
 *
 *  @author B Li
 */
public class Repository {

    /** The root folder of current project */
    File PROJECT_FOLDER = new File(System.getProperty("user.dir"));

    /** The .gitlet directory. */
    File GITLET_SYSTEM = Utils.join(PROJECT_FOLDER, ".gitlet");

    /** Stores all commit objects AND Blob objects */
    File OBJECT_FOLDER = Utils.join(GITLET_SYSTEM, "objects");

    /** Stores the reference of the current HEAD commit.
     * If HEAD is pointing to a branch, the .git/HEAD file will contain a reference to that branch, like ".gitlet/refs/head/master"
     * If in detached Head state (not on any branch, but on a specific commit), the .git/HEAD file will contain the commit hash directly. */
    File HEAD = Utils.join(GITLET_SYSTEM, "HEAD");

    /** Stores pointers of different branches */
    File REF_FOLDER = Utils.join(GITLET_SYSTEM, "refs");
    File LOCAL_BRANCH_FOLDER = Utils.join(REF_FOLDER, "heads");
    File MASTER = Utils.join(LOCAL_BRANCH_FOLDER, "master");

    /** StagingArea, stores the confirmed information of change from current version to next version */
    File STAGING_FILE = Utils.join(GITLET_SYSTEM, "index");

    /** Stores remote repositories */
    File REMOTE_REPO_FOLDER = Utils.join(REF_FOLDER, "remotes");

    /** Constructor */
    /** By default, that is, without naming a project folder, running Gitlet system on CWD */
    public Repository() {
    }

    /** Running Gitlet system under a specific project folder */
    public Repository(File projectFolder) {

        PROJECT_FOLDER = projectFolder;

        GITLET_SYSTEM = Utils.join(PROJECT_FOLDER, ".gitlet");

        OBJECT_FOLDER = Utils.join(GITLET_SYSTEM, "objects");

        HEAD = Utils.join(GITLET_SYSTEM, "HEAD");

        REF_FOLDER = Utils.join(GITLET_SYSTEM, "refs");

        LOCAL_BRANCH_FOLDER = Utils.join(REF_FOLDER, "heads");

        MASTER = Utils.join(LOCAL_BRANCH_FOLDER, "master");

        STAGING_FILE = Utils.join(GITLET_SYSTEM, "index");

        REMOTE_REPO_FOLDER = Utils.join(REF_FOLDER, "remotes");
    }
}
