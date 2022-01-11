package com.unisa.git;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

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

@TestMethodOrder(OrderAnnotation.class)
public class GitProtocolImplTest {
    private GitProtocol peer1, peer2, peer3, peer4;
    
    public GitProtocolImplTest() throws IOException{
        peer1 = new GitProtocolImpl(new DHTStorage(0, "127.0.0.1"));
        peer2 = new GitProtocolImpl(new DHTStorage(1, "127.0.0.1"));
        peer3 = new GitProtocolImpl(new DHTStorage(2, "127.0.0.1"));
        peer4 = new GitProtocolImpl(new DHTStorage(3, "127.0.0.1"));
    }

    @Test
    @Order(1)
    void testCaseCreateRepository(TestInfo testInfo){
        assertTrue(peer1.createRepository("ciao", new File(System.getProperty("user.dir"))));
        assertFalse(peer1.createRepository("ciao", new File(System.getProperty("user.dir"))));
    }

    @Test
    @Order(2)
    void testCaseAddFilesToRepository(TestInfo testInfo) throws IOException{
        String path_to_repo = Paths.get(System.getProperty("user.dir"), "ciao").toString();

        Path path1 = Paths.get(path_to_repo, "prova1.txt");
        Path path2 = Paths.get(path_to_repo, "prova2.txt");
        Path path3 = Paths.get(path_to_repo, "prova3.txt");
        Path path4 = Paths.get(path_to_repo, "prova4.txt");
        
        Files.write(path1, "ciao1".getBytes(StandardCharsets.UTF_8));
        Files.write(path2, "ciao2".getBytes(StandardCharsets.UTF_8));
        Files.write(path3, "ciao3".getBytes(StandardCharsets.UTF_8));
        Files.write(path4, "ciao4".getBytes(StandardCharsets.UTF_8));

        List<File> files = new ArrayList<>();
        files.add(new File(path1.toString()));
        files.add(new File(path2.toString()));
        files.add(new File(path3.toString()));
        files.add(new File(path4.toString()));

        assertFalse(peer1.addFilesToRepository("ciao", files));
    }

    @Test
    @Order(3)
    void testCaseRemoveFilesToRepository(TestInfo testInfo) throws IOException{
        String path_to_repo = Paths.get(System.getProperty("user.dir"), "ciao").toString();

        Path path1 = Paths.get(path_to_repo, "prova1.txt");
        Path path2 = Paths.get(path_to_repo, "prova2.txt");
        Path path3 = Paths.get(path_to_repo, "prova3.txt");
        Path path4 = Paths.get(path_to_repo, "prova4.txt");
        
        Files.write(path1, "ciao1".getBytes(StandardCharsets.UTF_8));
        Files.write(path2, "ciao2".getBytes(StandardCharsets.UTF_8));
        Files.write(path3, "ciao3".getBytes(StandardCharsets.UTF_8));
        Files.write(path4, "ciao4".getBytes(StandardCharsets.UTF_8));

        List<File> files = new ArrayList<>();
        files.add(new File(path1.toString()));
        files.add(new File(path2.toString()));
        files.add(new File(path3.toString()));
        files.add(new File(path4.toString()));

        assertTrue(peer1.removeFilesToRepository("ciao", files));
        assertFalse(peer1.removeFilesToRepository("ciao", files));
        System.out.println(peer1.status("ciao"));
    }
}
