package com.unisa;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.unisa.git.GitProtocol;
import com.unisa.git.GitProtocolImpl;
import com.unisa.git.MessageListener;
import com.unisa.git.GitStorage;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;

public class Main 
{
    private static final String CREATE = "create";
    private static final String ADD = "add";
    private static final String COMMIT = "commit";
    private static final String PUSH = "push";
    private static final String PULL = "pull";
    private static final String HELP = "help";
    private static final String EXIT = "exit";
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
            while(true){
                String input = textIO.newStringInputReader().read("git");
                String[] splitInput = input.split(" ");
                //the first string is the command
                switch(splitInput[0]){
                    case CREATE:
                        if(splitInput.length != 3)
                            terminal.printf(tip(splitInput[0]));
                        else {
                            boolean result = git.createRepository(splitInput[1], new File(splitInput[2]));
                            if(result) 
                                terminal.println("Repository created successfully!\n");
                            else 
                                terminal.println("Repository not created...\n");
                        }
                        break;
                    case ADD:
                        if(splitInput.length < 3)
                            terminal.printf(tip(splitInput[0]));
                        else {
                            List<File> files = new ArrayList<>();
                            for(int i = 2; i < splitInput.length; i++){
                                files.add(new File(splitInput[i]));
                                terminal.print("File: " + splitInput[i] + "\n");
                            }
                            boolean result = git.addFilesToRepository(splitInput[1], files);
                            if(result)
                                terminal.println("Added files successfully!\n");
                            else 
                                terminal.println("Add failed...\n");
                        }
                        break;
                    case COMMIT:
                        if(splitInput.length != 3)
                            terminal.print(tip(splitInput[0]));
                        else {
                            boolean result = git.commit(splitInput[1], splitInput[2]);
                            if(result)
                                terminal.println("Commit made successfully!\n");
                            else 
                                terminal.println("Commit failed...\n");
                        }
                        break;
                    case PUSH:
                        if(splitInput.length != 2)
                            terminal.print(tip(splitInput[0]));
                        else{
                            terminal.println(git.push(splitInput[1]));
                        }
                        break;
                    case PULL:
                        if(splitInput.length != 2)
                            terminal.print(tip(splitInput[0]));
                        else{
                            terminal.println(git.pull(splitInput[1]));
                        }
                        break;
                    case HELP:
                        terminal.print(help());
                        break;
                    case EXIT:
                        terminal.dispose();
                        textIO.dispose();
                        System.exit(0);
                    default:
                        terminal.print("Command not found.\n");
                        break;
                }
            }
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
        return "\nCommands:\n" +
                "\tcreate\tallows you the creation of a local repository.\n" +
                "\t\tSyntax: git create {0} {1}\n\t\targs: {0} repository name, {1} directory name.\n\n" +
                "\tadd\tallows you to add a file in a local repository.\n" +
                "\t\tSyntax: git add {0} || [list]\n\t\targs: {0} file name or directory, [list] file names separeted by a space\n\n" +
                "\tcommit\tallows you the creation of commit in the local repository.\n" +
                "\t\tSyntax: git commit {0}, {1}\n\t\targs: {0} repository name, {1} message\n\n" +
                "\tpush\tallows you to push files to the remote repository.\n" +
                "\t\tSyntax: git push {0}\n\t\targs: {0} repository name.\n\n" +
                "\tpull\tallows you to obtain files in the remote repository and store them in the local repository.\n" +
                "\t\tSyntax: git pull {0}\n\t\targs: {0} repository name.\n\n" +
                "\thelp\treprint commands list\n\n" +
                "\texit\tclose git.\n\n";
    }
    
    /**
     * Prints a tip on how to use the wrong command.
     * @param command wrong command used
     * @return string that rappresent the proper use of the command
     */
    private static String tip(String command){
        switch(command){
            case CREATE:
                return "\n\tSyntax error: git create {0} {1}\n\t\targs: {0} repository name, {1} directory name.\n\n";
            case ADD:
                return "Syntax error:  git add {0} || [list]\n\t\targs: {0} file name or directory," +
                        " [list] file names separeted by a space\n\n";
            case COMMIT:
                return "Syntax error: git commit {0}, {1}\n\t\targs: {0} repository name, {1} message\n\n";
            case PUSH:
                return "Syntax error: git push {0}\n\t\targs: {0} repository name.\n\n";
            case PULL:
                return "Syntax error: git pull {0}\n\t\targs: {0} repository name.\n\n";
            default:
                return "\nCRITICAL ERROR: Shouldn't happen...\n";
        }
    }
}
