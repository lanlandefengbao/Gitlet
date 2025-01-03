package gitlet;

import java.io.File;
import java.util.List;


/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author B Li
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ...
     */

    public static void main(String[] args) {

        final File CWD = new File(System.getProperty("user.dir"));

        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];

        switch (firstArg) {
            case "init":
                Commit c = new Commit();
                c.setupPersistence(null);
                break;
            case "add":
                if (args.length != 2) {
                    if(args.length == 1) {
                        System.out.println("Please enter a file name.");
                        System.exit(0);
                    } else {
                        System.out.println("Wrong number of arguments.");
                        System.exit(0);
                    }
                }
                if (args[1].isEmpty()) {
                    System.out.println("Please enter a file name.");
                    System.exit(0);
                }
                if (args[1].equals(".")) {
                    new Watcher().updateAll();
                } else {
                    File f = new File(args[1]).getAbsoluteFile(); /** For file object that represents relative path, getAbsoluteFile will complete it based on CWD. */
                    if (f.exists() && f.isFile()) {
                        new Watcher().addOne(f);
                    } else {
                        System.out.println("File does not exist.");
                        System.exit(0);
                    }
                }
                break;
            case "commit":
                if (args.length != 2) {
                    if (args.length == 1) {
                        System.out.println("Please enter a commit message.");
                        System.exit(0);
                    } else {
                        System.out.println("Wrong number of arguments.");
                        System.exit(0);
                    }
                }
                if (args[1].isEmpty()) {
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                }
                new Commit().makeCommit(args[1], null);
                break;
            case "status":
                if (args.length != 1) {
                    System.out.println("Wrong number of arguments.");
                    System.exit(0);
                }
                new Watcher().getStatus();
                break;
            case "log":
                new Commit().log();
                break;
            case "rm":
                if (args.length == 1 || (args.length == 2 && args[1].isEmpty())) {
                    System.out.println("Please enter a file name.");
                    System.exit(0);
                } else if (args.length > 2) {
                    System.out.println("Wrong number of arguments.");
                    System.exit(0);
                } else {
                    File f = new File(args[1]).getAbsoluteFile();
                    new Watcher().removeOne(f);
                }
                break;
            case "global-log":
                if (args.length != 1) {
                    System.out.println("Wrong number of arguments.");
                    System.exit(0);
                }
                new Commit().logGlobal();
                break;
            case "find":
                if (args.length == 1 || (args.length == 2 && args[1].isEmpty())) {
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                } else if (args.length > 2) {
                    System.out.println("Wrong number of arguments.");
                    System.exit(0);
                } else {
                    new Commit().find(args[1]);
                }
                break;
            case "checkout":
                if (args.length == 1) {
                    System.out.println("Please enter a command.");
                    System.exit(0);
                } else if (args.length == 3) {
                    if (!args[1].equals("--")) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    new Commit().checkoutFile(new File(args[2]).getAbsolutePath());
                } else if (args.length == 2) {
                    new Commit().checkoutBranch(args[1]);
                } else if (args.length == 4) {
                    if (!args[2].equals("--")) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    new Commit().checkoutCommitFile(args[1], args[3]);
                } else {
                    System.out.println("Wrong number of arguments.");
                    System.exit(0);
                }
                break;
            case "branch":
                if (args.length == 1 || (args.length == 2 && args[1].isEmpty())) {
                    System.out.println("Please enter a branch name.");
                    System.exit(0);
                } else if (args.length > 2) {
                    System.out.println("Wrong number of arguments.");
                    System.exit(0);
                } else {
                    new Commit().makeBranch(args[1]);
                }
                break;
            case "rm-branch":
                if (args.length == 1 || (args.length == 2 && args[1].isEmpty())) {
                    System.out.println("Please enter a branch name.");
                    System.exit(0);
                } else if (args.length > 2) {
                    System.out.println("Wrong number of arguments.");
                    System.exit(0);
                } else {
                    new Commit().rmBranch(args[1]);
                }
                break;
            case "reset":
                if (args.length == 1 || (args.length == 2 && args[1].isEmpty())) {
                    System.out.println("Please enter a commit hash.");
                    System.exit(0);
                } else if (args.length > 2) {
                    System.out.println("Wrong number of arguments.");
                    System.exit(0);
                } else {
                    new Commit().reset(args[1]);
                }
                break;
            case "merge":
                if (args.length == 1 || (args.length == 2 && args[1].isEmpty())) {
                    System.out.println("Please enter a branch name.");
                    System.exit(0);
                } else if (args.length > 2) {
                    System.out.println("Wrong number of arguments.");
                    System.exit(0);
                } else {
                    new Commit().merge(args[1]);
                }
                break;
            case "tinytest": // For testing purposes. write a Blob object to a file named "tinytest", and see whether it can be read back correctly.
                Utils.writeObject(Utils.join(new Repository().PROJECT_FOLDER, "tinytest"),new Blob());
                byte[] content = Utils.readContents(Utils.join(new Repository().PROJECT_FOLDER, "tinytest"));
                System.out.println(Utils.sha1((Object) content)); // the blob's sha1 hash once the "tinytest" is written to the file.
                break;
            case "add-remote":
                if (args.length != 3) {
                    System.out.println("Wrong number of arguments.");
                    System.exit(0);
                }
                new Commit().addRemote(args[1], args[2]);
                break;
            case "rm-remote":
                if (args.length != 2) {
                    System.out.println("Wrong number of arguments.");
                    System.exit(0);
                }
                new Commit().rmRemote(args[1]);
                break;
            case "push":
                if (args.length != 3) {
                    System.out.println("Wrong number of arguments.");
                    System.exit(0);
                }
                new Commit().pushRemote(args[1], args[2]);
                break;
            case "fetch":
                if (args.length != 3) {
                    System.out.println("Wrong number of arguments.");
                    System.exit(0);
                }
                new Commit().fetchRemote(args[1], args[2]);
                break;
            case "pull":
                if (args.length != 3) {
                    System.out.println("Wrong number of arguments.");
                    System.exit(0);
                }
                new Commit().pullRemote(args[1], args[2]);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }

}
