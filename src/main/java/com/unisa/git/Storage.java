package com.unisa.git;

import java.io.IOException;

/**
 * Storage is a abstract representation of a storage where all repo's of an user are stored.
 * It's rappresented by a DHT where are stored pair of <key, value> where key is an hash of the repo's name
 * and value is the repo
 */
public interface Storage {

    /**
     * Put a repository in the DHT with a key
     * @param key key needed to locate the repository in the peer's DHT
     * @param repository repository to store in the DHT
     * @return true if the repository is added, false otherwise.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public boolean put(String key, Repository repository) throws IOException;
    
    /**
     * Check if a repository with the same key is present in the peer's DHT
     * @param key key needed to locate the repository in the peer's DHT
     * @return the repository with the name equals to key, null otherwise
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Repository get(String key) throws ClassNotFoundException, IOException;
    
    /**
     * Add listener that allows to react on received message
     * @param listener object that implements the interface MessageListener
     */
    public void objectReply(final MessageListener listener);
}
