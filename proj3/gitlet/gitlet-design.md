# Gitlet Design Document

**Name**: Matt Chorlian

## Classes and Data Structures
**BLOB**
The class which records files as Blobs
- byte[] inside : retrieves the contents of a file
- String name: name of the file
    - getName()
- String hashID: the SHA1 of the contents of the file
    - getHashID()
    
**BRANCH**
Pointer to a branch in the current state (master, i.e.)
String _name: the name of the branch
String _head; the sha1 code for the front of the branch 
LinkedList<String> _commits: a list of commits contained in this branch

**COMMIT**
- sha1ID:
    - getSha1ID()
Private Variables:
- private String[] _parents;
    - get_parents();
- private String _message;
    - get_message();
- private String _timestamp;
    -get_timestamp();    
- private HashMap<String, String> _files;
- private String sha1ID;

**MAIN**
Where all the commands will be processed and delegated.
- private String _head;
     - the head commit.
- private HashMap<String, String> _branches;
    - branches to be staged or checkout
- private HashMap<String, String> _stage;
    - where commits will be staged
- private ArrayList<String> _untrackedFiles;
    - files which have not been added/committed
- private Stage _stagingArea;  
private Branch _branch: current branch being worked on  
    
**STAGE**
- private LinkedList<?> _staged: list for staged files
- private String _branch: the current branch.
- private HashMap<String, String> _tracked: files which have been tracked
    - uses file name and Sha1ID as keys, values in map;    
    
## Algorithms
**COMMIT CLASS**
1. Commit(String msg, Hashmap<String, String> f, String[] p)
    - initializes a commit with message msg, parent p, files in f
2. hashCommit()
    - converts files and parents of the commit to a string to be used for SHA1ID   

**BRANCH CLASS**
1. Branch(String name, String head)
    - initializes a branch with name name and head commit head;

**MAIN CLASS**
1. init()
    - initializes a gitlet directory
2. add()
    - adds a file to instance of Stage : _stagingArea which can be committed 
3. commit()
    -  saves the collection of files in the _stagingArea and creates a new instance of Commit.class 
4. rm
    - removes a file from _stagingArea;
5. log
    - this method will display the history of the current Branch, saved in the Branch class.  
6. global-log
    - this method will display every commit, aka every Branch. May create a class to track every commit in a large tree.
7. find
    - searches through the global-log of commits for a commit with desired name.
8. status
    - iterates through currently existing branches, and displays their name/files
9. checkout
    - there need be 3 checkout methods, one for each cases
    1. this method will require accessing the front of this branch, putting the necessary file in the CWD
    2. searches through the branches for file with given commit ID, and places it in CWD
    3. takes all files in _head and places them in CWD using Utils.join
    
 10. Branch
    - creates a new Branch.class instance
    
 11. rm-branch
    - removes branch with given name;
    
 12. Merge 
    - this method will require two Branch.class instances and will merge them:
    - will revisit this when more of the project is complete    
    
    
## Persistence
1. java gitlet.Main add [file]
The contents of the file will be read and written to a blob, which will be added to _stagingArea
Utils.WriteContents(blob, inside);

2. java gitlet.Main commit [message]
The currently staged files will be saved in an instance of the commit class, which will be linked to the previous commit
Utils.writeOject(newfile, newcommit)

3. When files are added, committed, removed etc. (for each time the Main method is run) the contents will be saved with a save(Class) method
- this will store the contents of the directory in between calls to main
- this is done through serialization, which will write objects to files to be saved, and retrievable if need be.