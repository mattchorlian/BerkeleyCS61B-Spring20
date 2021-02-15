package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Formatter;

/** @author Matt Chorlian. **/
public class Gitlet implements Serializable {

    /** points to the head commit. */
    private String _head;

    /** a hashmap to track branches according to their name. */
    private HashMap<String, String> _branches;

    /** a hashamp to track staged files: adding or removing .*/
    private HashMap<String, String> _stage;

    /** a list to keep track of untracked files. */
    private ArrayList<String> _untracked;

    /** Helper method to get commit corresponding to given sha-1Id.
     * @param sha1
     * the sha1 of the commit we are looking for
     * @return Commit
     * */
    public Commit getCommit(String sha1) {
        File f = new File(".gitlet/commits/" + sha1);
        if (f.exists()) {
            return Utils.readObject(f, Commit.class);
        } else {
            Utils.message("No commit with that id exists.");
            throw new GitletException();
        }
    }


    /** init. **/
    void init() {
        File gitlet = new File(".gitlet");
        if (gitlet.exists()) {
            System.out.println("A gitlet version-control system "
                    + "already exists in the current directory");
        } else {
            gitlet.mkdir();
            File commits = new File(".gitlet/commits");
            commits.mkdir();
            File stage = new File(".gitlet/stage");
            stage.mkdir();
            File blobs = new File(".gitlet/blobs");
            blobs.mkdir();

            Commit initialcommit = new Commit("initial commit",
                    null, null, true);
            File initial = new File(".gitlet/commits/"
                    + initialcommit.getSha1());
            Utils.writeContents(initial,
                    (Object) Utils.serialize(initialcommit));

            _head = "master";
            _branches = new HashMap<String, String>();
            _branches.put("master", initialcommit.getSha1());
            _stage = new HashMap<String, String>();
            _untracked = new ArrayList<String>();
        }

    }

    /** add.
     * @param args
     * file to be added
     * **/
    public void add(String args) {
        File f = new File(args);
        if (!f.exists()) {
            Utils.message("File does not exist.");
            throw new GitletException();
        } else {
            Blob b = new Blob(f);
            String blobcontents = Utils.readContentsAsString(f);

            File blobfile = new File(".gitlet/blobs/"
                    + Utils.sha1(blobcontents));
            File stagedblob = new File(".gitlet/stage/"
                    + Utils.sha1(blobcontents));

            Commit head = getCommit(_branches.get(_head));
            HashMap<String, String> headfiles = head.getFiles();

            if (headfiles == null || !headfiles.containsKey(args)
                || !headfiles.get(args).equals(Utils.sha1(blobcontents))) {
                _stage.put(args, Utils.sha1(blobcontents));
                Utils.writeContents(blobfile, (Object) blobcontents);
                Utils.writeContents(stagedblob, (Object) blobcontents);
            } else {
                if (stagedblob.exists()) {
                    _stage.remove(args);
                }
                _untracked.remove(args);
            }
        }
    }

    /** commit method.
     * @param args
     * commit message
     * **/
    public void commit(String args) {
        HashMap<String, String> parentfiles =
                getCommit(_branches.get(_head)).getFiles();

        if (args.trim().length() == 0) {
            Utils.message("Please enter a commit message.");
            throw new GitletException();
        }
        if (_stage.size() == 0 && _untracked.size() == 0) {
            System.out.println("No changes add to the commit.");
            throw new GitletException();
        } else {
            if (parentfiles == null) {
                parentfiles = new HashMap<String, String>();
            }
            for (String file : _stage.keySet()) {
                parentfiles.put(file, _stage.get(file));
            }
            for (String untracked : _untracked) {
                parentfiles.remove(untracked);
            }
        }

        _stage.clear();
        _untracked.clear();

        String commitmsg = args;
        String[] parent = new
                String[]{getCommit(_branches.get(_head)).getSha1()};
        Commit newest = new Commit(commitmsg,
                parentfiles, parent, false);
        File newcommit = new File(".gitlet/commits/"
                + newest.getSha1());
        Utils.writeObject(newcommit, newest);

        _branches.put(_head, newest.getSha1());
    }

