package com.unisa.git.repository;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import com.unisa.git.exceptions.RepositoryException;

/**
 * The repository of an user, the Repository class allows to manage, create and 
 * control the workflow of the repositories. 
 */
public class Repository implements Serializable{
    private String name;
    private String path;
    private LinkedHashMap<String, Crate> trackedFiles;
    private LinkedHashMap<String, Crate> stagedFiles;
    private ArrayList<Commit> commits;
    private String id;

    public Repository(String name, File directory) throws IOException{
        this.name = name;
        this.path = Paths.get(directory.getAbsolutePath(), name).toString();
        this.trackedFiles = new LinkedHashMap<>();
        this.stagedFiles = new LinkedHashMap<>();
        this.commits = new ArrayList<>();
        this.id = "null";
        Files.createDirectories(Paths.get(path));
    }

    public String getName(){
        return name;
    }

    public String getPath(){
        return path;
    }

    /**
     * Returns the id of the repository. The id is generated every time this method is called.
     * @return String reppresenting the id
     * @throws IOException problems when reading from the file
     */
    public String generateId() throws IOException {
        //Read all bytes
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        //Use iterator, otherwise concurrentModificationException is raised!
        Iterator<Entry<String, Crate>> it = this.trackedFiles.entrySet().iterator();
        while(it.hasNext()) {
            Entry<String, Crate> entry = it.next();
            if(entry.getValue().checkIfExists())
                outputStream.write(entry.getValue().getContent());
            else 
                it.remove();
        }
        //Gain all bytes read from the repository
        byte[] dirBytes = outputStream.toByteArray();
        //Generate the new id from the bytes of the directory
        this.id = UUID.nameUUIDFromBytes(dirBytes).toString();
        return id;
    }

    /**
     * Check if the last commit of the remote repository is present in the local repository,
     * this way we can check if the local repository is up to date or not.
     * @return true if up do date, false otherwise
     */
    public boolean checkLastCommit(Repository remoteRepository){
        if(!this.commits.isEmpty()){
            Commit lastRemoteCommit = remoteRepository.commits.get(remoteRepository.commits.size() - 1);
        
            return this.commits.contains(lastRemoteCommit);
        }
        else return false;
    }

    public boolean addFile(List<File> files) throws IOException, RepositoryException{
        for(File file : files){
            String filename = file.getName();
            Crate trackedCrate = this.trackedFiles.get(filename);
            Crate stagedCrate = this.stagedFiles.get(filename);
            Path path_to_file = Paths.get(this.getPath(), filename);

            //Check if the file exists, otherwise error
            if(!path_to_file.toFile().exists())
                throw new RepositoryException(filename + " doesn't exists...");
            //if the key is not present than we simple add a new entry
            if(stagedCrate == null){
                Crate newCrate = new Crate(path_to_file.toFile());
                //Check if the file we want to commited was already committed!
                if((trackedCrate != null) && trackedCrate.equals(newCrate))
                    throw new RepositoryException(filename + " was already committed...");
                
                this.stagedFiles.put(filename, newCrate);
            } 
            //The key is already present that we check if the content of the file is the same,
            //if the content is different that we replace the value associated at the key, otherwise already added.
            else {
                Crate newCrate = new Crate(path_to_file.toFile());
                if(!stagedCrate.equals(newCrate)){
                    
                    this.stagedFiles.put(filename, newCrate);
                }
                else throw new RepositoryException(filename + " already added...");
            } 
        }
        return true;
    }

    public boolean removeFile(List<File> files) throws RepositoryException{
        for(File file: files){
            String filename = file.getName();
            if(trackedFiles.get(filename) != null)
                stagedFiles.put(filename, null);
            else 
                throw new RepositoryException(filename + " isn't tracked by Git...");
        }
        return true;
    }

    /**
     * Do a commit, last step before the push of the modified files in the remote repository
     * @param commit commit to be made, compose by the repository name and a message.
     * @return true if there's something to commit, false otherwise.
     * @throws IOException
     */
    public boolean addCommit(String repo_name, String message) throws IOException {
        if(stagedFiles.size() > 0){
            Commit commit = new Commit(repo_name, message);
            
            commits.add(commit);
            for(Map.Entry<String, Crate> entry: stagedFiles.entrySet()){
                String filename = entry.getKey();
                Crate crate = entry.getValue();
                //value != null means add, value == null means remove!
                if(crate != null)
                    trackedFiles.put(filename, crate);
                else {
                    this.deleteFiles(Paths.get(this.getPath(), filename).toFile());
                    trackedFiles.remove(filename);
                }
                commit.addFile(filename);
            }
            stagedFiles.clear();
            this.id = generateId();
            return true;
        }
        return false;
    }

    /**
     * Check if there are commits to be pushed
     * @return true if there's something to push, false otherwise
     * @throws IOException
     */
    public boolean checkBeforePush() throws IOException {
        List<Commit> commitsNotPushed = this.commitsNotPushed();
        if(!commitsNotPushed.isEmpty()){
            for(Commit commit : commitsNotPushed)
                commit.updateStatus();
            return true;
        }
        return false;
    }

