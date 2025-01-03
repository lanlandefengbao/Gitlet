# Gitlet Design Document

A version control system that mimics the basic features of __Git__.  
**Author**: B Li  
**Duration**: 2024.10 - 2024.12

## Classes and Data Structures

### Main
This is the entry point of _Gitlet_. 
It only has a __main__ method which takes arguments from the command line and 
then executes the corresponding function of _Gitlet_.
#### Fields
None

### Repository
This class defines system files for _Gitlet_. For each object, all its instance variables are the representations of 
the files in the `.gitlet` folder of a specific repository.
#### Fields
See `Repository.java` for detail.

### Commit
This class defines the `Commit` object, which represents the snapshot of all project files at a specific time.
It has a static variable `repo` of class `Repository` to point to the current local repository.
Each object contains a `logMessage`; a `timestamp`; a `Blobs` holds all project files; a `Parent` points to the 
`Commit` object which the current object were built upon.  

`Commit` objects of may eventually form a __Directed Acyclic Graph (DAG)__, 
where each `Commit` object has a pointer to its parent `Commit` object, 
except the initial `Commit` object, which is automatically formed when the user
initialize `Gitlet` system and has no `Parent`. 

#### Fields
1. #### `String logMessage`
   User defined description for this commit. The logMessage for initial commit will be `initial commit`.
2. #### ` String timestamp` 
   The system time when the snapshot was made, of the pattern`"EEE MMM dd HH:mm:ss yyyy Z"`.The timestamp for initial commit will be `00:00:00 UTC, Thursday, 1 January 1970`.
3.  #### `Map<File, String> Blobs`
   A map of all files in the current commit, where the key is the file and the value is the SHA1 hash of the file content. The initial commit contains no file.
4. #### `List<String> Parent`
    A list of SHA1 hash of the parent commit(s). The initial commit has no parent.

### Blob
A `Blob` object represents a snapshot of a specific file, with the file name be the SHA1 that generated from its content,
and its content are stored in `CONTENT` in the form of _byte[]_.  

Every single `Commit` object, except the initial one, are linked with one or more `Blob` object through its `Blobs` variable.
By doing so, we can reuse files already exists in historical commits for version control, thus avoid memory waste.

#### Fields

1. #### `byte[] CONTENT`
   Stores the content of the file.

### StagedFile
This class represents the _Staging Area_ of _Gitlet_, which is a temporary storage for files that are going to be committed.

#### Fields
1. #### `Map<File, Blob> Addition`
   A map of files that are going to be added to the next commit, where the key is the file name and the value is the SHA1 hash of `Blob` object.
2. #### `Set<File> Removal`
   A set of files that are going to be removed from the current commit, where the key is the file.

### Watcher
This class is used to monitor the changes of files in the working directory compare with the current commit that the user is lying on.
There are plenty of situations that the changes could be, each of them is represented by an instance variable of this object.

#### Fields
See `Watcher.java` for detail.

### CommitTree
This class is used to represent the commit history, which is actually a _Directed Acyclic Graph_, of the current repository.
Each object stands for a graph after being traversed in _BFS_ manner, the information of such a graph has been stored in its instance variables.

#### Fields
1. #### HashMap<String, Commit[]> VerticesTable
   Each key-value pair in this map represents a vertex in the graph, where the key is the SHA1 hash of the `Commit` object and the value is an array of `Commit` objects that are the _parents_ of the key `Commit` object.
2. #### HashMap<String, Integer> DistanceTable
    Each key-value pair in this map represents a vertex in the graph, where the key is the SHA1 hash of the `Commit` object and the value is the distance from the initial commit to the key `Commit` object.
## Algorithms

### Commit

1. #### `Commit()`
   Construct the initial `Commit` object.
2. #### `Commit(String logMessage)`
   Construct a normal `Commit` object with the given log message.
3. #### `String hash()`
    Generate the SHA1 hash of the `Commit` object.
4. #### `void setupPersistence(File projectFolder)`
   Set up the initial Gitlet system under a given _PROJECT_DIRECTORY_ if it doesn't have one yet,
   including create directories specified in `Repository.java` and the initial commit.
5. #### `Commit getHeadCommit()`
   Get the current active `Commit` object.
6. #### `Commit[] getParents()`
   Get parents of the current active commit.
7. #### `boolean isDetached()`
    Check if the current active commit is a branch head.  

.........  

Get boring, skip the description of the rest methods. The code itself should be self-explanatory.  

However, the logic of defining files of various states is worth mentioning:
### Files in different states
1. #### Untracked files
   ##### Newly added files that waiting to be staged for addition:
   1. files in CWD that neither being staged for addition nor tracked in current commit.
   2. files staged for removal, but then re-created in CWD. The files could be _1_.the same as they were in current commit; or _2_.with different contents.