    /** remove fileName.
     * @param fileName
     * the file to be removed
     * **/
    public void rm(String fileName) {
        Boolean removed = false;
        File f = new File(fileName);
        String headID = _branches.get(_head);
        Commit head = getCommit(headID);
        HashMap<String, String> files = head.getFiles();

        if (!f.exists() && !files.containsKey(fileName)) {
            Utils.message("File does not exist.");
            throw new GitletException();
        }

        if (_stage.containsKey(fileName)) {
            _stage.remove(fileName);
            removed = true;
        }

        if (files != null && files.containsKey(fileName)) {
            _untracked.add(fileName);
            Utils.restrictedDelete(f);
            removed = true;
        }
        if (removed) {
            return;
        }
        Utils.message("No reason to remove the file.");
        throw new GitletException();


    }

    /** LOG. **/
    public void log() {
        String head = _branches.get(_head);
        while (head != null) {
            Commit c = getCommit(head);
            System.out.println("===");
            System.out.println("commit " + c.getSha1());
            System.out.println("Date: " + c.getTime());
            System.out.println(c.getMessage());
            System.out.println();
            head = c.getParentsha1();
        }
    }

    /** GLOBAL LOG.
     * @param args
     * not really needed
     * **/
    public void globalLog(String[] args) {
        File commitcentral = new File(".gitlet/commits");
        for (File file : Objects.requireNonNull(commitcentral.listFiles())) {
            Commit c = getCommit(file.getName());
            System.out.println("===");
            System.out.println("commit " + c.getSha1());
            System.out.println("Date: " + c.getTime());
            System.out.println(c.getMessage());
            System.out.println();
        }

    }

    /** FIND.
     * @param args
     * the commit message we search for
     * **/
    public void find(String args) {
        Boolean found = false;
        File c =  new File(".gitlet/commits");
        for (File file : Objects.requireNonNull(c.listFiles())) {
            Commit commit = getCommit(file.getName());
            if (args.equals(commit.getMessage())) {
                System.out.println(commit.getSha1());
                found = true;
            }
        }
        if (found) {
            return;
        }
        Utils.message("Found no commit with that message.");
        throw new GitletException();
    }

