package com.unisa.git;

import java.io.File;
import java.util.List;

public class GitProtocolImpl implements GitProtocol{

    @Override
    public boolean createRepository(String _repo_name, File _directory) {
        // TODO Auto-generated method stub
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
