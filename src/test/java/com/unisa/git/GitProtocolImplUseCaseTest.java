package com.unisa.git;

import org.junit.jupiter.api.*;

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

/**
 * This test class is made to test GitProtocolImpl with a network of 4 peers.
 */
public class GitProtocolImplUseCaseTest {
    private static GitProtocolImpl peer1, peer2, peer3, peer4;

    //Push messages
    private static final String pushOk = "Pushed all files successfully!\n";
    private static final String pushOutOfDate = "The repository is out of date, do a pull before pushing...\n";
    private static final String pushCreateRepo = "Created new remote repository, pushed all files successfully!\n";

    //Pull messages
    private static final String pullOk = "All up to date!\n";
    private static final String pullNoConflicts = "Pulled, no conflicts are present!\n";
    private static final String pullYesConflicts = "Pulled, but one or more conflicts are present.\n";

    //Repo names
    private static final String repo1 = "repo1";
    private static final String repo2 = "repo2";
    private static final String repo3 = "repo3";
    private static final String repo4 = "repo4";

    //Local storage path string for each peer
    private static final String pathRepo1 = Paths.get(System.getProperty("user.dir"), "peer1").toString();
    private static final String pathRepo2 = Paths.get(System.getProperty("user.dir"), "peer2").toString();
    private static final String pathRepo3 = Paths.get(System.getProperty("user.dir"), "peer3").toString();
    private static final String pathRepo4 = Paths.get(System.getProperty("user.dir"), "peer4").toString();

    public GitProtocolImplUseCaseTest() throws IOException{
        peer1 = new GitProtocolImpl(new DHTStorage(0, "127.0.0.1"));
        peer2 = new GitProtocolImpl(new DHTStorage(1, "127.0.0.1"));
        peer3 = new GitProtocolImpl(new DHTStorage(2, "127.0.0.1"));
        peer4 = new GitProtocolImpl(new DHTStorage(3, "127.0.0.1"));
    
        //cleanse
        deleteFiles(new File(pathRepo1));
        deleteFiles(new File(pathRepo2));
        deleteFiles(new File(pathRepo3));
        deleteFiles(new File(pathRepo4));

        //Create local storage for each peer
        Files.createDirectory(Paths.get(System.getProperty("user.dir"), "peer1"));
        Files.createDirectory(Paths.get(System.getProperty("user.dir"), "peer2"));
        Files.createDirectory(Paths.get(System.getProperty("user.dir"), "peer3"));
        Files.createDirectory(Paths.get(System.getProperty("user.dir"), "peer4"));
    }

