package com.unisa.git;

import java.time.LocalDate;
import java.util.UUID;

public class Commit {
    private String repositoryName;
    private String message;
    private String date;
    private String id;

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
    public String getLocalDate(){
        return date;
    }
    /**
     * Returns the id of the commit
     * @return id of the commit
     */
    public String getId(){
        return id;
    }
}
