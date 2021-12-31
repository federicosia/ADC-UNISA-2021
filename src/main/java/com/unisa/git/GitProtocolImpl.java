package com.unisa.git;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.unisa.git.exceptions.RepositoryException;

public class GitProtocolImpl implements GitProtocol{
    private GitStorage storage;
    //TODO maybe it's a list...
    private Repository localRepo;

    public GitProtocolImpl(GitStorage storage, MessageListener listener) throws IOException{
        this.storage = storage;
        this.storage.objectReply(listener);
    }

    @Override
    public boolean createRepository(String _repo_name, File _directory) {
        try {
            if((storage.get(_repo_name) == null) && _directory.isDirectory()){
                localRepo = new Repository(_repo_name, _directory);
                return true;
            }
            else 
                return false;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println(e);
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
        try {
            if(localRepo != null && localRepo.getName().equals(_repo_name))
                return localRepo.addCommit(new Commit(_repo_name, _message));
            else
                return false;
        }
        catch (RepositoryException e) {
            System.err.println(e);
            return false;
        }
    }

    @Override
    public String push(String _repo_name) {
        try {
            Repository remoteRepo = storage.get(_repo_name);
            //check if remote and local repos exists, then check if are different
            if((remoteRepo != null) && (localRepo != null) && remoteRepo.getName().equals(localRepo.getName())){
                //if not equals than there's something to push
                if(!remoteRepo.getId().equals(localRepo.getId())){
                    if(storage.put(localRepo.getName(), localRepo))
                        return "Pushed all files successfully!";
                    else
                        return "Push to the remote repository failed...";
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
            if((remoteRepo != null) && (localRepo != null) && (remoteRepo.getName().equals(localRepo.getName()))){
                //if not equals than there's something to push
                if(!remoteRepo.getId().equals(localRepo.getId())){
                    localRepo = storage.get(_repo_name);
                    if(localRepo != null)
                        return "Pulled all files successfully!";
                    else
                        return "Pull remote repository failed...";
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
