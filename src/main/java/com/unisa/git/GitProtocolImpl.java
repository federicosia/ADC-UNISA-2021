package com.unisa.git;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GitProtocolImpl implements GitProtocol{
    private Storage storage;
    //TODO maybe is a list...
    private Repository localRepo;

    public GitProtocolImpl(Storage storage) throws Exception{
        this.storage = storage;
    }

    @Override
    public boolean createRepository(String _repo_name, File _directory) {
        try {
            if(storage.get(_repo_name) == null){
                localRepo = new Repository(_repo_name, _directory);
                return true;
            }
            else 
                return false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean addFilesToRepository(String _repo_name, List<File> files) {
        try {
            return localRepo.addFile(files);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }        
    }

    @Override
    public boolean commit(String _repo_name, String _message) {
        if(localRepo != null){
            localRepo.addCommit(new Commit(_repo_name, _message));
            return true;
        } 
        else return false;
    }

    @Override
    public String push(String _repo_name) {
        try {
            Repository remoteRepo = storage.get(_repo_name);
            //check if remote and local repos exists, then check if are different
            if((remoteRepo != null) && (localRepo != null)){
                //if not equals than there's something to push
                if(!remoteRepo.getId().equals(localRepo.getId())){
                    storage.put(localRepo.getName(), localRepo);
                    return "Pushed all files successfully!";
                } else 
                    return "Nothing to push...";
            }
            else 
                return "Repos missing...";
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
            if((remoteRepo != null) && (localRepo != null)){
                //if not equals than there's something to push
                if(!remoteRepo.getId().equals(localRepo.getId())){
                    localRepo = storage.get(_repo_name);
                    return "Pulled all files successfully!";
                } else 
                    return "Nothing to pull...";
            }
            else 
                return "Repos missing...";
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return "Something went wrong...";
        }
    }
    
}