    @Test
    void testCaseGitProtocolImpl() throws IOException{
        assertTrue(peer1.createRepository(repo1, new File(pathRepo1)));
        assertTrue(peer2.createRepository(repo2, new File(pathRepo2)));
        assertTrue(peer3.createRepository(repo3, new File(pathRepo3)));
        assertTrue(peer4.createRepository(repo4, new File(pathRepo4)));

        //create files for the repo2
        Path path1 = Paths.get(pathRepo1, repo1, "esp1");
        Path path2 = Paths.get(pathRepo1, repo1, "esp2");
        Path path3 = Paths.get(pathRepo1, repo1, "esp3");
        Files.write(path1, "ciao1".getBytes(StandardCharsets.UTF_8));
        Files.write(path2, "ciao2".getBytes(StandardCharsets.UTF_8));
        Files.write(path3, "ciao3".getBytes(StandardCharsets.UTF_8));
        List<File> filesRepo1 = new ArrayList<File>();
        filesRepo1.add(path1.toFile());
        filesRepo1.add(path2.toFile());
        filesRepo1.add(path3.toFile());

        //create files for the repo2
        Path path4 = Paths.get(pathRepo2, repo2, "esp4");
        Path path5 = Paths.get(pathRepo2, repo2, "esp5");
        Files.write(path4, "ciao4".getBytes(StandardCharsets.UTF_8));
        Files.write(path5, "ciao5".getBytes(StandardCharsets.UTF_8));
        List<File> filesRepo2 = new ArrayList<File>();
        filesRepo2.add(path4.toFile());
        filesRepo2.add(path5.toFile());

        //create files for the repo3
        Path path6 = Paths.get(pathRepo3, repo3, "esp6");
        Path path7 = Paths.get(pathRepo3, repo3, "esp7");
        Files.write(path6, "ciao6".getBytes(StandardCharsets.UTF_8));
        Files.write(path7, "ciao7".getBytes(StandardCharsets.UTF_8));
        List<File> filesRepo3 = new ArrayList<File>();
        filesRepo3.add(path6.toFile());
        filesRepo3.add(path7.toFile());

        //create files for the repo4
        Path path8 = Paths.get(pathRepo4, repo4, "esp8");
        Path path9 = Paths.get(pathRepo4, repo4, "esp9");
        Files.write(path8, "ciao8".getBytes(StandardCharsets.UTF_8));
        Files.write(path9, "ciao9".getBytes(StandardCharsets.UTF_8));
        List<File> filesRepo4 = new ArrayList<File>();
        filesRepo4.add(path8.toFile());
        filesRepo4.add(path9.toFile());

        //add files
        assertTrue(peer1.addFilesToRepository(repo1, filesRepo1));
        assertTrue(peer2.addFilesToRepository(repo2, filesRepo2));
        assertTrue(peer3.addFilesToRepository(repo3, filesRepo3));
        assertTrue(peer4.addFilesToRepository(repo4, filesRepo4));
    
        List<String> testStaged = Arrays.asList("esp1", "esp2", "esp3");
        List<String> staged = peer1.statusGetStagedFiles(repo1);

        //check status peer1
        assertTrue(testStaged.containsAll(staged) && staged.containsAll(testStaged));
        assertArrayEquals(new String[] {}, peer1.statusGetUnstagedFiles(repo1).toArray());
        assertArrayEquals(new String[] {}, peer1.statusGetTrackedFiles(repo1).toArray());
        assertArrayEquals(new String[] {}, peer1.statusGetUntrackedFiles(repo1).toArray());


        //create another local repo for peer2, 3 and 4 (peer1 has already repo1)
        assertTrue(peer2.createRepository(repo1, new File(pathRepo2)));
        assertTrue(peer3.createRepository(repo1, new File(pathRepo3)));
        assertTrue(peer4.createRepository(repo1, new File(pathRepo4)));

        //commit and push repo1 from peer1
        assertTrue(peer1.commit(repo1, "ciao"));
        assertEquals(pushCreateRepo, peer1.push(repo1)); 

        //add from peer2 on repo1
        Path path1a = Paths.get(pathRepo2, repo1, "esp1");
        Path path2a = Paths.get(pathRepo2, repo1, "esp2");
        Files.write(path1a, "ciao ciao".getBytes(StandardCharsets.UTF_8));
        Files.write(path2a, "ciao123".getBytes(StandardCharsets.UTF_8));
        filesRepo2.clear();
        filesRepo2.add(path1a.toFile());
        filesRepo2.add(path2a.toFile());
        assertTrue(peer2.addFilesToRepository(repo1, filesRepo2));

        //add from peer3 on repo1
        Path path31 = Paths.get(pathRepo3, repo1, "esp11");
        Path path3a = Paths.get(pathRepo3, repo1, "esp3");
        Files.write(path31, "ciao aa".getBytes(StandardCharsets.UTF_8));
        Files.write(path3a, "ciaoo1oo2".getBytes(StandardCharsets.UTF_8));
        filesRepo3.clear();
        filesRepo3.add(path31.toFile());
        filesRepo3.add(path3a.toFile());
        assertTrue(peer3.addFilesToRepository(repo1, filesRepo3));

        //pull repo1 from peer4
        assertEquals(pullNoConflicts, peer4.pull(repo1));

        //commits repo1 from peer2 and peer3
        assertTrue(peer2.commit(repo1, "bho"));
        assertTrue(peer3.commit(repo1, "bho1"));
        //commit repo2 from peer2, and repo3 from peer3
        assertTrue(peer2.commit(repo2, "bho"));
        assertTrue(peer3.commit(repo3, "as"));

        //peer2 create repo2 on remote
        assertEquals(pushCreateRepo, peer2.push(repo2));

        //peer2 can't push repo1, should pull first
        assertEquals(pushOutOfDate, peer2.push(repo1));
        //peer2 pulls, but there are conflicts
        assertEquals(pullYesConflicts, peer2.pull(repo1));

        //peer2 removes old files in conflict
        Path path1Remove = Paths.get(pathRepo2, repo1, "esp1_(1)");
        Path path2Remove = Paths.get(pathRepo2, repo1, "esp2_(1)");
        filesRepo2.clear();
        filesRepo2.add(path1Remove.toFile());
        filesRepo2.add(path2Remove.toFile());
        assertTrue(peer2.removeFilesFromRepository(repo1, filesRepo2));
        
        List<String> tracked = peer2.statusGetTrackedFiles(repo1);
        List<String> testTracked = Arrays.asList("esp1", "esp2", "esp3", "esp1_(1)", "esp2_(1)");
        assertTrue(tracked.containsAll(testTracked) && testTracked.containsAll(tracked));
        
        assertTrue(peer2.commit(repo1, "rimozione"));
        
        tracked = peer2.statusGetTrackedFiles(repo1);
        testTracked = Arrays.asList("esp1", "esp2", "esp3");
        assertTrue(tracked.containsAll(testTracked) && testTracked.containsAll(tracked));
        
        assertEquals(pushOk, peer2.push(repo1));

        //peer4 commit and push repo4
        assertTrue(peer4.commit(repo4, "ddd"));
        assertEquals(pushCreateRepo, peer4.push(repo4));

        //peer4 add some files in repo1
        filesRepo4.clear();
        Path path12 = Paths.get(pathRepo4, repo1, "esp12");
        Path path32 = Paths.get(pathRepo4, repo1, "esp32");
        Path path14 = Paths.get(pathRepo4, repo1, "esp14");
        Files.write(path12, "oaic".getBytes(StandardCharsets.UTF_8));
        Files.write(path32, "osa".getBytes(StandardCharsets.UTF_8));
        Files.write(path14, "cccciao".getBytes(StandardCharsets.UTF_8));
        filesRepo4.add(path12.toFile());
        filesRepo4.add(path32.toFile());
        filesRepo4.add(path14.toFile());

        assertTrue(peer4.addFilesToRepository(repo1, filesRepo4));
        //just for a check
        assertFalse(peer4.addFilesToRepository(repo1, filesRepo4.subList(0, 1)));

        assertTrue(peer4.commit(repo1, "asda"));
        
        //unstaged changes
        Files.write(path12, "blblbl".getBytes(StandardCharsets.UTF_8));
        Files.write(path32, "aaaaa".getBytes(StandardCharsets.UTF_8));
        Files.write(path14, "asxz".getBytes(StandardCharsets.UTF_8));

        //check for unstaged files with status
        List<String> unstaged = peer4.statusGetUnstagedFiles(repo1);
        List<String> testUnstaged = Arrays.asList("esp12", "esp32", "esp14");

        assertTrue(unstaged.containsAll(testUnstaged) && testUnstaged.containsAll(unstaged));
        
        assertEquals(pushOutOfDate, peer4.push(repo1));
        assertEquals(pullYesConflicts, peer4.pull(repo1));
        //just for a check
        assertEquals(pullOk, peer4.pull(repo1));

        //peer3 pull but with conflicts, remove file at path32 and push repo1
        assertEquals(pullYesConflicts, peer3.pull(repo1));
        assertTrue(peer3.removeFilesFromRepository(repo1, filesRepo3.subList(1, 2)));
        assertTrue(peer3.commit(repo1, "rim"));
        assertEquals(pushOk, peer3.push(repo1));
        
        //cleanse
        deleteFiles(new File(pathRepo1));
        deleteFiles(new File(pathRepo2));
        deleteFiles(new File(pathRepo3));
        deleteFiles(new File(pathRepo4));
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
