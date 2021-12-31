package com.unisa.git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

/**
 * This class is used to wrap a file, it contains the file's abstraction and a 128 bit id that
 * rappresents the current content of the file.
 */
public class Crate {
    private File content;
    private String id;
    private boolean commited;

    public Crate(File file) throws IOException  {
        this.content = file;
        this.id = UUID.nameUUIDFromBytes(Files.readAllBytes(file.toPath())).toString();
        this.commited = false;
    }

    public boolean getCommit(){
        return commited;
    }

    public boolean setCommit(){
        if(!commited)
            commited = true;
        return commited;
    }

    public File getContent(){
        return content;
    }

    public String getId(){
        return id;
    }

    @Override
    public boolean equals(Object object){
        if(object instanceof Crate){
            Crate crate = (Crate) object;
            if(this.getId().equals(crate.getId()))
                return true;
            else 
                return false;
        }
        else 
            return false;
    }
}
