package com.unisa.git.storage;

import java.util.HashMap;

import com.unisa.git.repository.Repository;

public class GitStorage implements Storage{
    HashMap<String, Repository> localStorage;

    public GitStorage(){
        localStorage = new HashMap<>();
    }

    @Override
    public boolean put(String key, Repository repository) {
        if(localStorage.containsKey(key)){
            Repository oldRepo = localStorage.get(key);
            if(!repository.equals(oldRepo)){
                //replace
                localStorage.put(key, repository);
                return true;
            }
            //repos are equal
            else return false;
        }
        //not present
        else {
            localStorage.put(key, repository);
            return true;
        }
    }

    @Override
    public Repository get(String key) {
        return localStorage.get(key);
    }   
}