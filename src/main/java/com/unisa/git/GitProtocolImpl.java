package com.unisa.git;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.Number160;

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
