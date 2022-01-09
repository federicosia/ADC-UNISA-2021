package com.unisa.git.storage;

import java.io.IOException;
import java.net.InetAddress;

import com.unisa.git.repository.Repository;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

public class DHTStorage implements Storage{
    final private PeerDHT dht;
    final private int MASTER_PORT = 4000;

    public DHTStorage(int id, String master_peer) throws IOException{
        dht = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(id)).ports(MASTER_PORT + id).start()).start();     
        
        FutureBootstrap fb = dht.peer().bootstrap().inetAddress(InetAddress.getByName(master_peer)).ports(MASTER_PORT).start();
		fb.awaitUninterruptibly();
		if(fb.isSuccess()) {
			dht.peer().discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
        }
    }

    public boolean put(String key, Repository repository){
        try{
            dht.put(Number160.createHash(key)).data(new Data(repository)).start().awaitUninterruptibly();
            return true;
        } catch(IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Repository get(String key) {
        try{
            FutureGet futureGet = dht.get(Number160.createHash(key)).start();
            futureGet.awaitUninterruptibly();
            if(futureGet.isSuccess() && !futureGet.dataMap().values().isEmpty()){
                return (Repository) futureGet.dataMap().values().iterator().next().object();
            }
            else return null;
        } catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
