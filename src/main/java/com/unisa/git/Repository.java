package com.unisa.git;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.unisa.git.exceptions.RepositoryException;
/**
 * The repository of an user, the Repository class allows to manage, create and 
 * control the workflow of the repositories. 
 */
public class Repository implements Serializable{
    private String name;
    private File path;
    private HashMap<String, Crate> files;
    private HashMap<String, Crate> uncommittedFiles;
    private ArrayList<Commit> commits;
    private ArrayList<Commit> commitsNotPushed;
    private String id;

    public Repository(String name, File directory) throws IOException{
        this.name = name;
        this.path = new File(directory.getAbsolutePath().concat("\\"+name));
        this.files = new HashMap<>();
        this.uncommittedFiles = new HashMap<>();
        this.commits = new ArrayList<>();
        this.commitsNotPushed = new ArrayList<>();
        this.id = "null";
        this.path.mkdir();
    }

    public String getName(){
        return name;
    }

    public String getPath(){
        return path.getAbsolutePath();
    }

    /**
     * Returns the id of the repository, it's generated again every time this method is called.
     * The id is produced from the content of the repository.
     * @return id the new id generated
     * @throws IOException problems when reading from the file
     */
    public String getId() throws IOException {
        //Read all bytes of all 
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for(Map.Entry<String, Crate> entry: this.files.entrySet()){
            if(entry.getValue().update())
                outputStream.write(entry.getValue().getContent());
            else 
                this.files.remove(entry.getKey());
        }
        //Gain all bytes read from the directory of the repository
        byte[] dirBytes = outputStream.toByteArray();
        //Generate the new id from the bytes of the directory
        this.id = UUID.nameUUIDFromBytes(dirBytes).toString();
        return id;
    }

    @Override
    public boolean equals(Object object){
        try{
            if(object instanceof Repository){
                Repository repo = (Repository) object;
                //System.out.println("Id1 -> "+id+"\nId2 -> "+repo.id);
                if(this.getId().equals(repo.id))
                    return true;
            }
        } catch(IOException e){
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public boolean addFile(List<File> files) throws IOException, RepositoryException{
        for(File file : files){
            String fileName = file.getName();
            Crate oldCrate = this.uncommittedFiles.get(fileName);
            File path_to_file = new File(this.getPath().concat("\\" + fileName));
            //if the key is not present than we simple add a new entry
            if(oldCrate == null){
                Crate committedFile = this.files.get(fileName);
                Crate newCrate = new Crate(path_to_file);
                //Check if the file we want to commited was already committed!
                if((committedFile != null) && committedFile.equals(newCrate))
                    throw new RepositoryException(fileName + " was already committed...");

                //Check if the file is a directory, recursively add all files, otherwise is a file
                if(path_to_file.isDirectory())
                    this.addFile(Arrays.asList(path_to_file.listFiles()));
                
                this.uncommittedFiles.put(fileName, newCrate);
            } 
            //The key is already present that we check if the content of the file is the same,
            //if the content is different that we replace the value associated at the key, otherwise nothing.
            else {
                Crate newCrate = new Crate(path_to_file);
                if(!oldCrate.equals(newCrate)){
                    //Check if the file is a directory, recursively add all files, otherwise is a file
                    if(path_to_file.isDirectory())
                        this.addFile(Arrays.asList(path_to_file.listFiles()));

                    this.uncommittedFiles.put(fileName, newCrate);
                }
                else throw new RepositoryException(fileName + " already present...");
            } 
        }
        return true;
    }

    /**
     * Do a commit, last step before the push of the modified files in the remote repository
     * @param commit commit to be made, compose by the repository name and a message.
     * @return true if there's something to commit, false otherwise.
     * @throws IOException
     */
    public boolean addCommit(Commit commit) throws IOException {
        if(uncommittedFiles.size() > 0){
            commitsNotPushed.add(commit);
            for(Crate crate : uncommittedFiles.values()){
                files.put(crate.getName(), crate);
            }
            uncommittedFiles.clear();
            this.id = getId();
            return true;
        }
        return false;
    }

    /**
     * Check if there are commits to be pushed
     * @return true if there's something to push, false otherwise
     * @throws IOException
     */
    public boolean checkBeforePush() throws IOException {
        if(!commitsNotPushed.isEmpty()){
            for(int i = 0; i < commitsNotPushed.size(); i++){
                Commit commit = commitsNotPushed.get(i);
                if(!commit.updateStatus())
                    return false;
                else {
                    commits.add(commit);
                    commitsNotPushed.remove(i);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Donwload files in the current repository from the repository passed as argument.
     * This method doesn't fix conflicts, it just finds them and duplicate the files with different names,
     * the developer should fix the conflicts manually. Files that are not present in the 
     * local repository will be simply added.
     * @param repository the repository to compare with the current repository
     * @return 0 if no upgrades were made, 1 if files were added without conflicts, 
     * 2 if files were added but with conflicts
     * @throws IOException
     */
    public int update(Repository repository) throws IOException{
        int result = 0;
        for(String filename : repository.files.keySet()){
            if(this.files.containsKey(filename)){
                //also check if the content is different, otherwise do nothing
                if(!Arrays.equals(this.files.get(filename).getContent(), repository.files.get(filename).getContent())){
                    this.files.put(filename + "_(1)", repository.files.get(filename));
                    result = 2;
                }
                //else nothing
            }
            //else just add it in the repository
            else{
                this.files.put(filename, repository.files.get(filename));
                result = 1;
            }
        }
        materialize();
        this.id = getId();
        return result;
    }

    /**
     * Creates a representation of the files in the repository in the file system.
     * @throws IOException if something went wrong
     */
    private void materialize() throws IOException{
        for(Crate crate: this.files.values()){
            Path path = Paths.get(this.getPath().concat("\\"+crate.getName()));
            Files.write(path, crate.getContent());
        }
    }
}