    /**
     * Donwload files in the current repository from the remote repository passed as argument.
     * This method doesn't fix conflicts, it just finds them and create the file with a different name,
     * the user should fix the conflicts manually. Files that are not present in the 
     * local repository will be simply added. <p> 
     * N.B: In this method are managed conflicts with files that are TRACKED.
     * @param repository the repository to compare with the current repository
     * @return 0 if no upgrades were made, 1 if files were added without conflicts, 
     * 2 if files were added but with conflicts
     * @throws IOException
     */
    public int update(Repository remoteRepo) throws IOException{
        int result = 0;
        for(Map.Entry<String, Crate> entry : remoteRepo.trackedFiles.entrySet()){
            String filename = entry.getKey();
            Crate remoteCrate = entry.getValue();
            Crate localCrate = this.trackedFiles.get(filename);

            if(this.trackedFiles.containsKey(filename)){
                //also check if the content is different, if different the new file will not be tracked
                //the developer should first resolve the conflict than track the file with git add.
                if(!localCrate.equals(remoteCrate)){
                    this.trackedFiles.put(generateNewFilename(filename), remoteCrate);
                    result = 2;
                }
                //else nothing
            }
            //else just add it in the repository
            else{
                this.trackedFiles.put(filename, remoteCrate);
                if(result != 2)
                    result = 1;
            }
        }
        materialize();
        this.id = generateId();
        //update commits
        for(Commit commit: remoteRepo.commits){
            if(!this.commits.contains(commit))
                this.commits.add(0, commit);
        }
        return result;
    }

    /**
     * List of filenames to be committed
     * @return list of type String with filenames
     */
    public List<String> getStagedFiles(){
        List<String> names = new ArrayList<String>();
        for(String filename: this.stagedFiles.keySet())
            names.add(filename);

        return names;
    }

    /**
     * List of filenames with changes but not staged for commit
     * @return list of type String with filenames
     * @throws IOException
     */
    public List<String> getUnstagedFiles() throws IOException{
        List<String> names = new ArrayList<String>();

        Iterator<Entry<String, Crate>> it = this.trackedFiles.entrySet().iterator();
        while(it.hasNext()) {
            Entry<String, Crate> entry = it.next();
            String filename = entry.getKey();
            Crate crateInTracked = entry.getValue();
            Path path = Paths.get(this.getPath(), filename);
            File file = new File(path.toString());

            if(file.exists()){
                byte[] contentFileInFS = Files.readAllBytes(path);
                Crate crateInStaged = this.stagedFiles.get(filename);
                //The first check is needed because a file staged can be also unstaged,
                //for example add of a file, then the file is modified, the filename will appear
                //in staged and unstaged lists.
                if(crateInStaged != null){
                    if(!Arrays.equals(crateInStaged.getContent(), contentFileInFS)){
                        names.add(filename);
                    }
                } else if(!Arrays.equals(crateInTracked.getContent(), contentFileInFS)){
                    names.add(filename);
                }
            }
        }

        return names;
    }

    /**
     * List of filenames tracked by Git.
     * @return list type String with filenames of tracked files
     * @throws IOException
     */
    public List<String> getTrackedFiles() throws IOException {
        List<String> names = new ArrayList<String>();

        Iterator<Entry<String, Crate>> it = this.trackedFiles.entrySet().iterator();
        while(it.hasNext()) {
            Entry<String, Crate> entry = it.next();
            names.add(entry.getKey());
        }

        return names;
    }

    /**
     * List of filenames untracked by Git
     * @return list of type String with filenames
     */
    public List<String> getUntrackedFiles(){
        List<String> names = new ArrayList<String>();
        File[] files = Paths.get(path).toFile().listFiles();
        String filename = "";

        for(int i = 0; i < files.length; i++){
            filename = files[i].getName();
            if((stagedFiles.get(filename) == null) && (trackedFiles.get(filename) == null))
                names.add(filename);
        }

        return names;
    }

    @Override
    public boolean equals(Object object){
        if(object instanceof Repository){
            Repository repo = (Repository) object;
            if(this.id.equals(repo.id))
                return true;
        }
        return false;
    }

    /**
     * Creates a representation of the files of the repository in the file system 
     * after a pull operation. <p>
     * Conflicts with files that are not tracked by Git are managed creating the file
     * with a different name.
     * @throws IOException if something went wrong
     */
    private void materialize() throws IOException{
        for(Crate crate: this.trackedFiles.values()){
            Path path = Paths.get(this.getPath(), crate.getName());

            if(this.stagedFiles.get(crate.getName()) != null){
                if(path.toFile().exists() && !Arrays.equals(Files.readAllBytes(path), crate.getContent()))
                    path = Paths.get(this.getPath(), generateNewFilename(crate.getName()));
                Files.write(path, crate.getContent());
            }
            else {
                if(path.toFile().exists() && !Arrays.equals(Files.readAllBytes(path), crate.getContent()))
                    path = Paths.get(this.getPath(), generateNewFilename(crate.getName()));
                Files.write(path, crate.getContent());
            }
        }
    }

    /**
     * Generate a new filename to from another filename, this method is used to resolve conflicts when
     * pulling files from the remote repository.
     * @param oldFilename the old filename, used to create the new filename
     * @return a string that reppresent the new name of the file
     */
    private String generateNewFilename(String oldFilename){
        int lastdotIndex = oldFilename.indexOf(".");
        if(lastdotIndex > -1)
            return oldFilename.substring(0, lastdotIndex) + "_(1)" + oldFilename.substring(lastdotIndex);
        else
            return oldFilename + "_(1)";
    }

    /**
     * Returns a sublist of commits that represents all commits not pushed to remote
     * @return a sublist of commits 
     */
    private List<Commit> commitsNotPushed(){
        int i = 0;
        for(; i < commits.size(); i++){
            if(!commits.get(i).getPushed()) break;
        }
        return commits.subList(i, commits.size());
    }

    /**
     * Recursion delete files and dirs
     * @param file
     */
    private void deleteFiles(File file){
        File[] files = file.listFiles();
        if(files != null){
            for(File f: files){
                deleteFiles(f);
            }
        }
        file.delete();
    }
}