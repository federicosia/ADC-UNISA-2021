package com.unisa.git;

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
     * Upgrade the content and the id of the crate.
     * @throws IOException something went wrong...
     * @return true if crate was update successfully, false otherwise
     */
    public boolean update() throws IOException{
        if(!this.file.exists())
            return false;
        this.content = Files.readAllBytes(file.toPath());
        this.id = UUID.nameUUIDFromBytes(this.content).toString();    
        return true;
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
