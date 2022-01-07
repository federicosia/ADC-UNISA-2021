package com.unisa.git.repository;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

public class Commit implements Serializable{
    private String repositoryName;
    private String message;
    private String date;
    private String id;
    private boolean pushed;
    private ArrayList<String> name_files;

    /**
     * Create a new commit, a date and a unique id are created to indetify uniquely the commit
     * @param repo name of the repository
     * @param message message written for the commit
     */
    public Commit(String repo, String message){
        this.repositoryName = repo;
        this.message = message;
        this.date = LocalDate.now().toString();
        this.id = UUID.randomUUID().toString();
        this.pushed = false;
        this.name_files = new ArrayList<String>();
    }

    /**
     * Retuns the repository name where commit was made
     * @return the repository name
     */
    public String getRepositoryName(){
        return repositoryName;
    }

    /**
     * Returns the message written for the commit
     * @return the message written for the commit
     */
    public String getMessage(){
        return message;
    }

    /**
     * Returns the date when the commit was made
     * @return the date when the commit was made
     */
    public String getDate(){
        return date;
    }
    /**
     * Returns the id of the commit
     * @return id of the commit
     */
    public String getId(){
        return id;
    }

    /**
     * Add the name of the file in the commits, only for log purposes.
     * @param filename name of the file to be added in the commit
     */
    public void addFile(String filename){
        name_files.add(filename);
    }

    /**
     * Prints the whole list as a string
     * @return string that reppresent the list of files commited
     */
    public String getFiles(){
        String result = "";

        for(String name: name_files){
            result += name + " ";
        }

        return result;
    }

    /**
     * Update the status to prepare the commit to been pushed to the remote repository
     * @return true if is ready, false otherwise
     */
    public boolean updateStatus(){
        if(!pushed){
            pushed = true;
            return pushed;
        }
        else return false;
    }

    @Override
    public boolean equals(Object object){
        if(object instanceof Commit){
            Commit commit = (Commit) object;
            if(this.id.equals(commit.getId()) && this.date.equals(commit.getDate()))
                return true;
        }
        return false;
    }
}
