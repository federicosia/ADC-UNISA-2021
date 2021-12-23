package com.unisa.git;

import java.io.IOException;

public class Main 
{
    public static void main( String[] args ) {
        int id = 0;
        try {
            GitProtocol git = new GitProtocolImpl(new Storage(id, "127.0.0.1"));
            

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
