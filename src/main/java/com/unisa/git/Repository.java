package com.unisa.git;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
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
        this.path = directory;
        this.files = new HashMap<>();
        this.uncommitedFiles = new ArrayList<>();
        this.id = UUID.randomUUID().toString();
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

    public boolean addFile(List<File> files) throws IOException{
        boolean result = true;
        for(File file : files){
            String fileName = file.getName();
            Crate oldCrate = this.files.get(fileName);

            //if the key is not present than we simple add a new entry
            if(oldCrate == null){
                Crate newCrate = new Crate(file);
                this.files.put(fileName, newCrate);
                uncommitedFiles.add(newCrate);
            } 
            //The key is already present that we check if the content of the file is the same,
            //if the content is different that we replace the value associated at the key, otherwise nothing.
            //N.B: The replace made here is only local
            else {
                Crate newCrate = new Crate(file);
                if(!oldCrate.getId().equals(newCrate.getId())){
                    int index = -1;
                    this.files.replace(fileName, oldCrate, newCrate);
                    //replace the old file with the new file in the commit list, if not present do nothing
                    if((index = uncommitedFiles.indexOf(oldCrate)) > -1){
                        uncommitedFiles.set(index, newCrate);
                    }
                    //means that a file can't be added to the local repo
                } else result = false;
            }
        }
        return result;
    }
    /**
     * Returns the list of uncommited files
     * @return list of uncommited files
     */
    public ArrayList<Crate> getUncommitedFiles(){
        return uncommitedFiles;
    }


    public void addCommit(Commit commit){
        commits.add(commit);
        for(Crate crate : uncommitedFiles){
            crate.setCommit();
        }
        uncommitedFiles.clear();
        this.id = UUID.randomUUID().toString();
    }
}
