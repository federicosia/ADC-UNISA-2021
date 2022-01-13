package com.unisa.git;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.unisa.git.storage.DHTStorage;

/**
 * This test class is made to test GitProtocolImpl with a network of 4 peers.
 */
public class GitProtocolImplUseCaseTest {
    private static GitProtocol peer1, peer2, peer3, peer4;

    public GitProtocolImplUseCaseTest() throws IOException{
        peer1 = new GitProtocolImpl(new DHTStorage(0, "127.0.0.1"));
        peer2 = new GitProtocolImpl(new DHTStorage(1, "127.0.0.1"));
        peer3 = new GitProtocolImpl(new DHTStorage(2, "127.0.0.1"));
        peer4 = new GitProtocolImpl(new DHTStorage(3, "127.0.0.1"));
    }

    @Test
    void testCaseGitProtocolImpl(){
        
    }
}
