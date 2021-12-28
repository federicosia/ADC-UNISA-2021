package com.unisa;

import java.io.IOException;

import com.unisa.git.GitProtocol;
import com.unisa.git.GitProtocolImpl;
import com.unisa.git.MessageListener;
import com.unisa.git.GitStorage;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;

public class Main 
{
   public static void main( String[] args ) {
       
        class MessageListenerImpl implements MessageListener{
            private int peerid;

            public MessageListenerImpl(int peerid){
                this.peerid = peerid;
            }

            @Override
            public Object parseMessage(Object obj) {
                TextIO textIO = TextIoFactory.getTextIO();
                TextTerminal terminal = textIO.getTextTerminal();
                terminal.printf("\n"+peerid+"] (Direct Message Received) "+obj+"\n\n");
                return "Success";
            }
            
        }
        TextIO textIO = TextIoFactory.getTextIO();
        TextTerminal terminal = textIO.getTextTerminal();
        int id = textIO.newIntInputReader().read("Insert peer ID: ");
        try {
            GitProtocol git = new GitProtocolImpl(new GitStorage(id, "127.0.0.1"), new MessageListenerImpl(id));
            terminal.print(help());
        } catch (IOException e) {
            terminal.printf("Git protocol error...");
            e.printStackTrace();
        }
    }

    /**
     * List of permitted commands
     * @return list of commands
     */
    private static String help(){
        return "\nTo get started with git type \"git\" plus one of the commands listed below:\n" +
                "\tcreate\tallows you the creation of a local repository.\n" +
                "\t\tSyntax: git create {0}\n\t\targs: {0} repository name.\n\n" +
                "\tadd\tallows you to add a file in a local repository.\n" +
                "\t\tSyntax: git add {0} || [list]\n\t\targs: {0} file name, [list] file names separeted by a comma\n\n" +
                "\tcommit\tallows you the creation of commit in the local repository.\n" +
                "\t\tSyntax: git commit {0}, {1}\n\t\targs: {0} repository name, {1} message\n\n" +
                "\tpush\tallows you to push files to the remote repository.\n" +
                "\t\tSyntax: git push {0}\n\t\targs: {0} repository name.\n\n" +
                "\tpull\tallows you to obtain files in the remote repository and store them in the local repository.\n" +
                "\t\tSyntax: git pull {0}\n\t\targs: {0} repository name.\n\n";
    }
}
