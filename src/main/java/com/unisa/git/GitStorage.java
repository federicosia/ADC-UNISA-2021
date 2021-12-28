package com.unisa.git;

import java.io.IOException;
import java.net.InetAddress;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;

public class GitStorage implements Storage{
    final private PeerDHT dht;
    final private int MASTER_PORT = 4000;

    public GitStorage(int id, String master_peer) throws IOException{
        dht = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(id)).ports(MASTER_PORT + id).start()).start();     
        
        FutureBootstrap fb = dht.peer().bootstrap().inetAddress(InetAddress.getByName(master_peer)).ports(MASTER_PORT).start();
		fb.awaitUninterruptibly();
		if(fb.isSuccess()) {
			dht.peer().discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
        }
    }

    public boolean put(String key, Repository repository) throws IOException{
        dht.put(Number160.createHash(key)).data(new Data(repository)).start().awaitUninterruptibly();
        return true;
    }

    public Repository get(String key) throws ClassNotFoundException, IOException{
        FutureGet futureGet = dht.get(Number160.createHash(key)).start();
        futureGet.awaitUninterruptibly();
        if(futureGet.isSuccess() && (futureGet.dataMap().values() != null)){
            return (Repository) futureGet.dataMap().values().iterator().next().object();
        }
        else return null;
    }

    public void objectReply(final MessageListener listener){
        dht.peer().objectDataReply(new ObjectDataReply() {
            @Override
            public Object reply(PeerAddress sender, Object request) throws Exception {
                return listener.parseMessage(request);
            }
        });
    }
}
