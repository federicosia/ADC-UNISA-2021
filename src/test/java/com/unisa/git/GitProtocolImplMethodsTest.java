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
import java.util.Arrays;
import java.util.List;

import com.unisa.git.storage.DHTStorage;

@TestInstance(Lifecycle.PER_CLASS)
public class GitProtocolImplMethodsTest {
    //Peers
    private static GitProtocolImpl peer1, peer2;

    //repo's names
    private static final String repo_name1 = "test1";
    private static final String repo_name2 = "test2";
    private static final String repo_name3 = "test3";
    private static final String repo_name4 = "test4";
    private static final String repo_name5 = "test5";
    private static final String repo_name6 = "test6";
    private static final String repo_name7 = "test7";

    //repo's paths
    private static final String path_to_repo1 = Paths.get(System.getProperty("user.dir"), repo_name1).toString();
    private static final String path_to_repo2 = Paths.get(System.getProperty("user.dir"), repo_name2).toString();
    private static final String path_to_repo3 = Paths.get(System.getProperty("user.dir"), repo_name3).toString();
    private static final String path_to_repo4 = Paths.get(System.getProperty("user.dir"), repo_name4).toString();
    private static final String path_to_repo5 = Paths.get(System.getProperty("user.dir"), repo_name5).toString();
    private static final String path_to_repo61 = Paths.get(System.getProperty("user.dir"), repo_name6).toString();
    private static final String path_to_repo62 = Paths.get(System.getProperty("user.dir"), "supp", repo_name6).toString();
    private static final String path_to_repo7 = Paths.get(System.getProperty("user.dir"), repo_name7).toString();

    //files
    private static final String esp1 = "prova1.txt";
    private static final String esp2 = "prova2.txt";
    private static final String esp3 = "prova3.txt";
    private static final String esp4 = "prova4.txt";

    public GitProtocolImplMethodsTest() throws IOException{
        peer1 = new GitProtocolImpl(new DHTStorage(0, "127.0.0.1"));
        peer2 = new GitProtocolImpl(new DHTStorage(1, "127.0.0.1"));
    }

    @Test
    void testCaseCreateRepository(){
        assertTrue(peer1.createRepository(repo_name1, new File(System.getProperty("user.dir"))));
        this.deleteFiles(new File(path_to_repo1));
    }

    @Test
    void testCaseAddFilesToRepository() throws IOException, InterruptedException{
        peer1.createRepository(repo_name2, new File(System.getProperty("user.dir")));
        Path path1 = Paths.get(path_to_repo2, esp1);
        Path path2 = Paths.get(path_to_repo2, esp2);
        Path path3 = Paths.get(path_to_repo2, esp3);
        Path path4 = Paths.get(path_to_repo2, esp4);
        
        Files.write(path1, "ciao1".getBytes(StandardCharsets.UTF_8));
        Files.write(path2, "ciao2".getBytes(StandardCharsets.UTF_8));
        Files.write(path3, "ciao3".getBytes(StandardCharsets.UTF_8));
        Files.write(path4, "ciao4".getBytes(StandardCharsets.UTF_8));

        List<File> files = new ArrayList<>();
        files.add(new File(path1.toString()));
        files.add(new File(path2.toString()));
        files.add(new File(path3.toString()));
        files.add(new File(path4.toString()));

        assertTrue(peer1.addFilesToRepository(repo_name2, files));
        this.deleteFiles(new File(path_to_repo2));
    }

    @Test
    void testCaseRemoveFilesFromRepository() throws IOException{
        peer1.createRepository(repo_name3, new File(System.getProperty("user.dir")));
        Path path1 = Paths.get(path_to_repo3, esp1);
        Path path2 = Paths.get(path_to_repo3, esp2);
        Path path3 = Paths.get(path_to_repo3, esp3);
        Path path4 = Paths.get(path_to_repo3, esp4);
        
        Files.write(path1, "ciao1".getBytes(StandardCharsets.UTF_8));
        Files.write(path2, "ciao2".getBytes(StandardCharsets.UTF_8));
        Files.write(path3, "ciao3".getBytes(StandardCharsets.UTF_8));
        Files.write(path4, "ciao4".getBytes(StandardCharsets.UTF_8));

        List<File> files = new ArrayList<>();
        files.add(new File(path1.toString()));
        files.add(new File(path2.toString()));
        files.add(new File(path3.toString()));
        files.add(new File(path4.toString()));

        assertTrue(peer1.addFilesToRepository(repo_name3, files));
        assertTrue(peer1.commit(repo_name3, "commit"));
        assertTrue(peer1.removeFilesFromRepository(repo_name3, files));
        this.deleteFiles(new File(path_to_repo3));
    }

