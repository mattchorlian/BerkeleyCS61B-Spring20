package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

/** @author Matt Chorlian. **/
public class Commit implements Serializable {


    /** date format for commits. **/
    private static final SimpleDateFormat FORMAT =
            new SimpleDateFormat("EEE MMM d HH:mm:ss YYYY Z");

    /** the commit message. **/
    private String _message;

    /** the timestamp of this commit. **/
    private String _timestamp;

    /** the files of this commit. **/
    private HashMap<String, String> _files;

    /** the _parent(s) of this commit. **/
    private String[] _parent;

    /** the sha1 of this commit. **/
    private String _sha1ID;

    /** COMMIT CONSTRUCTOR.
     * @param message
     * the commit message
     * @param map
     * the file map
     * @param parent
     * the parent of this commit
     * @param initial
     * true if this is the first commit
     * **/
    public Commit(String message, HashMap<String,
            String> map, String[] parent, Boolean initial) {
        if (initial) {
            _timestamp = "Wed Dec 31 16:00:00 1969 -0800";
        } else {
            Date date = new Date();
            _timestamp = FORMAT.format(date);
        }
        _files = map;
        _parent = parent;
        _message = message;
        _sha1ID = hashCommit();

    }

    /** GET PARENTS SHA1.
     * @return String
     * **/
    public String getParentsha1() {
        if (_parent != null) {
            return _parent[0];
        } else {
            return null;
        }
    }

    /** HASH THIS COMMIT.
     * @return String
     * **/
    public String hashCommit() {
        String files;
        if (_files == null) {
            files = "";
        } else {
            files = _files.toString();
        }
        return Utils.sha1(_message,
                files, _timestamp, Arrays.toString(_parent));
    }


    /** GET SHA1.
     * @return String
     * **/
    public String getSha1() {
        return _sha1ID;
    }


    /** GET THIS COMMIT MESSAGE.
     * @return String
     * **/
    public String getMessage() {
        return _message;
    }

    /** GET THIS COMMIT's TIME.
     * @return String
     * **/
    public String getTime() {
        return _timestamp;
    }

    /** GET PARENT.
     * @return String
     * **/
    public String[] getParent() {
        return _parent;
    }

    /** get this commit's files.
     * @return HashMap
     * **/
    public HashMap<String, String> getFiles() {
        return _files;
    }
}