    /** STATUS. **/
    public void status() {
        System.out.println("=== Branches ===");
        Object[] keys = _branches.keySet().toArray();
        Arrays.sort(keys);
        for (Object branch : keys) {
            if (branch.equals(_head)) {
                System.out.println("*" + _head);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        Object[] files = _stage.keySet().toArray();
        for (Object file : files) {
            System.out.println(file);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        Object[] removed = _untracked.toArray();
        for (Object remove : removed) {
            System.out.println(remove);
        }
        System.out.println();
        statusHelper();

    }

    /** STATUS HELPER. **/
    public void statusHelper() {
        System.out.println("=== Modifications Not Staged For Commit ===");
        ArrayList<String> modifiedFiles = new ArrayList<>();
        ArrayList<String> untrackedFiles = new ArrayList<>();
        File cwdirectory = new File(System.getProperty("user.dir"));
        List<String> allFiles = Utils.plainFilenamesIn(cwdirectory);
        Commit head = getCommit(_branches.get(_head));

        if (allFiles != null) {
            for (String fileName : allFiles) {
                File file = new File(fileName);
                byte[] contents = Utils.readContents(file);
                String sha1 = Utils.sha1((Object) contents);
                if (_stage.containsKey(fileName)) {
                    if (!_stage.get(fileName).equals(sha1)) {
                        String s = fileName + " (modified)";
                        modifiedFiles.add(s);
                    }
                } else {
                    if (head.getFiles() != null) {
                        if (head.getFiles().containsKey(fileName)) {
                            if (!head.getFiles().get(fileName).equals(sha1)) {
                                String s = fileName + " (modified)";
                                modifiedFiles.add(s);
                            }
                        }
                    } else {
                        untrackedFiles.add(fileName);
                    }
                }
            }
        }
        for (String fileName : _stage.keySet()) {
            File f = new File(fileName);
            if (!f.exists()) {
                String s = fileName + " (deleted)";
                modifiedFiles.add(s);
            }
        }
        if (head.getFiles() != null) {
            for (String fileName : head.getFiles().keySet()) {
                File f = new File(fileName);
                if (!f.exists() && !modifiedFiles.contains(fileName)
                        && !_untracked.contains(fileName)) {
                    String s = fileName + " (deleted)";
                    modifiedFiles.add(s);
                }
            }
        }
        for (String s : modifiedFiles) {
            System.out.println(s);
        }

        System.out.println();
        System.out.println("=== Untracked Files ===");
        for (String s : untrackedFiles) {
            System.out.println(s);
        }
    }

    /** EXTEND SHA1.
     * @param iD
     * the id which we are extending
     * @return String
     * **/
    public String extendSha1(String iD) {
        File commits = new File(".gitlet/commits");
        if (commits.listFiles() == null) {
            Utils.message("Commit directory is empty");
        } else {
            for (File file : Objects.requireNonNull(commits.listFiles())) {
                if (file.getName().contains(iD)) {
                    return file.getName();
                }
            }
        }
        Utils.message("No commit with that id exists");
        throw new GitletException();

    }

    /** CHECKOUT.
     * @param args
     * could be commit id, branch name, file name
     * **/
    public void checkout(String[] args) {
        if (Arrays.copyOfRange(args, 1, args.length).length == 1) {
            if (!_branches.containsKey(args[1])) {
                Utils.message("No such branch exists");
                throw new GitletException();
            } else if (_head.equals(args[1])) {
                Utils.message("No need to checkout the current branch");
                throw new GitletException();
            } else {
                checkoutBranch(args[1]);
            }
        } else if (args.length == 3 && args[1].equals("--")) {
            checkoutFile(args[2]);
        } else if (args.length == 4 && args[2].equals("--")) {
            checkoutWithCommit(args[1], args[3]);
        } else {
            Utils.message("Incorrect Operands");
            throw new GitletException();
        }
    }


    /** CHECKOUT FILE.
     * @param fileName
     * the file we are checking out
     * **/
    public void checkoutFile(String fileName) {

        String commitID = _branches.get(_head);
        Commit c = getCommit(commitID);
        HashMap<String, String> cFiles = c.getFiles();

        if (cFiles.containsKey(fileName)) {
            File checkFile = new File(fileName);
            String blobFile = ".gitlet/stage/" + cFiles.get(fileName);
            File f =  new File(blobFile);
            Utils.writeContents(checkFile, Utils.readContentsAsString(f));

        } else {
            Utils.message("File does not exist in that commit");
            throw new GitletException();
        }

    }

    /** CHECKOUT GIVEN COMMIT.
     * @param commitID
     * the id of desired commit
     * @param fileName
     * the name of the file in the commit with commitID
     * **/
    public void checkoutWithCommit(String commitID, String fileName) {

        Commit c = getCommit(extendSha1(commitID));
        HashMap<String, String> cFiles = c.getFiles();
        if (cFiles.containsKey(fileName)) {
            File checkFile = new File(fileName);
            String blobFile = ".gitlet/stage/" + cFiles.get(fileName);
            File f =  new File(blobFile);
            Utils.writeContents(checkFile, Utils.readContentsAsString(f));

        } else {
            Utils.message("File does not exist in that commit");
            throw new GitletException();
        }
    }

    /** CHECKOUT BRANCH.
     * @param branchName
     * the name of the branch we are checking out
     * **/
    public void checkoutBranch(String branchName) {
        String commitID = _branches.get(branchName);
        HashMap<String, String> cFiles = getCommit(commitID).getFiles();
        File cwdirectory = new File(System.getProperty("user.dir"));
        HashMap<String, String> headFiles =
                getCommit(_branches.get(_head)).getFiles();

        if (headFiles == null
                && Objects.requireNonNull(cwdirectory.listFiles()).length > 1) {
            Utils.message("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
            throw new GitletException();
        }

        if (headFiles != null) {
            for (File file : Objects.requireNonNull(cwdirectory.listFiles())) {
                if (!headFiles.containsKey(file.getName())
                        && !file.getName().equals(".gitlet")
                        && !_stage.containsKey(file.getName())) {
                    Utils.message("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    throw new GitletException();
                }
            }
        }



        for (File file : Objects.requireNonNull(cwdirectory.listFiles())) {
            if (cFiles == null) {
                Utils.restrictedDelete(file);
            } else {
                if (!cFiles.containsKey(file.getName())) {
                    if (!file.getName().equals(".gitlet")) {
                        Utils.restrictedDelete(file);
                    }
                }
            }
        }

        if (cFiles != null) {
            for (String fileName : cFiles.keySet()) {
                File f = new File(".gitlet/stage/" + cFiles.get(fileName));
                String contents = Utils.readContentsAsString(f);
                Utils.writeContents(new File(fileName), contents);
            }
        }

        _stage.clear();
        _untracked.clear();
        _head = branchName;

    }


    /** BRANCH.
     * @param branchName
     * the name of the branch we are creating
     * **/
    public void branch(String branchName) {
        if (_branches.containsKey(branchName)) {
            Utils.message("A branch with that name already exists.");
            throw new GitletException();
        } else {
            _branches.put(branchName, _branches.get(_head));
        }
    }

    /** RM BRANCH.
     * @param branchName
     * the branch we are removing
     * **/
    public void rmBranch(String branchName) {
        if (!_branches.containsKey(branchName)) {
            Utils.message("A branch with that name does not exist.");
            throw new GitletException();
        } else if (branchName.equals(_head)) {
            Utils.message("Cannot remove the current branch.");
            throw new GitletException();
        } else {
            _branches.remove(branchName);
        }

    }

    /** RESET.
     * @param id
     * the id of the commit we are dealing with
     * **/
    public void reset(String id) {
        id = extendSha1(id);
        HashMap<String, String> cFiles = getCommit(id).getFiles();
        HashMap<String, String> headFiles =
                getCommit(_branches.get(_head)).getFiles();
        File cwdirectory = new File(System.getProperty("user.dir"));

        if (headFiles.isEmpty()
                && Objects.requireNonNull(cwdirectory.listFiles()).length > 1) {
            Utils.message("There is an untracked file in the way; "
                                + "delete it, or add and commit it first.");
            throw new GitletException();
        }

        if (!headFiles.isEmpty()) {
            for (File file : Objects.requireNonNull(cwdirectory.listFiles())) {
                if (!headFiles.containsKey(file.getName())
                        && !file.getName().equals(".gitlet")
                        && !_stage.containsKey(file.getName())) {
                    Utils.message("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    throw new GitletException();
                }
            }
        }



        for (File file : Objects.requireNonNull(cwdirectory.listFiles())) {
            if (cFiles.isEmpty()) {
                Utils.restrictedDelete(file);
            } else {
                if (!cFiles.containsKey(file.getName())) {
                    if (!file.getName().equals(".gitlet")) {
                        Utils.restrictedDelete(file);
                    }
                }
            }
        }

        for (String fileName : cFiles.keySet()) {
            File f = new File(".gitlet/stage/" + cFiles.get(fileName));
            String contents = Utils.readContentsAsString(f);
            Utils.writeContents(new File(fileName), contents);
        }


        _stage.clear();
        _branches.put(_head, id);
    }


    /** MERGE.
     * @param branchName
     * the branch we are merging with
     * **/
    public void merge(String branchName) {
        checkmergeError(branchName);
        Commit current = getCommit(_branches.get(_head));
        Commit given = getCommit(_branches.get(branchName));

        Commit split =  splitPoint(current, given);
        if (split.equals(current)) {
            checkoutBranch(branchName);
            Utils.message("Current branch fast-forwarded.");
            throw new GitletException();
        } else if (split.equals(given)) {
            Utils.message("Given branch is an ancestor of the current branch.");
            throw new GitletException();
        }

        mergeHelper(branchName, current, split, given);

    }

    /** MERGE HELPER.
     * @param branchName
     * the name of the branch we are merging
     * @param current
     * the current commit
     * @param split
     * the split commit
     * @param given
     * the given commit
     * **/
    public void mergeHelper(String branchName,
                            Commit current, Commit split, Commit given) {
        Set<String> allFiles = new HashSet<>();
        allFiles.addAll(split.getFiles().keySet());
        allFiles.addAll(current.getFiles().keySet());
        allFiles.addAll(given.getFiles().keySet());

        String sFile, cFile, gFile;
        for (String file : allFiles) {
            sFile = split.getFiles().get(file);
            cFile = current.getFiles().get(file);
            gFile = given.getFiles().get(file);
            boolean conflict = false;

            if (sFile == null) {
                if (gFile != null && cFile == null) {
                    checkoutWithCommit(given.getSha1(), file);
                    add(file);
                } else if (gFile != null && !gFile.equals(cFile)) {
                    conflict = true;
                }
            } else {
                if (sFile.equals(cFile)) {
                    if (gFile == null) {
                        rm(file);
                    } else if (!sFile.equals(gFile)) {
                        checkoutWithCommit(given.getSha1(), file);
                        add(file);
                    }
                } else if (gFile == null) {
                    if (cFile != null) {
                        conflict = true;
                    }
                } else if (cFile == null) {
                    if (!gFile.equals(sFile)) {
                        conflict = true;
                    }
                } else if (gFile.equals(cFile)) {
                    conflict = false;
                } else if (!gFile.equals(cFile)) {
                    conflict = true;
                }
            }
            if (conflict) {
                conflictResolver(file, gFile, cFile);
            }

            String message = "Merged " + branchName + " into " + _head + ".";
            commit(message);
            if (conflict) {
                Utils.message("Encountered a merge conflict");
            }
        }
    }

    /** MERGE ERROE CHECKER.
     * @param branchName
     * the branch we are merging with
     * **/
    public void checkmergeError(String branchName) {

        File cwdirectory = new File(System.getProperty("user.dir"));
        HashMap<String, String> headFiles =
                getCommit(_branches.get(_head)).getFiles();

        if (headFiles == null
                && Objects.requireNonNull(cwdirectory.listFiles()).length > 1) {
            Utils.message("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
            throw new GitletException();
        }

        if (headFiles != null) {
            for (File file : Objects.requireNonNull(cwdirectory.listFiles())) {
                if (!headFiles.containsKey(file.getName())
                        && !file.getName().equals(".gitlet")
                        && !_stage.containsKey(file.getName())) {
                    Utils.message("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    throw new GitletException();
                }
            }
        }

        if (!_untracked.isEmpty() || !_stage.isEmpty()) {
            Utils.message("You have uncommitted changes.");
            throw new GitletException();
        }

        if (!_branches.containsKey(branchName)) {
            Utils.message("A branch with that name does not exist.");
            throw new GitletException();
        }
        if (branchName.equals(_head)) {
            Utils.message("Cannot merge a branch with itself.");
            throw new GitletException();
        }

    }

    /** MERGE CONFLICT RESOLVER.
     * @param name
     * name of given file
     * @param gCode
     * the sha1 of the file in given commit
     * @param cCode
     * the sha1 of the file in the current commit
     * **/
    public void conflictResolver(String name, String gCode, String cCode) {
        File branchFile = new File(System.getProperty("user.dir")
                + "/.gitlet/blobs/" + gCode);
        File headFile = new File(System.getProperty("user.dir")
                + ".gitlet/blobs/" + cCode);
        File f = new File(name);
        Formatter result = new Formatter();
        String gContents = "";
        String cContents = "";

        if (branchFile.exists()) {
            gContents = Utils.readContentsAsString(branchFile);
        }
        if (headFile.exists()) {
            cContents = Utils.readContentsAsString(headFile);
        }

        result.format("<<<<<<< HEAD\n");
        result.format(cContents);
        result.format("=======\n");
        result.format(gContents);
        result.format(">>>>>>>\n");
        String s = result.toString();
        Utils.writeContents(f, s);
        add(name);

    }

    /** SPLIT FINDER.
     * @param current
     * the current commit
     * @param given
     * the given commit
     * @return Commit
     * **/
    public Commit splitPoint(Commit current, Commit given) {
        ArrayList<String> currParents = new ArrayList<>();
        ArrayList<String> givenParents = new ArrayList<>();

        currParents.add(current.getSha1());
        while (current.getParent() != null) {
            currParents.add(current.getParent()[0]);
            current = getCommit(current.getParent()[0]);
        }
        givenParents.add(given.getSha1());
        while (given.getParent() != null) {
            givenParents.add(given.getParent()[0]);
            given = getCommit(given.getParent()[0]);
        }



        for (String currParent : currParents) {
            for (String givenParent : givenParents) {
                if (currParent.equals(givenParent)) {
                    return getCommit(currParent);
                }
            }
        }
        Utils.message("No split point");
        throw new GitletException();
    }

}
