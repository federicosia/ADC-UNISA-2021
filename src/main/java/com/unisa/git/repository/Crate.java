package com.unisa.git.repository;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.UUID;

/**
 * This class is used to wrap a file, it contains the file's abstraction and a 128 bit id that
 * rappresents the current content of the file.
 */
public class Crate implements Serializable{
    private String name;
    private File file;
    private byte[] content;
    private String id;

    public Crate(File file) throws IOException  {
        this.name = file.getName();
        this.file = file;
        this.content = Files.readAllBytes(file.toPath());
        this.id = UUID.nameUUIDFromBytes(this.content).toString();
    }

    public String getName(){
        return name;
    }

    public String getId(){
        return id;
    }

    public byte[] getContent(){
        return content;
    }

    /**
     * Checks if the file represented by the Crate object is still present in the File System
     * of the local repository.
     * @return true if present, false otherwise.
     */
    public boolean checkIfExists() {
        return this.file.exists();
    }

    @Override
    public boolean equals(Object object){
        if(object instanceof Crate){
            Crate crate = (Crate) object;
                if(this.getId().equals(crate.getId()) && this.name.equals(crate.name))
                return true;
        }
        return false;
    }
}
