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
import java.util.HashMap;
import java.util.Iterator;
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
    private File path;
    private HashMap<String, Crate> trackedFiles;
    private HashMap<String, Crate> stagedFiles;
    private ArrayList<Commit> commits;
    private String id;

    public Repository(String name, File directory) throws IOException{
        this.name = name;
        this.path = new File(directory.getAbsolutePath().concat("\\"+name));
        this.trackedFiles = new HashMap<>();
        this.stagedFiles = new HashMap<>();
        this.commits = new ArrayList<>();
        this.id = "null";
        this.path.mkdir();
    }

    public String getName(){
        return name;
    }

    public String getPath(){
        return path.getAbsolutePath();
    }

    /**
     * Returns the id of the repository, it's generated again every time this method is called.
     * The id is produced from the files of the local repository.
     * @return id the new id generated
     * @throws IOException problems when reading from the file
     */
    public String generateId() throws IOException {
        //Read all bytes of all 
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        //Use iterator, otherwise concurrentModificationException is raised!
        Iterator<Entry<String, Crate>> it = this.trackedFiles.entrySet().iterator();
        while(it.hasNext()) {
            Entry<String, Crate> entry = it.next();
            if(entry.getValue().update())
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
            File path_to_file = new File(this.getPath().concat("\\" + filename));

            //Check if the file exists, otherwise error
            if(!path_to_file.exists())
                throw new RepositoryException(filename + " doesn't exists...");
            //if the key is not present than we simple add a new entry
            if(stagedCrate == null){
                Crate newCrate = new Crate(path_to_file);
                //Check if the file we want to commited was already committed!
                if((trackedCrate != null) && trackedCrate.equals(newCrate))
                    throw new RepositoryException(filename + " was already committed...");

                //Check if the file is a directory, recursively add all files, otherwise is a file
                if(path_to_file.isDirectory())
                    this.addFile(Arrays.asList(path_to_file.listFiles()));
                
                this.stagedFiles.put(filename, newCrate);
            } 
            //The key is already present that we check if the content of the file is the same,
            //if the content is different that we replace the value associated at the key, otherwise already added.
            else {
                Crate newCrate = new Crate(path_to_file);
                if(!stagedCrate.equals(newCrate)){
                    //Check if the file is a directory, recursively add all files, otherwise is a file
                    if(path_to_file.isDirectory())
                        this.addFile(Arrays.asList(path_to_file.listFiles()));

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
            File path_to_file = new File(this.getPath().concat("\\" + filename));

            if(path_to_file.exists()){
                if((trackedFiles.remove(filename) != null) || (stagedFiles.remove(filename) != null))
                    return true;
                else 
                    throw new RepositoryException(filename + " isn't tracked by Git...");
            }
            else 
                throw new RepositoryException(filename + " doesn't exists...");
        }
        return false;
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
                Crate crate = entry.getValue();
                trackedFiles.put(crate.getName(), crate);
                commit.addFile(crate.getName());
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
        for(String filename : remoteRepo.trackedFiles.keySet()){
            Crate localCrate = this.trackedFiles.get(filename);
            Crate remoteCrate = remoteRepo.trackedFiles.get(filename);

            if(this.trackedFiles.containsKey(filename)){
                //also check if the content is different, if different the new file will not be tracked
                //the developer should first resolve the conflict than track the file with git add.
                if(!Arrays.equals(localCrate.getContent(), remoteCrate.getContent())){
                    this.stagedFiles.put(generateNewFilename(filename), remoteCrate);
                    result = 2;
                }
                //else nothing
            }
            //else just add it in the repository
            else{
                this.trackedFiles.put(filename, remoteCrate);
                result = 1;
            }
        }
        materialize();
        this.id = generateId();
        //update commits
        for(Commit commit: remoteRepo.commits){
            if(!this.commits.contains(commit))
                this.commits.add(commit);
        }
        return result;
    }

    /**
     * List of filenames to be committed
     * @return list of type String with filenames
     */
    public List<String> getStagedFiles(){
        List<String> names = new ArrayList<String>();
        for(Crate crate : this.stagedFiles.values())
            names.add(crate.getName());

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
            Path path = Paths.get(this.getPath().concat("\\" + entry.getKey()));
            File file = new File(path.toString());
            System.out.println(path.toString());
            if(file.exists()){
                if(!Arrays.equals(Files.readAllBytes(path), entry.getValue().getContent()))
                    names.add(entry.getKey());
            }
            else names.add(entry.getKey());
        }

        return names;
    }

    /**
     * List of filenames untracked by Git
     * @return list of type String with filenames
     */
    public List<String> getUntrackedFiles(){
        List<String> names = new ArrayList<String>();
        File[] files = this.path.listFiles();
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
     * Creates a representation of the files in the repository in the file system 
     * after a pull operation. <p>
     * Conflicts with files that are not tracked by Git are managed creating the file
     * with a different name.
     * @throws IOException if something went wrong
     */
    private void materialize() throws IOException{
        for(Crate crate: this.trackedFiles.values()){
            Path path = Paths.get(this.getPath().concat("\\" + crate.getName()));
            File file = new File(path.toString());
            
            //If is present in the directory a file untracked, with the same name and different
            //content, than we have a conflict. The file pulled from remote will be created with different name.
            if(file.exists() && !Arrays.equals(Files.readAllBytes(path), crate.getContent()))
                path = Paths.get(this.getPath().concat("\\" + generateNewFilename(crate.getName())));
            Files.write(path, crate.getContent());
        }
        for(Crate crate: this.stagedFiles.values()){
            Path path = Paths.get(this.getPath().concat("\\" + crate.getName()));
            File file = new File(path.toString());
            
            //If is present in the directory a file untracked, with the same name and different
            //content, than we have a conflict. The file pulled from remote will be created with different name.
            if(file.exists() && !Arrays.equals(Files.readAllBytes(path), crate.getContent()))
                path = Paths.get(this.getPath().concat("\\" + generateNewFilename(crate.getName())));
            Files.write(path, crate.getContent());
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
        return oldFilename.substring(0, lastdotIndex) + "_(1)" + oldFilename.substring(lastdotIndex);
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
}