package com.unisa.git;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.unisa.git.exceptions.RepositoryException;
/**
 * The repository of an user, the Repository class allows to manage, create and 
 * control the workflow of the repositories. 
 */
public class Repository implements Serializable{
    private String name;
    private File path;
    private HashMap<String, Crate> files;
    private HashMap<String, Crate> uncommittedFiles;
    private ArrayList<Commit> commits;
    private ArrayList<Commit> commitsNotPushed;
    private String id;

    public Repository(String name, File directory) throws IOException{
        this.name = name;
        this.path = new File(directory.getAbsolutePath().concat("\\"+name));
        this.files = new HashMap<>();
        this.uncommittedFiles = new HashMap<>();
        this.commits = new ArrayList<>();
        this.commitsNotPushed = new ArrayList<>();
        this.id = UUID.nameUUIDFromBytes(Files.readAllBytes(path.toPath())).toString();
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
     * @return id the new id generated
     * @throws IOException problems when reading from the file
     */
    public String getId() throws IOException {
        this.id = UUID.nameUUIDFromBytes(Files.readAllBytes(path.toPath())).toString();
        return id;
    }

    @Override
    public boolean equals(Object object){
        try {
            if(object instanceof Repository){
                Repository repo = (Repository) object;
            
                if(this.getId().equals(repo.getId()))
                    return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public boolean addFile(List<File> files) throws IOException, RepositoryException{
        for(File file : files){
            String fileName = file.getName();
            Crate oldCrate = this.uncommittedFiles.get(fileName);
            File path_to_file = new File(this.getPath().concat("\\" + fileName));
            //if the key is not present than we simple add a new entry
            if(oldCrate == null){
                Crate committedFile = this.files.get(fileName);
                Crate newCrate = new Crate(path_to_file);
                //Check if the file we want to commited was already committed!
                if((committedFile != null) && committedFile.equals(newCrate))
                    throw new RepositoryException(fileName + " was already committed...");

                //Check if the file is a directory, recursively add all files, otherwise is a file
                if(path_to_file.isDirectory())
                    this.addFile(Arrays.asList(path_to_file.listFiles()));
                
                this.uncommittedFiles.put(fileName, newCrate);
            } 
            //The key is already present that we check if the content of the file is the same,
            //if the content is different that we replace the value associated at the key, otherwise nothing.
            else {
                Crate newCrate = new Crate(path_to_file);
                if(!oldCrate.equals(newCrate)){
                    //Check if the file is a directory, recursively add all files, otherwise is a file
                    if(path_to_file.isDirectory())
                        this.addFile(Arrays.asList(path_to_file.listFiles()));

                    this.uncommittedFiles.replace(fileName, oldCrate, newCrate);
                }
                else throw new RepositoryException(fileName + " already present...");
            } 
        }
        return true;
    }

    /**
     * Do a commit, last step before the push of the modified files in the remote repository
     * @param commit commit to be made, compose by the repository name and a message.
     * @return true if there's something to commit, false otherwise.
     */
    public boolean addCommit(Commit commit) {
        if(uncommittedFiles.size() > 0){
            //commits.add(commit);
            commitsNotPushed.add(commit);
            for(Crate crate : uncommittedFiles.values()){
                files.put(crate.getName(), crate);
            }
            uncommittedFiles.clear();
            return true;
        }
        return false;
    }

    /**
     * Check if the last commit of the remote repository is present in the local repository. 
     * Receiving true means that current repository is up to date, otherwise 
     * a pull is required before pushing something.
     * @param repository the repository to compare with
     * @return true if the current repository has the last commit, false otherwise.
     */
    public boolean checkLastCommit(Repository repository){
        if(!this.commits.isEmpty() && !repository.commits.isEmpty()){    
            Commit lastCommitRemoteRepo = repository.commits.get(repository.commits.size() - 1);
            //start from the last commit
            for(int i = this.commits.size() - 1; i >= 0; i--){
                Commit commit = this.commits.get(i);
                
                //if the current commit happened before the last commmit of the remote repository
                //then it's useless looking for the target commit. 
                if(LocalDate.parse(commit.getDate()).isBefore(LocalDate.parse(lastCommitRemoteRepo.getDate())))
                    break;

                if(commit.equals(lastCommitRemoteRepo))
                    return true;
            }
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
            this.id = getId();
            return true;
        }
        return false;
    }

    /**
     * Finds conflicts in the current repository with the one passed as argument at method call.
     * This method doesn't fix conflicts, it just finds them and duplicate the files with different names,
     * the developer should fix the conflicts manually.
     * @param repository the repository to compare with the current repository
     * @return true if one or more conflicts where present, false otherwise.
     * @throws IOException
     */
    public boolean findConflicts(Repository repository) throws IOException{
        boolean conflicts = false;
        for(String filename : repository.files.keySet()){
            //For each file in the current repo, if is present in the target repo than add the copy
            //in the current repo with different name
            if(this.files.containsKey(filename)){
                //also check if the content is different, otherwise do nothing
                if(!Arrays.equals(this.files.get(filename).getContent(), repository.files.get(filename).getContent())){
                    this.files.put(filename + "_(1)", repository.files.get(filename));
                    conflicts = true;
                }
            }
            //else just add it in the repository
            else this.files.put(filename, repository.files.get(filename));
        }
        this.id = getId();
        return conflicts;
    }

    /**
     * Creates a representation of the files in the repository in the file system.
     * @return false if something went wrong, true otherwise
     */
    public boolean materialize(){
        try {
            System.out.println("Taglia files -> "+files.size()+""); 
            for(Crate crate: this.files.values()){
                System.out.println(this.getPath().concat("\\" + crate.getName()));
                Path path = Paths.get(this.getPath().concat("\\"+crate.getName()));
                Files.write(path, crate.getContent());
            }
            return true;
        } catch (IOException e) {
            System.err.println(e);
            return false;
        }
    }
}
