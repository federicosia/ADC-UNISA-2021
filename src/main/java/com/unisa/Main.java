package com.unisa;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.unisa.git.GitProtocol;
import com.unisa.git.GitProtocolImpl;
import com.unisa.git.storage.DHTStorage;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class Main 
{
    private static final String CREATE = "create";
    private static final String ADD = "add";
    private static final String REMOVE = "remove";
    private static final String COMMIT = "commit";
    private static final String PUSH = "push";
    private static final String PULL = "pull";
    private static final String HELP = "help";
    private static final String EXIT = "exit";
    private static final String STATUS = "status";

    @Option(name="-m", aliases="--masterip", usage="the master peer ip address", required=true)
    private static String master;

    @Option(name="-id", aliases="--identifierpeer", usage="the unique identifier for this peer", required=true)
    private static int id;

   public static void main( String[] args ) {
        Main main = new Main();
        final CmdLineParser parser= new CmdLineParser(main);
        TextIO textIO = TextIoFactory.getTextIO();
        TextTerminal terminal = textIO.getTextTerminal();        

        try {
            parser.parseArgument(args);
            GitProtocol git = new GitProtocolImpl(new DHTStorage(id, master));
            terminal.print("\nPeer with id: " + id + " on node: " + master + "\n");
            terminal.print(help());
            while(true){
                String input = textIO.newStringInputReader().read("git");
                String[] splitInput = input.split(" ");
                //the first string is the command
                switch(splitInput[0]){
                    case CREATE:
                        if(splitInput.length != 3 && splitInput.length != 2)
                            terminal.printf(tip(splitInput[0]));
                        else {
                            String path;
                            //Use the current path to create a repo
                            if(splitInput.length == 2)
                                path = System.getProperty("user.dir");
                            else
                                path = splitInput[2];

                            boolean result = git.createRepository(splitInput[1], new File(path));
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
                            for(int i = 2; i < splitInput.length; i++)
                                files.add(new File(splitInput[i]));
                            
                            boolean result = git.addFilesToRepository(splitInput[1], files);
                            if(result)
                                terminal.println("Added files successfully!\n");
                        }
                        break;
                    case REMOVE:
                        if(splitInput.length < 3)
                            terminal.printf(tip(splitInput[0]));
                        else {
                            List<File> files = new ArrayList<>();
                            for(int i = 2; i < splitInput.length; i++)
                                files.add(new File(splitInput[i]));
        
                            boolean result = git.removeFilesFromRepository(splitInput[1], files);
                            if(result)
                                terminal.println("Removed files successfully!\n");
                        }
                        break;
                    case COMMIT:
                        if(splitInput.length < 3)
                            terminal.print(tip(splitInput[0]));
                        else {
                            String message = String.join(" ", Arrays.copyOfRange(splitInput, 2, splitInput.length));
                            boolean result = git.commit(splitInput[1], message);
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
                    case STATUS:
                        if(splitInput.length != 2)
                            terminal.print(tip(splitInput[0]));
                        else {
                            terminal.println(git.status(splitInput[1]));
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
            System.err.println("ERROR: Git protocol error...");
            e.printStackTrace();
        } catch (CmdLineException e) {
            System.err.println("ERROR: Unable to parse cmd options: ");
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
                "\tadd\tallows you to add one or multiple files to the local repository.\n" +
                "\t\tSyntax: git add {0} {1} || [list]\n\t\targs: {0} repository name, {1} file name or directory, [list] file names separeted by a space\n\n" +
                "\tremove allows you to remove one or multiple files from the local repository\n" +
                "\t\tSyntax: git remove {0} {1} || [list]\n\t\targs: {0} repository name, {1} file name or directory, [list] file names seprated by a space\n\n" +
                "\tcommit\tallows you the creation of commit in the local repository.\n" +
                "\t\tSyntax: git commit {0}, {1}\n\t\targs: {0} repository name, {1} message\n\n" +
                "\tpush\tallows you to push files to the remote repository.\n" +
                "\t\tSyntax: git push {0}\n\t\targs: {0} repository name.\n\n" +
                "\tpull\tallows you to obtain files from the remote repository and store them in the local repository.\n" +
                "\t\tSyntax: git pull {0}\n\t\targs: {0} repository name.\n\n" +
                "\tstatus\tallows you to check the status of the local repository, it shows the name of files that are staged,\n \t\tunstaged, tracked and untracked\n" +
                "\t\tSyntax: git status {0}\n\t\targs: {0} repository name.\n\n" +
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
                return "Syntax error:  git add {0} {1} || [list]\n\t\targs: {0} repository name," + 
                        " {1} file name or directory, [list] file names separeted by a space\n\n";
            case REMOVE:
                return "\t\tSyntax: git remove {0} {1} || [list]\n\t\targs: {0} repository name, " +
                        " {1} file name or directory, [list] file names seprated by a space\n\n";
            case COMMIT:
                return "Syntax error: git commit {0}, {1}\n\t\targs: {0} repository name, {1} message\n\n";
            case PUSH:
                return "Syntax error: git push {0}\n\t\targs: {0} repository name.\n\n";
            case PULL:
                return "Syntax error: git pull {0}\n\t\targs: {0} repository name.\n\n";
            case STATUS:
                return "Syntax error: git status {0}\n\t\targs: {0} repository name.\n\n";
            default:
                return "\nCRITICAL ERROR: Shouldn't happen...\n";
        }
    }
}
