package gitlet;

import java.io.File;


/** Driver class for Gitlet, the tiny stupid version-control system.
 * @author Matt Chorlian
 */


public class Main {

    /** the gitlet repository. **/
    private static Gitlet git;

    /** the path to the repo. **/
    private static final String REPOPATH = ".gitlet/repo";

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        File save = new File(REPOPATH);
        File gitlet = new File(".gitlet");
        try {
            if (args.length == 0) {
                Utils.message("Please enter a command.");
                throw new GitletException();
            }
            if (args[0].equals("init")) {
                git = new Gitlet();
                git.init();
                Utils.writeObject(save, git);
            } else if (gitlet.exists()) {
                git = Utils.readObject(save, Gitlet.class);
                argHelper(args);
                File saveagain = new File(REPOPATH);
                Utils.writeObject(saveagain, git);
            } else if (!gitlet.exists() || !gitlet.isDirectory()) {
                Utils.message("Not in an initialized Gitlet directory.");
                throw new GitletException();
            }

        } catch (GitletException e) {
            System.exit(0);
        }
    }



    /** ARGS HELPER.
     * @param args
     * the arguments inputted
     * **/
    public static void argHelper(String... args) {

        switch (args[0]) {
        case "add":
            git.add(args[1]);
            break;
        case "commit":
            git.commit(args[1]);
            break;
        case "rm":
            git.rm(args[1]);
            break;
        case "log":
            git.log();
            break;
        case "global-log":
            git.globalLog(args);
            break;
        case "find":
            git.find(args[1]);
            break;
        case "status":
            git.status();
            break;
        case "checkout":
            git.checkout(args);
            break;
        case "branch":
            git.branch(args[1]);
            break;
        case "rm-branch":
            git.rmBranch(args[1]);
            break;
        case "reset":
            git.reset(args[1]);
            break;
        case "merge":
            git.merge(args[1]);
            break;
        default:
            Utils.message("No command with that name exists");
            throw new GitletException();
        }
    }

}

