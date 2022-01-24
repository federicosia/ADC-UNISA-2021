package com.unisa.git.storage;

import com.unisa.git.repository.Repository;

/**
 * Storage is a abstract representation of a storage where all repositories are stored.
 * Repositories are stored in pairs of <key, value> where's key is an identifier of the repo's name
 * and value is the repo itself.
 */
public interface Storage {

    /**
     * Put a repository in the storage with a key
     * @param key key needed to locate the repository 
     * @param repository repository to store 
     * @return true if the repository is added, false otherwise.
     */
    public boolean put(String key, Repository repository);
    
    /**
     * Check if a repository with the same key is present in the storage.
     * @param key key needed to locate the repository
     * @return the repository with the same key, null otherwise
     */
    public Repository get(String key);
}
