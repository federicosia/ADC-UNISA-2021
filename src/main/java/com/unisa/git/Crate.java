package com.unisa.git;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.UUID;

/**
 * This class is used to wrap a file, it contains the file's abstraction and a 128 bit id that
 * rappresents the current content of the file.
 */
public class Crate implements Serializable{
    private String name;
    private byte[] content;
    private String id;

    public Crate(File file) throws IOException  {
        this.name = file.getName();
        this.content = Files.readAllBytes(file.toPath());
        this.id = UUID.nameUUIDFromBytes(Files.readAllBytes(file.toPath())).toString();
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

    @Override
    public boolean equals(Object object){
        if(object instanceof Crate){
            Crate crate = (Crate) object;
            if(this.getId().equals(crate.getId()))
                return true;
        }
        return false;
    }
}
