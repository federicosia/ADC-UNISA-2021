package com.unisa.git;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
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
    private HashMap<String, Crate> files;
    private HashMap<String, Crate> untrackedFiles;
    private ArrayList<Commit> commits;
    private ArrayList<Commit> commitsNotPushed;
    private String id;

    public Repository(String name, File directory) throws IOException{
        this.name = name;
        this.path = new File(directory.getAbsolutePath().concat("\\"+name));
        this.files = new HashMap<>();
        this.untrackedFiles = new HashMap<>();
        this.commits = new ArrayList<>();
        this.commitsNotPushed = new ArrayList<>();
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
     * The id is produced from the tracked files of the repository.
     * @return id the new id generated
     * @throws IOException problems when reading from the file
     */
    public String getId() throws IOException {
        //Read all bytes of all 
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        //Use iterator, otherwise concurrentModificationException is raised!
        Iterator<Entry<String, Crate>> it = this.files.entrySet().iterator();
        while(it.hasNext()) {
            Entry<String, Crate> entry = it.next();
            if(entry.getValue().update())
                outputStream.write(entry.getValue().getContent());
            else 
                it.remove();
        }
        //Gain all bytes read from the directory of the repository
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
            String fileName = file.getName();
            Crate uncommittedCrate = this.untrackedFiles.get(fileName);
            File path_to_file = new File(this.getPath().concat("\\" + fileName));
            //if the key is not present than we simple add a new entry
            if(uncommittedCrate == null){
                Crate currentCrate = this.files.get(fileName);
                Crate newCrate = new Crate(path_to_file);
                //Check if the file we want to commited was already committed!
                if((currentCrate != null) && currentCrate.equals(newCrate))
                    throw new RepositoryException(fileName + " was already committed...");

                //Check if the file is a directory, recursively add all files, otherwise is a file
                if(path_to_file.isDirectory())
                    this.addFile(Arrays.asList(path_to_file.listFiles()));
                
                this.untrackedFiles.put(fileName, newCrate);
            } 
            //The key is already present that we check if the content of the file is the same,
            //if the content is different that we replace the value associated at the key, otherwise already added.
            else {
                Crate newCrate = new Crate(path_to_file);
                if(!uncommittedCrate.equals(newCrate)){
                    //Check if the file is a directory, recursively add all files, otherwise is a file
                    if(path_to_file.isDirectory())
                        this.addFile(Arrays.asList(path_to_file.listFiles()));

                    this.untrackedFiles.put(fileName, newCrate);
                }
                else throw new RepositoryException(fileName + " already added...");
            } 
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
        if(untrackedFiles.size() > 0){
            Commit commit = new Commit(repo_name, message);
            
            commitsNotPushed.add(commit);
            for(Map.Entry<String, Crate> entry: untrackedFiles.entrySet()){
                Crate crate = entry.getValue();
                //now files are tracked
                files.put(crate.getName(), crate);
                commit.addFile(crate.getName());
            }
            untrackedFiles.clear();
            this.id = getId();
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
        if(!commitsNotPushed.isEmpty()){
            for(int i = 0; i < commitsNotPushed.size(); i++){
                Commit commit = commitsNotPushed.get(i);
                if(!commit.updateStatus())
                    return false;
                else {
                    commits.add(commit);
                    commitsNotPushed.remove(i);
                }
            }
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
        for(String filename : remoteRepo.files.keySet()){
            Crate localCrate = this.files.get(filename);
            Crate remoteCrate = remoteRepo.files.get(filename);
            Path path = Paths.get(this.path.getAbsolutePath(), filename);

            if(this.files.containsKey(filename)){
                //also check if the content is different, if different the new file will not be tracked
                //the developer should first resolve the conflict than track the file with git add.
                if(!Arrays.equals(localCrate.getContent(), remoteCrate.getContent())){
                    System.out.println("CONFLITTO "+ path + "\n");
                    this.untrackedFiles.put(generateNewFilename(filename), remoteCrate);
                    result = 2;
                }
                //else nothing
            }
            //else just add it in the repository
            else{
                System.out.println("OK " + path + "\n");
                this.files.put(filename, remoteCrate);
                result = 1;
            }
        }
        materialize();
        this.id = getId();
        //update commits
        System.out.println(commits.size());
        for(Commit commit: remoteRepo.commits){
            if(!this.commits.contains(commit))
                this.commits.add(commit);
        }
        System.out.println(commits.size());
        return result;
    }

    /**
     * Creates a representation of the files in the repository in the file system 
     * after a pull operation. <p>
     * Conflicts with files that are not tracked by Git are managed creating the file
     * with a different name.
     * @throws IOException if something went wrong
     */
    private void materialize() throws IOException{
        for(Crate crate: this.files.values()){
            Path path = Paths.get(this.getPath().concat("\\" + crate.getName()));
            File file = new File(path.toString());
            
            //If is present in the directory a file untracked, with the same name and different
            //content, than we have a conflict. The file pulled from remote will be created with different name.
            if(file.exists() && !Arrays.equals(Files.readAllBytes(path), crate.getContent()))
                path = Paths.get(this.getPath().concat("\\" + generateNewFilename(crate.getName())));
            System.out.println("Tracked file creato -> "+path.toString()+" "+new String(crate.getContent(), StandardCharsets.UTF_8));
            Files.write(path, crate.getContent());
        }
        for(Crate crate: this.untrackedFiles.values()){
            Path path = Paths.get(this.getPath().concat("\\" + crate.getName()));
            File file = new File(path.toString());
            
            //If is present in the directory a file untracked, with the same name and different
            //content, than we have a conflict. The file pulled from remote will be created with different name.
            if(file.exists() && !Arrays.equals(Files.readAllBytes(path), crate.getContent()))
                path = Paths.get(this.getPath().concat("\\" + generateNewFilename(crate.getName())));
            System.out.println("Untracked file creato -> "+path.toString()+" "+new String(crate.getContent(), StandardCharsets.UTF_8));
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
}