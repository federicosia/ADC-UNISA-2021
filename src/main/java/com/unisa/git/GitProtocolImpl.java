package com.unisa.git;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.unisa.git.exceptions.RepositoryException;
import com.unisa.git.repository.Repository;
import com.unisa.git.storage.DHTStorage;
import com.unisa.git.storage.GitStorage;

public class GitProtocolImpl implements GitProtocol{
    private DHTStorage remoteStorage;
    private GitStorage localStorage;

    public GitProtocolImpl(DHTStorage storage) throws IOException{
        this.remoteStorage = storage;
        this.localStorage = new GitStorage();
    }

    @Override
    public boolean createRepository(String _repo_name, File _directory) {
        try{
            if(_directory.isDirectory()){
                Repository result = localStorage.get(_repo_name);
                if(result == null){
                    //init new repository
                    Repository repo = new Repository(_repo_name, _directory);
                    localStorage.put(_repo_name, repo);
                    return true;
                }
            }
            return false;
        } catch(IOException e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean addFilesToRepository(String _repo_name, List<File> files) {
        try {
            Repository localRepo = localStorage.get(_repo_name);
            if(localRepo != null)
                return localRepo.addFile(files);
            else 
                return false;
        } catch (IOException | RepositoryException e) {
            System.err.println(e.getMessage());
            return false;
        }        
    }

    public boolean removeFilesToRepository(String _repo_name, List<File> files){
        try {
            Repository localRepo = localStorage.get(_repo_name);
            if(localRepo != null)
                return localRepo.removeFile(files);
            else 
                return false;
        } catch (RepositoryException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean commit(String _repo_name, String _message) {
        try{
            Repository localRepo = localStorage.get(_repo_name);
            if(localRepo != null)
                return localRepo.addCommit(_repo_name, _message);
            else
                return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String push(String _repo_name) {
        try {
            Repository remoteRepo = remoteStorage.get(_repo_name);
            Repository localRepo = localStorage.get(_repo_name);
            //Can't push without a local repository...
            if(localRepo != null){
                //check if remote repository exists
                if((remoteRepo != null)){
                    //if true then we can push to remote
                    if(localRepo.checkLastCommit(remoteRepo)){
                        //Check if the are commits to push, otherwise local and remote are equals
                        if(localRepo.checkBeforePush()){
                            if(remoteStorage.put(localRepo.getName(), localRepo))
                                return "Pushed all files successfully!\n";
                            else
                                return "Push to the remote repository failed...\n";
                        }
                        else return "Nothing to push...\n";
                    }
                    //Someone has pushed something, the repository must be updated with pull before pushing
                    else return "The repository is out of date, do a pull before pushing...\n";
                }
                //Just push and check if there are commits to push
                else {
                    if(localRepo.checkBeforePush() && remoteStorage.put(localRepo.getName(), localRepo))
                        return "Created new remote repository, pushed all files successfully!\n";
                    else
                        return "Creation of new remote repository and push failed...\n";
                }
            }
            else return "You should create a local repository first...\n";
        } catch (IOException e) {
            e.printStackTrace();
            return "Something went wrong...\n";
        }
    }

    @Override
    public String pull(String _repo_name) {
        try {
            Repository remoteRepo = remoteStorage.get(_repo_name);
            Repository localRepo = localStorage.get(_repo_name);
            //check if remote and local repos exists, then check if are different
            if(remoteRepo != null){ 
                if((localRepo != null)){
                    if(!localRepo.checkLastCommit(remoteRepo)){
                        switch(localRepo.update(remoteRepo)){
                            case 0:
                                return "All up to date!\n";
                            case 1:
                                return "Pulled, no conflicts are present!\n";
                            case 2:
                                return "Pulled, but one or more conflicts are present.\n";
                            default:
                                return "Shouldn't happen...\n";
                        }
                    }
                    else return "All up to date!\n";
                }
                else return "Create a local repository first...\n";
            } 
            else return "Remote repository missing...\n";
        } catch (IOException e) {
            e.printStackTrace();
            return "Something went wrong...\n";
        }
    }
    
    /**
     * Returns the status of the local repository, the status shows the filename of files
     * that are staged, unstaged and untracked.
     * @param _repo_name name of the local repository
     * @return a String object containing informations
     */
    public String status(String _repo_name){
        try{
            Repository localRepo = localStorage.get(_repo_name);
            if(localRepo != null){
                List<String> stagedFilenames = localRepo.getStagedFiles();
                List<String> unstagedFilenames = localRepo.getUnstagedFiles();
                List<String> untrackedFilenames = localRepo.getUntrackedFiles();
                String result = "\n\nStatus of the local repository " + localRepo.getName() + ":\n";
                result += "Changes to be committed:\n\n" + String.join(" ", stagedFilenames) + "\n\n";
                result += "Changes not staged for commit:\n\n" + String.join(" ", unstagedFilenames) + "\n\n";
                result += "Untracked files:\n\n" + String.join(" ", untrackedFilenames) + "\n";

                return result;
            }
            else
                return "Local repository missing...";
        } catch(IOException e) {
            e.printStackTrace();
            return "Something went wrong...";
        }
    }
}