2. #### Files Changed but not staged for commit
   ##### Newly removed files waiting to be staged for removal & Modified files waiting to be staged for addition:
   1. Files tracked in current commit, with content changed in CWD but not staged for addition;
   2. Files staged for addition, changed in CWD, but (this change) not staged. Including two cases:
      either _1._ files are tracked, and the updated contents are the same as the current commit version;
      or _2._ files not tracked / tracked but with different contents.
   3. Files staged for addition, but deleted in the working directory. Including two cases:
      _1_. files tracked in current commit; _2_. files not tracked in current commit.
   4. Files tracked in current commit but not staged for addition, deleted in CWD, but not staged for removal.
3. #### Staged files
   ##### The confirmed files from state 1 and 2, plus untouched files form current commit:
   1. The `add` command should enrich the `Addition` field with newly added files and files with updated contents, which could either from _1.1, 1.2.2, 2.1 or 2.2.2_.
   2. The `rm` command will enrich the `Removal` field with files newly confirmed to be removed, which is from _2.3.1, 2.4_
   3. Files may also be withdrawn from `Addition` or `Removal` when operating `add` / `rm`, which could either be the case of _1.2, 2.2.1 or 2.3_.
4. #### Removed files
   ##### Files be deleted from the current commit  

Another thing to mention is about the various situation be discussed in the `merge` function:
1. Any files that have been modified in the given branch since the split point, but not modified in the current branch since the split point should be changed to their versions in the given branch (checked out from the commit at the front of the given branch). These files should then all be automatically staged. To clarify, if a file is “modified in the given branch since the split point” this means the version of the file as it exists in the commit at the front of the given branch has different content from the version of the file at the split point. Remember: blobs are content addressable!

2. Any files that have been modified in the current branch but not in the given branch since the split point should stay as they are.

3. Any files that have been modified in both the current and given branch in the same way (i.e., both files now have the same content or were both removed) are left unchanged by the merge. If a file was removed from both the current and given branch, but a file of the same name is present in the working directory, it is left alone and continues to be absent (not tracked nor staged) in the merge.

4. Any files that were not present at the split point and are present only in the current branch should remain as they are.

5. Any files that were not present at the split point and are present only in the given branch should be checked out and staged.

6. Any files present at the split point, unmodified in the current branch, and absent in the given branch should be removed (and untracked).

7. Any files present at the split point, unmodified in the given branch, and absent in the current branch should remain absent.

8. Any files modified in different ways in the current and given branches are in conflict. “Modified in different ways” can mean that the contents of both are changed and different from other, or the contents of one are changed and the other file is deleted, or the file was absent at the split point and has different contents in the given and current branches.  

To define these cases, we need first find the `Split point` of the current branch and the given branch, which is the _latest common ancestor_ of the two branch head. See `Commit.java` for detail.


## Persistence
Both `Commit` and `Blob`are stored in folders under `.gitlet/objects`, 
where the folders are named by the first two characters of the object's SHA-1 hash, just like the real _Git_ does.
This approach functions similarly to a hashing mechanism,
which effectively narrows the search scope from all SHA-1 hashes in `.gitlet/objects` to those with a specific prefix, akin to the general principles of hashing
`StagingArea` stored in the file `.gitlet/index`.  
......  

```
├── PROJECT FOLDER               <==== Where we apply the Gitlet system
├── .gitlet                         <==== Where the Gitlet system files are stored
│   ├── objects              <==== Where the Commit and Blob objects are stored
│   │   ├── 00
│   │   │   ├── 8bf1d794ca2e9ef8a4007275acf3751c7170ff
│   │   │   └── 81c9575d180a215d1a636545b8fd9abfb1d2bb
│   │   ├── 5b
│   │   │   ├── 8fd9abfb1d2bbd5e1f0f0e6e0f5e1f0f0e6e0f
│   │   │   └── ......
│   │   ├── ......
│   ├── refs                   <==== Where the branch heads are stored (Both local and remote)
│   │   ├── heads             <==== Where the local branch heads are stored
│   │   │   ├── master
│   │   │   └── ......
│   │   ├── remotes         <==== Where the remote branch heads are stored
│   │   │   ├── R1           <==== Remote repository 1
│   │   │   │   ├── master
│   │   │   │   └── ......
│   │   │   ├── ......
│   ├── HEAD                  <==== Point to the file that represent the current branch head
│   ├── index                   <==== Where the StagingArea is stored
├── PROJECT FILES                         <==== current version of the project files
├── ....
```

See `Repository.java` for detail.


