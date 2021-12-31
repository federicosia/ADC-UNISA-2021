package com.unisa.git;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.unisa.git.exceptions.RepositoryException;
/**
 * The repository of an user, the Repository class allows to manage, create and 
 * control the workflow of the repositories. 
 */
public class Repository {
    private String name;
    private File path;
    private HashMap<String, Crate> files;
    private ArrayList<Crate> uncommitedFiles;
    private ArrayList<Commit> commits;
    private String id;

    public Repository(String name, File directory){
        this.name = name;
        this.path = new File(directory.getAbsolutePath().concat("\\"+name));
        this.files = new HashMap<>();
        this.uncommitedFiles = new ArrayList<>();
        this.commits = new ArrayList<>();
        this.id = UUID.randomUUID().toString();
        this.path.mkdir();
    }

    public String getName(){
        return name;
    }

    public String getPath(){
        return path.getAbsolutePath();
    }

    public String getId() {
        return id;
    }

    public boolean addFile(List<File> files) throws IOException, RepositoryException{
        for(File file : files){
            String fileName = file.getName();
            Crate oldCrate = this.files.get(fileName);
            File path_to_file = new File(this.getPath().concat("\\" + fileName));
            //if the key is not present than we simple add a new entry
            if(oldCrate == null){
                Crate newCrate = new Crate(path_to_file);
                //Check if the file is a directory, recursively add all files, otherwise is a file
                if(path_to_file.isDirectory())
                    this.addFile(Arrays.asList(path_to_file.listFiles()));
                
                this.files.put(fileName, newCrate);
                uncommitedFiles.add(newCrate);
            } 
            //The key is already present that we check if the content of the file is the same,
            //if the content is different that we replace the value associated at the key, otherwise nothing.
            //N.B: The replace made here is only local
            else {
                Crate newCrate = new Crate(path_to_file);
                if(!oldCrate.getId().equals(newCrate.getId())){
                    //Check if the file is a directory, recursively add all files, otherwise is a file
                    if(path_to_file.isDirectory())
                        this.addFile(Arrays.asList(path_to_file.listFiles()));
                        
                    int index = -1;
                    this.files.replace(fileName, oldCrate, newCrate);
                    //replace the old file with the new file in the commit list, if not present do nothing
                    if((index = uncommitedFiles.indexOf(oldCrate)) > -1){
                        uncommitedFiles.set(index, newCrate);
                    }
                    //means that a file can't be added to the local repo
                } else throw new RepositoryException(fileName + " already present...");
            }
        }
        return true;
    }
    /**
     * Returns the list of uncommited files
     * @return list of uncommited files
     */
    public ArrayList<Crate> getUncommitedFiles(){
        return uncommitedFiles;
    }

    /**
     * Do a commit, last step before the push of the modified files in the remote repository
     * @param commit commit to be made, compose by the repository name and a message.
     * @return true if there's something to commit, false otherwise.
     */
    public boolean addCommit(Commit commit) throws RepositoryException{
        if(uncommitedFiles.size() > 0){
            commits.add(commit);
            for(Crate crate : uncommitedFiles){
                crate.setCommit();
            }
            uncommitedFiles.clear();
            this.id = UUID.randomUUID().toString();
            return true;
        }
        else throw new RepositoryException("Nothing to commit...");
    }
}
