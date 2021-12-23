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
    
    public GitProtocolImpl(Storage storage) throws Exception{
        this.storage = storage; 
    }

    @Override
    public boolean createRepository(String _repo_name, File _directory) {
        return false;
    }

    @Override
    public boolean addFilesToRepository(String _repo_name, List<File> files) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean commit(String _repo_name, String _message) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String push(String _repo_name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String pull(String _repo_name) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
