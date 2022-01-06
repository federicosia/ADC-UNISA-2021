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
            System.err.println(e.getMessage());
            return false;
        }        
    }

    @Override
    public boolean commit(String _repo_name, String _message) {
        try{
            if(localRepo != null && localRepo.getName().equals(_repo_name))
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
            Repository remoteRepo = storage.get(_repo_name);
            //Can't push without a local repository...
            if(localRepo != null){
                //check if remote repository exists, then check if are different
                if((remoteRepo != null) && remoteRepo.getName().equals(localRepo.getName())){
                    //if true then we can push to remote
                    if(localRepo.checkLastCommit(remoteRepo)){
                        //Check if the are commits to push, otherwise local and remote are equals
                        if(localRepo.checkBeforePush()){
                            if(storage.put(localRepo.getName(), localRepo))
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
                    if(localRepo.checkBeforePush() && storage.put(localRepo.getName(), localRepo))
                        return "Created new remote repository, pushed all files successfully!\n";
                    else
                        return "Creation of new remote repository and push failed...\n";
                }
            }
            else return "You should create a local repository first...\n";
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return "Something went wrong...\n";
        }
    }

    @Override
    public String pull(String _repo_name) {
        try {
            Repository remoteRepo = storage.get(_repo_name);
            //check if remote and local repos exists, then check if are different
            if(remoteRepo != null){ 
                if((localRepo != null) && localRepo.getName().equals(remoteRepo.getName())){
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
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return "Something went wrong...\n";
        }
    }
    
}
