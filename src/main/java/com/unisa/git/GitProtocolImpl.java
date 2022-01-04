package com.unisa.git;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.unisa.git.exceptions.RepositoryException;

public class GitProtocolImpl implements GitProtocol{
    private GitStorage storage;
    private Repository localRepo;

    public GitProtocolImpl(GitStorage storage, MessageListener listener) throws IOException{
        this.storage = storage;
        this.storage.objectReply(listener);
    }

    @Override
    public boolean createRepository(String _repo_name, File _directory) {
        try{
            if((localRepo == null) && _directory.isDirectory()){
                localRepo = new Repository(_repo_name, _directory);
                return true;
            }
            else 
                return false;
        } catch(IOException e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean addFilesToRepository(String _repo_name, List<File> files) {
        try {
            if(localRepo != null && localRepo.getName().equals(_repo_name))
                return localRepo.addFile(files);
            else 
                return false;
        } catch (IOException | RepositoryException e) {
            System.err.println(e);
            return false;
        }        
    }

    @Override
    public boolean commit(String _repo_name, String _message) {
        if(localRepo != null && localRepo.getName().equals(_repo_name))
            return localRepo.addCommit(new Commit(_repo_name, _message));
        else
            return false;
    }

    @Override
    public String push(String _repo_name) {
        try {
            Repository remoteRepo = storage.get(_repo_name);
            //check if remote and local repos exists, then check if are different
            if((remoteRepo != null) && (localRepo != null) && remoteRepo.getName().equals(localRepo.getName())){
                //if equals than we can check if there's something to push, otherwise we have to pull before push
                if(remoteRepo.equals(localRepo)){
                    //Check if the are commits to push, otherwise local and remote are equals
                    if(localRepo.checkBeforePush()){
                        if(storage.put(localRepo.getName(), localRepo))
                            return "Pushed all files successfully!";
                        else
                            return "Push to the remote repository failed...";
                    }
                    else return "Nothing to push...";
                }
                //Someone has pushed something, the repository must be updated with pull before pushing
                else return "The repository is out of date, do a pull before pushing...";
            }
            //Just push and check if there are commits to push
            else {
                if(localRepo.checkBeforePush() && storage.put(localRepo.getName(), localRepo))
                    return "Created new remote repository, pushed all files successfully!";
                else
                    return "Creation of new remote repository and push failed...";
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return "Something went wrong...";
        }
    }

    @Override
    public String pull(String _repo_name) {
        try {
            Repository remoteRepo = storage.get(_repo_name);
            //check if remote and local repos exists, then check if are different
            if(remoteRepo != null){ 
                if((localRepo != null) && remoteRepo.getName().equals(localRepo.getName())){
                    //if the local repose doesn't have the last commit of the remote repo
                    //than a pull is needed, otherwise it's up to date
                    if(!localRepo.checkLastCommit(remoteRepo)){
                        if(localRepo.findConflicts(storage.get(_repo_name))){
                            localRepo.materialize();
                            return "Pulled, but some files are duplicated because some conflicts are present.";
                        }
                        else{
                            localRepo.materialize();
                            return "Pulled, no conflicts are present!";
                        }
                    }
                    else return "All up to date!";
                } 
                else return "You are working on a local repository completely different...";
            } 
            else return "Repository missing...";
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return "Something went wrong...";
        }
    }
    
}