    @Test
    void testCaseCommit() throws IOException{
        peer1.createRepository(repo_name4, new File(System.getProperty("user.dir")));
        Path path1 = Paths.get(path_to_repo4, esp1);
        Path path2 = Paths.get(path_to_repo4, esp2);
        Path path3 = Paths.get(path_to_repo4, esp3);
        Path path4 = Paths.get(path_to_repo4, esp4);
        
        Files.write(path1, "ciao1".getBytes(StandardCharsets.UTF_8));
        Files.write(path2, "ciao2".getBytes(StandardCharsets.UTF_8));
        Files.write(path3, "ciao3".getBytes(StandardCharsets.UTF_8));
        Files.write(path4, "ciao4".getBytes(StandardCharsets.UTF_8));

        List<File> files = new ArrayList<>();
        files.add(new File(path1.toString()));
        files.add(new File(path2.toString()));
        files.add(new File(path3.toString()));
        files.add(new File(path4.toString()));

        assertTrue(peer1.addFilesToRepository(repo_name4, files));
        assertTrue(peer1.commit(repo_name4, "This is a commit"));
        this.deleteFiles(new File(path_to_repo4));
    }

    @Test
    void testCasePush() throws IOException{
        String resultPush = "Created new remote repository, pushed all files successfully!\n";
        peer1.createRepository(repo_name5, new File(System.getProperty("user.dir")));
        Path path1 = Paths.get(path_to_repo5, esp1);
        Path path2 = Paths.get(path_to_repo5, esp2);
        Path path3 = Paths.get(path_to_repo5, esp3);
        Path path4 = Paths.get(path_to_repo5, esp4);
        
        Files.write(path1, "ciao1".getBytes(StandardCharsets.UTF_8));
        Files.write(path2, "ciao2".getBytes(StandardCharsets.UTF_8));
        Files.write(path3, "ciao3".getBytes(StandardCharsets.UTF_8));
        Files.write(path4, "ciao4".getBytes(StandardCharsets.UTF_8));

        List<File> files = new ArrayList<>();
        files.add(new File(path1.toString()));
        files.add(new File(path2.toString()));
        files.add(new File(path3.toString()));
        files.add(new File(path4.toString()));

        assertTrue(peer1.addFilesToRepository(repo_name5, files));
        assertTrue(peer1.commit(repo_name5, "This is a commit"));
        assertEquals(peer1.push(repo_name5), resultPush);
        this.deleteFiles(new File(path_to_repo5));
    }

    @Test
    void testCasePullWithConflicts() throws IOException, InterruptedException{        
        String resultPush1 = "Created new remote repository, pushed all files successfully!\n";
        String resultPush2 = "Pushed all files successfully!\n";
        String resultPull = "Pulled, but one or more conflicts are present.\n";
        peer1.createRepository(repo_name6, new File(System.getProperty("user.dir")));
        Path path11 = Paths.get(path_to_repo61, esp1);
        Path path12 = Paths.get(path_to_repo61, esp2);
        Path path13 = Paths.get(path_to_repo61, esp3);
        Path path14 = Paths.get(path_to_repo61, esp4);
        
        Files.createDirectories(Paths.get(System.getProperty("user.dir"), "supp"));
        assertTrue(peer2.createRepository(repo_name6, Paths.get(System.getProperty("user.dir"), "supp").toFile()));
        Path path21 = Paths.get(path_to_repo62, esp1);
        Path path22 = Paths.get(path_to_repo62, esp2);
        Path path23 = Paths.get(path_to_repo62, esp3);
        Path path24 = Paths.get(path_to_repo62, esp4);

        Files.write(path11, "ciao11".getBytes(StandardCharsets.UTF_8));
        Files.write(path12, "ciao12".getBytes(StandardCharsets.UTF_8));
        Files.write(path13, "ciao13".getBytes(StandardCharsets.UTF_8));
        Files.write(path14, "ciao14".getBytes(StandardCharsets.UTF_8));

        Files.write(path21, "ciao21".getBytes(StandardCharsets.UTF_8));
        Files.write(path22, "ciao22".getBytes(StandardCharsets.UTF_8));
        Files.write(path23, "ciao23".getBytes(StandardCharsets.UTF_8));
        Files.write(path24, "ciao24".getBytes(StandardCharsets.UTF_8));

        List<File> files1 = new ArrayList<>();
        files1.add(new File(path11.toString()));
        files1.add(new File(path12.toString()));
        files1.add(new File(path13.toString()));
        files1.add(new File(path14.toString()));

        List<File> files2 = new ArrayList<>();
        files2.add(new File(path21.toString()));
        files2.add(new File(path22.toString()));
        files2.add(new File(path23.toString()));
        files2.add(new File(path24.toString()));

        assertTrue(peer1.addFilesToRepository(repo_name6, files1));
        assertTrue(peer1.commit(repo_name6, "This is a commit"));
        assertEquals(peer1.push(repo_name6), resultPush1);

        assertTrue(peer2.addFilesToRepository(repo_name6, files2));
        assertTrue(peer2.commit(repo_name6, "This is a commit"));
        assertEquals(peer2.pull(repo_name6), resultPull);
        assertEquals(peer2.push(repo_name6), resultPush2);

        this.deleteFiles(new File(path_to_repo61));
        this.deleteFiles(new File(path_to_repo62));
        Files.delete(Paths.get(System.getProperty("user.dir"), "supp"));
    }

