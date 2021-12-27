package com.unisa.git;

import java.io.IOException;
import java.net.InetAddress;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

/**
 * Storage is a abstract rappresentation of a storage where all repo's of an user are stored.
 * It's rappresented by a DHT where are stored pair of <key, value> where key is an hash of a value
 * and value is a repo
 */
public class Storage {
    final private PeerDHT dht;
    final private int MASTER_PORT = 4000;

    public Storage(int id, String master_peer) throws IOException{
        dht = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(id)).ports(MASTER_PORT + id).start()).start();     
        
        FutureBootstrap fb = dht.peer().bootstrap().inetAddress(InetAddress.getByName(master_peer)).ports(MASTER_PORT).start();
		fb.awaitUninterruptibly();
		if(fb.isSuccess()) {
			dht.peer().discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
		}
    }

    /**
     * Put a repository in the DHT with a key
     * @param key key needed to locate the repository in the peer's DHT
     * @param repository repository to store in the DHT
     * @return true if the repository is added, false otherwise.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public boolean put(String key, Repository repository) throws IOException{
        dht.put(Number160.createHash(key)).data(new Data(repository)).start().awaitUninterruptibly();
        return true;
    }

    /**
     * Check if a repository with the same key is present in the peer's DHT
     * @param key key needed to locate the repository in the peer's DHT
     * @return the repository with the name equals to key, null otherwise
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Repository get(String key) throws ClassNotFoundException, IOException{
        FutureGet futureGet = dht.get(Number160.createHash(key)).start();
        futureGet.awaitUninterruptibly();
        if(futureGet.isSuccess() && (futureGet.dataMap().values() != null)){
            return (Repository) futureGet.dataMap().values().iterator().next().object();
        }
        else return null;
    }
}
