package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

/** @author Matt Chorlian. **/
public class Blob implements Serializable {

    /** the name of this blob. **/
    private String _name;

    /** the contents of this blob. **/
    private byte[] _contents;

    /** the hashID of this blob. **/
    private String hashID;

    /** BLOB CONSTRUCTOR.
     * @param f
     * the file which this blob is made of
     * **/
    public Blob(File f) {
        _contents = Utils.readContents(f);
        _name = f.getName();
        hashID = Utils.sha1(_name, _contents);

    }

    /** get blob name.
     * @return String
     * **/
    public String getName() {
        return this._name;
    }

    /** get blob hashID.
     * @return String
     * **/
    public String getHashID() {
        return this.hashID;
    }

    /** get contents of this blob.
     * @return byte[]
     * **/
    public byte[] getContents() {
        return this._contents;
    }

    /** get the contents of this blob as a string.
     * @return String
     * **/
    public String getStringContents() {
        return Arrays.toString(this._contents);
    }

}