    @Test
    void testCaseStatus() throws IOException{
        String resultPush = "Created new remote repository, pushed all files successfully!\n";
        peer1.createRepository(repo_name7, new File(System.getProperty("user.dir")));
        Path path1 = Paths.get(path_to_repo7, esp1);
        Path path2 = Paths.get(path_to_repo7, esp2);
        Path path3 = Paths.get(path_to_repo7, esp3);
        Path path4 = Paths.get(path_to_repo7, esp4);
        
        Files.write(path1, "ciao1".getBytes(StandardCharsets.UTF_8));
        Files.write(path2, "ciao2".getBytes(StandardCharsets.UTF_8));
        Files.write(path3, "ciao3".getBytes(StandardCharsets.UTF_8));
        Files.write(path4, "ciao4".getBytes(StandardCharsets.UTF_8));

        List<File> files = new ArrayList<>();
        files.add(new File(path1.toString()));
        files.add(new File(path2.toString()));
        files.add(new File(path3.toString()));
        files.add(new File(path4.toString()));

        assertTrue(peer1.addFilesToRepository(repo_name7, files));
        assertTrue(peer1.commit(repo_name7, "This is a commit"));
        
        //add
        files.clear();
        Files.write(path1, "ao1".getBytes(StandardCharsets.UTF_8));
        Files.write(path2, "ao2".getBytes(StandardCharsets.UTF_8));
        Files.write(path3, "ao3".getBytes(StandardCharsets.UTF_8));
        Files.write(path4, "ao4".getBytes(StandardCharsets.UTF_8));

        files.add(new File(path1.toString()));
        files.add(new File(path2.toString()));
        files.add(new File(path3.toString()));
        files.add(new File(path4.toString()));
        assertTrue(peer1.addFilesToRepository(repo_name7, files));

        //modify path1 without add
        Files.write(path1, "o1".getBytes(StandardCharsets.UTF_8));
        
        //staged
        List<String> testStaged = Arrays.asList(esp1, esp2, esp3, esp4);
        List<String> staged = peer1.statusGetStagedFiles(repo_name7);
        //unstaged
        List<String> testUnstaged = Arrays.asList(esp1);
        List<String> unstaged = peer1.statusGetUnstagedFiles(repo_name7);
        //tracked
        List<String> testTracked = Arrays.asList(esp1, esp2, esp3, esp4);
        List<String> tracked = peer1.statusGetTrackedFiles(repo_name7);
        //untracked
        List<String> testUntracked = Arrays.asList();
        List<String> untracked = peer1.statusGetUntrackedFiles(repo_name7);

        //assert status
        assertTrue(testStaged.containsAll(staged) && staged.containsAll(testStaged));
        assertTrue(testStaged.containsAll(unstaged) && staged.containsAll(testUnstaged));
        assertTrue(testStaged.containsAll(tracked) && staged.containsAll(testTracked));
        assertTrue(testStaged.containsAll(untracked) && staged.containsAll(testUntracked));

        assertEquals(peer1.push(repo_name7), resultPush);
        this.deleteFiles(new File(path_to_repo7));
    }

    private void deleteFiles(File file){
        File[] files = file.listFiles();
        if(files != null){
            for(File f: files){
                deleteFiles(f);
            }
        }
        file.delete();
    }
}
